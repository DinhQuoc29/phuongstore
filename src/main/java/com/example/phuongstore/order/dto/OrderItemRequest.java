package com.example.phuongstore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(

    @NotNull(message = "ID sản phẩm không được để trống")
    Long productId,

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    Integer quantity,

    @NotNull(message = "Đơn giá bán không được để trống")
    @Positive(message = "Đơn giá bán phải lớn hơn 0")
    BigDecimal unitPrice,

    BigDecimal costPrice
) {}
