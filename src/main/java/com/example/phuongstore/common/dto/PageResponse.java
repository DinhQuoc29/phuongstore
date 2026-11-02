package com.example.phuongstore.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper chuẩn cho response phân trang.
 * Dùng chung cho tất cả các API có hỗ trợ phân trang.
 *
 * @param <T> Kiểu dữ liệu của từng phần tử trong trang
 */
public record PageResponse<T>(
    List<T> content,
    int pageNumber,       // 0-based (theo Spring convention)
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    /**
     * Factory method: tạo PageResponse từ Spring Data Page object.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
