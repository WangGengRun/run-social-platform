package com.run.runsocialplatform.module.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.admin.model.dto.UserQueryDTO;
import com.run.runsocialplatform.module.admin.model.vo.*;
import com.run.runsocialplatform.module.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@Tag(name = "系统管理", description = "后台数据管理与统计")
public class AdminController {

    private final AdminService adminService;

    // 用户管理
    @GetMapping("/users")
    @Operation(summary = "获取用户列表")
    public Result<IPage<UserManageVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            UserQueryDTO queryDTO) {
        log.info("查询用户列表：pageNum={}, pageSize={}, queryDTO={}", pageNum, pageSize, queryDTO);
        IPage<UserManageVO> userList = adminService.getUserList(pageNum, pageSize, queryDTO);
        return Result.success(userList);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户详情")
    public Result<UserDetailVO> getUserDetail(@PathVariable Long userId) {
        log.info("查询用户详情：userId={}", userId);
        UserDetailVO userDetail = adminService.getUserDetail(userId);
        return Result.success(userDetail);
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        log.info("更新用户状态：userId={}, status={}", userId, status);
        adminService.updateUserStatus(userId, status);
        return Result.success();
    }

    @PutMapping("/users/{userId}/role")
    @Operation(summary = "更新用户角色")
    public Result<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        log.info("更新用户角色：userId={}, role={}", userId, role);
        adminService.updateUserRole(userId, role);
        return Result.success();
    }

    // 内容审核
    @GetMapping("/posts")
    @Operation(summary = "获取动态列表")
    public Result<IPage<PostAuditVO>> getPostList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        log.info("查询动态列表：pageNum={}, pageSize={}, status={}, keyword={}", pageNum, pageSize, status, keyword);
        IPage<PostAuditVO> postList = adminService.getPostList(pageNum, pageSize, status, keyword);
        return Result.success(postList);
    }

    @PostMapping("/posts/{postId}/audit")
    @Operation(summary = "审核动态")
    public Result<Void> auditPost(
            @PathVariable Long postId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        log.info("审核动态：postId={}, status={}, reason={}", postId, status, reason);
        adminService.auditPost(postId, status, reason);
        return Result.success();
    }

    @GetMapping("/comments")
    @Operation(summary = "获取评论列表")
    public Result<IPage<CommentAuditVO>> getCommentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        log.info("查询评论列表：pageNum={}, pageSize={}, status={}, keyword={}", pageNum, pageSize, status, keyword);
        IPage<CommentAuditVO> commentList = adminService.getCommentList(pageNum, pageSize, status, keyword);
        return Result.success(commentList);
    }

    @PostMapping("/comments/{commentId}/audit")
    @Operation(summary = "审核评论")
    public Result<Void> auditComment(
            @PathVariable Long commentId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        log.info("审核评论：commentId={}, status={}, reason={}", commentId, status, reason);
        adminService.auditComment(commentId, status, reason);
        return Result.success();
    }

    // 活动管理
    @GetMapping("/activities")
    @Operation(summary = "获取活动列表")
    public Result<IPage<ActivityManageVO>> getActivityList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        log.info("查询活动列表：pageNum={}, pageSize={}, status={}, keyword={}", pageNum, pageSize, status, keyword);
        IPage<ActivityManageVO> activityList = adminService.getActivityList(pageNum, pageSize, status, keyword);
        return Result.success(activityList);
    }

    @PutMapping("/activities/{activityId}/status")
    @Operation(summary = "更新活动状态")
    public Result<Void> updateActivityStatus(
            @PathVariable Long activityId,
            @RequestParam Integer status) {
        log.info("更新活动状态：activityId={}, status={}", activityId, status);
        adminService.updateActivityStatus(activityId, status);
        return Result.success();
    }

    @DeleteMapping("/activities/{activityId}")
    @Operation(summary = "删除活动")
    public Result<Void> deleteActivity(@PathVariable Long activityId) {
        log.info("删除活动：activityId={}", activityId);
        adminService.deleteActivity(activityId);
        return Result.success();
    }

    // 数据统计
    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘数据")
    public Result<DashboardVO> getDashboardData() {
        log.info("获取仪表盘数据");
        DashboardVO dashboardData = adminService.getDashboardData();
        return Result.success(dashboardData);
    }

    @GetMapping("/statistics/users")
    @Operation(summary = "获取用户统计数据")
    public Result<Map<String, Object>> getUserStatistics() {
        log.info("获取用户统计数据");
        Map<String, Object> userStatistics = adminService.getUserStatistics();
        return Result.success(userStatistics);
    }

    @GetMapping("/statistics/activities")
    @Operation(summary = "获取活动统计数据")
    public Result<Map<String, Object>> getActivityStatistics() {
        log.info("获取活动统计数据");
        Map<String, Object> activityStatistics = adminService.getActivityStatistics();
        return Result.success(activityStatistics);
    }

    @GetMapping("/statistics/interactions")
    @Operation(summary = "获取互动统计数据")
    public Result<Map<String, Object>> getInteractionStatistics() {
        log.info("获取互动统计数据");
        Map<String, Object> interactionStatistics = adminService.getInteractionStatistics();
        return Result.success(interactionStatistics);
    }
}
