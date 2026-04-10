package com.example.demo.DTO.Responce;

import com.example.demo.Enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderResponse {
    private Long id;
    private String username;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createTime;
}
