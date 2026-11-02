package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
    @NotNull(message = "Tráº¡ng thĂ¡i thanh toĂ¡n khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    PaymentStatus paymentStatus
) {}
