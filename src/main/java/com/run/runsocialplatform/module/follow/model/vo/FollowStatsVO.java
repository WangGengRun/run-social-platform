package com.run.runsocialplatform.module.follow.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "关注统计信息")
public class FollowStatsVO {

    @Schema(description = "关注数（我关注的）")
    private Long followingCount;

    @Schema(description = "粉丝数（关注我的）")
    private Long followerCount;

    @Schema(description = "互相关注数")
    private Long mutualFollowCount;
}

