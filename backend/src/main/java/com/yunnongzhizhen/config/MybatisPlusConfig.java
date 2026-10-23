package com.yunnongzhizhen.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置：分页与自动填充 createTime/updateTime。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 统一填充创建/更新时间。
     */
    @Component
    public static class TimeMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            setFieldValByName("createTime", now, metaObject);
            setFieldValByName("updateTime", now, metaObject);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
    }
}
