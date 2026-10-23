package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 帖子审核状态。
 * 对应状态图：PENDING→APPROVED/REJECTED；APPROVED→OFFLINE
 */
@Getter
public enum PostStatus {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    OFFLINE("OFFLINE", "已下架");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    PostStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
