package com.intelligent.trial.promotion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 以案促改分析模块启动类
 * 基于 DeepSeek AI 的案件分析与促改建议生成
 */
@SpringBootApplication(scanBasePackages = {"com.intelligent.trial.promotion", "com.intelligent.trial.common"})
@EnableAsync
@MapperScan("com.intelligent.trial.promotion.mapper")
public class PromotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionApplication.class, args);
    }
}
