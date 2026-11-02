package com.example.phuongstore.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO để tạo mới hoặc cập nhật sản phẩm.
 * productCode được sinh tự động bởi backend.
 */
public record ProductRequest(

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 300, message = "Tên sản phẩm không được vượt quá 300 ký tự")
    String productName,

    @Size(max = 1000, message = "URL hình ảnh không được vượt quá 1000 ký tự")
    String imageUrl
) {}
