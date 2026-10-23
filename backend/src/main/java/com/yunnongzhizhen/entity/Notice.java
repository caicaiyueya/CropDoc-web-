package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.PublishStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告。
 * 对应分析类图：Notice
 */
@Data
@TableName("notice")
public class Notice {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 状态 */
    private PublishStatus status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
