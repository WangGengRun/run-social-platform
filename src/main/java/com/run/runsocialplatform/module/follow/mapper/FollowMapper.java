package com.run.runsocialplatform.module.follow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.module.follow.model.entity.Follow;
import com.run.runsocialplatform.module.follow.model.vo.FollowListVO;
import org.apache.ibatis.annotations.Param;

/**
 * 关注关系Mapper
 */
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 查询我的关注列表
     */
    IPage<FollowListVO> selectFollowingList(Page<FollowListVO> page,
                                             @Param("followerId") Long followerId,
                                             @Param("currentUserId") Long currentUserId);

    /**
     * 查询我的粉丝列表
     */
    IPage<FollowListVO> selectFollowerList(Page<FollowListVO> page,
                                           @Param("followeeId") Long followeeId,
                                           @Param("currentUserId") Long currentUserId);

    /**
     * 查询互相关注列表
     */
    IPage<FollowListVO> selectMutualFollowList(Page<FollowListVO> page,
                                               @Param("userId") Long userId);

    /**
     * 统计关注数（我关注的）
     */
    Long countFollowing(@Param("userId") Long userId);

    /**
     * 统计粉丝数（关注我的）
     */
    Long countFollower(@Param("userId") Long userId);

    /**
     * 统计互相关注数
     */
    Long countMutualFollow(@Param("userId") Long userId);

    /**
     * 检查是否已关注
     */
    Follow checkFollowRelation(@Param("followerId") Long followerId,
                               @Param("followeeId") Long followeeId);
}

