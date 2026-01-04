package com.run.runsocialplatform.security.config;


import com.run.runsocialplatform.security.filter.JwtAuthenticationFilter;
import com.run.runsocialplatform.security.handler.JwtAccessDeniedHandler;
import com.run.runsocialplatform.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * SpringSecurity配置
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
//    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 开启CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(
                                "/api/auth/**",           // 认证相关
                                "/api/test/**",           // 测试接口
                                "/api/config/**",         // 配置检查
                                "/swagger-ui/**",         // Swagger UI
                                "/v3/api-docs/**",        // OpenAPI文档
                                "/doc.html",              // Knife4j文档
                                "/webjars/**",            // WebJars资源
                                "/favicon.ico",           // 网站图标
                                "/error"                  // 错误页面
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                // 配置会话管理
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置登出
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
//                        .logoutSuccessHandler(jwtLogoutSuccessHandler)
                        .permitAll()
                )
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 允许的来源（生产环境应该指定具体域名）
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",       // 本地开发
                "http://127.0.0.1:*",       // 本地开发
                "http://192.168.*.*:*",     // 局域网
                "https://*.example.com"     // 您的生产域名
        ));

        // 2. 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // 3. 允许的请求头（必须包含Authorization）
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",        // JWT Token
                "Content-Type",         // 内容类型
                "X-Requested-With",    // 异步请求标识
                "Accept",              // 接受的内容类型
                "Origin",              // 来源
                "X-Device-Id",         // 设备ID
                "X-Device-Type",       // 设备类型
                "X-CSRF-Token",        // CSRF Token（如果需要）
                "Cache-Control"        // 缓存控制
        ));

        // 4. 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",        // 让前端能访问Authorization头
                "Content-Disposition",  // 文件下载
                "X-Total-Count",        // 分页总数
                "X-Page-Size",          // 分页大小
                "X-Current-Page"        // 当前页码
        ));

        // 5. 允许携带凭证（Cookie、Authorization头等）
        configuration.setAllowCredentials(true);

        // 6. 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS配置已启用，允许的源: {}", configuration.getAllowedOriginPatterns());

        return source;
    }
}