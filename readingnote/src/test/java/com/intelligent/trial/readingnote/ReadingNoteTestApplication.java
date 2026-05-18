package com.intelligent.trial.readingnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 阅卷笔记模块集成测试启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
@MapperScan(basePackages = {
        "com.intelligent.trial.readingnote.mapper"
})
public class ReadingNoteTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReadingNoteTestApplication.class, args);
    }
}
