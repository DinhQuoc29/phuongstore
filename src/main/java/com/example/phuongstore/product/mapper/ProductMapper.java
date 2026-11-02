package com.example.phuongstore.product.mapper;

import com.example.phuongstore.product.dto.ProductRequest;
import com.example.phuongstore.product.dto.ProductResponse;
import com.example.phuongstore.product.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper thá»§ cĂ´ng cho Product.
 */
@Component
public class ProductMapper {

    /** Entity â†’ Response DTO */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getImageUrl(),
            product.getCreatedAt()
        );
    }

    /** Request DTO â†’ Entity má»›i (productCode Ä‘Æ°á»£c thiáº¿t láº­p riĂªng bá»Ÿi service) */
    public Product toEntity(ProductRequest request) {
        return Product.builder()
            .productName(request.productName().trim())
            .imageUrl(request.imageUrl())
            .build();
    }

    /** Cáº­p nháº­t entity hiá»‡n cĂ³ (productCode khĂ´ng thay Ä‘á»•i) */
    public void updateEntity(Product product, ProductRequest request) {
        product.setProductName(request.productName().trim());
        product.setImageUrl(request.imageUrl());
    }
}
