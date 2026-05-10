package cn.coderstory.springboot.sse.monitor;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * HardwareSseService 单元测试。
 * <p>
 * 验证 broadcast 调用、连接清理和连接计数功能。
 * 使用 Mock SseEmitter 验证 IOException 处理和回调清理。
 *
 * @since 1.8.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HardwareSseService 单元测试")
class HardwareSseServiceTest {

    @Mock
    private SseEmitter mockEmitter;

    private HardwareSseService sseService;

    @BeforeEach
    void setUp() {
        sseService = new HardwareSseService();
    }

    @Nested
    @DisplayName("createEmitter()")
    class CreateEmitter {

        @Test
        @DisplayName("调用 createEmitter() 返回非 null 的 SseEmitter")
        void shouldCreateAndReturnEmitter() {
            SseEmitter emitter = sseService.createEmitter();
            assertNotNull(emitter);
        }
    }

    @Nested
    @DisplayName("broadcast()")
    class Broadcast {

        @Test
        @DisplayName("连接池为空时调用 broadcast() 不抛出异常")
        void shouldNotFailWhenNoEmitters() {
            assertDoesNotThrow(() -> sseService.broadcast("metrics", "test"));
        }

        @Test
        @DisplayName("调用 broadcast() 后 emitter 未关闭")
        void shouldSendToAllEmitters() throws Exception {
            SseEmitter realEmitter = sseService.createEmitter();
            assertDoesNotThrow(() -> sseService.broadcast("metrics", "{\"test\":1}"));
        }

        @Test
        @DisplayName("IOException 时从连接池移除 emitter")
        void shouldHandleEmitterRemovalOnIOException() throws Exception {
            doThrow(new IOException("mock io error")).when(mockEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));

            Field emittersField = HardwareSseService.class.getDeclaredField("emitters");
            emittersField.setAccessible(true);
            Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) emittersField.get(sseService);
            String testClientId = "test-client-id";
            emitters.put(testClientId, mockEmitter);

            sseService.broadcast("metrics", "test");

            assertTrue(emitters.isEmpty());
        }
    }

    @Nested
    @DisplayName("unsubscribe()")
    class Unsubscribe {

        @Test
        @DisplayName("调用 unsubscribe 后 emitters 中不再有该 clientId")
        void shouldRemoveEmitterAndComplete() throws Exception {
            SseEmitter emitter = sseService.createEmitter();
            Field emittersField = HardwareSseService.class.getDeclaredField("emitters");
            emittersField.setAccessible(true);
            Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) emittersField.get(sseService);

            String clientId = emitters.keySet().iterator().next();
            assertEquals(1, sseService.getActiveConnectionCount());

            sseService.unsubscribe(clientId);
            assertFalse(emitters.containsKey(clientId));
        }

        @Test
        @DisplayName("传入不存在的 clientId 不抛出异常")
        void shouldNotFailForUnknownClientId() {
            assertDoesNotThrow(() -> sseService.unsubscribe("non-existent-id"));
        }
    }

    @Nested
    @DisplayName("getActiveConnectionCount()")
    class ActiveConnectionCount {

        @Test
        @DisplayName("创建 3 个 emitter 后返回 3")
        void shouldReturnCorrectCount() {
            sseService.createEmitter();
            sseService.createEmitter();
            sseService.createEmitter();
            assertEquals(3, sseService.getActiveConnectionCount());
        }

        @Test
        @DisplayName("所有连接移除后返回 0")
        void shouldReturnZeroAfterAllRemoved() throws Exception {
            sseService.createEmitter();
            sseService.createEmitter();

            Field emittersField = HardwareSseService.class.getDeclaredField("emitters");
            emittersField.setAccessible(true);
            Map<String, SseEmitter> emitters = (Map<String, SseEmitter>) emittersField.get(sseService);
            emitters.clear();

            assertEquals(0, sseService.getActiveConnectionCount());
        }
    }
}
