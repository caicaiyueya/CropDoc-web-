package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举。
 * 对应分析类图：User.status
 */
@Getter
public enum UserStatus {
    NORMAL("NORMAL", "正常"),
    DISABLED("DISABLED", "禁用");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    UserStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
