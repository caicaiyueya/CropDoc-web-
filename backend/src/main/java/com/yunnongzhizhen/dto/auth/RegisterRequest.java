package com.yunnongzhizhen.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度3-64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度6-64")
    private String password;

    /** 确认密码（须与 password 一致） */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Size(max = 64)
    private String nickname;
}
