package com.yunnongzhizhen.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 帖子标签。
 * 对应分析类图：Post.tag
 */
@Getter
public enum PostTag {
    NORMAL("NORMAL", "普通帖"),
    GUIDE("GUIDE", "农技指导");

    @EnumValue
    @JsonValue
    private final String code;
    private final String label;

    PostTag(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
