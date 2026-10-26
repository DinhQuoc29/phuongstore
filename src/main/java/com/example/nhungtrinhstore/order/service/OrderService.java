package com.example.nhungtrinhstore.order.service;

import com.example.nhungtrinhstore.common.dto.PageResponse;
import com.example.nhungtrinhstore.common.exception.BusinessException;
import com.example.nhungtrinhstore.common.exception.ResourceNotFoundException;
import com.example.nhungtrinhstore.customer.entity.Customer;
import com.example.nhungtrinhstore.customer.repository.CustomerRepository;
import com.example.nhungtrinhstore.order.dto.*;
import com.example.nhungtrinhstore.order.entity.Order;
import com.example.nhungtrinhstore.order.entity.OrderItem;
import com.example.nhungtrinhstore.order.enums.PaymentStatus;
import com.example.nhungtrinhstore.order.enums.PickupStatus;
import com.example.nhungtrinhstore.order.enums.ShippingStatus;
import com.example.nhungtrinhstore.order.mapper.OrderMapper;
import com.example.nhungtrinhstore.order.repository.OrderItemRepository;
import com.example.nhungtrinhstore.order.repository.OrderRepository;
import com.example.nhungtrinhstore.product.entity.Product;
import com.example.nhungtrinhstore.product.repository.ProductRepository;
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
            .orElseThrow(() -> new ResourceNotFoundException("KhĂ¡ch hĂ ng", request.customerId()));

        // 2. XĂ¢y dá»±ng Order cÆ¡ báº£n
        Order order = Order.builder()
            .customer(customer)
            .orderDate(request.orderDate() != null ? request.orderDate() : LocalDate.now())
            .note(request.note())
            .depositAmount(request.depositAmount() != null ? request.depositAmount() : BigDecimal.ZERO)
            .build();

        // 3. ThĂªm tá»«ng OrderItem
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Sáº£n pháº©m", itemReq.productId()));

            OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(itemReq.quantity())
                .unitPrice(itemReq.unitPrice())
                .pickedQuantity(0)
                .build();

            order.addItem(item);
        }

        // 4. TĂ­nh toĂ¡n tá»± Ä‘á»™ng (total, remaining, statuses)
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);

        // 5. LÆ°u (cascade sáº½ lÆ°u cáº£ items)
        Order saved = orderRepository.save(order);

        // 6. Load láº¡i vá»›i JOIN FETCH Ä‘á»ƒ tráº£ vá» response Ä‘áº§y Ä‘á»§
        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(saved.getId()).orElseThrow()
        );
    }

    // ================================================================
    // READ
    // ================================================================

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItemsAndCustomer(id)
            .orElseThrow(() -> new ResourceNotFoundException("ÄÆ¡n hĂ ng", id));
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
        String keyword,
        int page, int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<Order> resultPage = orderRepository.searchOrders(
            customerId, pickupStatus, paymentStatus, shippingStatus,
            fromDate, toDate, hasNote, keyword, pageable
        );
        return PageResponse.from(resultPage.map(orderMapper::toSummaryResponse));
    }

    // ================================================================
    // UPDATE â€” Deposit (Cáº­p nháº­t tiá»n Ä‘áº·t cá»c)
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

        BigDecimal total = order.getItems().stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        BigDecimal deposit = request.depositAmount();
        BigDecimal remaining = total.subtract(deposit).max(BigDecimal.ZERO);

        if (total.compareTo(BigDecimal.ZERO) > 0 && deposit.compareTo(total) >= 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setRemainingAmount(BigDecimal.ZERO);
        } else {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setRemainingAmount(remaining);
        }
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // UPDATE â€” Shipping Status
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

    @Transactional
    public OrderItemResponse updatePickedQuantity(Long orderItemId, UpdatePickedQuantityRequest request) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiáº¿t Ä‘Æ¡n hĂ ng", orderItemId));

        int newPicked = request.pickedQuantity();

        // Validation: khĂ´ng nháº·t nhiá»u hÆ¡n sá»‘ Ä‘Ă£ Ä‘áº·t
        if (newPicked > item.getQuantity()) {
            throw new BusinessException(
                "Sá»‘ lÆ°á»£ng nháº·t (%d) khĂ´ng Ä‘Æ°á»£c vÆ°á»£t quĂ¡ sá»‘ lÆ°á»£ng Ä‘áº·t (%d) cho sáº£n pháº©m '%s'"
                    .formatted(newPicked, item.getQuantity(), item.getProduct().getProductName())
            );
        }

        // Cáº­p nháº­t trá»±c tiáº¿p qua JPQL UPDATE (khĂ´ng load cáº£ collection)
        if (newPicked < item.getShippedQuantity()) {
            throw new BusinessException(
                "Số lượng đã lấy (%d) không được nhỏ hơn số lượng đã gửi (%d) cho sản phẩm '%s'"
                    .formatted(newPicked, item.getShippedQuantity(), item.getProduct().getProductName())
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
                "Số lượng gửi (%d) không được vượt quá số lượng đặt (%d) cho sản phẩm '%s'"
                    .formatted(newShipped, item.getQuantity(), item.getProduct().getProductName())
            );
        }

        orderItemRepository.updateShippedQuantity(orderItemId, newShipped);
        item.setShippedQuantity(newShipped);

        // Tái tính ShippingStatus của đơn hàng cha
        recalculateShippingStatusById(item.getOrder().getId());

        return orderMapper.toItemResponse(item);
    }

    // ================================================================
    // DELETE
    // ================================================================

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Đơn hàng", id);
        }
        orderRepository.deleteById(id);
    }

    void recalculateOrderFinancials(Order order) {
        // total = SUM(qty * unitPrice) trên tất cả items
        BigDecimal total = order.getItems().stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        BigDecimal deposit = order.getDepositAmount() != null
            ? order.getDepositAmount() : BigDecimal.ZERO;

        BigDecimal remaining = total.subtract(deposit).max(BigDecimal.ZERO);

        if (order.getPaymentStatus() == PaymentStatus.PAID
            || (total.compareTo(BigDecimal.ZERO) > 0 && deposit.compareTo(total) >= 0)) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setRemainingAmount(BigDecimal.ZERO);
        } else {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setRemainingAmount(remaining);
        }
    }

    void recalculatePickupStatus(Order order) {
        List<OrderItem> items = order.getItems();
        if (items.isEmpty()) return;

        int totalQty    = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int totalPicked = items.stream().mapToInt(OrderItem::getPickedQuantity).sum();

        order.setPickupStatus(computePickupStatus(totalQty, totalPicked));
    }

    void recalculatePickupStatusById(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) return;

        int totalQty    = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int totalPicked = items.stream().mapToInt(OrderItem::getPickedQuantity).sum();

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
    // UPDATE — Payment Status (cập nhật trạng thái thanh toán)
    // ================================================================

    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);
        order.setPaymentStatus(request.paymentStatus());
        if (request.paymentStatus() == PaymentStatus.PAID) {
            order.setRemainingAmount(BigDecimal.ZERO);
        } else {
            BigDecimal deposit = order.getDepositAmount() != null ? order.getDepositAmount() : BigDecimal.ZERO;
            order.setRemainingAmount(order.getTotalAmount().subtract(deposit).max(BigDecimal.ZERO));
        }
        orderRepository.save(order);
        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // UPDATE — Order Item (chinh sa san pham/so luong/don gia)
    // ================================================================

    @Transactional
    public OrderResponse updateOrderItem(Long orderItemId, UpdateOrderItemRequest request) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiet don hang", orderItemId));

        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("San pham", request.productId()));

        int newQty = request.quantity();
        int newPicked  = Math.min(item.getPickedQuantity(),  newQty);
        int newShipped = Math.min(item.getShippedQuantity(), newPicked);

        item.setProduct(product);
        item.setQuantity(newQty);
        item.setUnitPrice(request.unitPrice());
        item.setPickedQuantity(newPicked);
        item.setShippedQuantity(newShipped);
        orderItemRepository.save(item);

        Order order = findOrderWithItemsOrThrow(item.getOrder().getId());
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);
        recalculateShippingStatus(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(order.getId()).orElseThrow()
        );
    }

    // ================================================================
    // CREATE — Them san pham vao don hang dang co
    // ================================================================

    @Transactional
    public OrderResponse addOrderItem(Long orderId, OrderItemRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("San pham", request.productId()));

        OrderItem item = OrderItem.builder()
            .product(product)
            .quantity(request.quantity())
            .unitPrice(request.unitPrice())
            .pickedQuantity(0)
            .shippedQuantity(0)
            .build();

        order.addItem(item);
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);
        recalculateShippingStatus(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    // ================================================================
    // DELETE — Xoa dong san pham khoi don hang
    // ================================================================

    @Transactional
    public OrderResponse deleteOrderItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findByIdWithOrderAndProduct(orderItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Chi tiet don hang", orderItemId));

        Long orderId = item.getOrder().getId();
        Order order = findOrderWithItemsOrThrow(orderId);

        if (order.getItems().size() <= 1) {
            throw new BusinessException("Don hang phai co it nhat mot san pham.");
        }

        order.getItems().removeIf(i -> i.getId().equals(orderItemId));
        recalculateOrderFinancials(order);
        recalculatePickupStatus(order);
        recalculateShippingStatus(order);
        orderRepository.save(order);

        return orderMapper.toResponse(
            orderRepository.findByIdWithItemsAndCustomer(orderId).orElseThrow()
        );
    }

    /** Tinh lai shippingStatus tu items da load trong memory. */
    void recalculateShippingStatus(Order order) {
        java.util.List<OrderItem> items = order.getItems();
        if (items.isEmpty()) return;
        int totalQty     = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int totalShipped = items.stream().mapToInt(i -> i.getShippedQuantity() != null ? i.getShippedQuantity() : 0).sum();
        order.setShippingStatus(computeShippingStatus(totalQty, totalShipped));
    }
    void recalculateShippingStatusById(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) return;

        int totalQty = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int totalShipped = items.stream().mapToInt(i -> i.getShippedQuantity() != null ? i.getShippedQuantity() : 0).sum();

        ShippingStatus newStatus = computeShippingStatus(totalQty, totalShipped);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));
        order.setShippingStatus(newStatus);
        orderRepository.save(order);
    }

    private ShippingStatus computeShippingStatus(int totalQty, int totalShipped) {
        if (totalShipped == 0) {
            return ShippingStatus.NOT_SHIPPED;
        } else if (totalShipped >= totalQty) {
            return ShippingStatus.SHIPPED;
        } else {
            return ShippingStatus.PARTIALLY_SHIPPED;
        }
    }

    private Order findOrderWithItemsOrThrow(Long orderId) {
        return orderRepository.findByIdWithItemsAndCustomer(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("ÄÆ¡n hĂ ng", orderId));
    }
}


