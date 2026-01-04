package com.run.runsocialplatform.module.auth.controller;

// 创建一个测试Controller来验证Token

import com.run.runsocialplatform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/test")
@Tag(name = "认证测试", description = "测试认证和授权功能")
public class AuthTestController {

    @GetMapping("/public")
    @Operation(summary = "公开测试接口")
    public Result<String> publicTest() {
        return Result.success("这是一个公开接口，无需认证");
    }

    @GetMapping("/private")
    @Operation(summary = "私有测试接口")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> privateTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个需要认证的接口");
        data.put("principal", authentication.getPrincipal());
        data.put("name", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        data.put("authenticated", authentication.isAuthenticated());

        log.info("用户访问私有接口: {}", authentication.getName());

        return Result.success(data);
    }

    @GetMapping("/admin")
    @Operation(summary = "管理员测试接口")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Map<String, Object>> adminTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个需要管理员权限的接口");
        data.put("user", authentication.getName());
        data.put("role", "ADMIN");

        log.info("管理员访问接口: {}", authentication.getName());

        return Result.success(data);
    }

    @GetMapping("/alumni")
    @Operation(summary = "校友测试接口")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Map<String, Object>> alumniTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个需要校友权限的接口");
        data.put("user", authentication.getName());
        data.put("role", "ALUMNI");

        return Result.success(data);
    }

    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户信息")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> userInfo = new HashMap<>();
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            userInfo.put("username", userDetails.getUsername());
            userInfo.put("authorities", userDetails.getAuthorities());
        } else {
            userInfo.put("username", authentication.getName());
            userInfo.put("principal", authentication.getPrincipal().toString());
        }

        userInfo.put("authenticated", authentication.isAuthenticated());

        return Result.success(userInfo);
    }
}