package com.yunnongzhizhen.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.dto.cert.ReviewCertificationRequest;
import com.yunnongzhizhen.entity.AdminOperationLog;
import com.yunnongzhizhen.entity.CertificationApplication;
import com.yunnongzhizhen.entity.enums.CertStatus;
import com.yunnongzhizhen.service.CertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员认证审核 /api/admin/certification。
 */
@RestController
@RequestMapping("/api/admin/certification")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCertificationController {

    private final CertificationService certificationService;

    /**
     * 申请列表。
     */
    @GetMapping("/list")
    public ApiResult<Page<CertificationApplication>> list(
            @RequestParam(required = false) CertStatus status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(certificationService.listCertifications(status, page, size));
    }

    /**
     * 通过。
     */
    @PostMapping("/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id, @RequestBody(required = false) ReviewCertificationRequest request) {
        certificationService.approveCertification(id, request != null ? request : new ReviewCertificationRequest());
        return ApiResult.okMessage("已通过");
    }

    /**
     * 驳回。
     */
    @PostMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id, @RequestBody(required = false) ReviewCertificationRequest request) {
        certificationService.rejectCertification(id, request != null ? request : new ReviewCertificationRequest());
        return ApiResult.okMessage("已驳回");
    }

    /**
     * 认证审核日志。
     */
    @GetMapping("/logs")
    public ApiResult<List<AdminOperationLog>> logs() {
        return ApiResult.ok(certificationService.certificationLogs());
    }
}
