package com.example.phuongstore.order.controller;

import com.example.phuongstore.common.dto.PageResponse;
import com.example.phuongstore.order.dto.*;
import com.example.phuongstore.order.enums.PaymentStatus;
import com.example.phuongstore.order.enums.PickupStatus;
import com.example.phuongstore.order.enums.ShippingStatus;
import com.example.phuongstore.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> searchOrders(
        @RequestParam(required = false) Long customerId,
        @RequestParam(required = false) PickupStatus pickupStatus,
        @RequestParam(required = false) PaymentStatus paymentStatus,
        @RequestParam(required = false) ShippingStatus shippingStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) Boolean hasNote,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.searchOrders(
            customerId, pickupStatus, paymentStatus, shippingStatus,
            fromDate, toDate, hasNote, keyword, page, size
        ));
    }

    @PatchMapping("/{id}/deposit")
    public ResponseEntity<OrderResponse> updateDeposit(
        @PathVariable Long id,
        @Valid @RequestBody UpdateDepositRequest request
    ) {
        return ResponseEntity.ok(orderService.updateDeposit(id, request));
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePaymentStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, request));
    }

    @PatchMapping("/{id}/shipping-status")
    public ResponseEntity<OrderResponse> updateShippingStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateShippingStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updateShippingStatus(id, request));
    }

    @PatchMapping("/items/{orderItemId}/picked-quantity")
    public ResponseEntity<OrderItemResponse> updatePickedQuantity(
        @PathVariable Long orderItemId,
        @Valid @RequestBody UpdatePickedQuantityRequest request
    ) {
        return ResponseEntity.ok(orderService.updatePickedQuantity(orderItemId, request));
    }

    @PatchMapping("/items/{orderItemId}/shipped-quantity")
    public ResponseEntity<OrderItemResponse> updateShippedQuantity(
        @PathVariable Long orderItemId,
        @Valid @RequestBody UpdateShippedQuantityRequest request
    ) {
        return ResponseEntity.ok(orderService.updateShippedQuantity(orderItemId, request));
    }

    @PatchMapping("/items/{orderItemId}")
    public ResponseEntity<OrderResponse> updateOrderItem(
        @PathVariable Long orderItemId,
        @Valid @RequestBody UpdateOrderItemRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderItem(orderItemId, request));
    }


    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addOrderItem(
        @PathVariable Long orderId,
        @Valid @RequestBody OrderItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.addOrderItem(orderId, request));
    }

    @DeleteMapping("/items/{orderItemId}")
    public ResponseEntity<OrderResponse> deleteOrderItem(@PathVariable Long orderItemId) {
        return ResponseEntity.ok(orderService.deleteOrderItem(orderItemId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
