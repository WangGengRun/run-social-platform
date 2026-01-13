package com.run.runsocialplatform.module.message.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.module.message.mapper.PrivateMessageMapper;
import com.run.runsocialplatform.module.message.model.dto.MessageSendDTO;
import com.run.runsocialplatform.module.message.model.entity.PrivateMessage;
import com.run.runsocialplatform.module.message.model.vo.ConversationVO;
import com.run.runsocialplatform.module.message.model.vo.PrivateMessageVO;
import com.run.runsocialplatform.module.message.model.vo.UnreadCountVO;
import com.run.runsocialplatform.module.message.service.PrivateMessageService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import com.run.runsocialplatform.websocket.service.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 私信服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
        implements PrivateMessageService {

    private final PrivateMessageMapper messageMapper;
    private final UserService userService;
    private final WebSocketMessageService webSocketMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(MessageSendDTO sendDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 不能给自己发消息
        if (currentUserId.equals(sendDTO.getReceiverId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能给自己发消息");
        }

        // 检查接收者是否存在
        var receiver = userService.getById(sendDTO.getReceiverId());
        if (receiver == null || !receiver.getStatus().equals(1)) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "接收者不存在或已被禁用");
        }

        // 创建消息
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(currentUserId);
        message.setReceiverId(sendDTO.getReceiverId());
        message.setContent(sendDTO.getContent());
        message.setMessageType(sendDTO.getMessageType() != null ? sendDTO.getMessageType() : 1);
        message.setIsRead(0); // 未读
        message.setStatus(1); // 正常
        save(message);

        // 实时推送消息给接收者（如果在线）
        PrivateMessageVO messageVO = convertToVO(message, currentUserId);
        webSocketMessageService.sendPrivateMessage(sendDTO.getReceiverId(), messageVO);

        log.info("发送私信成功，消息ID: {}, 发送者: {}, 接收者: {}", 
                message.getId(), currentUserId, sendDTO.getReceiverId());
        return message.getId();
    }

    @Override
    public Page<ConversationVO> getConversationList(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<ConversationVO> page = new Page<>(pageNum, pageSize);
        IPage<ConversationVO> result = messageMapper.selectConversationList(page, currentUserId);
        return (Page<ConversationVO>) result;
    }

    @Override
    public Page<PrivateMessageVO> getMessageListWithUser(Long otherUserId, Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 检查对方用户是否存在
        var otherUser = userService.getById(otherUserId);
        if (otherUser == null || !otherUser.getStatus().equals(1)) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在或已被禁用");
        }

        Page<PrivateMessageVO> page = new Page<>(pageNum, pageSize);
        IPage<PrivateMessageVO> result = messageMapper.selectMessageListWithUser(page, currentUserId, otherUserId);
        return (Page<PrivateMessageVO>) result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long otherUserId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 批量标记为已读
        lambdaUpdate()
                .eq(PrivateMessage::getReceiverId, currentUserId)
                .eq(PrivateMessage::getSenderId, otherUserId)
                .eq(PrivateMessage::getIsRead, 0)
                .eq(PrivateMessage::getStatus, 1)
                .set(PrivateMessage::getIsRead, 1)
                .set(PrivateMessage::getReadTime, LocalDateTime.now())
                .update();

        log.info("标记消息为已读，用户ID: {}, 对方用户ID: {}", currentUserId, otherUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        PrivateMessage message = getById(messageId);
        if (message == null || message.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "消息不存在");
        }

        // 只能删除自己发送或接收的消息
        if (!message.getSenderId().equals(currentUserId) 
                && !message.getReceiverId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该消息");
        }

        // 逻辑删除
        message.setStatus(0);
        updateById(message);
        log.info("删除消息成功，消息ID: {}", messageId);
    }

    @Override
    public UnreadCountVO getUnreadCount(Long otherUserId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Long totalUnreadCount = messageMapper.countUnreadMessages(currentUserId);
        Long userUnreadCount = null;

        if (otherUserId != null) {
            userUnreadCount = messageMapper.countUnreadMessagesWithUser(currentUserId, otherUserId);
        }

        return UnreadCountVO.builder()
                .totalUnreadCount(totalUnreadCount)
                .userUnreadCount(userUnreadCount)
                .build();
    }

    /**
     * 转换为VO（用于WebSocket推送）
     */
    private PrivateMessageVO convertToVO(PrivateMessage message, Long currentUserId) {
        var sender = userService.getById(message.getSenderId());
        var receiver = userService.getById(message.getReceiverId());
        
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setMessageType(message.getMessageType());
        vo.setIsRead(message.getIsRead() == 1);
        vo.setReadTime(message.getReadTime());
        vo.setIsSelf(message.getSenderId().equals(currentUserId));
        vo.setCreatedAt(message.getCreatedAt());
        
        if (sender != null) {
            vo.setSenderUsername(sender.getUsername());
            vo.setSenderAvatar(sender.getAvatar());
        }
        if (receiver != null) {
            vo.setReceiverUsername(receiver.getUsername());
            vo.setReceiverAvatar(receiver.getAvatar());
        }
        
        return vo;
    }
}

