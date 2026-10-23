package com.yunnongzhizhen.dto.user;

import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import lombok.Data;

/**
 * 用户信息（不含密码）。
 */
@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String region;
    private String contact;
    private UserRole role;
    private UserStatus status;

    /**
     * @param user 实体
     * @return VO
     */
    public static UserInfoVO from(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRegion(user.getRegion());
        vo.setContact(user.getContact());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
