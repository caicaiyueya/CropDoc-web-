package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.PublishStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Banner。
 * 对应分析类图：Banner
 */
@Data
@TableName("banner")
public class Banner {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 图片路径 */
    private String imagePath;

    /** 跳转链接 */
    private String linkUrl;

    /** 排序，越大越靠前 */
    private Integer sortOrder;

    /** 状态 */
    private PublishStatus status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
