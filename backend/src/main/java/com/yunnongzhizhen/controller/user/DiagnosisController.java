package com.yunnongzhizhen.controller.user;

import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.service.DiagnosisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     */
    @PostMapping("/chat")
    public ApiResult<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request) {
        return ApiResult.ok(diagnosisService.chat(request.getDetectionRecordId(), request.getSessionId(), request.getQuestion()));
    }

    @Data
    public static class ChatRequest {
        @NotNull
        private Long detectionRecordId;
        private String sessionId;
        @NotBlank
        private String question;
    }
}
