package com.yunnongzhizhen.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Comment;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.enums.PostTag;
import com.yunnongzhizhen.entity.enums.ReportTargetType;
import com.yunnongzhizhen.service.FavoriteService;
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

import java.util.Map;

/**
 * 用户端社区帖子 /api/user/post、评论、收藏、举报。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FavoriteService favoriteService;
    private final ReportService reportService;

    /**
     * 帖子列表：仅 APPROVED；sort=time|hot。
     */
    @GetMapping("/post/list")
    public ApiResult<Page<Post>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) PostTag tag,
            @RequestParam(defaultValue = "time") String sort) {
        return ApiResult.ok(postService.listPosts(page, size, tag, sort));
    }

    /**
     * 发帖：普通帖 PENDING（FARMER/EXPERT）。GUIDE 须调用 create-guide，避免同类自调用绕过鉴权。
     */
    @PostMapping("/post/create")
    @PreAuthorize("hasAnyRole('FARMER','EXPERT','ADMIN')")
    public ApiResult<Post> create(@RequestBody CreatePostRequest request) {
        if (request.getTag() == PostTag.GUIDE) {
            throw new BusinessException("发布农技指导请使用 POST /api/user/post/create-guide");
        }
        return ApiResult.ok(postService.createPost(request.getTitle(), request.getContent(),
                request.getImages(), PostTag.NORMAL));
    }

    /**
     * 扩展用例：认证农技员发布「农技指导」标签帖。
     */
    @PostMapping("/post/create-guide")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    public ApiResult<Post> createGuide(@RequestBody CreatePostRequest request) {
        return ApiResult.ok(postService.createPost(request.getTitle(), request.getContent(),
                request.getImages(), PostTag.GUIDE));
    }

    @GetMapping("/post/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.ok(postService.getPost(id));
    }

    /**
     * 发表评论；EXPERT 评论在 Service 中标记 isProfessional=true。
     */
    @PostMapping("/comment/create")
    public ApiResult<Comment> createComment(@RequestBody CreateCommentRequest request) {
        return ApiResult.ok(postService.createComment(request.getPostId(), request.getContent(), request.getParentId()));
    }

    /**
     * 扩展用例：标记优质回答（仅 EXPERT/ADMIN）。
     */
    @PostMapping("/comment/{id}/mark-quality")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    public ApiResult<Comment> markQuality(@PathVariable Long id) {
        return ApiResult.ok(postService.markQuality(id));
    }

    @PostMapping("/favorite/toggle")
    public ApiResult<Map<String, Object>> toggleFavorite(@RequestBody FavoriteRequest request) {
        return ApiResult.ok(favoriteService.toggleFavorite(request.getPostId()));
    }

    @PostMapping("/report/create")
    public ApiResult<com.yunnongzhizhen.entity.Report> createReport(@RequestBody ReportRequest request) {
        return ApiResult.ok(reportService.createReport(request.getTargetType(), request.getTargetId(), request.getReason()));
    }

    @Data
    public static class CreatePostRequest {
        private String title;
        private String content;
        private String images;
        private PostTag tag;
    }

    @Data
    public static class CreateCommentRequest {
        private Long postId;
        private String content;
        private Long parentId;
    }

    @Data
    public static class FavoriteRequest {
        private Long postId;
    }

    @Data
    public static class ReportRequest {
        private ReportTargetType targetType;
        private Long targetId;
        private String reason;
    }
}
