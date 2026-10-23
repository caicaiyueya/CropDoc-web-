package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.dto.auth.ChangePasswordRequest;
import com.yunnongzhizhen.dto.auth.LoginRequest;
import com.yunnongzhizhen.dto.auth.LoginResponse;
import com.yunnongzhizhen.dto.auth.RegisterRequest;
import com.yunnongzhizhen.dto.auth.ResetPasswordRequest;
import com.yunnongzhizhen.dto.user.UserInfoVO;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.mapper.UserMapper;
import com.yunnongzhizhen.security.JwtUtil;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务：注册、登录、改密、重置密码。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MOCK_VERIFY_CODE = "123456";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用例：用户注册。顺序图：AuthController→AuthService→UserMapper.insert
     *
     * @param request 注册请求
     * @return 用户信息
     */
    public UserInfoVO register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setRole(UserRole.FARMER);
        user.setStatus(UserStatus.NORMAL);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        return UserInfoVO.from(user);
    }

    /**
     * 用例：用户登录。顺序图：AuthController→AuthService→校验密码→JwtUtil.generateToken
     *
     * @param request 登录请求
     * @return token + 用户信息（含 role）
     */
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, UserInfoVO.from(user));
    }

    /**
     * 用例：修改密码。顺序图：校验旧密码→更新 passwordHash
     *
     * @param request 改密请求
     */
    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 用例：重置密码（简化：旧密码或模拟验证码 123456）。
     *
     * @param request 重置请求
     */
    public void resetPassword(ResetPasswordRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        boolean ok = false;
        if (StringUtils.hasText(request.getOldPassword())
                && passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            ok = true;
        }
        if (MOCK_VERIFY_CODE.equals(request.getVerifyCode())) {
            ok = true;
        }
        if (!ok) {
            throw new BusinessException("旧密码或验证码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
