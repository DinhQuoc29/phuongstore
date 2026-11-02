package com.example.phuongstore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdatePickedQuantityRequest(

    @NotNull(message = "Số lượng đã lấy không được để trống")
    @Min(value = 0, message = "Số lượng đã lấy không được âm")
    Integer pickedQuantity
) {}
