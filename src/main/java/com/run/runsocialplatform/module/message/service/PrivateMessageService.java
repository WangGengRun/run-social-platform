package com.run.runsocialplatform.module.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.message.model.dto.MessageSendDTO;
import com.run.runsocialplatform.module.message.model.entity.PrivateMessage;
import com.run.runsocialplatform.module.message.model.vo.ConversationVO;
import com.run.runsocialplatform.module.message.model.vo.PrivateMessageVO;
import com.run.runsocialplatform.module.message.model.vo.UnreadCountVO;

/**
 * 私信服务接口
 */
public interface PrivateMessageService extends IService<PrivateMessage> {

    /**
     * 发送私信
     */
    Long sendMessage(MessageSendDTO sendDTO);

    /**
     * 获取会话列表
     */
    Page<ConversationVO> getConversationList(Integer pageNum, Integer pageSize);

    /**
     * 获取与指定用户的消息列表
     */
    Page<PrivateMessageVO> getMessageListWithUser(Long otherUserId, Integer pageNum, Integer pageSize);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long otherUserId);

    /**
     * 删除消息（逻辑删除）
     */
    void deleteMessage(Long messageId);

    /**
     * 获取未读消息数
     */
    UnreadCountVO getUnreadCount(Long otherUserId);
}

