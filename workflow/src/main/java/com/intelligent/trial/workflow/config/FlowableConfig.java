package com.intelligent.trial.workflow.config;

import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

/**
 * Flowable引擎配置类
 * 自定义Flowable流程引擎的行为和参数
 *
 * @author intelligent-trial
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    /**
     * 配置流程引擎
     * 设置流程实例名称生成策略、字体、历史级别等
     *
     * @param springProcessEngineConfiguration 流程引擎配置对象
     */
    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        // 设置字体，支持中文显示
        springProcessEngineConfiguration.setActivityFontName("宋体");
        springProcessEngineConfiguration.setLabelFontName("宋体");
        springProcessEngineConfiguration.setAnnotationFontName("宋体");

        // 设置完整的历史记录级别
        springProcessEngineConfiguration.setHistoryLevel(HistoryLevel.FULL);
    }
}
