package com.example.phuongstore.product.repository;

import com.example.phuongstore.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    /**
     * TĂ¬m kiáº¿m sáº£n pháº©m theo tĂªn hoáº·c mĂ£.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.createdAt DESC
        """)
    Page<Product> searchProducts(
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
