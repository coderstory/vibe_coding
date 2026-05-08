package cn.coderstory.springboot.util;

import com.github.luben.zstd.Zstd;
import lombok.extern.slf4j.Slf4j;

/**
 * Zstd 压缩工具类。
 * <p>
 * 封装 Zstd 原生库的压缩和解压缩方法，用于大文件数据的传输优化。
 *
 * @since 1.7.0
 */
@Slf4j
public class ZstdUtil {

    /**
     * 压缩字节数组数据。
     *
     * @param data 原始数据
     * @return 压缩后的数据，输入为空时返回 null
     */
    public static byte[] compress(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return Zstd.compress(data);
        } catch (Exception e) {
            log.error("zstd compress error", e);
            return null;
        }
    }

    /**
     * 解压缩字节数组数据。
     *
     * @param compressed   压缩后的数据
     * @param originalSize 原始数据大小
     * @return 解压后的数据，输入为空时返回 null
     */
    public static byte[] decompress(byte[] compressed, long originalSize) {
        if (compressed == null || compressed.length == 0) {
            return null;
        }
        try {
            byte[] decompressed = new byte[(int) originalSize];
            Zstd.decompress(decompressed, compressed);
            return decompressed;
        } catch (Exception e) {
            log.error("zstd decompress error", e);
            return null;
        }
    }
}
