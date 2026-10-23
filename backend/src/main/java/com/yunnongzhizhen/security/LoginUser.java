package com.yunnongzhizhen.security;

import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Security 上下文中的登录用户（role 来自数据库最新值）。
 */
@Getter
public class LoginUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final UserRole role;
    private final UserStatus status;

    /**
     * @param user 数据库用户
     */
    public LoginUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == UserStatus.NORMAL;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.NORMAL;
    }
}
