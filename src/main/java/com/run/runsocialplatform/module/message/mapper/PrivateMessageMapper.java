package com.run.runsocialplatform.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.module.message.model.entity.PrivateMessage;
import com.run.runsocialplatform.module.message.model.vo.ConversationVO;
import com.run.runsocialplatform.module.message.model.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Param;

/**
 * 私信Mapper
 */
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 查询会话列表
     */
    IPage<ConversationVO> selectConversationList(Page<ConversationVO> page,
                                                  @Param("userId") Long userId);

    /**
     * 查询与指定用户的消息列表
     */
    IPage<PrivateMessageVO> selectMessageListWithUser(Page<PrivateMessageVO> page,
                                                       @Param("userId") Long userId,
                                                       @Param("otherUserId") Long otherUserId);

    /**
     * 统计未读消息总数
     */
    Long countUnreadMessages(@Param("userId") Long userId);

    /**
     * 统计与指定用户的未读消息数
     */
    Long countUnreadMessagesWithUser(@Param("userId") Long userId,
                                     @Param("otherUserId") Long otherUserId);

    /**
     * 查询最后一条消息（用于会话列表）
     */
    PrivateMessage selectLastMessage(@Param("userId") Long userId,
                                     @Param("otherUserId") Long otherUserId);
}

