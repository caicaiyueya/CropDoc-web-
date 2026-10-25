package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.entity.enums.PostTag;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子。
 * 对应分析类图：Post
 */
@Data
@TableName("post")
public class Post {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者ID */
    private Long authorId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 图片路径，逗号分隔 */
    private String images;

    /** 标签 */
    private PostTag tag;

    /** 状态 */
    private PostStatus status;

    /** 是否置顶 */
    private Boolean pinned;

    /** 浏览量 */
    private Integer viewCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 展示用：作者昵称（非表字段） */
    @TableField(exist = false)
    private String authorName;

    /** 展示用：作者角色 FARMER/EXPERT/ADMIN（非表字段） */
    @TableField(exist = false)
    private String authorRole;
}
