package com.yunnongzhizhen.controller.user;

import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.dto.cert.SubmitCertificationRequest;
import com.yunnongzhizhen.entity.CertificationApplication;
import com.yunnongzhizhen.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户端认证申请接口 /api/user/certification。
 */
@RestController
@RequestMapping("/api/user/certification")
@RequiredArgsConstructor
public class UserCertificationController {

    private final CertificationService certificationService;

    /**
     * 提交认证申请（仅 FARMER；是否已有 PENDING 等业务判断在 Service）。
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('FARMER')")
    public ApiResult<CertificationApplication> submit(@Valid @RequestBody SubmitCertificationRequest request) {
        return ApiResult.ok(certificationService.submitCertification(request));
    }

    /**
     * 我的申请列表。
     */
    @GetMapping("/my")
    public ApiResult<List<CertificationApplication>> my() {
        return ApiResult.ok(certificationService.myCertifications());
    }

    /**
     * 当前认证状态。
     */
    @GetMapping("/status")
    public ApiResult<Map<String, Object>> status() {
        return ApiResult.ok(certificationService.certificationStatus());
    }

    /**
     * EXPERT 专属探测接口：审核通过后同一 token 下次请求应成功（role 从 DB 加载）。
     */
    @GetMapping("/expert-ping")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    public ApiResult<String> expertPing() {
        return ApiResult.ok("EXPERT_OK");
    }
}
