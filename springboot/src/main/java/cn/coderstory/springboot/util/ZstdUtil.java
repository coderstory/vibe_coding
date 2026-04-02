package cn.coderstory.springboot.util;

import com.github.luben.zstd.Zstd;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ZstdUtil {
    
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
    
    public static byte[] decompress(byte[] compressed, long originalSize) {
        if (compressed == null || compressed.length == 0) {
            return null;
        }
        try {
            return Zstd.decompress(new byte[(int) originalSize], compressed);
        } catch (Exception e) {
            log.error("zstd decompress error", e);
            return null;
        }
    }
}
