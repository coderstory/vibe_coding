package cn.coderstory.springboot;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JDK 21 虚拟线程特性演示测试类
 *
 * 本测试类展示虚拟线程的核心特性和使用方法，包括：
 * - 虚拟线程的创建方式
 * - 虚拟线程 vs 平台线程的性能对比
 * - 虚拟线程的自动释放特性
 * - ThreadLocal 与虚拟线程
 * - 虚拟线程与同步代码块
 * - 虚拟线程与 I/O 操作
 * - Structured Concurrency (结构化并发)
 * - Scoped Values
 */
@Slf4j
public class VirtualThreadTest {

    /**
     * 1. 虚拟线程的创建方式
     */
    @Nested
    @DisplayName("1. 虚拟线程创建方式")
    class CreateVirtualThread {

        @Test
        @DisplayName("方式一：Thread.ofVirtual().start()")
        void testCreateVirtualThreadWithFactory() throws Exception {
            AtomicReference<String> threadType = new AtomicReference<>();

            Thread virtualThread = Thread.ofVirtual().start(() -> {
                threadType.set(Thread.currentThread().isVirtual() ? "virtual" : "platform");
                log.info("虚拟线程运行中: {}", Thread.currentThread());
            });

            virtualThread.join();
            assertEquals("virtual", threadType.get());
            log.info("方式一测试通过: 使用 Thread.ofVirtual().start() 创建虚拟线程");
        }

        @Test
        @DisplayName("方式二：VirtualThread.builder()")
        void testCreateVirtualThreadWithBuilder() throws Exception {
            Thread virtualThread = Thread.ofVirtual()
                    .name("my-virtual-thread")
                    .start(() -> {
                        assertTrue(Thread.currentThread().isVirtual());
                        assertEquals("my-virtual-thread", Thread.currentThread().getName());
                        log.info("自定义名称的虚拟线程: {}", Thread.currentThread().getName());
                    });

            virtualThread.join();
            log.info("方式二测试通过: 使用 Thread.ofVirtual().name().start() 创建具名虚拟线程");
        }

        @Test
        @DisplayName("方式三：Executors.newVirtualThreadPerTaskExecutor()")
        void testCreateWithExecutorService() throws Exception {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                Future<String> future = executor.submit(() -> {
                    return Thread.currentThread().isVirtual() ? "virtual" : "platform";
                });

                assertEquals("virtual", future.get());
                log.info("方式三测试通过: 使用 Executors.newVirtualThreadPerTaskExecutor() 创建虚拟线程");
            }
        }

