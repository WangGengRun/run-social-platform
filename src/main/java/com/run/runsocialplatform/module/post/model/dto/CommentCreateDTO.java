package com.run.runsocialplatform.module.post.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论DTO
 */
@Data
@Schema(description = "创建评论请求参数")
public class CommentCreateDTO {

    @NotNull(message = "动态ID不能为空")
    @Schema(description = "动态ID")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID，0或null表示直接评论动态", example = "0")
    private Long parentId = 0L;
}

