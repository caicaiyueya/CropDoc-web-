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
 * 阶段8：用户管理、角色调整、启禁用、仪表盘；FARMER 访问管理员接口 403。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminUserLifecycleAndFarmerForbidden() throws Exception {
        String farmerName = "admin_mgmt_" + System.currentTimeMillis();
        String farmerToken = registerAndLogin(farmerName);
        String adminToken = login("admin", "Admin@123");

        // FARMER 访问管理员接口 → 403
        mockMvc.perform(get("/api/admin/user/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/dashboard/stats").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/user/list")
                        .param("keyword", farmerName)
                        .param("role", "FARMER")
                        .param("status", "NORMAL")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].username").value(farmerName));

        long userId = objectMapper.readTree(
                        mockMvc.perform(get("/api/admin/user/list")
                                        .param("keyword", farmerName)
                                        .header("Authorization", "Bearer " + adminToken))
                                .andReturn().getResponse().getContentAsString())
                .path("data").path("records").path(0).path("id").asLong();

        mockMvc.perform(post("/api/admin/user/" + userId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EXPERT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 同一 token 下次请求即为 EXPERT（DB 加载 role）
        mockMvc.perform(get("/api/user/certification/expert-ping")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/user/" + userId + "/reset-password")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"NewPass@123\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"NewPass@123"}
                                """.formatted(farmerName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("EXPERT"));

        mockMvc.perform(post("/api/admin/user/" + userId + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"NewPass@123"}
                                """.formatted(farmerName)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/user/" + userId + "/enable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"NewPass@123"}
                                """.formatted(farmerName)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/dashboard/stats").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userTotal").isNumber())
                .andExpect(jsonPath("$.data.farmerCount").isNumber())
                .andExpect(jsonPath("$.data.expertCount").isNumber())
                .andExpect(jsonPath("$.data.adminCount").isNumber())
                .andExpect(jsonPath("$.data.certPending").isNumber())
                .andExpect(jsonPath("$.data.postPending").isNumber())
                .andExpect(jsonPath("$.data.todayDetection").isNumber());
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Pass@123","confirmPassword":"Pass@123","nickname":"管理测试"}
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
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }
}
