package com.intelligent.trial.auth.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP 配置
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.intelligent.trial.auth.aop")
public class AopConfig {
}
