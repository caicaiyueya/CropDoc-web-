package com.yunnongzhizhen.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求（简化：旧密码或模拟验证码）。
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 旧密码（与 verifyCode 二选一） */
    private String oldPassword;

    /** 模拟验证码，固定 123456 视为通过 */
    private String verifyCode;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "新密码长度6-64")
    private String newPassword;
}
