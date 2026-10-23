package com.yunnongzhizhen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunnongzhizhen.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 阶段5：上传→识别（Mock AiService）落库；诊断多轮；非法类型拒绝。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DetectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @Test
    void uploadDetectHistoryAndChat_withMockAi() throws Exception {
        when(aiService.detectByImage(anyString(), nullable(String.class)))
                .thenReturn(Map.of(
                        "result", "稻瘟病",
                        "confidence", 0.91,
                        "description", "叶片出现梭形病斑"
                ));
        when(aiService.chatDiagnose(anyList(), anyString(), anyString()))
                .thenReturn("建议及时清除病叶并喷施三环唑。");

        String token = registerAndLogin("detect_user_" + System.currentTimeMillis());

        // 1px PNG
        byte[] png = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF,
                (byte) 0xC0, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01, 0x00,
                0x05, (byte) 0xFE, (byte) 0xD4, (byte) 0xEF, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42,
                0x60, (byte) 0x82
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "leaf.png", "image/png", png);

        MvcResult upload = mockMvc.perform(multipart("/api/user/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.path").isNotEmpty())
                .andExpect(jsonPath("$.data.contentType").value("image/png"))
                .andReturn();
        String path = objectMapper.readTree(upload.getResponse().getContentAsString())
                .path("data").path("path").asText();

        MvcResult detect = mockMvc.perform(post("/api/user/detection/detect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"imagePath":"%s","cropId":null}
                                """.formatted(path)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.result").value("稻瘟病"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        long recordId = objectMapper.readTree(detect.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/user/detection/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].result").value("稻瘟病"));

        MvcResult chat1 = mockMvc.perform(post("/api/user/diagnosis/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"detectionRecordId":%d,"question":"怎么防治？"}
                                """.formatted(recordId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.answer").isNotEmpty())
                .andExpect(jsonPath("$.data.sessionId").isNotEmpty())
                .andReturn();
        String sessionId = objectMapper.readTree(chat1.getResponse().getContentAsString())
                .path("data").path("sessionId").asText();

        mockMvc.perform(post("/api/user/diagnosis/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"detectionRecordId":%d,"sessionId":"%s","question":"需要打几次药？"}
                                """.formatted(recordId, sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sessionId").value(sessionId));
    }

    @Test
    void upload_rejectNonImage() throws Exception {
        String token = registerAndLogin("detect_bad_" + System.currentTimeMillis());
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "hello".getBytes());
        mockMvc.perform(multipart("/api/user/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Pass@123","confirmPassword":"Pass@123","nickname":"识别用户"}
                                """.formatted(username)))
                .andExpect(status().isOk());
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Pass@123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }
}
