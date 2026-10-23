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
     * 用例：智能诊断对话。顺序图：加载识别记录→AiService.chatDiagnose→持久化会话
     *
     * @param detectionRecordId 识别记录ID
     * @param sessionId         会话ID（可空，空则新建）
     * @param question          用户问题
     * @return 会话与回复
     */
    public Map<String, Object> chat(Long detectionRecordId, String sessionId, String question) {
        if (!StringUtils.hasText(question)) {
            throw new BusinessException("问题不能为空");
        }
        Long userId = SecurityUtils.requireUserId();
        DetectionRecord record = detectionRecordMapper.selectById(detectionRecordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("识别记录不存在");
        }
        String context = "识别结果=" + record.getResult() + "; 描述=" + record.getDescription();
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
        String answer = aiService.chatDiagnose(history, question, context);
        history.add(Map.of("role", "user", "content", question));
        history.add(Map.of("role", "assistant", "content", answer));

        LocalDateTime now = LocalDateTime.now();
        try {
            String contentJson = objectMapper.writeValueAsString(history);
            if (session == null) {
                session = new DiagnosisSession();
                session.setDetectionRecordId(detectionRecordId);
                session.setUserId(userId);
                session.setSessionId(StringUtils.hasText(sessionId) ? sessionId : UUID.randomUUID().toString().replace("-", ""));
                session.setSessionContent(contentJson);
                session.setCreateTime(now);
                session.setUpdateTime(now);
                diagnosisSessionMapper.insert(session);
            } else {
                session.setSessionContent(contentJson);
                session.setUpdateTime(now);
                diagnosisSessionMapper.updateById(session);
            }
        } catch (Exception e) {
            throw new BusinessException("会话保存失败");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("answer", answer);
        result.put("session", session);
        return result;
    }
}
