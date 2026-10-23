package com.yunnongzhizhen.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体。
 * 对应分析类图：User
 */
@Data
@TableName("`user`")
public class User {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号（用户名/手机号/邮箱登录标识） */
    private String username;

    /** 密码 BCrypt 哈希 */
    private String passwordHash;

    /** 昵称 */
    private String nickname;

    /** 头像路径 */
    private String avatar;

    /** 地区 */
    private String region;

    /** 联系方式 */
    private String contact;

    /** 角色 */
    private UserRole role;

    /** 状态 */
    private UserStatus status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
