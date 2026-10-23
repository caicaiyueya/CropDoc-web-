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
 * 阶段6：发帖审核、GUIDE 权限、优质标记、收藏。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommunityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createApproveList_andFarmerCannotGuide() throws Exception {
        String farmerToken = registerAndLogin("comm_farmer_" + System.currentTimeMillis());
        String adminToken = login("admin", "Admin@123");

        mockMvc.perform(post("/api/user/post/create-guide")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"指导","content":"内容","tag":"GUIDE"}
                                """))
                .andExpect(status().isForbidden());

        MvcResult create = mockMvc.perform(post("/api/user/post/create")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"叶子发黄怎么办","content":"水稻叶片发黄","tag":"NORMAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        long postId = objectMapper.readTree(create.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/user/post/list")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray());

        mockMvc.perform(post("/api/admin/post/" + postId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/user/post/list")
                        .param("sort", "time")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value((int) postId));

        mockMvc.perform(post("/api/user/favorite/toggle")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":" + postId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true));

        mockMvc.perform(post("/api/user/comment/" + 1 + "/mark-quality")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void expertCanGuideAndMarkQuality() throws Exception {
        String farmerToken = registerAndLogin("comm_up_" + System.currentTimeMillis());
        String adminToken = login("admin", "Admin@123");

        MvcResult submit = mockMvc.perform(post("/api/user/certification/submit")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"qualificationDesc":"农技十年","materialPath":"/uploads/c.pdf"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long appId = objectMapper.readTree(submit.getResponse().getContentAsString())
                .path("data").path("id").asLong();
        mockMvc.perform(post("/api/admin/certification/" + appId + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewComment\":\"ok\"}"))
                .andExpect(status().isOk());

        // 升级后同一 token 即为 EXPERT
        MvcResult guide = mockMvc.perform(post("/api/user/post/create-guide")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"春季施肥指导","content":"注意氮磷钾配比"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tag").value("GUIDE"))
                .andReturn();
        long guideId = objectMapper.readTree(guide.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/admin/post/" + guideId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        MvcResult comment = mockMvc.perform(post("/api/user/comment/create")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":%d,"content":"专业建议：先测土再施肥"}
                                """.formatted(guideId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isProfessional").value(true))
                .andReturn();
        long commentId = objectMapper.readTree(comment.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/user/comment/" + commentId + "/mark-quality")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isQuality").value(true));
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Pass@123","confirmPassword":"Pass@123","nickname":"社区用户"}
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
