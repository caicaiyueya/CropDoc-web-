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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 阶段7：资讯、病害资料（全角色一致）、个人中心、Banner/公告/反馈、仪表盘。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void newsDiseaseProfileFeedbackAndDashboard() throws Exception {
        String farmerToken = registerAndLogin("content_f_" + System.currentTimeMillis());
        String adminToken = login("admin", "Admin@123");

        // 资讯：管理员发布 → 用户可见 → 下架后不可见
        MvcResult newsSave = mockMvc.perform(post("/api/admin/news/save")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"春耕提醒","content":"注意田间排水","status":"ONLINE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        long newsId = objectMapper.readTree(newsSave.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/user/news/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value((int) newsId));
        mockMvc.perform(get("/api/user/news/" + newsId).header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("春耕提醒"));

        mockMvc.perform(post("/api/admin/news/" + newsId + "/offline")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/news/" + newsId).header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        // 作物 + 病害（农户与农技员可见同一套资料，不区分 BASIC/PRO）
        MvcResult cropSave = mockMvc.perform(post("/api/admin/crop/save")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试水稻\",\"description\":\"课设测试作物\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long cropId = objectMapper.readTree(cropSave.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/user/crop/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult basic = mockMvc.perform(post("/api/admin/disease/save")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropId":%d,"name":"基础稻瘟","description":"基础","preventionAdvice":"清园"}
                                """.formatted(cropId)))
                .andExpect(status().isOk())
                .andReturn();
        long basicId = objectMapper.readTree(basic.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        MvcResult pro = mockMvc.perform(post("/api/admin/disease/save")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropId":%d,"name":"专业纹枯","description":"专业","preventionAdvice":"药剂轮换"}
                                """.formatted(cropId)))
                .andExpect(status().isOk())
                .andReturn();
        long proId = objectMapper.readTree(pro.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/user/disease/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.name=='专业纹枯')]").isNotEmpty());

        mockMvc.perform(get("/api/user/disease/" + basicId).header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("基础稻瘟"));

        mockMvc.perform(get("/api/user/disease/" + proId).header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("专业纹枯"));

        // 个人中心
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"新昵称","region":"湖南","contact":"13800000000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("新昵称"));
        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.region").value("湖南"));

        mockMvc.perform(get("/api/user/favorite/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult fb = mockMvc.perform(post("/api/user/feedback/create")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"希望增加更多作物\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long fbId = objectMapper.readTree(fb.getResponse().getContentAsString())
                .path("data").path("id").asLong();
        mockMvc.perform(get("/api/user/feedback/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value((int) fbId));

        mockMvc.perform(post("/api/admin/feedback/" + fbId + "/reply")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reply\":\"已收到，感谢反馈\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REPLIED"));

        // Banner / 公告 / 仪表盘
        mockMvc.perform(post("/api/admin/banner/save")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"首页横幅","imagePath":"/uploads/b.png","linkUrl":"/news","sortOrder":1,"status":"ONLINE"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/banner/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("首页横幅"));

        mockMvc.perform(post("/api/admin/notice/save")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"系统公告","content":"今晚维护","status":"ONLINE"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/notice/list").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("系统公告"));

        mockMvc.perform(get("/api/admin/dashboard/stats").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userTotal").isNumber())
                .andExpect(jsonPath("$.data.certPending").isNumber())
                .andExpect(jsonPath("$.data.todayDetection").isNumber());
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Pass@123","confirmPassword":"Pass@123","nickname":"内容用户"}
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
