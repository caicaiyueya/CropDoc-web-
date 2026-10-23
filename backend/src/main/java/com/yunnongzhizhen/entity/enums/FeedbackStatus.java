package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户反馈状态。
 * 对应分析类图：Feedback.status
 */
@Getter
public enum FeedbackStatus {
    OPEN("OPEN", "待处理"),
    REPLIED("REPLIED", "已回复"),
    CLOSED("CLOSED", "已关闭");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    FeedbackStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
