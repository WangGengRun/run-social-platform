package com.run.runsocialplatform.module.message.controller;

import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.message.model.vo.NoticeVO;
import com.run.runsocialplatform.module.message.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息中心（点赞/评论/关注）
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Tag(name = "消息中心", description = "动态互动与关注通知")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    @Operation(summary = "获取消息中心列表（点赞/评论/关注）")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<NoticeVO>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(noticeService.getNoticeList(pageNum, pageSize));
    }

    @GetMapping("/unread")
    @Operation(summary = "获取消息中心未读数")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getUnreadCount() {
        return Result.success(noticeService.getUnreadCount());
    }

    @PutMapping("/read")
    @Operation(summary = "标记消息中心为已读")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAllAsRead() {
        noticeService.markAllAsRead();
        return Result.success();
    }
}

