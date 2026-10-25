package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.dto.user.UserInfoVO;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.mapper.UserMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 管理员用户管理。
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminLogService adminLogService;
    private final CertificationService certificationService;

    public Page<UserInfoVO> listUsers(String keyword, UserRole role, UserStatus status, long page, long size) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (role != null) {
            qw.eq(User::getRole, role);
        }
        if (status != null) {
            qw.eq(User::getStatus, status);
        }
        Page<User> raw = userMapper.selectPage(new Page<>(page, size), qw);
        Page<UserInfoVO> voPage = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        voPage.setRecords(raw.getRecords().stream().map(UserInfoVO::from).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 重置用户密码。
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = requireUser(userId);
        String pwd = StringUtils.hasText(newPassword) ? newPassword : "Reset@123";
        user.setPasswordHash(passwordEncoder.encode(pwd));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        adminLogService.log(SecurityUtils.requireUserId(), userId, "RESET_PASSWORD", "管理员重置密码");
    }

    /**
     * 调整角色；设为 EXPERT 时同步写入认证审核「已通过」记录。
     */
    public void changeRole(Long userId, UserRole role) {
        if (role == null) {
            throw new BusinessException("角色不能为空");
        }
        User user = requireUser(userId);
        UserRole old = user.getRole();
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        adminLogService.log(SecurityUtils.requireUserId(), userId, "CHANGE_ROLE",
                "角色 " + old + " → " + role);
        if (role == UserRole.EXPERT) {
            certificationService.ensureApprovedForExpert(userId);
        }
    }

    public void disable(Long userId) {
        User user = requireUser(userId);
        user.setStatus(UserStatus.DISABLED);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        adminLogService.log(SecurityUtils.requireUserId(), userId, "DISABLE_USER", "禁用账号");
    }

    public void enable(Long userId) {
        User user = requireUser(userId);
        user.setStatus(UserStatus.NORMAL);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        adminLogService.log(SecurityUtils.requireUserId(), userId, "ENABLE_USER", "启用账号");
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
