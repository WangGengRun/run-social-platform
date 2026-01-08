package com.run.runsocialplatform.module.post.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态VO
 */
@Data
@Schema(description = "动态信息")
public class PostVO {

    @Schema(description = "动态ID")
    private Long id;

    @Schema(description = "发布者用户ID")
    private Long userId;

    @Schema(description = "发布者用户名")
    private String username;

    @Schema(description = "发布者真实姓名")
    private String realName;

    @Schema(description = "发布者头像")
    private String avatar;

    @Schema(description = "动态内容")
    private String content;

    @Schema(description = "图片URL（数据库字段，逗号分隔）")
    private String imageUrls;

    @Schema(description = "图片URL列表")
    private List<String> imageUrlList;

    @Schema(description = "可见性：0-公开, 1-仅校友, 2-仅自己")
    private Integer visibility;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked = false;

    @Schema(description = "是否已关注发布者")
    private Boolean isFollowed = false;

    @Schema(description = "是否是本人发布的")
    private Boolean isSelf = false;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

