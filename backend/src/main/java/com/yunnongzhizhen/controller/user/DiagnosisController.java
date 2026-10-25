package com.yunnongzhizhen.controller.user;

import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.service.DiagnosisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 诊断接口 /api/user/diagnosis。
 */
@RestController
@RequestMapping("/api/user/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    /**
     * 用例：智能诊断对话（与识别用例独立）。
     * detectionRecordId 可空：图床直连识别场景可只传 contextHint。
     */
    @PostMapping("/chat")
    public ApiResult<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request) {
        return ApiResult.ok(diagnosisService.chat(
                request.getDetectionRecordId(),
                request.getSessionId(),
                request.getQuestion(),
                request.getContextHint(),
                request.getHistory()
        ));
    }

    @Data
    public static class ChatRequest {
        /** 可空：无后端识别记录时依赖 contextHint */
        private Long detectionRecordId;
        private String sessionId;
        @NotBlank
        private String question;
        /** 可选上下文，如「病害=玉米普通锈病;置信度=0.98;作物=Corn」 */
        private String contextHint;
        /** 前端会话历史（自由问答未落库时使用）[{role,content}] */
        private java.util.List<java.util.Map<String, String>> history;
    }
}
