package com.yunnongzhizhen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yunnongzhizhen.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek（OpenAI 兼容）封装；API Key 仅后端环境变量，绝不返回前端。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.vision-model}")
    private String visionModel;

    @Value("${deepseek.chat-model}")
    private String chatModel;

    /**
     * 视觉识别：根据图片给出病害名与描述。
     *
     * @param imageRelativePath 图片相对路径
     * @param cropHint          作物提示（可空）
     * @return result/confidence/description
     */
    public Map<String, Object> detectByImage(String imageRelativePath, String cropHint) {
        try {
            Path abs = fileStorageService.resolveAbsolute(imageRelativePath);
            if (!Files.exists(abs)) {
                throw new BusinessException("图片文件不存在");
            }
            byte[] bytes = Files.readAllBytes(abs);
            String b64 = Base64.getEncoder().encodeToString(bytes);
            String mime = guessMime(imageRelativePath);
            String prompt = "你是农作物病虫害识别助手。请根据图片判断可能的病虫害名称，"
                    + "并给出简要描述与防治建议。作物提示：" + (cropHint != null ? cropHint : "未知")
                    + "。请用JSON返回：{\"result\":\"病害名\",\"confidence\":0.0到1.0,\"description\":\"说明\"}";

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", visionModel);
            ArrayNode messages = body.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            ArrayNode content = userMsg.putArray("content");
            content.addObject().put("type", "text").put("text", prompt);
            ObjectNode imagePart = content.addObject();
            imagePart.put("type", "image_url");
            imagePart.putObject("image_url").put("url", "data:" + mime + ";base64," + b64);

            String reply = chatCompletion(body);
            return parseDetectReply(reply);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI 识别失败: {}", e.getMessage());
            throw new BusinessException("病虫害识别暂时不可用，请稍后重试");
        }
    }

    /**
     * 诊断对话：基于识别结果多轮问答。
     *
     * @param history 历史消息 [{role, content}]
     * @param question 用户问题
     * @param context  识别上下文
     * @return 助手回复文本
     */
    public String chatDiagnose(List<Map<String, String>> history, String question, String context) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", chatModel);
            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system")
                    .put("content", "你是农作物病虫害诊断助手。结合识别上下文回答农户问题，给出可操作的防治建议。"
                            + "识别上下文：" + (context != null ? context : "无"));
            if (history != null) {
                for (Map<String, String> m : history) {
                    messages.addObject().put("role", m.getOrDefault("role", "user"))
                            .put("content", m.getOrDefault("content", ""));
                }
            }
            messages.addObject().put("role", "user").put("content", question);
            return chatCompletion(body);
        } catch (Exception e) {
            log.warn("AI 诊断失败: {}", e.getMessage());
            throw new BusinessException("智能诊断暂时不可用，请稍后重试");
        }
    }

    private String chatCompletion(ObjectNode body) {
        if (!org.springframework.util.StringUtils.hasText(apiKey)
                || apiKey.contains("your_deepseek")) {
            throw new BusinessException("未配置 DEEPSEEK_API_KEY，无法调用 AI");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        String url = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, new HttpEntity<>(body.toString(), headers), String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new BusinessException("AI 返回为空");
            }
            return content.asText();
        } catch (RestClientException e) {
            throw new BusinessException("AI 服务超时或不可用，请稍后重试");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("AI 调用失败");
        }
    }

    private Map<String, Object> parseDetectReply(String reply) {
        Map<String, Object> map = new HashMap<>();
        try {
            String json = reply;
            int start = reply.indexOf('{');
            int end = reply.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = reply.substring(start, end + 1);
            }
            JsonNode node = objectMapper.readTree(json);
            map.put("result", node.path("result").asText("未知病害"));
            map.put("confidence", node.path("confidence").asDouble(0.5));
            map.put("description", node.path("description").asText(reply));
        } catch (Exception e) {
            map.put("result", "识别结果");
            map.put("confidence", 0.5);
            map.put("description", reply);
        }
        return map;
    }

    private String guessMime(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
