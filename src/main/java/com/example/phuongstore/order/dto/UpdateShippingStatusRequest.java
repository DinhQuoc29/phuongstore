package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.ShippingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShippingStatusRequest(

    @NotNull(message = "Tráº¡ng thĂ¡i váº­n chuyá»ƒn khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    ShippingStatus shippingStatus
) {}
