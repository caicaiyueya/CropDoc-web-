package com.yunnongzhizhen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 阶段4：提交→通过→同 token 访问 EXPERT 接口成功；驳回仍 FARMER。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CertificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void approveThenSameTokenGetsExpert() throws Exception {
        String farmerToken = registerAndLogin("farmer_ok_" + System.currentTimeMillis());
        String adminToken = login("admin", "Admin@123");

        mockMvc.perform(get("/api/user/certification/expert-ping")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());

        MvcResult submit = mockMvc.perform(post("/api/user/certification/submit")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"qualificationDesc":"种植经验十年","materialPath":"/uploads/cert.pdf"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        long appId = objectMapper.readTree(submit.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/admin/certification/" + appId + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewComment\":\"材料齐全\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 同一 token，因过滤器每次从 DB 加载 role，应立刻具备 EXPERT
        mockMvc.perform(get("/api/user/certification/expert-ping")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("EXPERT_OK"));
    }

    @Test
    void rejectKeepsFarmer() throws Exception {
        String farmerToken = registerAndLogin("farmer_rej_" + System.currentTimeMillis());
        String adminToken = login("admin", "Admin@123");

        MvcResult submit = mockMvc.perform(post("/api/user/certification/submit")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"qualificationDesc":"材料不足测试","materialPath":"/uploads/x.pdf"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long appId = objectMapper.readTree(submit.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/admin/certification/" + appId + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewComment\":\"材料不全\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/certification/expert-ping")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/user/certification/status")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("FARMER"));
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Pass@123","confirmPassword":"Pass@123","nickname":"农户"}
                                """.formatted(username)))
                .andExpect(status().isOk());
        return login(username, "Pass@123");
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }
}
