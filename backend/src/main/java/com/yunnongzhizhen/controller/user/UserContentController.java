package com.yunnongzhizhen.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.dto.user.UserInfoVO;
import com.yunnongzhizhen.entity.Banner;
import com.yunnongzhizhen.entity.Crop;
import com.yunnongzhizhen.entity.Disease;
import com.yunnongzhizhen.entity.Feedback;
import com.yunnongzhizhen.entity.News;
import com.yunnongzhizhen.entity.Notice;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.service.ContentManageService;
import com.yunnongzhizhen.service.CropService;
import com.yunnongzhizhen.service.DiseaseService;
import com.yunnongzhizhen.service.FavoriteService;
import com.yunnongzhizhen.service.FeedbackService;
import com.yunnongzhizhen.service.NewsService;
import com.yunnongzhizhen.service.ProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端资讯/病害/作物/资料/收藏/反馈 /api/user。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserContentController {

    private final NewsService newsService;
    private final DiseaseService diseaseService;
    private final CropService cropService;
    private final ProfileService profileService;
    private final FavoriteService favoriteService;
    private final FeedbackService feedbackService;
    private final ContentManageService contentManageService;

    @GetMapping("/news/list")
    public ApiResult<Page<News>> newsList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(newsService.listOnline(page, size));
    }

    @GetMapping("/news/{id}")
    public ApiResult<News> newsDetail(@PathVariable Long id) {
        return ApiResult.ok(newsService.getById(id));
    }

    @GetMapping("/disease/list")
    public ApiResult<Page<Disease>> diseaseList(
            @RequestParam(required = false) Long cropId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(diseaseService.listByRole(cropId, page, size));
    }

    @GetMapping("/disease/{id}")
    public ApiResult<Disease> diseaseDetail(@PathVariable Long id) {
        return ApiResult.ok(diseaseService.getById(id));
    }

    @GetMapping("/crop/list")
    public ApiResult<List<Crop>> cropList() {
        return ApiResult.ok(cropService.listAll());
    }

    @GetMapping("/profile")
    public ApiResult<UserInfoVO> profile() {
        return ApiResult.ok(profileService.getProfile());
    }

    @PutMapping("/profile")
    public ApiResult<UserInfoVO> updateProfile(@RequestBody ProfileRequest request) {
        return ApiResult.ok(profileService.updateProfile(
                request.getNickname(), request.getAvatar(), request.getRegion(), request.getContact()));
    }

    @GetMapping("/favorite/list")
    public ApiResult<List<Post>> favoriteList() {
        return ApiResult.ok(favoriteService.myFavorites());
    }

    @PostMapping("/feedback/create")
    public ApiResult<Feedback> createFeedback(@RequestBody FeedbackRequest request) {
        return ApiResult.ok(feedbackService.submit(request.getContent()));
    }

    @GetMapping("/feedback/list")
    public ApiResult<Page<Feedback>> feedbackList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(feedbackService.myList(page, size));
    }

    @GetMapping("/banner/list")
    public ApiResult<List<Banner>> banners() {
        return ApiResult.ok(contentManageService.listOnlineBanners());
    }

    @GetMapping("/notice/list")
    public ApiResult<List<Notice>> notices() {
        return ApiResult.ok(contentManageService.listOnlineNotices());
    }

    @Data
    public static class ProfileRequest {
        private String nickname;
        private String avatar;
        private String region;
        private String contact;
    }

    @Data
    public static class FeedbackRequest {
        private String content;
    }
}
