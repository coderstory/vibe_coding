package cn.coderstory.springboot.service.monitor.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RingBuffer 单元测试。
 * <p>
 * 验证环形缓冲区的插入顺序、容量覆盖和线程安全性。
 *
 * @since 1.8.0
 */
@DisplayName("RingBuffer 单元测试")
class RingBufferTest {

    private RingBuffer<String> buffer;

    @BeforeEach
    void setUp() {
        buffer = new RingBuffer<>(3);
    }

    @Nested
    @DisplayName("add and snapshot")
    class AddAndSnapshotTests {

        @Test
        @DisplayName("添加元素后 snapshot 返回正确数量和 FIFO 顺序")
        void whenAddElements_snapshotReturnsInOrder() {
            buffer.add("A");
            buffer.add("B");
            buffer.add("C");

            List<String> result = buffer.snapshot();

            assertEquals(3, result.size());
            assertEquals("A", result.get(0));
            assertEquals("B", result.get(1));
            assertEquals("C", result.get(2));
        }

        @Test
        @DisplayName("部分填充时 snapshot 按插入顺序返回")
        void whenPartiallyFilled_returnsInOrder() {
            buffer.add("X");
            buffer.add("Y");

            List<String> result = buffer.snapshot();

            assertEquals(2, result.size());
            assertEquals("X", result.get(0));
            assertEquals("Y", result.get(1));
        }
    }

    @Nested
    @DisplayName("capacity")
    class CapacityTests {

        @Test
        @DisplayName("超出容量时最早元素被覆盖")
        void whenExceedsCapacity_overwritesOldest() {
            buffer.add("A");
            buffer.add("B");
            buffer.add("C");
            buffer.add("D");
            buffer.add("E");

            List<String> result = buffer.snapshot();

            assertEquals(3, result.size());
            assertEquals("C", result.get(0));
            assertEquals("D", result.get(1));
            assertEquals("E", result.get(2));
        }

        @Test
        @DisplayName("恰好填满容量时所有元素保留")
        void whenExactlyFilled_allElementsPreserved() {
            buffer.add("A");
            buffer.add("B");
            buffer.add("C");

            List<String> result = buffer.snapshot();

            assertEquals(3, result.size());
            assertEquals("A", result.get(0));
            assertEquals("B", result.get(1));
            assertEquals("C", result.get(2));
        }
    }

    @Nested
    @DisplayName("empty buffer")
    class EmptyBufferTests {

        @Test
        @DisplayName("空 buffer 的 snapshot 返回空列表")
        void whenEmpty_snapshotReturnsEmptyList() {
            assertTrue(buffer.snapshot().isEmpty());
        }

        @Test
        @DisplayName("空 buffer 的 size 返回 0")
        void whenEmpty_sizeIsZero() {
            assertEquals(0, buffer.size());
        }

        @Test
        @DisplayName("空 buffer 的 capacity 返回构造时设定值")
        void whenEmpty_capacityIsCorrect() {
            assertEquals(3, buffer.capacity());
        }
    }

    @Nested
    @DisplayName("thread safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发 add 和 snapshot 不抛出异常")
        void whenConcurrentAddAndSnapshot_noException() throws InterruptedException {
            int threadCount = 4;
            AtomicBoolean exceptionCaught = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                for (int i = 0; i < threadCount; i++) {
                    final int id = i;
                    executor.submit(() -> {
                        try {
                            for (int j = 0; j < 100; j++) {
                                buffer.add("Thread-" + id + "-" + j);
                                buffer.snapshot();
                            }
                        } catch (Exception e) {
                            exceptionCaught.set(true);
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                latch.await();
                assertFalse(exceptionCaught.get(), "并发操作不应抛出异常");
                assertEquals(3, buffer.size());
            }
        }
    }
}
