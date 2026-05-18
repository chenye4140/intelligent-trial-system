package com.intelligent.trial.document;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 文档模块集成测试启动类
 * 扩展 MapperScan 包含 repository 模块的 mapper（测试中会被 MockBean 替代）
 * 排除 repository 模块的 MinioConfig 避免与 document 模块的 MinioConfig 冲突
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.intelligent\\.trial\\.repository\\.config\\..*"
        ))
@EntityScan(basePackages = {"com.intelligent.trial.document.entity"})
@MapperScan(basePackages = {
        "com.intelligent.trial.document.mapper",
        "com.intelligent.trial.repository.mapper"
})
public class DocumentTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentTestApplication.class, args);
    }
}
