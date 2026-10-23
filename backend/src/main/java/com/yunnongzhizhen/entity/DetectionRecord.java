package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病虫害识别记录。
 * 对应分析类图：DetectionRecord
 */
@Data
@TableName("detection_record")
public class DetectionRecord {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 图片相对路径 */
    private String imagePath;

    /** 作物ID */
    private Long cropId;

    /** 识别结果（病害名等） */
    private String result;

    /** 置信度 0-1 */
    private BigDecimal confidence;

    /** 识别描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
