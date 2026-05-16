package com.intelligent.trial.punishment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 处分执行模块启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.intelligent.trial.punishment",
    "com.intelligent.trial.common"
})
@MapperScan("com.intelligent.trial.punishment.mapper")
public class PunishmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PunishmentApplication.class, args);
    }
}
