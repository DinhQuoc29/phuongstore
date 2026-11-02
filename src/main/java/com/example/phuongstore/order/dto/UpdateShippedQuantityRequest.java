package com.example.phuongstore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateShippedQuantityRequest(
    @NotNull(message = "Số lượng đã gửi không được để trống")
    @Min(value = 0, message = "Số lượng đã gửi không được âm")
    Integer shippedQuantity
) {}
