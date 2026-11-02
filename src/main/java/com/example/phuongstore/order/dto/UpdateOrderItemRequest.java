package com.example.phuongstore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateOrderItemRequest(
    @NotNull(message = "ID sáº£n pháº©m khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    Long productId,

    @NotNull(message = "Sá»‘ lÆ°á»£ng khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 1, message = "Sá»‘ lÆ°á»£ng pháº£i Ă­t nháº¥t lĂ  1")
    Integer quantity,

    @NotNull(message = "ÄÆ¡n giĂ¡ khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Positive(message = "ÄÆ¡n giĂ¡ pháº£i lá»›n hÆ¡n 0")
    BigDecimal unitPrice
) {}
