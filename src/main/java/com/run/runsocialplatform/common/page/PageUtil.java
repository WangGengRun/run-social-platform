package com.run.runsocialplatform.common.page;


import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 */
public class PageUtil {

    /**
     * 创建MyBatis Plus分页对象
     */
    public static <T> Page<T> createPage(Integer pageNum, Integer pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 创建MyBatis Plus分页对象并设置排序
     */
    public static <T> Page<T> createPage(Integer pageNum, Integer pageSize,
                                         String sortField, String sortOrder) {
        Page<T> page = new Page<>(pageNum, pageSize);

        if (sortField != null && !sortField.trim().isEmpty()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(sortField);
            orderItem.setAsc("asc".equalsIgnoreCase(sortOrder));
            page.addOrder(orderItem);
        }

        return page;
    }

    /**
     * 转换分页数据的类型
     */
    public static <T, R> Page<R> convertPage(Page<T> sourcePage, Function<T, R> converter) {
        Page<R> targetPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        targetPage.setRecords(sourcePage.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList()));
        return targetPage;
    }

    /**
     * 计算总页数
     */
    public static int calculateTotalPages(long total, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 检查是否有下一页
     */
    public static boolean hasNextPage(int pageNum, long total, int pageSize) {
        return (pageNum - 1) * pageSize + pageSize < total;
    }

    /**
     * 检查是否有上一页
     */
    public static boolean hasPreviousPage(int pageNum) {
        return pageNum > 1;
    }
}