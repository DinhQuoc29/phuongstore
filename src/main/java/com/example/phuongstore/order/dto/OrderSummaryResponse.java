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
    BigDecimal totalAmount,
    BigDecimal depositAmount,
    BigDecimal remainingAmount,
    PickupStatus pickupStatus,
    PaymentStatus paymentStatus,
    ShippingStatus shippingStatus,
    int itemCount,             // Tá»•ng sá»‘ dĂ²ng sáº£n pháº©m
    int totalQuantity,         // Tá»•ng SL Ä‘áº·t (sum of quantity)
    int totalPickedQuantity    // Tá»•ng SL Ä‘Ă£ láº¥y (sum of pickedQuantity)
) {}
