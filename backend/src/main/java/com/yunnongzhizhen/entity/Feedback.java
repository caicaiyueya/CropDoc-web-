package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.FeedbackStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈。
 * 对应分析类图：Feedback
 */
@Data
@TableName("feedback")
public class Feedback {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 反馈内容 */
    private String content;

    /** 管理员回复 */
    private String reply;

    /** 状态 */
    private FeedbackStatus status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
