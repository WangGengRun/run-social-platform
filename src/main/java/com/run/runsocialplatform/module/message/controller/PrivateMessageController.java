package com.run.runsocialplatform.module.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.message.model.dto.MessageSendDTO;
import com.run.runsocialplatform.module.message.model.vo.ConversationVO;
import com.run.runsocialplatform.module.message.model.vo.PrivateMessageVO;
import com.run.runsocialplatform.module.message.model.vo.UnreadCountVO;
import com.run.runsocialplatform.module.message.service.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 私信管理控制器
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "私信管理", description = "私信相关接口")
public class PrivateMessageController {

    private final PrivateMessageService messageService;

    @PostMapping
    @Operation(summary = "发送私信")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Long> sendMessage(@Valid @RequestBody MessageSendDTO sendDTO) {
        Long messageId = messageService.sendMessage(sendDTO);
        return Result.success(messageId);
    }

    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<ConversationVO>> getConversationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<ConversationVO> page = messageService.getConversationList(pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取与指定用户的消息列表")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<PageResult<PrivateMessageVO>> getMessageListWithUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PrivateMessageVO> page = messageService.getMessageListWithUser(userId, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @PutMapping("/read/{userId}")
    @Operation(summary = "标记与指定用户的消息为已读")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> markAsRead(@PathVariable Long userId) {
        messageService.markAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return Result.success();
    }

    @GetMapping("/unread")
    @Operation(summary = "获取未读消息数")
    @PreAuthorize("hasAuthority('ALUMNI')")
    public Result<UnreadCountVO> getUnreadCount(
            @RequestParam(required = false) Long userId) {
        UnreadCountVO unreadCount = messageService.getUnreadCount(userId);
        return Result.success(unreadCount);
    }
}

