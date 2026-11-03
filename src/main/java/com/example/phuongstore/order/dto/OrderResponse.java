package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.PaymentStatus;
import com.example.phuongstore.order.enums.PickupStatus;
import com.example.phuongstore.order.enums.ShippingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,

    // --- Customer info (inline, không cần gọi thêm API) ---
    Long customerId,
    String customerName,
    String customerPhone,
    String customerAddress,
    String customerFacebookUrl,

    LocalDate orderDate,
    String note,
    String engraving,

    // --- Financials ---
    BigDecimal totalAmount,
    BigDecimal totalCostAmount,
    BigDecimal profitAmount,
    BigDecimal depositAmount,
    BigDecimal remainingAmount,

    // --- Statuses ---
    PickupStatus pickupStatus,
    PaymentStatus paymentStatus,
    ShippingStatus shippingStatus,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    // --- Items ---
    List<OrderItemResponse> items
) {}
