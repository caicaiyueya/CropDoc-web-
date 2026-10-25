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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型调用（OpenAI 兼容）。优先智谱 GLM-5，可回退 DeepSeek 配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;

    @Value("${zhipu.api-key:}")
    private String zhipuApiKey;

    @Value("${zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String zhipuBaseUrl;

    @Value("${zhipu.vision-model:glm-5}")
    private String zhipuVisionModel;

    @Value("${zhipu.chat-model:glm-5}")
    private String zhipuChatModel;

    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${deepseek.vision-model:deepseek-chat}")
    private String deepseekVisionModel;

    @Value("${deepseek.chat-model:deepseek-chat}")
    private String deepseekChatModel;

    /**
     * 视觉识别：根据图片给出病害名与描述。
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
            body.put("model", resolveVisionModel());
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
     * 诊断对话：基于识别结果或多轮历史问答。
     */
    public String chatDiagnose(List<Map<String, String>> history, String question, String context) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", resolveChatModel());
            ArrayNode messages = body.putArray("messages");
            String ctx = StringUtils.hasText(context) ? context : "无特定识别结果，按通用农技问答回答";
            messages.addObject().put("role", "system")
                    .put("content", "你是农作物病虫害诊断助手「云农智诊」。结合识别上下文回答农户问题，"
                            + "给出可操作的发病原因分析与防治建议，用语简洁、分点清晰。"
                            + "识别上下文：" + ctx);
            if (history != null) {
                for (Map<String, String> m : history) {
                    messages.addObject().put("role", m.getOrDefault("role", "user"))
                            .put("content", m.getOrDefault("content", ""));
                }
            }
            messages.addObject().put("role", "user").put("content", question);
            return chatCompletion(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI 诊断失败: {}", e.getMessage());
            throw new BusinessException("智能诊断暂时不可用，请稍后重试");
        }
    }

    private boolean useZhipu() {
        return StringUtils.hasText(zhipuApiKey) && !zhipuApiKey.contains("your_");
    }

    private String resolveApiKey() {
        if (useZhipu()) {
            return zhipuApiKey.trim();
        }
        if (StringUtils.hasText(deepseekApiKey) && !deepseekApiKey.contains("your_deepseek")) {
            return deepseekApiKey.trim();
        }
        throw new BusinessException("未配置 ZHIPU_API_KEY（或 DEEPSEEK_API_KEY），无法调用 AI");
    }

    private String resolveBaseUrl() {
        return useZhipu() ? zhipuBaseUrl : deepseekBaseUrl;
    }

    private String resolveChatModel() {
        return useZhipu() ? zhipuChatModel : deepseekChatModel;
    }

    private String resolveVisionModel() {
        return useZhipu() ? zhipuVisionModel : deepseekVisionModel;
    }

    /**
     * 智谱：{base}/chat/completions；DeepSeek 等：{base}/v1/chat/completions
     */
    private String resolveCompletionsUrl() {
        String base = resolveBaseUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.contains("/paas/v4") || base.endsWith("/v4")) {
            return base + "/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    private String chatCompletion(ObjectNode body) {
        // GLM-5 默认可能走思维链，content 为空；关闭后直接返回正文
        if (useZhipu() && !body.has("thinking")) {
            body.putObject("thinking").put("type", "disabled");
        }
        String apiKey = resolveApiKey();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        String url = resolveCompletionsUrl();
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    url, new HttpEntity<>(body.toString(), headers), String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode err = root.path("error").path("message");
            if (!err.isMissingNode() && !err.asText("").isBlank()) {
                throw new BusinessException("AI 调用失败：" + err.asText());
            }
            JsonNode message = root.path("choices").path(0).path("message");
            String text = extractAssistantText(message);
            if (!StringUtils.hasText(text) || "null".equalsIgnoreCase(text.trim())) {
                throw new BusinessException("AI 返回为空");
            }
            return text.trim();
        } catch (RestClientException e) {
            log.warn("AI HTTP 失败: {}", e.getMessage());
            throw new BusinessException("AI 服务超时或不可用，请稍后重试");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI 解析失败: {}", e.getMessage());
            throw new BusinessException("AI 调用失败");
        }
    }

    /**
     * 解析助手正文：兼容 content 为 null / 字符串 / 多段数组，以及 reasoning_content 回退。
     */
    private String extractAssistantText(JsonNode message) {
        if (message == null || message.isMissingNode() || message.isNull()) {
            return null;
        }
        String content = nodeToPlainText(message.get("content"));
        if (StringUtils.hasText(content) && !"null".equalsIgnoreCase(content.trim())) {
            return content;
        }
        String reasoning = nodeToPlainText(message.get("reasoning_content"));
        if (StringUtils.hasText(reasoning) && !"null".equalsIgnoreCase(reasoning.trim())) {
            return reasoning;
        }
        return null;
    }

    private String nodeToPlainText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode part : node) {
                if (part == null || part.isNull()) {
                    continue;
                }
                if (part.isTextual()) {
                    sb.append(part.asText());
                } else if (part.has("text")) {
                    sb.append(part.path("text").asText(""));
                } else if ("text".equals(part.path("type").asText()) && part.has("text")) {
                    sb.append(part.path("text").asText(""));
                }
            }
            String s = sb.toString().trim();
            return s.isEmpty() ? null : s;
        }
        if (node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        return null;
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
