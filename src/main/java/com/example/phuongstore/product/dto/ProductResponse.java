package com.example.phuongstore.product.dto;

import java.time.LocalDateTime;

/**
 * DTO tráº£ vá» thĂ´ng tin sáº£n pháº©m cho client.
 */
public record ProductResponse(
    Long id,
    String productCode,
    String productName,
    String imageUrl,
    LocalDateTime createdAt
) {}
