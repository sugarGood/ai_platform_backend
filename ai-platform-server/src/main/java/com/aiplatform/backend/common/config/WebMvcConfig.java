package com.aiplatform.backend.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.aiplatform.backend.common.security.AuthContextLoadInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC：Sa-Token 登录校验 + {@link AuthContextLoadInterceptor} 填充 {@code AuthContext}。
 *
 * <p>测试环境可将 {@code app.security.enabled=false}，跳过上述拦截器（便于 MockMvc 无 Token）。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] AUTH_WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout"
    };

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    private final AuthContextLoadInterceptor authContextLoadInterceptor;

    public WebMvcConfig(AuthContextLoadInterceptor authContextLoadInterceptor) {
        this.authContextLoadInterceptor = authContextLoadInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!securityEnabled) {
            return;
        }
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(AUTH_WHITE_LIST)
                .order(0);

        registry.addInterceptor(authContextLoadInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(AUTH_WHITE_LIST)
                .order(1);
    }
}
