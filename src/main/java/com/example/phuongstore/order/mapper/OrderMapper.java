package com.example.phuongstore.order.mapper;

import com.example.phuongstore.order.dto.*;
import com.example.phuongstore.order.entity.Order;
import com.example.phuongstore.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {
        var product = item.getProduct();
        BigDecimal costPrice = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
        return new OrderItemResponse(
            item.getId(),
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getImageUrl(),
            item.getQuantity(),
            item.getPickedQuantity(),
            item.getShippedQuantity(),
            item.getUnitPrice(),
            costPrice,
            item.getLineTotal(),
            item.getLineCostTotal(),
            item.getLineProfit()
        );
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(this::toItemResponse)
            .toList();

        var customer = order.getCustomer();
        return new OrderResponse(
            order.getId(),
            customer.getId(),
            customer.getFullName(),
            customer.getPhoneNumber(),
            customer.getAddress(),
            customer.getFacebookUrl(),
            order.getOrderDate(),
            order.getNote(),
            order.getEngraving(),
            order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO,
            order.getTotalCostAmount() != null ? order.getTotalCostAmount() : BigDecimal.ZERO,
            order.getProfitAmount() != null ? order.getProfitAmount() : BigDecimal.ZERO,
            order.getDepositAmount() != null ? order.getDepositAmount() : BigDecimal.ZERO,
            order.getRemainingAmount() != null ? order.getRemainingAmount() : BigDecimal.ZERO,
            order.getPickupStatus(),
            order.getPaymentStatus(),
            order.getShippingStatus(),
            order.getCreatedAt(),
            itemResponses
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        var customer = order.getCustomer();
        var items = order.getItems();
        int totalQuantity      = items.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
        int totalPickedQuantity = items.stream().mapToInt(i -> i.getPickedQuantity() != null ? i.getPickedQuantity() : 0).sum();
        return new OrderSummaryResponse(
            order.getId(),
            customer.getId(),
            customer.getFullName(),
            customer.getPhoneNumber(),
            order.getOrderDate(),
            order.getNote(),
            order.getEngraving(),
            order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO,
            order.getTotalCostAmount() != null ? order.getTotalCostAmount() : BigDecimal.ZERO,
            order.getProfitAmount() != null ? order.getProfitAmount() : BigDecimal.ZERO,
            order.getDepositAmount() != null ? order.getDepositAmount() : BigDecimal.ZERO,
            order.getRemainingAmount() != null ? order.getRemainingAmount() : BigDecimal.ZERO,
            order.getPickupStatus(),
            order.getPaymentStatus(),
            order.getShippingStatus(),
            items.size(),
            totalQuantity,
            totalPickedQuantity
        );
    }
}
