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
     * Láº¥y Ä‘Æ¡n hĂ ng kĂ¨m toĂ n bá»™ items vĂ  thĂ´ng tin sáº£n pháº©m (1 query, trĂ¡nh N+1).
     * DĂ¹ng cho trang chi tiáº¿t Ä‘Æ¡n hĂ ng.
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
     * TĂ¬m kiáº¿m Ä‘Æ¡n hĂ ng cĂ³ lá»c nhiá»u tiĂªu chĂ­, há»— trá»£ phĂ¢n trang.
     * KhĂ´ng fetch items (dĂ¹ng cho list view).
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
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
