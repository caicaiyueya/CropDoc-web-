package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户角色枚举。
 * 对应分析类图：User.role
 */
@Getter
public enum UserRole {
    FARMER("FARMER", "普通农户"),
    EXPERT("EXPERT", "认证农技员"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    UserRole(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
