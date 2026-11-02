package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.ShippingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderRequest(

    @NotNull(message = "ID khĂ¡ch hĂ ng khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    Long customerId,

    @NotNull(message = "NgĂ y Ä‘áº·t hĂ ng khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    LocalDate orderDate,

    String note,

    @NotNull(message = "Tiá»n Ä‘áº·t cá»c khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @PositiveOrZero(message = "Tiá»n Ä‘áº·t cá»c khĂ´ng Ä‘Æ°á»£c Ă¢m")
    BigDecimal depositAmount,

    @NotEmpty(message = "ÄÆ¡n hĂ ng pháº£i cĂ³ Ă­t nháº¥t 1 sáº£n pháº©m")
    @Valid
    List<OrderItemRequest> items
) {}
