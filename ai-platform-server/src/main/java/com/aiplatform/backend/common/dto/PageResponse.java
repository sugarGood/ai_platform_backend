package com.aiplatform.backend.common.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 通用分页响应封装，用于将 MyBatis Plus 的分页结果转换为前端友好的 JSON 格式。
 *
 * @param data  当前页的数据列表
 * @param total 符合条件的记录总数
 * @param page  当前页码（从 1 开始）
 * @param size  每页记录数
 * @param <T>   数据元素类型
 */
public record PageResponse<T>(List<T> data, long total, int page, int size) {

    /**
     * 将 MyBatis Plus 分页对象转换为 {@link PageResponse}，同时对每条记录执行类型转换。
     *
     * @param iPage     MyBatis Plus 分页查询结果
     * @param converter 实体到 DTO 的转换函数
     * @param <E>       源实体类型
     * @param <T>       目标 DTO 类型
     * @return 分页响应对象
     */
    public static <E, T> PageResponse<T> from(IPage<E> iPage, java.util.function.Function<E, T> converter) {
        List<T> data = iPage.getRecords().stream().map(converter).toList();
        return new PageResponse<>(data, iPage.getTotal(), (int) iPage.getCurrent(), (int) iPage.getSize());
    }

    /**
     * 将 MyBatis Plus 分页对象直接转换为 {@link PageResponse}，无需类型转换。
     *
     * @param iPage MyBatis Plus 分页查询结果
     * @param <T>   数据元素类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> from(IPage<T> iPage) {
        return new PageResponse<>(iPage.getRecords(), iPage.getTotal(), (int) iPage.getCurrent(), (int) iPage.getSize());
    }
}
