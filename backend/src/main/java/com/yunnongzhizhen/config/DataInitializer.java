package com.yunnongzhizhen.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 启动时确保存在 ADMIN 账号（用户名 admin / 密码 Admin@123）。
 * 若种子数据哈希错误，会自动纠正密码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, ADMIN_USERNAME));
        if (admin == null) {
            admin = new User();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setNickname("系统管理员");
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.NORMAL);
            LocalDateTime now = LocalDateTime.now();
            admin.setCreateTime(now);
            admin.setUpdateTime(now);
            userMapper.insert(admin);
            log.info("已初始化管理员账号 admin / Admin@123");
            return;
        }
        if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.NORMAL);
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(admin);
            log.info("已重置管理员密码为 Admin@123");
        }
    }
}
