package com.aiplatform.backend.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类，注册所需的拦截器插件。
 *
 * <p>当前注册了分页拦截器，使 MyBatis Plus 的 {@code Page} 查询能够自动生成
 * 带 {@code LIMIT/OFFSET} 的分页 SQL。</p>
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建 MyBatis Plus 拦截器并添加分页插件。
     *
     * @return 配置好的 {@link MybatisPlusInterceptor} 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }
}
