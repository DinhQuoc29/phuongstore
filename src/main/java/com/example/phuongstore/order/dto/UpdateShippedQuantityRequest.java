package com.example.phuongstore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateShippedQuantityRequest(
    @NotNull(message = "Sá»‘ lÆ°á»£ng Ä‘Ă£ gá»­i khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 0, message = "Sá»‘ lÆ°á»£ng Ä‘Ă£ gá»­i khĂ´ng Ä‘Æ°á»£c Ă¢m")
    Integer shippedQuantity
) {}
