package com.run.runsocialplatform.module.message.service.impl;

import com.run.runsocialplatform.common.page.PageResult;
import com.run.runsocialplatform.module.message.mapper.NoticeMapper;
import com.run.runsocialplatform.module.message.mapper.NoticeReadStateMapper;
import com.run.runsocialplatform.module.message.model.entity.NoticeReadState;
import com.run.runsocialplatform.module.message.model.vo.NoticeVO;
import com.run.runsocialplatform.module.message.service.NoticeService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息中心服务实现
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeReadStateMapper noticeReadStateMapper;

    @Override
    public PageResult<NoticeVO> getNoticeList(Integer pageNum, Integer pageSize) {
        int pn = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 50);

        Long currentUserId = SecurityUtil.getCurrentUserId();
        long offset = (long) (pn - 1) * ps;

        long total = noticeMapper.countNoticeList(currentUserId);
        List<NoticeVO> list = total == 0 ? java.util.List.of() : noticeMapper.selectNoticeList(currentUserId, offset, ps);
        int totalPages = (int) Math.ceil(total * 1.0 / ps);

        LocalDateTime lastReadAt = getLastReadAt(currentUserId);
        if (lastReadAt != null && list != null) {
            for (NoticeVO item : list) {
                if (item == null || item.getCreatedAt() == null) continue;
                item.setIsRead(!item.getCreatedAt().isAfter(lastReadAt));
            }
        } else if (list != null) {
            // 从未阅读：全部未读
            for (NoticeVO item : list) {
                if (item != null) item.setIsRead(false);
            }
        }

        return PageResult.<NoticeVO>builder()
                .pageNum(pn)
                .pageSize(ps)
                .total(total)
                .totalPages(totalPages)
                .list(list)
                .build();
    }

    @Override
    public Long getUnreadCount() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDateTime lastReadAt = getLastReadAt(currentUserId);
        if (lastReadAt == null) {
            return noticeMapper.countNoticeList(currentUserId);
        }
        return noticeMapper.countUnreadNoticeList(currentUserId, lastReadAt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        NoticeReadState state = noticeReadStateMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoticeReadState>()
                        .eq(NoticeReadState::getUserId, currentUserId)
                        .last("LIMIT 1")
        );
        if (state == null) {
            state = new NoticeReadState();
            state.setUserId(currentUserId);
            state.setLastReadAt(now);
            noticeReadStateMapper.insert(state);
        } else {
            state.setLastReadAt(now);
            noticeReadStateMapper.updateById(state);
        }
    }

    private LocalDateTime getLastReadAt(Long userId) {
        NoticeReadState state = noticeReadStateMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoticeReadState>()
                        .eq(NoticeReadState::getUserId, userId)
                        .last("LIMIT 1")
        );
        return state == null ? null : state.getLastReadAt();
    }
}

