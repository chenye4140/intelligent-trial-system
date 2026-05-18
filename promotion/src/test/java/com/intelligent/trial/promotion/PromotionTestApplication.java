package com.intelligent.trial.promotion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 以案促改模块集成测试启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
@MapperScan(basePackages = {
        "com.intelligent.trial.promotion.mapper",
        "com.intelligent.trial.repository.mapper"
})
public class PromotionTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionTestApplication.class, args);
    }
}
