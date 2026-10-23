package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作物。
 * 对应分析类图：Crop
 */
@Data
@TableName("crop")
public class Crop {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作物名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
