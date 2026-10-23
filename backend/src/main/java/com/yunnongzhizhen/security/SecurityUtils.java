package com.yunnongzhizhen.security;

import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * @return 当前 LoginUser
     */
    public static LoginUser requireLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return loginUser;
    }

    /**
     * @return 当前用户ID
     */
    public static Long requireUserId() {
        return requireLoginUser().getId();
    }

    /**
     * @return 当前角色（来自 DB）
     */
    public static UserRole requireRole() {
        return requireLoginUser().getRole();
    }
}
