package com.yunnongzhizhen.service;

import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 帖子状态流转集中于此（Controller 不写业务 if-else）。
 * 状态图：PENDING→APPROVED/REJECTED；APPROVED→OFFLINE。
 */
@Service
@RequiredArgsConstructor
public class PostStatusService {

    private final PostMapper postMapper;

    /**
     * 用例：审核通过帖子。顺序图消息：approve
     *
     * @param postId 帖子ID
     */
    public void approve(Long postId) {
        Post post = require(postId);
        if (post.getStatus() != PostStatus.PENDING) {
            throw new BusinessException("仅待审核帖子可通过");
        }
        post.setStatus(PostStatus.APPROVED);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    /**
     * 审核驳回。
     *
     * @param postId 帖子ID
     */
    public void reject(Long postId) {
        Post post = require(postId);
        if (post.getStatus() != PostStatus.PENDING) {
            throw new BusinessException("仅待审核帖子可驳回");
        }
        post.setStatus(PostStatus.REJECTED);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    /**
     * 下架。
     *
     * @param postId 帖子ID
     */
    public void offline(Long postId) {
        Post post = require(postId);
        if (post.getStatus() != PostStatus.APPROVED) {
            throw new BusinessException("仅已通过帖子可下架");
        }
        post.setStatus(PostStatus.OFFLINE);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    private Post require(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return post;
    }
}
