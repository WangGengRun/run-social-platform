package com.run.runsocialplatform.module.post.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 发布动态DTO
 */
@Data
@Schema(description = "发布动态请求参数")
public class PostCreateDTO {

    @NotBlank(message = "动态内容不能为空")
    @Schema(description = "动态内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "可见性：0-公开, 1-仅校友, 2-仅自己", example = "0")
    @NotNull(message = "可见性不能为空")
    private Integer visibility = 0;
}

