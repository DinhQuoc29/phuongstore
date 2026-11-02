package com.example.phuongstore.order.dto;

import com.example.phuongstore.order.enums.ShippingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShippingStatusRequest(

    @NotNull(message = "Trạng thái vận chuyển không được để trống")
    ShippingStatus shippingStatus
) {}
