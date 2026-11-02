package com.example.phuongstore.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

    @NotBlank(message = "Há» tĂªn khĂ¡ch hĂ ng khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 150, message = "Há» tĂªn khĂ´ng Ä‘Æ°á»£c vÆ°á»£t quĂ¡ 150 kĂ½ tá»±")
    String fullName,

    @Size(max = 500, message = "URL Facebook khĂ´ng Ä‘Æ°á»£c vÆ°á»£t quĂ¡ 500 kĂ½ tá»±")
    String facebookUrl,

    @Pattern(regexp = "^(\\+84|0)[0-9]{8,10}$", message = "Sá»‘ Ä‘iá»‡n thoáº¡i khĂ´ng há»£p lá»‡ (VD: 0901234567)")
    String phoneNumber,

    String address
) {}
