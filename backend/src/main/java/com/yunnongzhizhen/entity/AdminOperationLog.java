package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志。
 * 对应分析类图：AdminOperationLog
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long operatorId;

    /** 目标用户ID */
    private Long targetUserId;

    /** 操作类型 */
    private String action;

    /** 详情 */
    private String detail;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
