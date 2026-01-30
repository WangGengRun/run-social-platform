package com.run.runsocialplatform.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.module.admin.model.dto.UserQueryDTO;
import com.run.runsocialplatform.module.admin.model.vo.*;
import com.run.runsocialplatform.module.admin.service.AdminService;
import com.run.runsocialplatform.module.activity.mapper.ActivityMapper;
import com.run.runsocialplatform.module.activity.mapper.ActivityParticipationMapper;
import com.run.runsocialplatform.module.activity.model.entity.Activity;
import com.run.runsocialplatform.module.activity.model.entity.ActivityParticipation;
import com.run.runsocialplatform.module.alumni.mapper.AlumniProfileMapper;
import com.run.runsocialplatform.module.alumni.model.entity.AlumniInfo;
import com.run.runsocialplatform.module.auth.mapper.UserMapper;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.post.mapper.PostInteractionMapper;
import com.run.runsocialplatform.module.post.mapper.PostMapper;
import com.run.runsocialplatform.module.post.model.entity.Post;
import com.run.runsocialplatform.module.post.model.entity.PostInteraction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AlumniProfileMapper alumniProfileMapper;
    private final PostMapper postMapper;
    private final PostInteractionMapper postInteractionMapper;
    private final ActivityMapper activityMapper;
    private final ActivityParticipationMapper activityParticipationMapper;

    // 用户管理
    @Override
    public IPage<UserManageVO> getUserList(Integer pageNum, Integer pageSize, UserQueryDTO queryDTO) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            if (queryDTO.getUsername() != null) {
                wrapper.like(UserEntity::getUsername, queryDTO.getUsername());
            }
            if (queryDTO.getEmail() != null) {
                wrapper.like(UserEntity::getEmail, queryDTO.getEmail());
            }
            if (queryDTO.getPhone() != null) {
                wrapper.like(UserEntity::getPhone, queryDTO.getPhone());
            }
            if (queryDTO.getRole() != null) {
                wrapper.eq(UserEntity::getRole, queryDTO.getRole());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(UserEntity::getStatus, queryDTO.getStatus());
            }
        }

        wrapper.orderByDesc(UserEntity::getCreatedAt);
        Page<UserEntity> userPage = userMapper.selectPage(page, wrapper);

        return userPage.convert(user -> {
            UserManageVO vo = new UserManageVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
            vo.setStatus(user.getStatus());
            vo.setLastLoginTime(user.getLastLoginTime());
            vo.setCreatedAt(user.getCreatedAt());

            // 查询校友信息
            AlumniInfo alumniInfo = alumniProfileMapper.selectOne(
                    new LambdaQueryWrapper<AlumniInfo>().eq(AlumniInfo::getUserId, user.getId())
            );
            if (alumniInfo != null) {
                vo.setRealName(alumniInfo.getRealName());
                vo.setStudentId(alumniInfo.getStudentId());
                vo.setVerifyStatus(alumniInfo.getVerifyStatus());
            }

            return vo;
        });
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        UserDetailVO vo = new UserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());

        // 查询校友信息
        AlumniInfo alumniInfo = alumniProfileMapper.selectOne(
                new LambdaQueryWrapper<AlumniInfo>().eq(AlumniInfo::getUserId, userId)
        );
        if (alumniInfo != null) {
            UserAlumniInfoVO alumniInfoVO = new UserAlumniInfoVO();
            alumniInfoVO.setId(alumniInfo.getId());
            alumniInfoVO.setRealName(alumniInfo.getRealName());
            alumniInfoVO.setStudentId(alumniInfo.getStudentId());
            alumniInfoVO.setAdmissionYear(alumniInfo.getAdmissionYear());
            alumniInfoVO.setGraduationYear(alumniInfo.getGraduationYear());
            alumniInfoVO.setCollege(alumniInfo.getCollege());
            alumniInfoVO.setMajor(alumniInfo.getMajor());
            alumniInfoVO.setCompany(alumniInfo.getCompany());
            alumniInfoVO.setPosition(alumniInfo.getPosition());
            alumniInfoVO.setCity(alumniInfo.getCity());
            alumniInfoVO.setBio(alumniInfo.getBio());
            alumniInfoVO.setVerifyStatus(alumniInfo.getVerifyStatus());
            alumniInfoVO.setVerifyNotes(alumniInfo.getVerifyNotes());
            alumniInfoVO.setVerifyTime(alumniInfo.getVerifyTime());
            alumniInfoVO.setVerifyAdminId(alumniInfo.getVerifyAdminId());
            alumniInfoVO.setCreatedAt(alumniInfo.getCreatedAt());
            alumniInfoVO.setUpdatedAt(alumniInfo.getUpdatedAt());
            vo.setAlumniInfo(alumniInfoVO);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("更新用户状态：userId={}, status={}", userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(Long userId, String role) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setRole(role);
        userMapper.updateById(user);
        log.info("更新用户角色：userId={}, role={}", userId, role);
    }

    // 内容审核
    @Override
    public IPage<PostAuditVO> getPostList(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        if (keyword != null) {
            wrapper.like(Post::getContent, keyword);
        }

        wrapper.orderByDesc(Post::getCreatedAt);
        Page<Post> postPage = postMapper.selectPage(page, wrapper);

        return postPage.convert(post -> {
            PostAuditVO vo = new PostAuditVO();
            vo.setId(post.getId());
            vo.setUserId(post.getUserId());
            
            // 查询用户信息
            UserEntity user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            
            vo.setContent(post.getContent());
            vo.setImageUrls(post.getImageUrls());
            vo.setVisibility(post.getVisibility());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setStatus(post.getStatus());
            vo.setCreatedAt(post.getCreatedAt());
            vo.setUpdatedAt(post.getUpdatedAt());
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long postId, Integer status, String reason) {
        Post post = new Post();
        post.setId(postId);
        post.setStatus(status);
        postMapper.updateById(post);
        log.info("审核动态：postId={}, status={}, reason={}", postId, status, reason);
    }

    @Override
    public IPage<CommentAuditVO> getCommentList(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<PostInteraction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PostInteraction> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(PostInteraction::getType, 2); // 2表示评论
        if (status != null) {
            wrapper.eq(PostInteraction::getStatus, status);
        }
        if (keyword != null) {
            wrapper.like(PostInteraction::getContent, keyword);
        }

        wrapper.orderByDesc(PostInteraction::getCreatedAt);
        Page<PostInteraction> commentPage = postInteractionMapper.selectPage(page, wrapper);

        return commentPage.convert(comment -> {
            CommentAuditVO vo = new CommentAuditVO();
            vo.setId(comment.getId());
            vo.setPostId(comment.getPostId());
            vo.setUserId(comment.getUserId());
            
            // 查询用户信息
            UserEntity user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            
            vo.setContent(comment.getContent());
            vo.setParentId(comment.getParentId());
            vo.setStatus(comment.getStatus());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setUpdatedAt(comment.getUpdatedAt());
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long commentId, Integer status, String reason) {
        PostInteraction comment = new PostInteraction();
        comment.setId(commentId);
        comment.setStatus(status);
        postInteractionMapper.updateById(comment);
        log.info("审核评论：commentId={}, status={}, reason={}", commentId, status, reason);
    }

    // 活动管理
    @Override
    public IPage<ActivityManageVO> getActivityList(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Activity::getState, status);
        }
        if (keyword != null) {
            wrapper.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword);
        }

        wrapper.orderByDesc(Activity::getCreatedAt);
        Page<Activity> activityPage = activityMapper.selectPage(page, wrapper);

        return activityPage.convert(activity -> {
            ActivityManageVO vo = new ActivityManageVO();
            vo.setId(activity.getId());
            vo.setTitle(activity.getTitle());
            vo.setDescription(activity.getDescription());
            vo.setCoverImage(activity.getCoverImage());
            vo.setLocation(activity.getLocation());
            vo.setStartTime(activity.getStartTime());
            vo.setEndTime(activity.getEndTime());
            vo.setOrganizerId(activity.getOrganizerId());
            
            // 查询组织者信息
            UserEntity user = userMapper.selectById(activity.getOrganizerId());
            if (user != null) {
                vo.setOrganizerName(user.getUsername());
            }
            
            vo.setMaxParticipants(activity.getMaxParticipants());
            vo.setCurrentParticipants(activity.getCurrentParticipants());
            vo.setStatus(activity.getState());
            vo.setCreatedAt(activity.getCreatedAt());
            vo.setUpdatedAt(activity.getUpdatedAt());
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus(Long activityId, Integer status) {
        Activity activity = new Activity();
        activity.setId(activityId);
        activity.setState(status);
        activityMapper.updateById(activity);
        log.info("更新活动状态：activityId={}, status={}", activityId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId) {
        // 先删除活动参与记录
        activityParticipationMapper.delete(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, activityId));
        
        // 再删除活动
        activityMapper.deleteById(activityId);
        log.info("删除活动：activityId={}", activityId);
    }

    // 数据统计
    @Override
    public DashboardVO getDashboardData() {
        DashboardVO dashboardVO = new DashboardVO();

        // 统计用户数据
        dashboardVO.setTotalUsers(userMapper.selectCount(null).intValue());
        dashboardVO.setActiveUsers(userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, 1)).intValue());
        dashboardVO.setPendingVerifyUsers(alumniProfileMapper.selectCount(new LambdaQueryWrapper<AlumniInfo>()
                .eq(AlumniInfo::getVerifyStatus, 0)).intValue());

        // 统计动态数据
        dashboardVO.setTotalPosts(postMapper.selectCount(null).intValue());
        dashboardVO.setPendingAuditPosts(postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 0)).intValue());

        // 统计活动数据
        dashboardVO.setTotalActivities(activityMapper.selectCount(null).intValue());
        dashboardVO.setOngoingActivities(activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getState, 1)).intValue());

        // 统计评论数据
        dashboardVO.setTotalComments(postInteractionMapper.selectCount(new LambdaQueryWrapper<PostInteraction>()
                .eq(PostInteraction::getType, 2)).intValue());
        dashboardVO.setPendingAuditComments(postInteractionMapper.selectCount(new LambdaQueryWrapper<PostInteraction>()
                .eq(PostInteraction::getType, 2)
                .eq(PostInteraction::getStatus, 0)).intValue());

        // 统计互动数据
        dashboardVO.setTotalInteractions(postInteractionMapper.selectCount(null).intValue());

        // 统计关注数据
        // 这里需要查看关注关系表的Mapper，暂时使用默认值
        dashboardVO.setTotalFollows(0);

        return dashboardVO;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总用户数
        statistics.put("totalUsers", userMapper.selectCount(null));

        // 按角色统计
        statistics.put("adminCount", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getRole, "ADMIN")));
        statistics.put("alumniCount", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getRole, "ALUMNI")));

        // 按状态统计
        statistics.put("activeUsers", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, 1)));
        statistics.put("disabledUsers", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, 0)));

        // 今日新增用户
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        statistics.put("todayNewUsers", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .ge(UserEntity::getCreatedAt, today)));

        return statistics;
    }

    @Override
    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总活动数
        statistics.put("totalActivities", activityMapper.selectCount(null));

        // 按状态统计
        statistics.put("pendingActivities", activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getState, 0)));
        statistics.put("ongoingActivities", activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getState, 1)));
        statistics.put("endedActivities", activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getState, 2)));
        statistics.put("cancelledActivities", activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getState, 3)));

        // 今日新增活动
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        statistics.put("todayNewActivities", activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .ge(Activity::getCreatedAt, today)));

        return statistics;
    }

    @Override
    public Map<String, Object> getInteractionStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总互动数
        statistics.put("totalInteractions", postInteractionMapper.selectCount(null));

        // 点赞数
        statistics.put("totalLikes", postInteractionMapper.selectCount(new LambdaQueryWrapper<PostInteraction>()
                .eq(PostInteraction::getType, 1)));

        // 评论数
        statistics.put("totalComments", postInteractionMapper.selectCount(new LambdaQueryWrapper<PostInteraction>()
                .eq(PostInteraction::getType, 2)));

        // 今日互动数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        statistics.put("todayInteractions", postInteractionMapper.selectCount(new LambdaQueryWrapper<PostInteraction>()
                .ge(PostInteraction::getCreatedAt, today)));

        return statistics;
    }
}
