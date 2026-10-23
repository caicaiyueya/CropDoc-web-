package com.yunnongzhizhen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 云农智诊应用入口。
 * 对应 UML：系统边界「云农智诊」部署节点 / 分析模型系统根。
 */
@SpringBootApplication
@MapperScan("com.yunnongzhizhen.mapper")
public class YunNongZhiZhenApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunNongZhiZhenApplication.class, args);
    }
}
