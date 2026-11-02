package com.example.phuongstore.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long productId,
    String productCode,
    String productName,
    String productImageUrl,
    Integer quantity,
    Integer pickedQuantity,
    Integer shippedQuantity,
    BigDecimal unitPrice,
    BigDecimal costPrice,
    BigDecimal lineTotal,
    BigDecimal lineCostTotal,
    BigDecimal lineProfit
) {}
