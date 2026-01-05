package com.run.runsocialplatform.security.utils;

import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.security.model.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取当前认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户ID
     */
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }

        throw new BusinessException(ResultCode.FORBIDDEN, "无法获取用户信息");
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        return principal.toString();
    }

    /**
     * 检查是否有指定权限
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}