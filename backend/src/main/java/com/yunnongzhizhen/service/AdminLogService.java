package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.entity.AdminOperationLog;
import com.yunnongzhizhen.mapper.AdminOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 管理员操作日志写入与查询。
 */
@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminOperationLogMapper adminOperationLogMapper;

    /**
     * 记录操作日志。
     *
     * @param operatorId   操作人
     * @param targetUserId 目标用户
     * @param action       动作
     * @param detail       详情
     */
    public void log(Long operatorId, Long targetUserId, String action, String detail) {
        AdminOperationLog row = new AdminOperationLog();
        row.setOperatorId(operatorId);
        row.setTargetUserId(targetUserId);
        row.setAction(action);
        row.setDetail(detail);
        LocalDateTime now = LocalDateTime.now();
        row.setCreateTime(now);
        row.setUpdateTime(now);
        adminOperationLogMapper.insert(row);
    }

    /**
     * 查询认证审核相关日志。
     *
     * @return 日志列表
     */
    public List<AdminOperationLog> listCertificationLogs() {
        return adminOperationLogMapper.selectList(new LambdaQueryWrapper<AdminOperationLog>()
                .in(AdminOperationLog::getAction, Arrays.asList("APPROVE_CERTIFICATION", "REJECT_CERTIFICATION"))
                .orderByDesc(AdminOperationLog::getCreateTime));
    }

    /**
     * 查询全部操作日志（可按动作过滤）。
     *
     * @param action 可选动作
     * @return 日志列表
     */
    public List<AdminOperationLog> listLogs(String action) {
        LambdaQueryWrapper<AdminOperationLog> qw = new LambdaQueryWrapper<AdminOperationLog>()
                .orderByDesc(AdminOperationLog::getCreateTime);
        if (action != null && !action.isBlank()) {
            qw.eq(AdminOperationLog::getAction, action);
        }
        return adminOperationLogMapper.selectList(qw);
    }
}
