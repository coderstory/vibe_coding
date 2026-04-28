package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.service.RocketMQAdminService;
import cn.coderstory.springboot.vo.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RocketMQController 单元测试
 *
 * 测试 RocketMQ 管理 API 的完整流程：
 * 1. Topic 列表查询
 * 2. Topic 详情查询
 * 3. Topic 创建
 * 4. Topic 删除
 * 5. Consumer Group 列表查询
 * 6. Consumer Group 详情查询
 * 7. Consumer Group 位点重置
 *
 * 注意: 这些测试使用 MockMvcBuilders.standaloneSetup() 进行单元测试
 * 不依赖 Spring Boot Web 测试切片
 *
 * @author system
 * @version 1.0
 * @since 2026-04-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RocketMQController 单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RocketMQControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RocketMQAdminService rocketMQAdminService;

    @InjectMocks
    private RocketMQController rocketMQController;

    // ==================== Topic 测试数据 ====================

    private List<Map<String, Object>> createMockTopicList() {
        List<Map<String, Object>> topics = new ArrayList<>();

        Map<String, Object> topic1 = new HashMap<>();
        topic1.put("topicName", "TestTopic1");
        topic1.put("queueCount", 8);
        topic1.put("readQueueNums", 8);
        topic1.put("perm", "READ_WRITE");
        topic1.put("status", "ACTIVE");
        topics.add(topic1);

        Map<String, Object> topic2 = new HashMap<>();
        topic2.put("topicName", "TestTopic2");
        topic2.put("queueCount", 4);
        topic2.put("readQueueNums", 4);
        topic2.put("perm", "READ");
        topic2.put("status", "ACTIVE");
        topics.add(topic2);

        return topics;
    }

    private Map<String, Object> createMockTopicDetail(String topicName) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("topicName", topicName);
        detail.put("queueCount", 8);
        detail.put("readQueueNums", 8);
        detail.put("perm", "READ_WRITE");
        detail.put("status", "ACTIVE");
        return detail;
    }

    // ==================== Consumer Group 测试数据 ====================

    private List<Map<String, Object>> createMockConsumerGroupList() {
        List<Map<String, Object>> groups = new ArrayList<>();

        Map<String, Object> group1 = new HashMap<>();
        group1.put("group", "TestGroup1");
        group1.put("groupType", "CLUSTERING");
        group1.put("status", "OK");
        group1.put("consumerCount", 3);
        group1.put("accumulatedDiff", 100L);
        groups.add(group1);

        Map<String, Object> group2 = new HashMap<>();
        group2.put("group", "TestGroup2");
        group2.put("groupType", "BROADCASTING");
        group2.put("status", "OK");
        group2.put("consumerCount", 5);
        group2.put("accumulatedDiff", 0L);
        groups.add(group2);

        return groups;
    }

    private Map<String, Object> createMockConsumerGroupDetail(String groupName) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("group", groupName);
        detail.put("groupType", "CLUSTERING");
        detail.put("status", "OK");
        detail.put("consumerCount", 3);
        detail.put("totalDiff", 100L);

        Map<String, Long> offsetTable = new HashMap<>();
        offsetTable.put("TestTopic-0", 1000L);
        offsetTable.put("TestTopic-1", 1000L);
        offsetTable.put("TestTopic-2", 1000L);
        detail.put("offsetTable", offsetTable);

        return detail;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rocketMQController)
                .setControllerAdvice(new cn.coderstory.springboot.exception.GlobalExceptionHandler())
                .build();
    }

    // ==================== Topic API 测试 ====================

    @Test
    @Order(1)
    @DisplayName("GET /api/rocketmq/topics - 应返回 Topic 列表")
    void shouldGetTopicList() throws Exception {
        List<Map<String, Object>> mockTopics = createMockTopicList();
        when(rocketMQAdminService.getTopicList(isNull())).thenReturn(mockTopics);

        mockMvc.perform(get("/api/rocketmq/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].topicName").value("TestTopic1"))
                .andExpect(jsonPath("$.data.records[1].topicName").value("TestTopic2"))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/rocketmq/topics?keyword=xxx - 应返回过滤后的 Topic 列表")
    void shouldGetTopicListWithKeyword() throws Exception {
        List<Map<String, Object>> mockTopics = new ArrayList<>();
        Map<String, Object> topic1 = new HashMap<>();
        topic1.put("topicName", "FilteredTopic");
        topic1.put("queueCount", 8);
        topic1.put("status", "ACTIVE");
        mockTopics.add(topic1);

        when(rocketMQAdminService.getTopicList("Filtered")).thenReturn(mockTopics);

        mockMvc.perform(get("/api/rocketmq/topics")
                        .param("keyword", "Filtered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].topicName").value("FilteredTopic"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/rocketmq/topics/{topicName} - 应返回 Topic 详情")
    void shouldGetTopicDetail() throws Exception {
        String topicName = "TestTopic1";
        Map<String, Object> mockDetail = createMockTopicDetail(topicName);
        when(rocketMQAdminService.getTopicDetail(topicName)).thenReturn(mockDetail);

        mockMvc.perform(get("/api/rocketmq/topics/{topicName}", topicName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.topicName").value(topicName))
                .andExpect(jsonPath("$.data.queueCount").value(8))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/rocketmq/topics - 应创建 Topic")
    void shouldCreateTopic() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("topicName", "NewTopic");
        request.put("queueCount", 8);
        request.put("perm", "READ_WRITE");

        mockMvc.perform(post("/api/rocketmq/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Topic 创建成功"));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/rocketmq/topics/{topicName} - 应删除 Topic")
    void shouldDeleteTopic() throws Exception {
        String topicName = "ToDeleteTopic";

        mockMvc.perform(delete("/api/rocketmq/topics/{topicName}", topicName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Topic 删除成功"));
    }

    // ==================== Consumer Group API 测试 ====================

    @Test
    @Order(6)
    @DisplayName("GET /api/rocketmq/consumer-groups - 应返回 Consumer Group 列表")
    void shouldGetConsumerGroupList() throws Exception {
        List<Map<String, Object>> mockGroups = createMockConsumerGroupList();
        when(rocketMQAdminService.getConsumerGroupList(isNull())).thenReturn(mockGroups);

        mockMvc.perform(get("/api/rocketmq/consumer-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].group").value("TestGroup1"))
                .andExpect(jsonPath("$.data.records[0].groupType").value("CLUSTERING"))
                .andExpect(jsonPath("$.data.records[1].group").value("TestGroup2"))
                .andExpect(jsonPath("$.data.records[1].groupType").value("BROADCASTING"))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/rocketmq/consumer-groups?keyword=xxx - 应返回过滤后的列表")
    void shouldGetConsumerGroupListWithKeyword() throws Exception {
        List<Map<String, Object>> mockGroups = new ArrayList<>();
        Map<String, Object> group1 = new HashMap<>();
        group1.put("group", "SearchedGroup");
        group1.put("groupType", "CLUSTERING");
        group1.put("status", "OK");
        group1.put("consumerCount", 2);
        mockGroups.add(group1);

        when(rocketMQAdminService.getConsumerGroupList("Searched")).thenReturn(mockGroups);

        mockMvc.perform(get("/api/rocketmq/consumer-groups")
                        .param("keyword", "Searched"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].group").value("SearchedGroup"));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/rocketmq/consumer-groups/{group} - 应返回 Consumer Group 详情")
    void shouldGetConsumerGroupDetail() throws Exception {
        String groupName = "TestGroup1";
        Map<String, Object> mockDetail = createMockConsumerGroupDetail(groupName);
        when(rocketMQAdminService.getConsumerGroupDetail(groupName)).thenReturn(mockDetail);

        mockMvc.perform(get("/api/rocketmq/consumer-groups/{group}", groupName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.group").value(groupName))
                .andExpect(jsonPath("$.data.groupType").value("CLUSTERING"))
                .andExpect(jsonPath("$.data.consumerCount").value(3))
                .andExpect(jsonPath("$.data.offsetTable").isMap());
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/rocketmq/consumer-groups/{group}/reset-offset - 应重置位点")
    void shouldResetConsumerOffset() throws Exception {
        String groupName = "TestGroup1";
        Map<String, Object> request = new HashMap<>();
        request.put("topic", "TestTopic");
        request.put("timestamp", System.currentTimeMillis());

        mockMvc.perform(post("/api/rocketmq/consumer-groups/{group}/reset-offset", groupName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("位点重置成功"));
    }

    // ==================== 异常场景测试 ====================

    @Test
    @Order(10)
    @DisplayName("GET /api/rocketmq/topics/{topicName} - Topic 不存在应返回错误")
    void shouldReturnErrorWhenTopicNotFound() throws Exception {
        String topicName = "NonExistentTopic";
        when(rocketMQAdminService.getTopicDetail(topicName))
                .thenThrow(new BusinessException("Topic 不存在: " + topicName));

        mockMvc.perform(get("/api/rocketmq/topics/{topicName}", topicName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/rocketmq/consumer-groups/{group} - Group 不存在应返回错误")
    void shouldReturnErrorWhenConsumerGroupNotFound() throws Exception {
        String groupName = "NonExistentGroup";
        when(rocketMQAdminService.getConsumerGroupDetail(groupName))
                .thenThrow(new BusinessException("Consumer Group 不存在: " + groupName));

        mockMvc.perform(get("/api/rocketmq/consumer-groups/{group}", groupName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}