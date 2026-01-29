package com.run.runsocialplatform.module.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.activity.model.dto.ActivityCreateDTO;
import com.run.runsocialplatform.module.activity.model.dto.ActivityUpdateDTO;
import com.run.runsocialplatform.module.activity.model.vo.ActivityDetailVO;
import com.run.runsocialplatform.module.activity.model.vo.ActivityListVO;
import com.run.runsocialplatform.module.activity.model.vo.ParticipantVO;
import com.run.runsocialplatform.module.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "活动中心", description = "线上/线下活动管理")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @Operation(summary = "创建活动（状态：待发布）")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Long> create(@Valid @RequestBody ActivityCreateDTO dto) {
        return Result.success(activityService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新活动")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityUpdateDTO dto) {
        activityService.update(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布活动")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> publish(@PathVariable Long id) {
        activityService.publish(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消活动")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> cancel(@PathVariable Long id) {
        activityService.cancel(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<ActivityDetailVO> detail(@PathVariable Long id) {
        return Result.success(activityService.detail(id));
    }

    @GetMapping("/list")
    @Operation(summary = "活动列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<ActivityListVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "20") Integer pageSize,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword) {
        Page<ActivityListVO> page = activityService.listActivities(pageNum, pageSize, status, keyword);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/my")
    @Operation(summary = "我创建的活动列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<ActivityListVO>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<ActivityListVO> page = activityService.listMyOrganized(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @PostMapping("/{id}/signup")
    @Operation(summary = "报名活动")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> signup(@PathVariable Long id) {
        activityService.signup(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel-signup")
    @Operation(summary = "取消报名")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> cancelSignup(@PathVariable Long id) {
        activityService.cancelSignup(id);
        return Result.success();
    }

    @PostMapping("/{id}/checkin")
    @Operation(summary = "活动签到")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> checkin(@PathVariable Long id) {
        activityService.checkIn(id);
        return Result.success();
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "报名名单")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<ParticipantVO>> participants(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "50") Integer pageSize) {
        Page<ParticipantVO> page = activityService.listParticipants(id, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }
}

