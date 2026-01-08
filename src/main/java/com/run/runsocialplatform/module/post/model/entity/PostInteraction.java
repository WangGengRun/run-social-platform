package com.run.runsocialplatform.module.post.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 动态互动实体类（点赞和评论）
 */
@Data
@TableName("post_interaction")
public class PostInteraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 动态ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型：1-点赞, 2-评论
     */
    private Integer type;

    /**
     * 评论内容，点赞时为空
     */
    private String content;

    /**
     * 父评论ID，0表示直接评论动态
     */
    private Long parentId;

    /**
     * 状态：0-删除, 1-正常
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

