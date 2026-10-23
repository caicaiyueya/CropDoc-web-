package com.yunnongzhizhen.controller;

import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.dto.auth.ChangePasswordRequest;
import com.yunnongzhizhen.dto.auth.LoginRequest;
import com.yunnongzhizhen.dto.auth.LoginResponse;
import com.yunnongzhizhen.dto.auth.RegisterRequest;
import com.yunnongzhizhen.dto.auth.ResetPasswordRequest;
import com.yunnongzhizhen.dto.user.UserInfoVO;
import com.yunnongzhizhen.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口 /api/auth。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册。
     */
    @PostMapping("/register")
    public ApiResult<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.ok(authService.register(request));
    }

    /**
     * 登录。
     */
    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.ok(authService.login(request));
    }

    /**
     * 修改密码（需登录）。
     */
    @PostMapping("/change-password")
    public ApiResult<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResult.okMessage("密码修改成功");
    }

    /**
     * 重置密码（可匿名，旧密码或模拟验证码）。
     */
    @PostMapping("/reset-password")
    public ApiResult<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResult.okMessage("密码重置成功");
    }
}
