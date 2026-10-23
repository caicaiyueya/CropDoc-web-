package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Feedback;
import com.yunnongzhizhen.entity.enums.FeedbackStatus;
import com.yunnongzhizhen.mapper.FeedbackMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户反馈。
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public Feedback submit(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("反馈内容不能为空");
        }
        Feedback fb = new Feedback();
        fb.setUserId(SecurityUtils.requireUserId());
        fb.setContent(content);
        fb.setStatus(FeedbackStatus.OPEN);
        LocalDateTime now = LocalDateTime.now();
        fb.setCreateTime(now);
        fb.setUpdateTime(now);
        feedbackMapper.insert(fb);
        return fb;
    }

    public Page<Feedback> myList(long page, long size) {
        return feedbackMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getUserId, SecurityUtils.requireUserId())
                        .orderByDesc(Feedback::getCreateTime));
    }

    public Page<Feedback> adminList(FeedbackStatus status, long page, long size) {
        LambdaQueryWrapper<Feedback> qw = new LambdaQueryWrapper<Feedback>()
                .orderByDesc(Feedback::getCreateTime);
        if (status != null) {
            qw.eq(Feedback::getStatus, status);
        }
        return feedbackMapper.selectPage(new Page<>(page, size), qw);
    }

    public Feedback reply(Long id, String reply) {
        Feedback fb = feedbackMapper.selectById(id);
        if (fb == null) {
            throw new BusinessException("反馈不存在");
        }
        fb.setReply(reply);
        fb.setStatus(FeedbackStatus.REPLIED);
        fb.setUpdateTime(LocalDateTime.now());
        feedbackMapper.updateById(fb);
        return fb;
    }
}
