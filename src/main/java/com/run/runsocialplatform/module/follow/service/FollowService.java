package com.run.runsocialplatform.module.follow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.follow.model.entity.Follow;
import com.run.runsocialplatform.module.follow.model.vo.FollowListVO;
import com.run.runsocialplatform.module.follow.model.vo.FollowStatsVO;

/**
 * 关注服务接口
 */
public interface FollowService extends IService<Follow> {

    /**
     * 关注用户
     */
    void follow(Long followeeId);

    /**
     * 取消关注
     */
    void unfollow(Long followeeId);

    /**
     * 获取我的关注列表
     */
    Page<FollowListVO> getFollowingList(Integer pageNum, Integer pageSize);

    /**
     * 获取我的粉丝列表
     */
    Page<FollowListVO> getFollowerList(Integer pageNum, Integer pageSize);

    /**
     * 获取互相关注列表
     */
    Page<FollowListVO> getMutualFollowList(Integer pageNum, Integer pageSize);

    /**
     * 获取指定用户的关注列表
     */
    Page<FollowListVO> getUserFollowingList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取指定用户的粉丝列表
     */
    Page<FollowListVO> getUserFollowerList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取关注统计信息
     */
    FollowStatsVO getFollowStats(Long userId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long followeeId);

    /**
     * 检查是否互相关注
     */
    boolean isMutualFollow(Long userId);
}

