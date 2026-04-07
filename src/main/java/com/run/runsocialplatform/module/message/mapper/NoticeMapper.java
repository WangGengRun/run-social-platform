package com.run.runsocialplatform.module.message.mapper;

import com.run.runsocialplatform.module.message.model.vo.NoticeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 消息中心通知 Mapper
 */
public interface NoticeMapper {

    List<NoticeVO> selectNoticeList(@Param("userId") Long userId,
                                    @Param("offset") long offset,
                                    @Param("limit") long limit);

    long countNoticeList(@Param("userId") Long userId);

    long countUnreadNoticeList(@Param("userId") Long userId,
                               @Param("lastReadAt") LocalDateTime lastReadAt);
}

