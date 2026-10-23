package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Report;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.ReportStatus;
import com.yunnongzhizhen.entity.enums.ReportTargetType;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.mapper.ReportMapper;
import com.yunnongzhizhen.mapper.UserMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 举报与用户警告/禁用。
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;

    /**
     * 用例：创建举报。
     */
    public Report createReport(ReportTargetType targetType, Long targetId, String reason) {
        if (targetType == null || targetId == null || !StringUtils.hasText(reason)) {
            throw new BusinessException("举报参数不完整");
        }
        Report report = new Report();
        report.setReporterId(SecurityUtils.requireUserId());
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        report.setCreateTime(now);
        report.setUpdateTime(now);
        reportMapper.insert(report);
        return report;
    }

    /**
     * 管理员举报列表。
     */
    public Page<Report> listReports(ReportStatus status, long page, long size) {
        LambdaQueryWrapper<Report> qw = new LambdaQueryWrapper<Report>()
                .orderByDesc(Report::getCreateTime);
        if (status != null) {
            qw.eq(Report::getStatus, status);
        }
        return reportMapper.selectPage(new Page<>(page, size), qw);
    }

    /**
     * 警告用户（写日志，不禁用）。
     */
    public void warnUser(Long userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Long op = SecurityUtils.requireUserId();
        adminLogService.log(op, userId, "WARN_USER", reason);
    }

    /**
     * 禁用用户。
     */
    public void disableUser(Long userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatus.DISABLED);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        adminLogService.log(SecurityUtils.requireUserId(), userId, "DISABLE_USER", reason);
    }
}
