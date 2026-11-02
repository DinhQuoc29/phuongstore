package com.example.phuongstore.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
    Long id,
    String fullName,
    String facebookUrl,
    String phoneNumber,
    String address,
    LocalDateTime createdAt
) {}
