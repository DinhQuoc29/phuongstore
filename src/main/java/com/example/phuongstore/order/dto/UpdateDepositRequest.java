package com.example.phuongstore.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateDepositRequest(

    @NotNull(message = "Tiá»n Ä‘áº·t cá»c khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @PositiveOrZero(message = "Tiá»n Ä‘áº·t cá»c khĂ´ng Ä‘Æ°á»£c Ă¢m")
    BigDecimal depositAmount
) {}
