package com.example.phuongstore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatisticsResponse {
    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private long paidOrderCount;
}

