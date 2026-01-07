package com.run.runsocialplatform.module.follow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.follow.model.vo.FollowListVO;
import com.run.runsocialplatform.module.follow.model.vo.FollowStatsVO;
import com.run.runsocialplatform.module.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 关注管理控制器
 */
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
@Tag(name = "关注管理", description = "关注/粉丝相关接口")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followeeId}")
    @Operation(summary = "关注用户")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> follow(@PathVariable Long followeeId) {
        followService.follow(followeeId);
        return Result.success();
    }

    @DeleteMapping("/{followeeId}")
    @Operation(summary = "取消关注")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> unfollow(@PathVariable Long followeeId) {
        followService.unfollow(followeeId);
        return Result.success();
    }

    @GetMapping("/following")
    @Operation(summary = "获取我的关注列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<FollowListVO>> getFollowingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<FollowListVO> page = followService.getFollowingList(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/follower")
    @Operation(summary = "获取我的粉丝列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<FollowListVO>> getFollowerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<FollowListVO> page = followService.getFollowerList(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/mutual")
    @Operation(summary = "获取互相关注列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<FollowListVO>> getMutualFollowList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<FollowListVO> page = followService.getMutualFollowList(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/user/{userId}/following")
    @Operation(summary = "获取指定用户的关注列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<FollowListVO>> getUserFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<FollowListVO> page = followService.getUserFollowingList(userId, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/user/{userId}/follower")
    @Operation(summary = "获取指定用户的粉丝列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<FollowListVO>> getUserFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<FollowListVO> page = followService.getUserFollowerList(userId, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取我的关注统计信息")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<FollowStatsVO> getMyFollowStats() {
        Long currentUserId = com.run.runsocialplatform.security.utils.SecurityUtil.getCurrentUserId();
        FollowStatsVO stats = followService.getFollowStats(currentUserId);
        return Result.success(stats);
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "获取指定用户的关注统计信息")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<FollowStatsVO> getUserFollowStats(@PathVariable Long userId) {
        FollowStatsVO stats = followService.getFollowStats(userId);
        return Result.success(stats);
    }

    @GetMapping("/check/{followeeId}")
    @Operation(summary = "检查是否已关注")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Boolean> checkFollowing(@PathVariable Long followeeId) {
        boolean isFollowing = followService.isFollowing(followeeId);
        return Result.success(isFollowing);
    }

    @GetMapping("/mutual/check/{userId}")
    @Operation(summary = "检查是否互相关注")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Boolean> checkMutualFollow(@PathVariable Long userId) {
        boolean isMutual = followService.isMutualFollow(userId);
        return Result.success(isMutual);
    }
}

