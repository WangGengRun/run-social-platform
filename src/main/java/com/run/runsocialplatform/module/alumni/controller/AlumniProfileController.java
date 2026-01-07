package com.run.runsocialplatform.module.alumni.controller;

import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniProfileDTO;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniSearchDTO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniListItemVO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniProfileVO;
import com.run.runsocialplatform.module.alumni.service.AlumniProfileService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@Tag(name = "校友档案模块", description = "校友信息管理和搜索")
public class AlumniProfileController {

    private final AlumniProfileService alumniProfileService;

    @GetMapping("/profile/current")
    @Operation(summary = "获取当前用户校友档案")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<AlumniProfileVO> getCurrentProfile() {
        AlumniProfileVO profile = alumniProfileService.getCurrentUserProfile();
        return Result.success(profile);
    }

    @PutMapping("/profile/current")
    @Operation(summary = "更新当前用户校友档案")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> updateProfile(@Validated @RequestBody AlumniProfileDTO profileDTO) {
        alumniProfileService.updateCurrentUserProfile(profileDTO);
        return Result.success();
    }

    @GetMapping("/profile/{id}")
    @Operation(summary = "获取校友档案详情")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<AlumniProfileVO> getProfile(@PathVariable Long id) {
        AlumniProfileVO profile = alumniProfileService.getAlumniProfile(id);
        return Result.success(profile);
    }

    @PostMapping("/search")
    @Operation(summary = "搜索校友")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<AlumniListItemVO>> searchAlumni(@Validated @RequestBody AlumniSearchDTO searchDTO) {
        Page<AlumniListItemVO> pageResult = alumniProfileService.searchAlumni(searchDTO);
        PageResult<AlumniListItemVO> result = PageResult.of(pageResult);
        return Result.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取校友列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<AlumniListItemVO>> getAlumniList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<AlumniListItemVO> page = new Page<>(pageNum, pageSize);
        Page<AlumniListItemVO> result = alumniProfileService.getAlumniList(page);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门校友推荐")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<AlumniListItemVO>> getHotAlumni(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AlumniListItemVO> page = new Page<>(pageNum, pageSize);
        Page<AlumniListItemVO> result = alumniProfileService.getHotAlumni(page);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/validate")
    @Operation(summary = "验证校友信息是否完整")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Boolean> validateProfileComplete() {
        Long currentUserId = getCurrentUserId();
        boolean isComplete = alumniProfileService.validateProfileComplete(currentUserId);
        return Result.success(isComplete);
    }

    /**
     * 获取当前用户ID的逻辑
     * @return
     */
    private Long getCurrentUserId() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return currentUserId; // 临时返回
    }
}