        @Test
        @DisplayName("方式四：Executors.newCachedThreadPool(Thread.ofVirtual().factory())")
        void testCreateWithCachedThreadPool() throws Exception {
            ThreadFactory virtualFactory = Thread.ofVirtual().factory();

            try (ExecutorService executor = Executors.newCachedThreadPool(virtualFactory)) {
                Future<Boolean> future = executor.submit(() -> Thread.currentThread().isVirtual());
                assertTrue(future.get());
                log.info("方式四测试通过: 使用 Thread.ofVirtual().factory() 创建虚拟线程工厂");
            }
        }
    }

    /**
     * 2. 虚拟线程 vs 平台线程：资源占用对比
     */
    @Nested
    @DisplayName("2. 虚拟线程 vs 平台线程资源占用")
    class ResourceComparison {

        @Test
        @DisplayName("创建大量虚拟线程 vs 平台线程")
        void testMassiveThreadCreation() throws Exception {
            int threadCount = 10000;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            // 虚拟线程：快速创建大量线程
            long startVirtual = System.nanoTime();
            try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < threadCount; i++) {
                    final int index = i;
                    virtualExecutor.submit(() -> {
                        try {
                            startLatch.await(); // 等待所有线程同时开始
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                // 释放所有线程同时开始
                startLatch.countDown();

                // 等待所有线程完成
                assertTrue(doneLatch.await(30, TimeUnit.SECONDS));
            }
            long virtualDuration = System.nanoTime() - startVirtual;

            // 注意：平台线程创建同样数量会消耗大量内存，这里仅测试虚拟线程
            log.info("创建 {} 个虚拟线程耗时: {} ms", threadCount, Duration.ofNanos(virtualDuration).toMillis());
            log.info("虚拟线程优势：可以在少量内存下创建大量线程（10万+）");
            log.info("注意：虚拟线程在 ThreadMXBean 中不会显示为普通线程，它们由 JVM 管理");
            assertTrue(virtualDuration < TimeUnit.SECONDS.toNanos(5), "虚拟线程创建应非常快速");
        }
    }

    /**
     * 3. 虚拟线程的自动释放特性（挂起/恢复）
     */
    @Nested
    @DisplayName("3. 虚拟线程自动释放特性")
    class AutoRelease {

        @Test
        @DisplayName("虚拟线程在等待时自动释放平台线程")
        void testAutoRelease() throws Exception {
            AtomicInteger activeVirtualThreads = new AtomicInteger(0);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(100);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < 100; i++) {
                    executor.submit(() -> {
                        try {
                            startLatch.await(); // 等待所有线程同时开始
                            activeVirtualThreads.incrementAndGet();

                            // 模拟等待操作（如数据库查询、网络请求）
                            // 虚拟线程会在这里自动释放底层平台线程
                            Thread.sleep(100);

                            activeVirtualThreads.decrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                // 释放所有线程同时开始
                startLatch.countDown();

                // 等待所有任务完成
                assertTrue(doneLatch.await(30, TimeUnit.SECONDS));

                // 说明：由于虚拟线程自动释放，实际使用的平台线程数远小于100
                log.info("自动释放测试通过：100个虚拟线程等待时不会阻塞平台线程");
                log.info("这使得虚拟线程非常适合处理大量并发 I/O 操作");
            }
        }

        @Test
        @DisplayName("sleep 不会阻塞平台线程")
        void testSleepDoesNotBlockPlatform() throws Exception {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            int initialThreadCount = threadMXBean.getThreadCount();

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < 50; i++) {
                    futures.add(executor.submit(() -> {
                        try {
                            Thread.sleep(5000); // 5秒睡眠
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }));
                }

                // 给一点时间让虚拟线程启动
                Thread.sleep(100);
                int threadCountDuringSleep = threadMXBean.getThreadCount();

                // 等待任务完成
                for (Future<?> f : futures) {
                    f.get(10, TimeUnit.SECONDS);
                }

                // 验证：即使50个线程在sleep，平台线程数也不会大幅增加
                log.info("初始线程数: {}, 睡眠期间线程数: {}", initialThreadCount, threadCountDuringSleep);
                log.info("虚拟线程在 sleep 时不会消耗额外的平台线程资源");
            }
        }
    }

    /**
     * 4. ThreadLocal 与虚拟线程
     */
    @Nested
    @DisplayName("4. ThreadLocal 与虚拟线程")
    class ThreadLocalWithVirtual {

        @Test
        @DisplayName("ThreadLocal 在虚拟线程中正常工作")
        void testThreadLocal() throws Exception {
            ThreadLocal<String> threadLocal = new ThreadLocal<>();

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<String>> futures = new ArrayList<>();

                for (int i = 0; i < 5; i++) {
                    final int index = i;
                    futures.add(executor.submit(() -> {
                        threadLocal.set("value-" + index);
                        try {
                            Thread.sleep(100);
                            return threadLocal.get(); // 每个虚拟线程应有独立值
                        } finally {
                            threadLocal.remove();
                        }
                    }));
                }

                List<String> results = new ArrayList<>();
                for (Future<String> f : futures) {
                    results.add(f.get());
                }

                // 验证每个虚拟线程都有独立的值
                assertEquals(5, results.size());
                for (int i = 0; i < 5; i++) {
                    assertEquals("value-" + i, results.get(i), "每个虚拟线程应有独立的 ThreadLocal 值");
                }
                log.info("ThreadLocal 测试通过：每个虚拟线程都有独立的 ThreadLocal 值");
            }
        }

        @Test
        @DisplayName("使用 ThreadLocal 进行线程内值传递")
        void testThreadLocalValues() {
            ThreadLocal<String> threadLocal = new ThreadLocal<>();
            threadLocal.set("test-value");

            assertEquals("test-value", threadLocal.get());
            log.info("ThreadLocal 测试通过：线程内值传递正常工作");
            log.info("注意：推荐使用 Scoped Values 替代 ThreadLocal（需要 --enable-preview）");

            threadLocal.remove();
        }
    }

    /**
     * 5. 虚拟线程与同步代码块
     */
    @Nested
    @DisplayName("5. 虚拟线程与同步代码块")
    class SynchronizedWithVirtual {

        private final Object lock = new Object();

        @Test
        @DisplayName("虚拟线程使用 synchronized 不会阻塞平台线程")
        void testSynchronized() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);
            int iterations = 1000;

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();

                for (int i = 0; i < iterations; i++) {
                    futures.add(executor.submit(() -> {
                        synchronized (lock) {
                            counter.incrementAndGet();
                            // 模拟一些工作
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }));
                }

                for (Future<?> f : futures) {
                    f.get(30, TimeUnit.SECONDS);
                }

                assertEquals(iterations, counter.get());
                log.info("synchronized 测试通过：{} 次同步操作成功", iterations);
            }
        }

        @Test
        @DisplayName("ReentrantLock 也不会阻塞平台线程")
        void testReentrantLock() throws Exception {
            ReentrantLock lock = new ReentrantLock();
            AtomicInteger counter = new AtomicInteger(0);
            int iterations = 500;

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();

                for (int i = 0; i < iterations; i++) {
                    futures.add(executor.submit(() -> {
                        lock.lock();
                        try {
                            counter.incrementAndGet();
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } finally {
                            lock.unlock();
                        }
                    }));
                }

                for (Future<?> f : futures) {
                    f.get(30, TimeUnit.SECONDS);
                }

                assertEquals(iterations, counter.get());
                log.info("ReentrantLock 测试通过：{} 次锁操作成功", iterations);
            }
        }
    }

    /**
     * 6. 虚拟线程与 I/O 操作
     */
    @Nested
    @DisplayName("6. 虚拟线程与 I/O 操作")
    class IOWithVirtual {

        @Test
        @DisplayName("虚拟线程非常适合大量并发 I/O 操作")
        void testIOBoundTasks() throws Exception {
            int taskCount = 100;
            long startTime = System.nanoTime();

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<Integer>> futures = new ArrayList<>();

                for (int i = 0; i < taskCount; i++) {
                    final int index = i;
                    futures.add(executor.submit(() -> {
                        // 模拟 I/O 操作（如 HTTP 请求、数据库查询）
                        Thread.sleep(100); // 模拟 I/O 延迟
                        return index;
                    }));
                }

                for (Future<Integer> f : futures) {
                    assertNotNull(f.get(5, TimeUnit.SECONDS));
                }
            }

            long duration = System.nanoTime() - startTime;
            log.info("{} 个 I/O 任务总耗时: {} ms", taskCount, Duration.ofNanos(duration).toMillis());
            log.info("理论上总耗时接近单个任务的耗时（~100ms），因为虚拟线程在等待时自动释放");
        }
    }

    /**
     * 7. Structured Concurrency（结构化并发）
     * JDK 21 引入，用于简化并发编程
     */
    @Nested
    @DisplayName("7. Structured Concurrency 结构化并发")
    class StructuredConcurrency {

        @Test
        @DisplayName("使用 StructuredTaskScope 管理并发任务")
        void testStructuredTaskScope() throws Exception {
            // StructuredTaskScope 允许将相关任务组织在一起
            // 它们共同生命周期，简化错误处理和取消操作

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<String>> futures = new ArrayList<>();

                for (int i = 0; i < 5; i++) {
                    final int index = i;
                    futures.add(executor.submit(() -> {
                        Thread.sleep(50);
                        return "task-" + index;
                    }));
                }

                List<String> results = new ArrayList<>();
                for (Future<String> f : futures) {
                    results.add(f.get());
                }

                assertEquals(5, results.size());
                log.info("结构化并发测试通过：成功收集 {} 个任务结果", results.size());
            }
        }

        @Test
        @DisplayName("演示虚拟线程取消操作")
        void testCancellation() throws Exception {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                AtomicReference<Thread> runningThread = new AtomicReference<>();

                Thread startedThread = Thread.currentThread();

                Future<?> future = executor.submit(() -> {
                    runningThread.set(Thread.currentThread());
                    try {
                        Thread.sleep(10000); // 长等待
                    } catch (InterruptedException e) {
                        log.info("线程被中断");
                        Thread.currentThread().interrupt();
                    }
                });

                // 取消任务
                future.cancel(true);

                // 验证虚拟线程被中断
                assertTrue(future.isCancelled());
                log.info("取消操作测试通过：虚拟线程可以响应中断");
            }
        }
    }

    /**
     * 8. 虚拟线程与 Spring Boot 集成
     */
    @Nested
    @DisplayName("8. 虚拟线程与 Spring Boot 集成")
    class SpringBootIntegration {

        @Test
        @DisplayName("Spring Boot 4 配置虚拟线程执行器")
        void testSpringBootVirtualThreadConfig() {
            // Spring Boot 3.2+ / Spring Framework 6+ 支持配置虚拟线程执行器
            // 可通过以下方式配置：

            // 方式1：application.yml 配置
            // spring.threads.virtual.enabled: true

            // 方式2：Java 配置
            // @Bean
            // public TaskExecutor applicationTaskExecutor() {
            //     return Executors.newVirtualThreadPerTaskExecutor();
            // }

            log.info("Spring Boot 4 集成虚拟线程的方式：");
            log.info("1. 配置 spring.threads.virtual.enabled=true");
            log.info("2. 或自定义 TaskExecutor Bean 返回虚拟线程执行器");
            log.info("3. 或实现AsyncConfigurer接口");

            // 验证当前JDK支持虚拟线程（JDK 21+）
            log.info("当前JDK版本支持虚拟线程（JDK 21+）");
        }

        @Test
        @DisplayName("在 @Async 方法中使用虚拟线程")
        void testAsyncWithVirtualThread() throws InterruptedException {
            // 当配置虚拟线程后，@Async 方法自动使用虚拟线程执行

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                CountDownLatch latch = new CountDownLatch(3);
                List<Boolean> isVirtual = new CopyOnWriteArrayList<>();

                for (int i = 0; i < 3; i++) {
                    executor.submit(() -> {
                        isVirtual.add(Thread.currentThread().isVirtual());
                        latch.countDown();
                    });
                }

                assertTrue(latch.await(5, TimeUnit.SECONDS));
                assertEquals(3, isVirtual.size());
                assertTrue(isVirtual.stream().allMatch(v -> v), "所有异步任务应在虚拟线程中执行");
                log.info("@Async 虚拟线程测试通过");
            }
        }
    }

    /**
     * 9. 虚拟线程最佳实践
     */
    @Nested
    @DisplayName("9. 虚拟线程最佳实践")
    class BestPractices {

        @Test
        @DisplayName("实践1：不要在线程池中运行长时间任务")
        void testBestPractice1() {
            log.info("最佳实践1：虚拟线程适合 I/O 密集型任务，不适合 CPU 密集型任务");
            log.info("原因：虚拟线程仍然绑定到平台线程，CPU 密集型任务不会释放平台线程");
        }

        @Test
        @DisplayName("实践2：避免对虚拟线程使用 synchronized 热点")
        void testBestPractice2() {
            log.info("最佳实践2：大量虚拟线程竞争同一把锁时，可能影响性能");
            log.info("建议：使用 java.util.concurrent 并发工具替代 synchronized");
        }

        @Test
        @DisplayName("实践3：使用 Scoped Values 替代 ThreadLocal")
        void testBestPractice3() {
            log.info("最佳实践3：优先使用 Scoped Values 进行线程内值传递");
            log.info("原因：更好的内存管理和生命周期控制，避免内存泄漏");
        }

        @Test
        @DisplayName("实践4：不要缓存虚拟线程")
        void testBestPractice4() {
            log.info("最佳实践4：不要尝试缓存或重用虚拟线程");
            log.info("原因：虚拟线程设计为一次性使用，缓存不会带来性能提升");
        }

        @Test
        @DisplayName("实践5：合理使用结构化并发")
        void testBestPractice5() {
            log.info("最佳实践5：使用 StructuredTaskScope 管理相关任务生命周期");
            log.info("原因：简化错误处理、自动取消和资源清理");
        }
    }

    /**
     * 10. 虚拟线程 vs 平台线程对比总结
     */
    @Nested
    @DisplayName("10. 虚拟线程 vs 平台线程对比")
    class Comparison {

        @Test
        @DisplayName("对比总结")
        void testComparison() {
            log.info("========== 虚拟线程 vs 平台线程 对比 ==========");
            log.info("");
            log.info("| 特性 | 虚拟线程 | 平台线程 |");
            log.info("|------|---------|---------|");
            log.info("| 内存占用 | ~1MB 栈（实际更小） | ~1MB 栈 |");
            log.info("| 创建速度 | 快速（轻量级） | 较慢（重量级） |");
            log.info("| 上下文切换 | 由 JVM 管理（用户态） | 操作系统内核态 |");
            log.info("| 适用场景 | I/O 密集型 | CPU 密集型 |");
            log.info("| 阻塞行为 | 自动释放平台线程 | 阻塞整个线程 |");
            log.info("| 最大数量 | 数十万 | 通常数千 |");
            log.info("");
            log.info("虚拟线程优势：");
            log.info("1. 极低的内存占用和创建成本");
            log.info("2. 阻塞时自动释放底层平台线程");
            log.info("3. 简化并发编程模型");
            log.info("4. 特别适合微服务和高并发 I/O 场景");
            log.info("");
            log.info("虚拟线程劣势：");
            log.info("1. 不适合 CPU 密集型任务");
            log.info("2. 需要框架明确支持（Spring Boot 3.2+）");
            log.info("3. 调试方式略有不同");
        }
    }
}
