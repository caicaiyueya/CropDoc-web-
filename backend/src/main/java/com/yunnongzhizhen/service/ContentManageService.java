package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Banner;
import com.yunnongzhizhen.entity.Notice;
import com.yunnongzhizhen.entity.enums.PublishStatus;
import com.yunnongzhizhen.mapper.BannerMapper;
import com.yunnongzhizhen.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Banner / 公告管理。
 */
@Service
@RequiredArgsConstructor
public class ContentManageService {

    private final BannerMapper bannerMapper;
    private final NoticeMapper noticeMapper;

    public List<Banner> listOnlineBanners() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, PublishStatus.ONLINE)
                .orderByDesc(Banner::getSortOrder));
    }

    public List<Banner> listAllBanners() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>().orderByDesc(Banner::getSortOrder));
    }

    public Banner saveBanner(Banner banner) {
        LocalDateTime now = LocalDateTime.now();
        if (banner.getId() == null) {
            if (banner.getStatus() == null) {
                banner.setStatus(PublishStatus.ONLINE);
            }
            if (banner.getSortOrder() == null) {
                banner.setSortOrder(0);
            }
            banner.setCreateTime(now);
            banner.setUpdateTime(now);
            bannerMapper.insert(banner);
        } else {
            banner.setUpdateTime(now);
            bannerMapper.updateById(banner);
        }
        return banner;
    }

    public void deleteBanner(Long id) {
        bannerMapper.deleteById(id);
    }

    public List<Notice> listOnlineNotices() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, PublishStatus.ONLINE)
                .orderByDesc(Notice::getCreateTime));
    }

    public List<Notice> listAllNotices() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreateTime));
    }

    public Notice saveNotice(Notice notice) {
        LocalDateTime now = LocalDateTime.now();
        if (notice.getId() == null) {
            if (notice.getStatus() == null) {
                notice.setStatus(PublishStatus.ONLINE);
            }
            notice.setCreateTime(now);
            notice.setUpdateTime(now);
            noticeMapper.insert(notice);
        } else {
            notice.setUpdateTime(now);
            noticeMapper.updateById(notice);
        }
        return notice;
    }

    public Notice requireNotice(Long id) {
        Notice n = noticeMapper.selectById(id);
        if (n == null) {
            throw new BusinessException("公告不存在");
        }
        return n;
    }
}
