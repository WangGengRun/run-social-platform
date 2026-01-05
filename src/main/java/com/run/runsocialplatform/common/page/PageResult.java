package com.run.runsocialplatform.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果包装类")
public class PageResult<T> {

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "每页大小")
    private Integer pageSize;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Schema(description = "数据列表")
    private List<T> list;

    /**
     * 从MyBatis Plus的Page对象转换
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return PageResult.<T>builder()
                .pageNum((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .total(page.getTotal())
                .totalPages((int) page.getPages())
                .list(page.getRecords())
                .build();
    }

    /**
     * 构建空分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return PageResult.<T>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(0L)
                .totalPages(0)
                .list(List.of())
                .build();
    }
}