package cn.coderstory.springboot.service.monitor.hardware.impl;

import cn.coderstory.springboot.config.MonitorHardwareProperties;
import cn.coderstory.springboot.dto.monitor.hardware.CpuMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.DiskMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.HardwareMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.MemoryMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.MetricValue;
import cn.coderstory.springboot.dto.monitor.hardware.NetworkMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.SystemInfoDTO;
import cn.coderstory.springboot.service.monitor.hardware.HardwareMetricsService;
import cn.coderstory.springboot.service.monitor.hardware.RingBuffer;
import cn.coderstory.springboot.sse.monitor.HardwareSseService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.ffm.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 硬件指标定时采集服务实现类。
 * <p>
 * 使用 OSHI FFM 库定时采集 CPU、内存、磁盘、网络和系统信息。
 * 采用单写入者 + 多读取者架构：采集线程每 2s 采集一次并写入缓存，
 * API 请求从缓存读取，不直接调用 OSHI。使用 {@link ReentrantReadWriteLock}
 * 保证缓存读写一致性。单个指标采集失败时局部降级，不影响其他指标。
 *
 * @since 1.8.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HardwareMetricsServiceImpl implements HardwareMetricsService {

    private final SystemInfo systemInfo;
    private final ScheduledExecutorService scheduler;
    private final MonitorHardwareProperties properties;
    private final HardwareSseService hardwareSseService;

    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private volatile HardwareMetricsDTO currentMetrics;
    private RingBuffer<HardwareMetricsDTO> historyBuffer;

    private long[] prevCpuTicks;
    private long[][] prevPerCoreTicks;
    private int diskNetCounter = 0;
    private int trendCounter = 0;
    private List<HWDiskStore> prevDisks;
    private List<NetworkIF> prevNets;

    /**
     * 初始化 OSHI 采集服务。
     * <p>
     * 预热 CPU ticks 以避免首次采集返回 -1，预热磁盘和网络状态用于差分计算，
     * 启动定时调度线程。
     */
    @PostConstruct
    public void init() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        // 预热 CPU ticks（Pitfall 2 防护：首次 getSystemCpuLoadBetweenTicks 返回 -1）
        prevCpuTicks = hal.getProcessor().getSystemCpuLoadTicks();
        prevPerCoreTicks = hal.getProcessor().getProcessorCpuLoadTicks();

        // 预热磁盘和网络状态（用于差分计算）
        prevDisks = hal.getDiskStores();
        List<NetworkIF> nets = hal.getNetworkIFs();
        nets.forEach(net -> net.updateAttributes());
        prevNets = nets;

        historyBuffer = new RingBuffer<>(properties.getTrendBufferSize());

        // 启动定时采集（D-09：使用 ScheduledExecutorService 而非 @Scheduled）
        scheduler.scheduleAtFixedRate(
                this::collect,
                0,
                properties.getSamplingInterval(),
                TimeUnit.MILLISECONDS
        );
        log.info("硬件监控采集线程已启动 (interval={}ms, diskNetInterval={}, trendDecimation={})",
                properties.getSamplingInterval(), properties.getDiskNetInterval(), properties.getTrendDecimation());
    }

    /**
     * 主采集循环。
     * <p>
     * 每次采样创建新的快照 DTO，分别采集 CPU、内存、系统信息（每次采样），
     * 磁盘和网络（每 diskNetInterval 次采样）。写入缓存时使用写锁保护，
     * 按趋势降采样率写入环形缓冲区。
     */
    public void collect() {
        HardwareMetricsDTO snapshot = new HardwareMetricsDTO();
        snapshot.setTimestamp(System.currentTimeMillis());

        // CPU & 内存 — 每次采样
        snapshot.setCpu(sampleCpu());
        snapshot.setMemory(sampleMemory());

        // 系统信息 — 每次采样
        snapshot.setSystem(sampleSystem());

        // 磁盘 & 网络 — 每 diskNetInterval 次采样更新（D-08）
        diskNetCounter++;
        if (diskNetCounter >= properties.getDiskNetInterval()) {
            diskNetCounter = 0;
            snapshot.setDisks(sampleDisks());
            snapshot.setNetwork(sampleNetwork());
        } else {
            // 非更新周期，从缓存复用上次值（读锁保护）
            cacheLock.readLock().lock();
            try {
                if (currentMetrics != null) {
                    snapshot.setDisks(currentMetrics.getDisks());
                    snapshot.setNetwork(currentMetrics.getNetwork());
                }
            } finally {
                cacheLock.readLock().unlock();
            }
        }

        // 写入缓存（写锁保护）— D-10
        cacheLock.writeLock().lock();
        try {
            currentMetrics = snapshot;
            // 每 trendDecimation 次采样写入趋势（D-06）
            trendCounter++;
            if (trendCounter >= properties.getTrendDecimation()) {
                trendCounter = 0;
                historyBuffer.add(snapshot);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }

        // SSE 广播：在写锁外部，避免阻塞采集线程
        if (hardwareSseService != null) {
            hardwareSseService.broadcast("metrics", snapshot);
        }
    }

    // ==================== 采样方法（每个 sampler 有独立 try-catch 实现 D-12 局部降级） ====================

    /**
     * 采样 CPU 指标。
     * <p>
     * 使用 OSHI CentralProcessor 的 tick 差分 API 计算总体和各核使用率。
     * 保留 prevTicks 引用以实现连续差分计算。
     *
     * @return CPU 指标 DTO，异常时返回空实例
     */
    private CpuMetricsDTO sampleCpu() {
        try {
            CentralProcessor processor = systemInfo.getHardware().getProcessor();

            // 总体使用率
            long[] currentTicks = processor.getSystemCpuLoadTicks();
            double systemLoad = processor.getSystemCpuLoadBetweenTicks(prevCpuTicks, currentTicks);
            prevCpuTicks = currentTicks;

            // 各核使用率
            long[][] currentPerCoreTicks = processor.getProcessorCpuLoadTicks();
            double[] perCoreLoad = processor.getProcessorCpuLoadBetweenTicks(prevPerCoreTicks, currentPerCoreTicks);
            prevPerCoreTicks = currentPerCoreTicks;

            // 缩放到 Task Manager 兼容格式（0-100% 范围）
            double systemCpuPercent = (systemLoad / processor.getLogicalProcessorCount()) * 100;
            double[] perCorePercent = Arrays.stream(perCoreLoad)
                    .map(l -> l * 100)
                    .toArray();

            CentralProcessor.ProcessorIdentifier identifier = processor.getProcessorIdentifier();

            CpuMetricsDTO dto = new CpuMetricsDTO();
            dto.setSystemLoad(Math.round(systemCpuPercent * 10.0) / 10.0);
            dto.setPerCoreLoad(perCorePercent);
            dto.setLogicalCores(processor.getLogicalProcessorCount());
            dto.setPhysicalCores(processor.getPhysicalProcessorCount());
            dto.setProcessorName(identifier.getName());
            dto.setProcessorFrequency(identifier.getVendorFreq());
            dto.setCpu64bit(identifier.isCpu64bit());
            return dto;
        } catch (Exception e) {
            log.warn("CPU 采集失败", e);
            return CpuMetricsDTO.empty();
        }
    }

    /**
     * 采样内存指标。
     * <p>
     * 使用 OSHI GlobalMemory 获取物理内存总量、可用量、已用量和使用率。
     * 所有值通过 MetricValue 封装为格式化结构。
     *
     * @return 内存指标 DTO，异常时返回空实例
     */
    private MemoryMetricsDTO sampleMemory() {
        try {
            GlobalMemory memory = systemInfo.getHardware().getMemory();

            long total = memory.getTotal();
            long available = memory.getAvailable();
            long used = total - available;
            double usagePercent = used * 100.0 / total;

            MemoryMetricsDTO dto = new MemoryMetricsDTO();
            dto.setTotal(formatBytes(total));
            dto.setAvailable(formatBytes(available));
            dto.setUsed(formatBytes(used));
            dto.setUsagePercent(formatPercent(usagePercent));
            return dto;
        } catch (Exception e) {
            log.warn("内存采集失败", e);
            return MemoryMetricsDTO.empty();
        }
    }

    /**
     * 采样磁盘指标。
     * <p>
     * 使用 OSHI HWDiskStore 获取磁盘容量和型号信息。读写速度和 IOPS
     * 通过两次采样差分除以时间间隔计算（磁盘每 6s 更新一次）。
     *
     * @return 磁盘指标 DTO 列表，异常时返回空列表
     */
    private List<DiskMetricsDTO> sampleDisks() {
        try {
            List<HWDiskStore> currentDisks = systemInfo.getHardware().getDiskStores();
            List<DiskMetricsDTO> result = new ArrayList<>();

            for (int i = 0; i < currentDisks.size(); i++) {
                HWDiskStore current = currentDisks.get(i);
                HWDiskStore previous = i < prevDisks.size() ? prevDisks.get(i) : null;

                DiskMetricsDTO dto = new DiskMetricsDTO();
                dto.setName(current.getName());
                dto.setModel(current.getModel());
                dto.setSize(formatBytes(current.getSize()));
                String diskType = current.getDiskType();
                dto.setDiskType(diskType != null ? diskType : "UNKNOWN");

                // 差分计算（仅当有前值时）
                if (previous != null) {
                    long intervalMs = properties.getSamplingInterval() * properties.getDiskNetInterval();
                    long readBytesPerSec = (current.getReadBytes() - previous.getReadBytes()) * 1000 / intervalMs;
                    long writeBytesPerSec = (current.getWriteBytes() - previous.getWriteBytes()) * 1000 / intervalMs;
                    long readsPerSec = (current.getReads() - previous.getReads()) * 1000 / intervalMs;
                    long writesPerSec = (current.getWrites() - previous.getWrites()) * 1000 / intervalMs;

                    dto.setReadBytesPerSec(MetricValue.of(readBytesPerSec, formatBytesPerSec(readBytesPerSec), "B/s"));
                    dto.setWriteBytesPerSec(MetricValue.of(writeBytesPerSec, formatBytesPerSec(writeBytesPerSec), "B/s"));
                    dto.setReadsPerSec(MetricValue.of(readsPerSec, readsPerSec + " IOPS", "IOPS"));
                    dto.setWritesPerSec(MetricValue.of(writesPerSec, writesPerSec + " IOPS", "IOPS"));
                }

                result.add(dto);
            }

            prevDisks = currentDisks;
            return result;
        } catch (Exception e) {
            log.warn("磁盘采集失败", e);
            return List.of();
        }
    }

    /**
     * 采样网络接口指标。
     * <p>
     * 使用 OSHI NetworkIF 获取网络接口的上下行速率和包速率。
     * 通过两次采样差分除以时间间隔计算（网络每 6s 更新一次）。
     *
     * @return 网络接口指标 DTO 列表，异常时返回空列表
     */
    private List<NetworkMetricsDTO> sampleNetwork() {
        try {
            List<NetworkIF> currentNets = systemInfo.getHardware().getNetworkIFs();
            currentNets.forEach(net -> net.updateAttributes());
            List<NetworkMetricsDTO> result = new ArrayList<>();

            for (int i = 0; i < currentNets.size(); i++) {
                NetworkIF current = currentNets.get(i);
                NetworkIF previous = i < prevNets.size() ? prevNets.get(i) : null;

                NetworkMetricsDTO dto = new NetworkMetricsDTO();
                dto.setDisplayName(current.getDisplayName());
                dto.setMacAddress(current.getMacaddr());
                dto.setSpeed(current.getSpeed());
                // isKnownVm 在 OSHI 7.1.0 FFM 中可能不可用，跳过

                // 差分计算（仅当有前值时）
                if (previous != null) {
                    long intervalMs = properties.getSamplingInterval() * properties.getDiskNetInterval();
                    long bytesSentPerSec = (current.getBytesSent() - previous.getBytesSent()) * 1000 / intervalMs;
                    long bytesRecvPerSec = (current.getBytesRecv() - previous.getBytesRecv()) * 1000 / intervalMs;
                    long packetsSentPerSec = (current.getPacketsSent() - previous.getPacketsSent()) * 1000 / intervalMs;
                    long packetsRecvPerSec = (current.getPacketsRecv() - previous.getPacketsRecv()) * 1000 / intervalMs;

                    dto.setBytesSentPerSec(MetricValue.of(bytesSentPerSec, formatBytesPerSec(bytesSentPerSec), "B/s"));
                    dto.setBytesRecvPerSec(MetricValue.of(bytesRecvPerSec, formatBytesPerSec(bytesRecvPerSec), "B/s"));
                    dto.setPacketsSentPerSec(MetricValue.of(packetsSentPerSec, packetsSentPerSec + " pps", "pps"));
                    dto.setPacketsRecvPerSec(MetricValue.of(packetsRecvPerSec, packetsRecvPerSec + " pps", "pps"));
                }

                result.add(dto);
            }

            prevNets = currentNets;
            return result;
        } catch (Exception e) {
            log.warn("网络采集失败", e);
            return List.of();
        }
    }

    /**
     * 采样系统基本信息。
     * <p>
     * 使用 OSHI OperatingSystem 获取操作系统版本、系统运行时间和进程数。
     * 运行时间同时提供秒数和格式化字符串形式。
     *
     * @return 系统基本信息 DTO，异常时返回空实例
     */
    private SystemInfoDTO sampleSystem() {
        try {
            OperatingSystem os = systemInfo.getOperatingSystem();

            SystemInfoDTO dto = new SystemInfoDTO();
            dto.setOsFamily(os.getFamily());
            dto.setOsVersion(os.getVersionInfo().toString());
            dto.setOsVersionInfo(os.getVersionInfo().getCodeName());
            dto.setSystemUptime(os.getSystemUptime());
            dto.setProcessCount(os.getProcessCount());
            dto.setSystemUptimeFormatted(formatUptime(os.getSystemUptime()));
            return dto;
        } catch (Exception e) {
            log.warn("系统信息采集失败", e);
            return new SystemInfoDTO();
        }
    }

    // ==================== Service 接口实现 ====================

    @Override
    public HardwareMetricsDTO getCurrentMetrics() {
        cacheLock.readLock().lock();
        try {
            return currentMetrics != null ? currentMetrics : HardwareMetricsDTO.empty();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public List<HardwareMetricsDTO> getTrendHistory() {
        cacheLock.readLock().lock();
        try {
            return historyBuffer != null ? historyBuffer.snapshot() : List.of();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public SystemInfoDTO getSystemInfo() {
        cacheLock.readLock().lock();
        try {
            if (currentMetrics != null && currentMetrics.getSystem() != null) {
                return currentMetrics.getSystem();
            }
            return new SystemInfoDTO();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    // ==================== 生命周期管理 ====================

    /**
     * 优雅关闭采集调度器。
     * <p>
     * 先尝试平滑关闭（等待 5 秒），超时后强制关闭。
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 格式化辅助方法 ====================

    /**
     * 将字节数格式化为人类可读的字符串。
     * <p>
     * 使用 1024 进制除法：Bytes → KB → MB → GB → TB。
     *
     * @param bytes 字节数
     * @return 格式化后的 MetricValue
     */
    private MetricValue<Long> formatBytes(long bytes) {
        if (bytes == 0) {
            return MetricValue.of(0L, "0 B", "B");
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double value = (double) bytes;
        while (Math.abs(value) >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        String format;
        if (unitIndex == 0) {
            format = String.format("%d B", (long) value);
        } else {
            format = String.format("%.2f %s", value, units[unitIndex]);
        }
        return MetricValue.of(bytes, format, units[unitIndex]);
    }

    /**
     * 将每秒字节数格式化为速率字符串。
     *
     * @param bytesPerSec 每秒字节数
     * @return 格式化字符串（如 "12.5 MB/s"）
     */
    private String formatBytesPerSec(long bytesPerSec) {
        if (bytesPerSec == 0) {
            return "0 B/s";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double value = (double) bytesPerSec;
        while (Math.abs(value) >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        if (unitIndex == 0) {
            return String.format("%d B/s", (long) value);
        } else {
            return String.format("%.2f %s/s", value, units[unitIndex]);
        }
    }

    /**
     * 将百分比值格式化为 MetricValue。
     *
     * @param pct 百分比原始值（如 50.0 表示 50%）
     * @return 格式化后的 MetricValue
     */
    private MetricValue<Double> formatPercent(double pct) {
        double rounded = Math.round(pct * 10.0) / 10.0;
        return MetricValue.of(rounded, String.format("%.1f%%", rounded), "%");
    }

    /**
     * 将秒数格式化为人类可读的运行时间字符串。
     *
     * @param seconds 运行秒数
     * @return 格式化字符串（如 "2d 3h 15m"）
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        sb.append(minutes).append("m");
        return sb.toString();
    }
}
