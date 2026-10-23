package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.News;
import com.yunnongzhizhen.entity.enums.PublishStatus;
import com.yunnongzhizhen.mapper.NewsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 资讯业务。
 */
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsMapper newsMapper;

    public Page<News> listOnline(long page, long size) {
        return newsMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<News>()
                        .eq(News::getStatus, PublishStatus.ONLINE)
                        .orderByDesc(News::getCreateTime));
    }

    public News getById(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null || news.getStatus() != PublishStatus.ONLINE) {
            throw new BusinessException("资讯不存在");
        }
        return news;
    }

    public News saveOrUpdate(News news) {
        LocalDateTime now = LocalDateTime.now();
        if (news.getId() == null) {
            if (!StringUtils.hasText(news.getTitle()) || !StringUtils.hasText(news.getContent())) {
                throw new BusinessException("标题和内容不能为空");
            }
            if (news.getStatus() == null) {
                news.setStatus(PublishStatus.ONLINE);
            }
            news.setCreateTime(now);
            news.setUpdateTime(now);
            newsMapper.insert(news);
        } else {
            news.setUpdateTime(now);
            newsMapper.updateById(news);
        }
        return news;
    }

    public Page<News> adminList(long page, long size) {
        return newsMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<News>().orderByDesc(News::getCreateTime));
    }

    /**
     * 上下架。
     */
    public News setStatus(Long id, PublishStatus status) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BusinessException("资讯不存在");
        }
        news.setStatus(status);
        news.setUpdateTime(LocalDateTime.now());
        newsMapper.updateById(news);
        return news;
    }

    /**
     * 删除资讯。
     */
    public void delete(Long id) {
        if (newsMapper.selectById(id) == null) {
            throw new BusinessException("资讯不存在");
        }
        newsMapper.deleteById(id);
    }
}
