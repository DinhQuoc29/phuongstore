package com.example.phuongstore.product.mapper;

import com.example.phuongstore.product.dto.ProductRequest;
import com.example.phuongstore.product.dto.ProductResponse;
import com.example.phuongstore.product.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper thủ công cho Product.
 */
@Component
public class ProductMapper {

    /** Entity → Response DTO */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getImageUrl(),
            product.getCreatedAt()
        );
    }

    /** Request DTO → Entity mới (productCode được thiết lập riêng bởi service) */
    public Product toEntity(ProductRequest request) {
        return Product.builder()
            .productName(request.productName().trim())
            .imageUrl(request.imageUrl())
            .build();
    }

    /** Cập nhật entity hiện có (productCode không thay đổi) */
    public void updateEntity(Product product, ProductRequest request) {
        product.setProductName(request.productName().trim());
        product.setImageUrl(request.imageUrl());
    }
}
