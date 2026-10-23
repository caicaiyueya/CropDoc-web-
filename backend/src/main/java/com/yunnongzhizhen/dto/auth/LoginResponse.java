package com.yunnongzhizhen.dto.auth;

import com.yunnongzhizhen.dto.user.UserInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应：token + 用户信息（含 role）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UserInfoVO user;
}
