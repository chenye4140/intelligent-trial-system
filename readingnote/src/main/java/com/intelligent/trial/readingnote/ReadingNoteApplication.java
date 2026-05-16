package com.intelligent.trial.readingnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.intelligent.trial.readingnote", "com.intelligent.trial.common"})
@MapperScan("com.intelligent.trial.readingnote.mapper")
public class ReadingNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReadingNoteApplication.class, args);
    }
}
