package com.run.runsocialplatform.module.admin.model.vo;

import lombok.Data;

@Data
public class DashboardVO {
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer pendingVerifyUsers;
    private Integer totalPosts;
    private Integer pendingAuditPosts;
    private Integer totalActivities;
    private Integer ongoingActivities;
    private Integer totalComments;
    private Integer pendingAuditComments;
    private Integer totalInteractions;
    private Integer totalFollows;
}
