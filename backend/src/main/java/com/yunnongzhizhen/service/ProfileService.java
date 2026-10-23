package com.yunnongzhizhen.service;

import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.dto.user.UserInfoVO;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.mapper.UserMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 个人资料。
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserMapper userMapper;

    public UserInfoVO getProfile() {
        User user = userMapper.selectById(SecurityUtils.requireUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserInfoVO.from(user);
    }

    public UserInfoVO updateProfile(String nickname, String avatar, String region, String contact) {
        User user = userMapper.selectById(SecurityUtils.requireUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (region != null) {
            user.setRegion(region);
        }
        if (contact != null) {
            user.setContact(contact);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return UserInfoVO.from(user);
    }
}
