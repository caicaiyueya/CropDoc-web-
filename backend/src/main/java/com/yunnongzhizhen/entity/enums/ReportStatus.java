package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 举报处理状态。
 * 对应分析类图：Report.status
 */
@Getter
public enum ReportStatus {
    PENDING("PENDING", "待处理"),
    HANDLED("HANDLED", "已处理");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    ReportStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
