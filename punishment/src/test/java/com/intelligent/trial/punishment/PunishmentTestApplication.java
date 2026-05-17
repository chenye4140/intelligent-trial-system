package com.intelligent.trial.punishment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 处分执行模块集成测试启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
@MapperScan(basePackages = {
        "com.intelligent.trial.punishment.mapper",
        "com.intelligent.trial.repository.mapper"
})
public class PunishmentTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PunishmentTestApplication.class, args);
    }
}
