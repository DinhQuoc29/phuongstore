package com.example.phuongstore.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO Ä‘á»ƒ táº¡o má»›i hoáº·c cáº­p nháº­t sáº£n pháº©m.
 * productCode Ä‘Æ°á»£c sinh tá»± Ä‘á»™ng bá»Ÿi backend.
 */
public record ProductRequest(

    @NotBlank(message = "TĂªn sáº£n pháº©m khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 300, message = "TĂªn sáº£n pháº©m khĂ´ng Ä‘Æ°á»£c vÆ°á»£t quĂ¡ 300 kĂ½ tá»±")
    String productName,

    @Size(max = 1000, message = "URL hĂ¬nh áº£nh khĂ´ng Ä‘Æ°á»£c vÆ°á»£t quĂ¡ 1000 kĂ½ tá»±")
    String imageUrl
) {}
