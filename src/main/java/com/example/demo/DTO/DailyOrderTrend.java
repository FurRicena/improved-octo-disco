package com.example.demo.DTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyOrderTrend {
    private LocalDate date;          // 日期
    private Long orderCount;         // 订单数量
    private BigDecimal totalAmount;  // 总金额（可选）

    public DailyOrderTrend(LocalDate date, Long orderCount, BigDecimal totalAmount) {
        this.date = date;
        this.orderCount = orderCount;
        this.totalAmount = totalAmount;
    }
}