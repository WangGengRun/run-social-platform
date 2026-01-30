package com.run.runsocialplatform.security.filter;


import cn.hutool.core.util.StrUtil;

import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.common.utils.JsonUtil;
import com.run.runsocialplatform.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 由于JwtAuthenticationFilter和SecurityConfig有循环依赖问题
     * 使用 @Lazy 注解解决循环依赖
     * @param jwtUtil
     * @param userDetailsService
     */
    public JwtAuthenticationFilter(@Lazy JwtUtil jwtUtil,
                                   @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(jwtUtil.getHeader());

        // 只处理有Authorization头的请求
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (StrUtil.isNotBlank(token) && !jwtUtil.isTokenExpired(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);

                    if (StrUtil.isNotBlank(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // 获取令牌中的权限信息
                        List<String> authorityStrings = jwtUtil.getAuthoritiesFromToken(token);
                        List<SimpleGrantedAuthority> authorities = authorityStrings.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        authorities
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT认证成功: {}", username);
                    }
                }
            } catch (Exception e) {
                // 验证失败：记录日志但不拦截请求
                // 公开接口会在后续的SecurityConfig中被允许通过
                // 受保护接口会在后续的SecurityConfig中被拦截
                log.debug("JWT验证失败，继续处理请求: {}", e.getMessage());
            }
        }

        // 无论验证结果如何，都继续处理请求
        filterChain.doFilter(request, response);
    }
}