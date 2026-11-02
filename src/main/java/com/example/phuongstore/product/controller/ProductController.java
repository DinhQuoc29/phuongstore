package com.example.phuongstore.product.controller;

import com.example.phuongstore.common.dto.PageResponse;
import com.example.phuongstore.product.dto.ProductRequest;
import com.example.phuongstore.product.dto.ProductResponse;
import com.example.phuongstore.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /** GET /api/products?keyword=...&page=0&size=20 â€” TĂ¬m kiáº¿m sáº£n pháº©m cĂ³ phĂ¢n trang. */
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.search(keyword, page, size));
    }

    /** GET /api/products/all â€” Láº¥y toĂ n bá»™ danh sĂ¡ch (dĂ¹ng cho dropdown). */
    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    /** PUT /api/products/{id} â€” Cáº­p nháº­t sáº£n pháº©m. */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /** DELETE /api/products/{id} â€” XĂ³a sáº£n pháº©m. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
