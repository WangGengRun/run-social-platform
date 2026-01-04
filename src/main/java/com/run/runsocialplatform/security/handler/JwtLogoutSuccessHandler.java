package com.run.runsocialplatform.security.handler;

/**
 * 登出成功处理器
 */

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.security.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String TOKEN_REFRESH_PREFIX = "token:refresh:";
    private static final String USER_ONLINE_KEY = "user:online:";

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        // 从请求头中获取token
        String authHeader = request.getHeader(jwtUtil.getTokenPrefix());
        String username = null;

        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 从token中获取用户名
                username = jwtUtil.getUsernameFromToken(token);

                if (StrUtil.isNotBlank(username)) {
                    // 1. 将token加入黑名单
                    addTokenToBlacklist(token);

                    // 2. 清除用户的refresh token（如果存在）
                    clearRefreshToken(username);

                    // 3. 更新用户在线状态
                    updateUserOnlineStatus(username, false);

                    log.info("用户登出成功: {}", username);
                }
            } catch (Exception e) {
                log.warn("处理登出token时发生异常: {}", e.getMessage());
                // 即使token无效，也继续执行登出流程
            }
        }

        // 清理SecurityContext
        SecurityContextHolder.clearContext();

        // 设置响应
        Result<String> result = Result.success("登出成功");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }

    /**
     * 将token加入黑名单（在剩余有效期内禁止使用）
     */
    private void addTokenToBlacklist(String token) {
        try {
            // 获取token的过期时间
            Long expiration = getTokenRemainingTime(token);

            if (expiration != null && expiration > 0) {
                // 将token加入黑名单，设置过期时间为token剩余时间
                String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", expiration, TimeUnit.SECONDS);

                log.debug("Token已加入黑名单，剩余有效期: {}秒", expiration);
            }
        } catch (Exception e) {
            log.error("加入token黑名单失败", e);
        }
    }

    /**
     * 获取token剩余有效时间（秒）
     */
    private Long getTokenRemainingTime(String token) {
        try {
            if (jwtUtil.isTokenExpired(token)) {
                return null;
            }

            long expirationTime = jwtUtil.getExpirationDateFromToken(token).getTime();
            long currentTime = System.currentTimeMillis();

            return (expirationTime - currentTime) / 1000;
        } catch (Exception e) {
            log.error("获取token剩余时间失败", e);
            return null;
        }
    }

    /**
     * 清除用户的refresh token
     */
    private void clearRefreshToken(String username) {
        try {
            String refreshTokenKey = TOKEN_REFRESH_PREFIX + username;
            Boolean deleted = redisTemplate.delete(refreshTokenKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.debug("已清除用户的refresh token: {}", username);
            }
        } catch (Exception e) {
            log.error("清除refresh token失败", e);
        }
    }

    /**
     * 更新用户在线状态
     */
    private void updateUserOnlineStatus(String username, boolean isOnline) {
        try {
            String onlineKey = USER_ONLINE_KEY + username;

            if (isOnline) {
                // 用户登录，设置在线状态，有效期为token过期时间
                redisTemplate.opsForValue().set(onlineKey, "online", 24, TimeUnit.HOURS);
            } else {
                // 用户登出，删除在线状态
                redisTemplate.delete(onlineKey);
            }
        } catch (Exception e) {
            log.error("更新用户在线状态失败", e);
        }
    }
}