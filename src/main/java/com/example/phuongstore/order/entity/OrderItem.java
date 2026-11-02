package com.example.phuongstore.order.entity;

import com.example.phuongstore.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "picked_quantity", nullable = false)
    @Builder.Default
    private Integer pickedQuantity = 0;

    @Column(name = "shipped_quantity", nullable = false)
    @Builder.Default
    private Integer shippedQuantity = 0;

    /** Đơn giá bán */
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 0)
    private BigDecimal unitPrice;

    /** Giá gốc (giá vốn nhập vào) */
    @Column(name = "cost_price", precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Transient
    public BigDecimal getLineTotal() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Transient
    public BigDecimal getLineCostTotal() {
        if (quantity == null || costPrice == null) return BigDecimal.ZERO;
        return costPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Transient
    public BigDecimal getLineProfit() {
        return getLineTotal().subtract(getLineCostTotal());
    }
}
