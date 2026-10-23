package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.DiseaseLevel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病害资料。
 * 对应分析类图：Disease
 */
@Data
@TableName("disease")
public class Disease {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作物ID */
    private Long cropId;

    /** 病害名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 防治建议 */
    private String preventionAdvice;

    /** 资料层级 BASIC/PRO */
    private DiseaseLevel level;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
