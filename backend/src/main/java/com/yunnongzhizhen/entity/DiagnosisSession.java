package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能诊断会话。
 * 对应分析类图：DiagnosisSession
 */
@Data
@TableName("diagnosis_session")
public class DiagnosisSession {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联检测记录ID */
    private Long detectionRecordId;

    /** 用户ID */
    private Long userId;

    /** 会话ID（多轮同一会话） */
    private String sessionId;

    /** 会话内容 JSON */
    private String sessionContent;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
