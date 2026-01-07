package com.run.runsocialplatform.module.follow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.module.follow.mapper.FollowMapper;
import com.run.runsocialplatform.module.follow.model.entity.Follow;
import com.run.runsocialplatform.module.follow.model.vo.FollowListVO;
import com.run.runsocialplatform.module.follow.model.vo.FollowStatsVO;
import com.run.runsocialplatform.module.follow.service.FollowService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关注服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    private final FollowMapper followMapper;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followeeId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 不能关注自己
        if (currentUserId.equals(followeeId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能关注自己");
        }

        // 检查被关注用户是否存在
        var followeeUser = userService.getById(followeeId);
        if (followeeUser == null || !followeeUser.getStatus().equals(1)) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在或已被禁用");
        }

        // 检查是否已存在关注关系
        Follow existingFollow = followMapper.checkFollowRelation(currentUserId, followeeId);

        if (existingFollow != null) {
            // 如果已存在但状态为0（已取消），则恢复关注
            if (existingFollow.getStatus() == 0) {
                existingFollow.setStatus(1);
                updateById(existingFollow);
                log.info("恢复关注关系，关注者ID: {}, 被关注者ID: {}", currentUserId, followeeId);
            } else {
                throw new BusinessException(ResultCode.DATA_EXISTS, "已关注该用户");
            }
        } else {
            // 创建新的关注关系
            Follow follow = new Follow();
            follow.setFollowerId(currentUserId);
            follow.setFolloweeId(followeeId);
            follow.setStatus(1);
            save(follow);
            log.info("关注成功，关注者ID: {}, 被关注者ID: {}", currentUserId, followeeId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followeeId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 查找关注关系
        Follow follow = followMapper.checkFollowRelation(currentUserId, followeeId);

        if (follow == null || follow.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "未关注该用户");
        }

        // 逻辑删除（设置为取消状态）
        follow.setStatus(0);
        updateById(follow);
        log.info("取消关注成功，关注者ID: {}, 被关注者ID: {}", currentUserId, followeeId);
    }

    @Override
    public Page<FollowListVO> getFollowingList(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<FollowListVO> page = new Page<>(pageNum, pageSize);
        IPage<FollowListVO> result = followMapper.selectFollowingList(page, currentUserId, currentUserId);
        return (Page<FollowListVO>) result;
    }

    @Override
    public Page<FollowListVO> getFollowerList(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<FollowListVO> page = new Page<>(pageNum, pageSize);
        IPage<FollowListVO> result = followMapper.selectFollowerList(page, currentUserId, currentUserId);
        return (Page<FollowListVO>) result;
    }

    @Override
    public Page<FollowListVO> getMutualFollowList(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<FollowListVO> page = new Page<>(pageNum, pageSize);
        IPage<FollowListVO> result = followMapper.selectMutualFollowList(page, currentUserId);
        return (Page<FollowListVO>) result;
    }

    @Override
    public Page<FollowListVO> getUserFollowingList(Long userId, Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<FollowListVO> page = new Page<>(pageNum, pageSize);
        IPage<FollowListVO> result = followMapper.selectFollowingList(page, userId, currentUserId);
        return (Page<FollowListVO>) result;
    }

    @Override
    public Page<FollowListVO> getUserFollowerList(Long userId, Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<FollowListVO> page = new Page<>(pageNum, pageSize);
        IPage<FollowListVO> result = followMapper.selectFollowerList(page, userId, currentUserId);
        return (Page<FollowListVO>) result;
    }

    @Override
    public FollowStatsVO getFollowStats(Long userId) {
        Long followingCount = followMapper.countFollowing(userId);
        Long followerCount = followMapper.countFollower(userId);
        Long mutualFollowCount = followMapper.countMutualFollow(userId);

        return FollowStatsVO.builder()
                .followingCount(followingCount)
                .followerCount(followerCount)
                .mutualFollowCount(mutualFollowCount)
                .build();
    }

    @Override
    public boolean isFollowing(Long followeeId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Follow follow = followMapper.checkFollowRelation(currentUserId, followeeId);
        return follow != null && follow.getStatus() == 1;
    }

    @Override
    public boolean isMutualFollow(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Follow follow1 = followMapper.checkFollowRelation(currentUserId, userId);
        Follow follow2 = followMapper.checkFollowRelation(userId, currentUserId);
        return follow1 != null && follow1.getStatus() == 1
                && follow2 != null && follow2.getStatus() == 1;
    }
}

