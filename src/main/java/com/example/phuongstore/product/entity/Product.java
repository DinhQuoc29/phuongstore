package com.example.phuongstore.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Thá»±c thá»ƒ Sáº£n pháº©m.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_code", columnList = "product_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MĂ£ sáº£n pháº©m ná»™i bá»™, unique, dĂ¹ng Ä‘á»ƒ tra cá»©u nhanh khi nháº·t hĂ ng. */
    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 300)
    private String productName;

    /** URL hĂ¬nh áº£nh sáº£n pháº©m (link áº£nh máº¡ng xĂ£ há»™i hoáº·c storage). */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
