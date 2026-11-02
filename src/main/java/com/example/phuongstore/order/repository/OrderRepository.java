package com.example.phuongstore.order.repository;

import com.example.phuongstore.order.entity.Order;
import com.example.phuongstore.order.enums.PaymentStatus;
import com.example.phuongstore.order.enums.PickupStatus;
import com.example.phuongstore.order.enums.ShippingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Lấy đơn hàng kèm toàn bộ items và thông tin sản phẩm (1 query, tránh N+1).
     * Dùng cho trang chi tiết đơn hàng.
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.items oi
        LEFT JOIN FETCH oi.product
        LEFT JOIN FETCH o.customer
        WHERE o.id = :id
        """)
    Optional<Order> findByIdWithItemsAndCustomer(@Param("id") Long id);

    /**
     * Tìm kiếm đơn hàng có lọc nhiều tiêu chí, hỗ trợ phân trang.
     * Không fetch items (dùng cho list view).
     */
    @Query("""
        SELECT o FROM Order o
        JOIN o.customer c
        WHERE (:customerId IS NULL OR o.customer.id = :customerId)
          AND (:pickupStatus IS NULL OR o.pickupStatus = :pickupStatus)
          AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
          AND (:shippingStatus IS NULL OR o.shippingStatus = :shippingStatus)
          AND (:fromDate IS NULL OR o.orderDate >= :fromDate)
          AND (:toDate IS NULL OR o.orderDate <= :toDate)
          AND (
               :hasNote IS NULL
               OR (:hasNote = true AND o.note IS NOT NULL AND TRIM(o.note) != '')
               OR (:hasNote = false AND (o.note IS NULL OR TRIM(o.note) = ''))
          )
          AND (
               :hasEngraving IS NULL
               OR (:hasEngraving = true AND o.engraving IS NOT NULL AND TRIM(o.engraving) != '')
               OR (:hasEngraving = false AND (o.engraving IS NULL OR TRIM(o.engraving) = ''))
          )
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR c.phoneNumber LIKE CONCAT('%', :keyword, '%'))
        ORDER BY o.orderDate DESC, o.createdAt DESC
        """)
    Page<Order> searchOrders(
        @Param("customerId") Long customerId,
        @Param("pickupStatus") PickupStatus pickupStatus,
        @Param("paymentStatus") PaymentStatus paymentStatus,
        @Param("shippingStatus") ShippingStatus shippingStatus,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("hasNote") Boolean hasNote,
        @Param("hasEngraving") Boolean hasEngraving,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * Thống kê tổng doanh thu và tổng tiền lời của các đơn hàng đã thanh toán (PAID) trong khoảng thời gian.
     */
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0),
               COALESCE(SUM(o.profitAmount), 0),
               COUNT(o)
        FROM Order o
        WHERE o.paymentStatus = com.example.phuongstore.order.enums.PaymentStatus.PAID
          AND o.orderDate >= :startDate
          AND o.orderDate <= :endDate
        """)
    Object getMonthlyStatisticsRaw(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
