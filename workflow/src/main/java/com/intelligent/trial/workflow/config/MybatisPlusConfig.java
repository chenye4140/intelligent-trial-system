package com.intelligent.trial.workflow.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.intelligent.trial.common.util.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * MyBatis-Plus 配置
 * 包含分页插件和自动填充处理器
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 限制单页最大条数防止内存溢出
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    /**
     * 自动填充处理器：自动填充 createTime, updateTime, createBy, updateBy
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
                this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
                // createBy/updateBy 从用户上下文获取，如果为空则使用默认值
                Long userId = UserContext.getUserId();
                String currentUser = userId != null ? userId.toString() : "system";
                this.strictInsertFill(metaObject, "createBy", String.class, currentUser);
                this.strictInsertFill(metaObject, "updateBy", String.class, currentUser);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
                Long userId = UserContext.getUserId();
                String currentUser = userId != null ? userId.toString() : "system";
                this.strictUpdateFill(metaObject, "updateBy", String.class, currentUser);
            }
        };
    }
}
