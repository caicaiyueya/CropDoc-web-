package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.DetectionRecord;
import com.yunnongzhizhen.entity.DiagnosisSession;
import com.yunnongzhizhen.mapper.DetectionRecordMapper;
import com.yunnongzhizhen.mapper.DiagnosisSessionMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 智能诊断业务（与识别分离）。
 */
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisSessionMapper diagnosisSessionMapper;
    private final DetectionRecordMapper detectionRecordMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    /**
     * 用例：智能诊断对话。
     *
     * @param detectionRecordId 识别记录ID（可空）
     * @param sessionId         会话ID（可空，空则新建）
     * @param question          用户问题
     * @param contextHint       无识别记录时的上下文补充
     * @param clientHistory     前端带来的多轮历史（自由问答优先）
     */
    public Map<String, Object> chat(Long detectionRecordId, String sessionId, String question,
                                    String contextHint, List<Map<String, String>> clientHistory) {
        if (!StringUtils.hasText(question)) {
            throw new BusinessException("问题不能为空");
        }
        Long userId = SecurityUtils.requireUserId();

        String context;
        if (detectionRecordId != null) {
            DetectionRecord record = detectionRecordMapper.selectById(detectionRecordId);
            if (record == null || !record.getUserId().equals(userId)) {
                throw new BusinessException("识别记录不存在");
            }
            context = "识别结果=" + record.getResult() + "; 描述=" + record.getDescription();
        } else if (StringUtils.hasText(contextHint)) {
            context = contextHint.trim();
        } else {
            context = "自由农技问答，无特定识别记录";
        }

        DiagnosisSession session = null;
        List<Map<String, String>> history = new ArrayList<>();
        if (StringUtils.hasText(sessionId)) {
            session = diagnosisSessionMapper.selectOne(new LambdaQueryWrapper<DiagnosisSession>()
                    .eq(DiagnosisSession::getSessionId, sessionId)
                    .eq(DiagnosisSession::getUserId, userId)
                    .last("LIMIT 1"));
            if (session != null && StringUtils.hasText(session.getSessionContent())) {
                try {
                    history = objectMapper.readValue(session.getSessionContent(), new TypeReference<>() {
                    });
                } catch (Exception ignored) {
                    history = new ArrayList<>();
                }
            }
        }
        // 库中无历史时，使用前端传入的多轮上下文（自由问答）
        if (history.isEmpty() && clientHistory != null && !clientHistory.isEmpty()) {
            for (Map<String, String> m : clientHistory) {
                if (m == null) continue;
                String role = m.getOrDefault("role", "user");
                String content = m.getOrDefault("content", "");
                if (!StringUtils.hasText(content)) continue;
                if (!"user".equals(role) && !"assistant".equals(role) && !"system".equals(role)) {
                    role = "user";
                }
                history.add(Map.of("role", role, "content", content));
            }
        }
        String answer = aiService.chatDiagnose(history, question, context);
        history.add(Map.of("role", "user", "content", question));
        history.add(Map.of("role", "assistant", "content", answer));

        String sid = StringUtils.hasText(sessionId)
                ? sessionId
                : (session != null ? session.getSessionId() : UUID.randomUUID().toString().replace("-", ""));

        // 无识别记录时不落库（diagnosis_session.detection_record_id 非空约束）
        if (detectionRecordId != null) {
            LocalDateTime now = LocalDateTime.now();
            try {
                String contentJson = objectMapper.writeValueAsString(history);
                if (session == null) {
                    session = new DiagnosisSession();
                    session.setDetectionRecordId(detectionRecordId);
                    session.setUserId(userId);
                    session.setSessionId(sid);
                    session.setSessionContent(contentJson);
                    session.setCreateTime(now);
                    session.setUpdateTime(now);
                    diagnosisSessionMapper.insert(session);
                } else {
                    session.setSessionContent(contentJson);
                    session.setUpdateTime(now);
                    diagnosisSessionMapper.updateById(session);
                }
                sid = session.getSessionId();
            } catch (Exception e) {
                throw new BusinessException("会话保存失败");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sid);
        result.put("answer", answer);
        result.put("session", session);
        return result;
    }

    /** 兼容旧调用 */
    public Map<String, Object> chat(Long detectionRecordId, String sessionId, String question) {
        return chat(detectionRecordId, sessionId, question, null, null);
    }

    public Map<String, Object> chat(Long detectionRecordId, String sessionId, String question, String contextHint) {
        return chat(detectionRecordId, sessionId, question, contextHint, null);
    }
}
