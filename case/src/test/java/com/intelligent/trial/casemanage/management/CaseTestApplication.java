package com.intelligent.trial.casemanage.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 案件管理集成测试启动类
 * 扩展 MapperScan 包含 repository 模块的 mapper（测试中会被 MockBean 替代）
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
@MapperScan(basePackages = {
        "com.intelligent.trial.casemanage.management.mapper",
        "com.intelligent.trial.repository.mapper"
})
public class CaseTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseTestApplication.class, args);
    }
}
