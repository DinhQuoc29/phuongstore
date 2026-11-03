package com.example.phuongstore.order.service;

import com.example.phuongstore.common.dto.PageResponse;
import com.example.phuongstore.common.exception.BusinessException;
import com.example.phuongstore.common.exception.ResourceNotFoundException;
import com.example.phuongstore.customer.entity.Customer;
import com.example.phuongstore.customer.repository.CustomerRepository;
import com.example.phuongstore.order.dto.*;
import com.example.phuongstore.order.entity.Order;
import com.example.phuongstore.order.entity.OrderItem;
import com.example.phuongstore.order.enums.PaymentStatus;
import com.example.phuongstore.order.enums.PickupStatus;
import com.example.phuongstore.order.enums.ShippingStatus;
import com.example.phuongstore.order.mapper.OrderMapper;
import com.example.phuongstore.order.repository.OrderItemRepository;
import com.example.phuongstore.order.repository.OrderRepository;
import com.example.phuongstore.product.entity.Product;
import com.example.phuongstore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Validate & load customer
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", request.customerId()));

        // 2. Xây dựng Order cơ bản (mặc định chưa gửi)
        Order order = Order.builder()
            .customer(customer)
            .orderDate(request.orderDate() != null ? request.orderDate() : LocalDate.now())
            .note(request.note())
            .engraving(request.engraving())
            .depositAmount(request.depositAmount() != null ? request.depositAmount() : BigDecimal.ZERO)
            .shippingStatus(ShippingStatus.NOT_SHIPPED)
            .build();

        // 3. Thêm từng OrderItem
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", itemReq.productId()));

            BigDecimal costPrice = itemReq.costPrice() != null ? itemReq.costPrice() : BigDecimal.ZERO;
            String colorCode = itemReq.colorCode() != null && !itemReq.colorCode().isBlank() 
                ? itemReq.colorCode().trim() : null;

            OrderItem item = OrderItem.builder()
                .product(product)
                .colorCode(colorCode)
                .quantity(itemReq.quantity())
                .unitPrice(itemReq.unitPrice())
                .costPrice(costPrice)
                .pickedQuantity(0)
                .shippedQuantity(0)
                .build();

            order.addItem(item);
        }

        // 4. Tính toán tự động (total, cost, profit, remaining, pickupStatus)
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);

        // 5. Lưu (cascade sẽ lưu cả items)
        Order saved = orderRepository.save(order);

        // 6. Load lại với JOIN FETCH để trả về response đầy đủ
        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(saved.getId()).orElseThrow()
        );
    }

    // ================================================================
    // READ
    // ================================================================

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItemsAndCustomer(id)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", id));
        return orderMapper.toResponse(order);
    }

    public PageResponse<OrderSummaryResponse> searchOrders(
        Long customerId,
        PickupStatus pickupStatus,
        PaymentStatus paymentStatus,
        ShippingStatus shippingStatus,
        LocalDate fromDate,
        LocalDate toDate,
        Boolean hasNote,
        Boolean hasEngraving,
        String keyword,
        int page, int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<Order> resultPage = orderRepository.searchOrders(
            customerId, pickupStatus, paymentStatus, shippingStatus,
            fromDate, toDate, hasNote, hasEngraving, keyword, pageable
        );
        return PageResponse.from(resultPage.map(orderMapper::toSummaryResponse));
    }

    // ================================================================
    // UPDATE — Deposit (Cập nhật tiền đặt cọc)
    // ================================================================

    @Transactional
    public OrderResponse updateDeposit(Long orderId, UpdateDepositRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);

        if (request.depositAmount().compareTo(order.getTotalAmount()) > 0) {
            throw new BusinessException(
                "Tiền đặt cọc (%s₫) không được lớn hơn tổng tiền đơn hàng (%s₫)"
                    .formatted(request.depositAmount().toPlainString(),
                               order.getTotalAmount().toPlainString())
            );
        }

        order.setDepositAmount(request.depositAmount());
        recalculateOrderFinancials(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // UPDATE — Shipping Status (Người dùng chủ động chuyển trạng thái)
    // ================================================================

    @Transactional
    public OrderResponse updateShippingStatus(Long orderId, UpdateShippingStatusRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);
        order.setShippingStatus(request.shippingStatus());
        orderRepository.save(order);
        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // UPDATE — Picked quantity (nhặt hàng theo item)
    // ================================================================

    @Transactional
    public OrderItemResponse updatePickedQuantity(Long orderItemId, UpdatePickedQuantityRequest request) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiết đơn hàng", orderItemId));

        int newPicked = request.pickedQuantity();

        // Validation: không lấy nhiều hơn số đã đặt, không được âm
        if (newPicked > item.getQuantity()) {
            throw new BusinessException(
                "Số lượng đã lấy (%d) không được vượt quá số lượng đặt (%d) cho sản phẩm '%s'"
                    .formatted(newPicked, item.getQuantity(), item.getProduct().getProductName())
            );
        }

        orderItemRepository.updatePickedQuantity(orderItemId, newPicked);
        item.setPickedQuantity(newPicked);

        recalculatePickupStatusById(item.getOrder().getId());

        return orderMapper.toItemResponse(item);
    }

    @Transactional
    public OrderItemResponse updateShippedQuantity(Long orderItemId, UpdateShippedQuantityRequest request) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiết đơn hàng", orderItemId));

        int newShipped = request.shippedQuantity();

        if (newShipped > item.getQuantity()) {
            throw new BusinessException(
                "Số lượng đã gửi (%d) không được vượt quá số lượng đặt (%d) cho sản phẩm '%s'"
                    .formatted(newShipped, item.getQuantity(), item.getProduct().getProductName())
            );
        }

        orderItemRepository.updateShippedQuantity(orderItemId, newShipped);
        item.setShippedQuantity(newShipped);

        return orderMapper.toItemResponse(item);
    }

    // ================================================================
    // UPDATE — Payment Status
    // ================================================================

    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);
        order.setPaymentStatus(request.paymentStatus());
        orderRepository.save(order);
        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // BUSINESS LOGIC: Recalculate
    // ================================================================

    void recalculateOrderFinancials(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal costPrice = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;

            total = total.add(unitPrice.multiply(qty));
            totalCost = totalCost.add(costPrice.multiply(qty));
        }

        order.setTotalAmount(total);
        order.setTotalCostAmount(totalCost);
        order.setProfitAmount(total.subtract(totalCost));

        BigDecimal deposit = order.getDepositAmount() != null ? order.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal remaining = total.subtract(deposit);
        order.setRemainingAmount(remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining);

        if (order.getPaymentStatus() == null || order.getPaymentStatus() == PaymentStatus.UNPAID) {
            if (deposit.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }
    }

    void recalculatePickupStatus(Order order) {
        List<OrderItem> items = order.getItems();
        if (items.isEmpty()) return;

        int totalQty = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int totalPicked = items.stream().mapToInt(i -> i.getPickedQuantity() != null ? i.getPickedQuantity() : 0).sum();

        order.setPickupStatus(computePickupStatus(totalQty, totalPicked));
    }

    void recalculatePickupStatusById(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) return;

        int totalQty = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int totalPicked = items.stream().mapToInt(i -> i.getPickedQuantity() != null ? i.getPickedQuantity() : 0).sum();

        PickupStatus newStatus = computePickupStatus(totalQty, totalPicked);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));
        order.setPickupStatus(newStatus);
        orderRepository.save(order);
    }

    private PickupStatus computePickupStatus(int totalQty, int totalPicked) {
        if (totalPicked == 0) {
            return PickupStatus.NOT_PICKED;
        } else if (totalPicked >= totalQty) {
            return PickupStatus.FULLY_PICKED;
        } else {
            return PickupStatus.PARTIALLY_PICKED;
        }
    }

    // ================================================================
    // UPDATE — Item CRUD trong đơn hàng
    // ================================================================

    @Transactional
    public OrderResponse updateOrderItem(Long orderItemId, UpdateOrderItemRequest request) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiết đơn hàng", orderItemId));

        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", request.productId()));

        item.setProduct(product);
        item.setColorCode(request.colorCode() != null && !request.colorCode().isBlank() 
            ? request.colorCode().trim() : null);
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        if (request.costPrice() != null) {
            item.setCostPrice(request.costPrice());
        }

        if (item.getPickedQuantity() > request.quantity()) {
            item.setPickedQuantity(request.quantity());
        }

        orderItemRepository.save(item);

        Long orderId = item.getOrder().getId();
        Order order = findOrderWithItemsOrThrow(orderId);
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    @Transactional
    public OrderResponse addOrderItem(Long orderId, OrderItemRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);

        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", request.productId()));

        BigDecimal costPrice = request.costPrice() != null ? request.costPrice() : BigDecimal.ZERO;
        String colorCode = request.colorCode() != null && !request.colorCode().isBlank() 
            ? request.colorCode().trim() : null;

        OrderItem item = OrderItem.builder()
            .product(product)
            .colorCode(colorCode)
            .quantity(request.quantity())
            .unitPrice(request.unitPrice())
            .costPrice(costPrice)
            .pickedQuantity(0)
            .shippedQuantity(0)
            .build();

        order.addItem(item);
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // DELETE — Xóa dòng sản phẩm khỏi đơn hàng
    // ================================================================

    @Transactional
    public OrderResponse deleteOrderItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiết đơn hàng", orderItemId));

        Long orderId = item.getOrder().getId();
        Order order = findOrderWithItemsOrThrow(orderId);

        if (order.getItems().size() <= 1) {
            throw new BusinessException("Đơn hàng phải có ít nhất một sản phẩm.");
        }

        order.getItems().removeIf(i -> i.getId().equals(orderItemId));
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // DELETE — Xóa toàn bộ đơn hàng
    // ================================================================

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Đơn hàng", id);
        }
        orderRepository.deleteById(id);
    }

    private Order findOrderWithItemsOrThrow(Long orderId) {
        return orderRepository.findByIdWithItemsAndCustomer(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));
    }

    @Transactional(readOnly = true)
    public MonthlyStatisticsResponse getMonthlyStatistics(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Object result = orderRepository.getMonthlyStatisticsRaw(startDate, endDate);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        long paidOrderCount = 0L;

        if (result != null) {
            Object[] row = (result instanceof Object[]) ? (Object[]) result : new Object[]{result};
            if (row.length > 0 && row[0] != null) {
                totalRevenue = new BigDecimal(row[0].toString());
            }
            if (row.length > 1 && row[1] != null) {
                totalProfit = new BigDecimal(row[1].toString());
            }
            if (row.length > 2 && row[2] != null) {
                paidOrderCount = ((Number) row[2]).longValue();
            }
        }

        return MonthlyStatisticsResponse.builder()
            .year(year)
            .month(month)
            .totalRevenue(totalRevenue)
            .totalProfit(totalProfit)
            .paidOrderCount(paidOrderCount)
            .build();
    }
}
