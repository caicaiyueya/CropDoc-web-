package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.ReportStatus;
import com.yunnongzhizhen.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报。
 * 对应分析类图：Report
 */
@Data
@TableName("report")
public class Report {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人ID */
    private Long reporterId;

    /** 目标类型 */
    private ReportTargetType targetType;

    /** 目标ID */
    private Long targetId;

    /** 举报原因 */
    private String reason;

    /** 状态 */
    private ReportStatus status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理结果 */
    private String handleResult;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
