package com.yunnongzhizhen.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.dto.user.UserInfoVO;
import com.yunnongzhizhen.entity.Banner;
import com.yunnongzhizhen.entity.Crop;
import com.yunnongzhizhen.entity.Disease;
import com.yunnongzhizhen.entity.Feedback;
import com.yunnongzhizhen.entity.News;
import com.yunnongzhizhen.entity.Notice;
import com.yunnongzhizhen.entity.enums.FeedbackStatus;
import com.yunnongzhizhen.entity.enums.PublishStatus;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.service.AdminUserService;
import com.yunnongzhizhen.service.ContentManageService;
import com.yunnongzhizhen.service.CropService;
import com.yunnongzhizhen.service.DashboardService;
import com.yunnongzhizhen.service.DiseaseService;
import com.yunnongzhizhen.service.FeedbackService;
import com.yunnongzhizhen.service.NewsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理员：用户/Banner/公告/反馈/仪表盘/资讯。
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminManageController {

    private final AdminUserService adminUserService;
    private final DashboardService dashboardService;
    private final ContentManageService contentManageService;
    private final FeedbackService feedbackService;
    private final NewsService newsService;
    private final DiseaseService diseaseService;
    private final CropService cropService;

    @GetMapping("/dashboard/stats")
    public ApiResult<Map<String, Object>> stats() {
        return ApiResult.ok(dashboardService.stats());
    }

    @GetMapping("/user/list")
    public ApiResult<Page<UserInfoVO>> userList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(adminUserService.listUsers(keyword, role, status, page, size));
    }

    @PostMapping("/user/{id}/reset-password")
    public ApiResult<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPwdRequest request) {
        adminUserService.resetPassword(id, request != null ? request.getNewPassword() : null);
        return ApiResult.okMessage("密码已重置");
    }

    @PostMapping("/user/{id}/role")
    public ApiResult<Void> changeRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        adminUserService.changeRole(id, request.getRole());
        return ApiResult.okMessage("角色已更新");
    }

    @PostMapping("/user/{id}/enable")
    public ApiResult<Void> enable(@PathVariable Long id) {
        adminUserService.enable(id);
        return ApiResult.okMessage("已启用");
    }

    /**
     * 用例：禁用用户（与社区治理 disable 共用路径约定 /api/admin/user/{id}/disable）。
     */
    @PostMapping("/user/{id}/disable")
    public ApiResult<Void> disableUser(@PathVariable Long id) {
        adminUserService.disable(id);
        return ApiResult.okMessage("已禁用");
    }

    @GetMapping("/banner/list")
    public ApiResult<List<Banner>> banners() {
        return ApiResult.ok(contentManageService.listAllBanners());
    }

    @PostMapping("/banner/save")
    public ApiResult<Banner> saveBanner(@RequestBody Banner banner) {
        return ApiResult.ok(contentManageService.saveBanner(banner));
    }

    @PostMapping("/banner/{id}/delete")
    public ApiResult<Void> deleteBanner(@PathVariable Long id) {
        contentManageService.deleteBanner(id);
        return ApiResult.okMessage("已删除");
    }

    @GetMapping("/notice/list")
    public ApiResult<List<Notice>> notices() {
        return ApiResult.ok(contentManageService.listAllNotices());
    }

    @PostMapping("/notice/save")
    public ApiResult<Notice> saveNotice(@RequestBody Notice notice) {
        return ApiResult.ok(contentManageService.saveNotice(notice));
    }

    @GetMapping("/feedback/list")
    public ApiResult<Page<Feedback>> feedbackList(
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(feedbackService.adminList(status, page, size));
    }

    @PostMapping("/feedback/{id}/reply")
    public ApiResult<Feedback> replyFeedback(@PathVariable Long id, @RequestBody ReplyRequest request) {
        return ApiResult.ok(feedbackService.reply(id, request.getReply()));
    }

    @GetMapping("/news/list")
    public ApiResult<Page<News>> newsList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(newsService.adminList(page, size));
    }

    @PostMapping("/news/save")
    public ApiResult<News> saveNews(@RequestBody News news) {
        return ApiResult.ok(newsService.saveOrUpdate(news));
    }

    @PostMapping("/news/{id}/delete")
    public ApiResult<Void> deleteNews(@PathVariable Long id) {
        newsService.delete(id);
        return ApiResult.okMessage("已删除");
    }

    @PostMapping("/news/{id}/online")
    public ApiResult<News> newsOnline(@PathVariable Long id) {
        return ApiResult.ok(newsService.setStatus(id, PublishStatus.ONLINE));
    }

    @PostMapping("/news/{id}/offline")
    public ApiResult<News> newsOffline(@PathVariable Long id) {
        return ApiResult.ok(newsService.setStatus(id, PublishStatus.OFFLINE));
    }

    @PostMapping("/crop/save")
    public ApiResult<Crop> saveCrop(@RequestBody Crop crop) {
        return ApiResult.ok(cropService.save(crop));
    }

    @GetMapping("/disease/list")
    public ApiResult<Page<Disease>> diseaseList(
            @RequestParam(required = false) Long cropId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(diseaseService.adminList(cropId, page, size));
    }

    @PostMapping("/disease/save")
    public ApiResult<Disease> saveDisease(@RequestBody Disease disease) {
        return ApiResult.ok(diseaseService.save(disease));
    }

    @PostMapping("/disease/{id}/delete")
    public ApiResult<Void> deleteDisease(@PathVariable Long id) {
        diseaseService.delete(id);
        return ApiResult.okMessage("已删除");
    }

    @Data
    public static class ResetPwdRequest {
        private String newPassword;
    }

    @Data
    public static class RoleRequest {
        private UserRole role;
    }

    @Data
    public static class ReplyRequest {
        private String reply;
    }
}
