package com.run.runsocialplatform.module.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.module.admin.model.dto.UserQueryDTO;
import com.run.runsocialplatform.module.admin.model.vo.*;

import java.util.Map;

public interface AdminService {

    // 用户管理
    IPage<UserManageVO> getUserList(Integer pageNum, Integer pageSize, UserQueryDTO queryDTO);
    UserDetailVO getUserDetail(Long userId);
    void updateUserStatus(Long userId, Integer status);
    void updateUserRole(Long userId, String role);

    // 内容审核
    IPage<PostAuditVO> getPostList(Integer pageNum, Integer pageSize, Integer status, String keyword);
    void auditPost(Long postId, Integer status, String reason);
    IPage<CommentAuditVO> getCommentList(Integer pageNum, Integer pageSize, Integer status, String keyword);
    void auditComment(Long commentId, Integer status, String reason);

    // 活动管理
    IPage<ActivityManageVO> getActivityList(Integer pageNum, Integer pageSize, Integer status, String keyword);
    void updateActivityStatus(Long activityId, Integer status);
    void deleteActivity(Long activityId);

    // 数据统计
    DashboardVO getDashboardData();
    Map<String, Object> getUserStatistics();
    Map<String, Object> getActivityStatistics();
    Map<String, Object> getInteractionStatistics();
}
