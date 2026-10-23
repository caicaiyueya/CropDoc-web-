package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 病害资料层级。
 * 对应分析类图：Disease.level
 */
@Getter
public enum DiseaseLevel {
    BASIC("BASIC", "基础资料"),
    PRO("PRO", "专业资料");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    DiseaseLevel(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
