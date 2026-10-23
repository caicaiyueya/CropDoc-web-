package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 举报目标类型。
 * 对应分析类图：Report.targetType
 */
@Getter
public enum ReportTargetType {
    POST("POST", "帖子"),
    COMMENT("COMMENT", "评论"),
    USER("USER", "用户");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    ReportTargetType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
