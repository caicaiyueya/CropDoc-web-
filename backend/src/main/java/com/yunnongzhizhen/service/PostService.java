package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Comment;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.entity.enums.PostTag;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.mapper.CommentMapper;
import com.yunnongzhizhen.mapper.PostMapper;
import com.yunnongzhizhen.mapper.UserMapper;
import com.yunnongzhizhen.security.LoginUser;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社区帖子与评论业务。
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostStatusService postStatusService;

    /**
     * 用例：创建帖子 createPost。顺序图：校验 GUIDE 标签权限→insert PENDING
     *
     * @param title   标题
     * @param content 内容
     * @param images  图片
     * @param tag     标签
     * @return 帖子
     */
    public Post createPost(String title, String content, String images, PostTag tag) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new BusinessException("标题和内容不能为空");
        }
        LoginUser user = SecurityUtils.requireLoginUser();
        PostTag realTag = tag != null ? tag : PostTag.NORMAL;
        if (realTag == PostTag.GUIDE && user.getRole() == UserRole.FARMER) {
            throw new BusinessException("仅认证农技员可发布农技指导");
        }
        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setImages(images);
        post.setTag(realTag);
        post.setStatus(PostStatus.PENDING);
        post.setPinned(false);
        post.setViewCount(0);
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        postMapper.insert(post);
        fillPostAuthor(post, userMap(Set.of(user.getId())));
        return post;
    }

    /**
     * 用例：帖子列表（仅已通过）。支持时间/热度排序。
     *
     * @param sort time=时间倒序（默认），hot=浏览量倒序
     */
    public Page<Post> listPosts(long page, long size, PostTag tag, String sort) {
        LambdaQueryWrapper<Post> qw = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, PostStatus.APPROVED)
                .orderByDesc(Post::getPinned);
        if ("hot".equalsIgnoreCase(sort)) {
            qw.orderByDesc(Post::getViewCount).orderByDesc(Post::getCreateTime);
        } else {
            qw.orderByDesc(Post::getCreateTime);
        }
        if (tag != null) {
            qw.eq(Post::getTag, tag);
        }
        Page<Post> result = postMapper.selectPage(new Page<>(page, size), qw);
        fillPostsAuthors(result.getRecords());
        return result;
    }

    /**
     * 用例：帖子详情。
     */
    public Map<String, Object> getPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        Long userId = SecurityUtils.requireUserId();
        UserRole role = SecurityUtils.requireRole();
        boolean canView = post.getStatus() == PostStatus.APPROVED
                || post.getAuthorId().equals(userId)
                || role == UserRole.ADMIN;
        if (!canView) {
            throw new BusinessException("帖子不可见");
        }
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        postMapper.updateById(post);
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, id)
                .orderByDesc(Comment::getIsQuality)
                .orderByDesc(Comment::getCreateTime));
        Set<Long> authorIds = new HashSet<>();
        if (post.getAuthorId() != null) {
            authorIds.add(post.getAuthorId());
        }
        comments.stream().map(Comment::getAuthorId).filter(Objects::nonNull).forEach(authorIds::add);
        Map<Long, User> users = userMap(authorIds);
        fillPostAuthor(post, users);
        comments.forEach(c -> fillCommentAuthor(c, users));
        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        map.put("comments", comments);
        return map;
    }

    /**
     * 用例：发表评论。
     */
    public Comment createComment(Long postId, String content, Long parentId) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("评论内容不能为空");
        }
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != PostStatus.APPROVED) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        LoginUser user = SecurityUtils.requireLoginUser();
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(user.getId());
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setIsProfessional(user.getRole() == UserRole.EXPERT || user.getRole() == UserRole.ADMIN);
        comment.setIsQuality(false);
        LocalDateTime now = LocalDateTime.now();
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        commentMapper.insert(comment);
        fillCommentAuthor(comment, userMap(Set.of(user.getId())));
        return comment;
    }

    /**
     * 用例：标记优质回答 markQuality（EXPERT/ADMIN）。
     */
    public Comment markQuality(Long commentId) {
        LoginUser user = SecurityUtils.requireLoginUser();
        if (user.getRole() != UserRole.EXPERT && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅农技员或管理员可标记优质");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        comment.setIsQuality(true);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
        fillCommentAuthor(comment, userMap(Set.of(comment.getAuthorId())));
        return comment;
    }

    /**
     * 管理员：待审核帖子。
     */
    public Page<Post> pendingPosts(long page, long size) {
        Page<Post> result = postMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, PostStatus.PENDING)
                        .orderByAsc(Post::getCreateTime));
        fillPostsAuthors(result.getRecords());
        return result;
    }

    public void approvePost(Long id) {
        postStatusService.approve(id);
    }

    public void rejectPost(Long id) {
        postStatusService.reject(id);
    }

    public void offlinePost(Long id) {
        postStatusService.offline(id);
    }

    private void fillPostsAuthors(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        Set<Long> ids = posts.stream().map(Post::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> users = userMap(ids);
        posts.forEach(p -> fillPostAuthor(p, users));
    }

    private Map<Long, User> userMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> list = userMapper.selectBatchIds(ids);
        return list.stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
    }

    private void fillPostAuthor(Post post, Map<Long, User> users) {
        if (post == null || post.getAuthorId() == null) {
            return;
        }
        User u = users.get(post.getAuthorId());
        if (u != null) {
            post.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            post.setAuthorRole(u.getRole() != null ? u.getRole().name() : null);
        }
    }

    private void fillCommentAuthor(Comment comment, Map<Long, User> users) {
        if (comment == null || comment.getAuthorId() == null) {
            return;
        }
        User u = users.get(comment.getAuthorId());
        if (u != null) {
            comment.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            comment.setAuthorRole(u.getRole() != null ? u.getRole().name() : null);
        }
    }
}
