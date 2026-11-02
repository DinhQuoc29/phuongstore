package com.example.phuongstore.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Thực thể Sản phẩm.
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

    /** Mã sản phẩm nội bộ, unique, dùng để tra cứu nhanh khi lấy hàng. */
    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 300)
    private String productName;

    /** URL hình ảnh sản phẩm (link ảnh mạng xã hội hoặc storage). */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
