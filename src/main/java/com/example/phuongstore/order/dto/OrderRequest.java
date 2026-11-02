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

    @NotNull(message = "ID khách hàng không được để trống")
    Long customerId,

    @NotNull(message = "Ngày đặt hàng không được để trống")
    LocalDate orderDate,

    String note,

    String engraving,

    @NotNull(message = "Tiền đặt cọc không được để trống")
    @PositiveOrZero(message = "Tiền đặt cọc không được âm")
    BigDecimal depositAmount,

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    List<OrderItemRequest> items
) {}
