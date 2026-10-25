package com.yunnongzhizhen.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.Report;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.entity.enums.ReportStatus;
import com.yunnongzhizhen.service.PostService;
import com.yunnongzhizhen.service.ReportService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员社区审核 /api/admin。
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;
    private final ReportService reportService;

    @GetMapping("/post/pending")
    public ApiResult<Page<Post>> pending(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(postService.pendingPosts(page, size));
    }

    /** 管理员帖子列表：status 为空返回全部（与用户端社区同源数据） */
    @GetMapping("/post/list")
    public ApiResult<Page<Post>> list(
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long size) {
        return ApiResult.ok(postService.adminListPosts(status, page, size));
    }

    @PostMapping("/post/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id) {
        postService.approvePost(id);
        return ApiResult.okMessage("已通过");
    }

    @PostMapping("/post/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id) {
        postService.rejectPost(id);
        return ApiResult.okMessage("已驳回");
    }

    @PostMapping("/post/{id}/offline")
    public ApiResult<Void> offline(@PathVariable Long id) {
        postService.offlinePost(id);
        return ApiResult.okMessage("已下架");
    }

    @GetMapping("/report/list")
    public ApiResult<Page<Report>> reports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(reportService.listReports(status, page, size));
    }

    /**
     * 社区治理：警告用户（禁用统一走 AdminManageController /user/{id}/disable）。
     */
    @PostMapping("/user/{id}/warn")
    public ApiResult<Void> warn(@PathVariable Long id, @RequestBody(required = false) ReasonRequest request) {
        reportService.warnUser(id, request != null ? request.getReason() : "警告");
        return ApiResult.okMessage("已警告");
    }

    @Data
    public static class ReasonRequest {
        private String reason;
    }
}
