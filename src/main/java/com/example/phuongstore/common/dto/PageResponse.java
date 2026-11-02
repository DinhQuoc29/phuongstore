package com.example.phuongstore.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper chuáº©n cho response phĂ¢n trang.
 * DĂ¹ng chung cho táº¥t cáº£ cĂ¡c API cĂ³ há»— trá»£ phĂ¢n trang.
 *
 * @param <T> Kiá»ƒu dá»¯ liá»‡u cá»§a tá»«ng pháº§n tá»­ trong trang
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
     * Factory method: táº¡o PageResponse tá»« Spring Data Page object.
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
