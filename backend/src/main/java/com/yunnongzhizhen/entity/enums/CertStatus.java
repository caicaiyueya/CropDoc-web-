package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 认证申请状态。
 * 对应分析类图：CertificationApplication.status；状态图：PENDING→APPROVED/REJECTED
 */
@Getter
public enum CertStatus {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    CertStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
