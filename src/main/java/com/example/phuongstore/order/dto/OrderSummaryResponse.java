package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.PaymentStatus;
import com.example.phuongstore.order.enums.PickupStatus;
import com.example.phuongstore.order.enums.ShippingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderSummaryResponse(
    Long id,
    Long customerId,
    String customerName,
    String customerPhone,
    LocalDate orderDate,
    String note,
    String engraving,
    BigDecimal totalAmount,
    BigDecimal totalCostAmount,
    BigDecimal profitAmount,
    BigDecimal depositAmount,
    BigDecimal remainingAmount,
    PickupStatus pickupStatus,
    PaymentStatus paymentStatus,
    ShippingStatus shippingStatus,
    int itemCount,             // Tổng số dòng sản phẩm
    int totalQuantity,         // Tổng SL đặt (sum of quantity)
    int totalPickedQuantity    // Tổng SL đã lấy (sum of pickedQuantity)
) {}
