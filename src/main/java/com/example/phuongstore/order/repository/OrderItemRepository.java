package com.example.phuongstore.order.repository;

import com.example.phuongstore.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT oi FROM OrderItem oi
        JOIN FETCH oi.order o
        JOIN FETCH o.customer
        JOIN FETCH oi.product
        WHERE oi.id = :id
        """)
    Optional<OrderItem> findByIdWithOrderAndProduct(@Param("id") Long id);

    @Query("""
        SELECT oi FROM OrderItem oi
        JOIN FETCH oi.product p
        JOIN FETCH oi.order o
        JOIN FETCH o.customer c
        WHERE o.pickupStatus <> com.example.phuongstore.order.enums.PickupStatus.FULLY_PICKED
        ORDER BY p.productCode, c.fullName
        """)
    List<OrderItem> findAllPendingPickupItemsWithDetails();

    /** Cáº­p nháº­t trá»±c tiáº¿p picked_quantity. */
    @Modifying
    @Query("UPDATE OrderItem oi SET oi.pickedQuantity = :pickedQuantity WHERE oi.id = :id")
    int updatePickedQuantity(@Param("id") Long id, @Param("pickedQuantity") Integer pickedQuantity);

    /** Cáº­p nháº­t trá»±c tiáº¿p shipped_quantity. */
    @Modifying
    @Query("UPDATE OrderItem oi SET oi.shippedQuantity = :shippedQuantity WHERE oi.id = :id")
    int updateShippedQuantity(@Param("id") Long id, @Param("shippedQuantity") Integer shippedQuantity);

    /** Láº¥y táº¥t cáº£ items cá»§a má»™t Ä‘Æ¡n hĂ ng. */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
}
