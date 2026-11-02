package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
    @NotNull(message = "Trạng thái thanh toán không được để trống")
    PaymentStatus paymentStatus
) {}
