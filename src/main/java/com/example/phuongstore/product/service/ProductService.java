package com.example.phuongstore.product.service;

import com.example.phuongstore.common.dto.PageResponse;
import com.example.phuongstore.common.exception.ResourceNotFoundException;
import com.example.phuongstore.product.dto.ProductRequest;
import com.example.phuongstore.product.dto.ProductResponse;
import com.example.phuongstore.product.entity.Product;
import com.example.phuongstore.product.mapper.ProductMapper;
import com.example.phuongstore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // ---------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String code = generateUniqueProductCode();

        Product product = Product.builder()
            .productCode(code)
            .productName(request.productName().trim())
            .imageUrl(request.imageUrl())
            .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    public PageResponse<ProductResponse> search(String keyword, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
            productRepository.searchProducts(keyword, pageable)
                .map(productMapper::toResponse)
        );
    }

    /** Láº¥y toĂ n bá»™ danh sĂ¡ch sáº£n pháº©m (dĂ¹ng cho dropdown khi thĂªm sáº£n pháº©m vĂ o Ä‘Æ¡n). */
    public List<ProductResponse> getAll() {
        return productRepository.findAll(Sort.by("productName").ascending())
            .stream()
            .map(productMapper::toResponse)
            .toList();
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        // productCode giá»¯ nguyĂªn, chá»‰ cáº­p nháº­t tĂªn vĂ  áº£nh
        productMapper.updateEntity(product, request);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sáº£n pháº©m", id);
        }
        productRepository.deleteById(id);
    }

    // ---------------------------------------------------------------
    // Internal helper
    // ---------------------------------------------------------------

    Product findOrThrow(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sáº£n pháº©m", id));
    }

    /**
     * Sinh mĂ£ sáº£n pháº©m ngáº«u nhiĂªn 6 sá»‘, Ä‘áº£m báº£o khĂ´ng trĂ¹ng láº·p.
     * VĂ­ dá»¥: SP-123456
     */
    private String generateUniqueProductCode() {
        String code;
        do {
            int randomNum = ThreadLocalRandom.current().nextInt(100000, 1000000);
            code = "SP-" + randomNum;
        } while (productRepository.existsByProductCode(code));
        return code;
    }
}
