package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.CertStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 农技员认证申请。
 * 对应分析类图：CertificationApplication
 */
@Data
@TableName("certification_application")
public class CertificationApplication {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请人用户ID */
    private Long userId;

    /** 资质说明 */
    private String qualificationDesc;

    /** 材料文件路径 */
    private String materialPath;

    /** 状态 */
    private CertStatus status;

    /** 审核人ID */
    private Long reviewerId;

    /** 审核意见 */
    private String reviewComment;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
