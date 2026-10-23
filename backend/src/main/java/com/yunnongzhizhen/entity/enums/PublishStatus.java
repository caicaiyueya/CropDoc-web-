package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 资讯/Banner/公告上下架状态。
 */
@Getter
public enum PublishStatus {
    ONLINE("ONLINE", "上架"),
    OFFLINE("OFFLINE", "下架");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    PublishStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
