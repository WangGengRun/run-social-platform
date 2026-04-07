package com.run.runsocialplatform.module.message.service;

import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.module.message.model.vo.NoticeVO;

/**
 * 消息中心服务
 */
public interface NoticeService {

    PageResult<NoticeVO> getNoticeList(Integer pageNum, Integer pageSize);

    /**
     * 获取未读数
     */
    Long getUnreadCount();

    /**
     * 标记为已读（更新最后阅读时间为当前时间）
     */
    void markAllAsRead();
}

