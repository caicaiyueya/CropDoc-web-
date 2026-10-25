package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论。
 * 对应分析类图：Comment
 */
@Data
@TableName("comment")
public class Comment {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 作者ID */
    private Long authorId;

    /** 内容 */
    private String content;

    /** 是否专业回答 */
    private Boolean isProfessional;

    /** 是否优质 */
    private Boolean isQuality;

    /** 父评论ID */
    private Long parentId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 展示用：作者昵称（非表字段） */
    @TableField(exist = false)
    private String authorName;

    /** 展示用：作者角色（非表字段） */
    @TableField(exist = false)
    private String authorRole;
}
