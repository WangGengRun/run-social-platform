package com.run.runsocialplatform.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
@Schema(description = "分页参数基类")
public class PageParam {

    @Schema(description = "当前页码，从1开始", defaultValue = "1", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小，最大100", defaultValue = "10", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;

    /**
     * 获取偏移量（用于数据库查询）
     */
    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}