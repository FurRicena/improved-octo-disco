package com.example.demo.DTO.Responce;

import com.example.demo.Enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createTime;

    private List<Item> items;

    @Data
    public static class Item {
        private Long menuId;
        private String name;
        private Integer quantity;
        private BigDecimal price;
    }
}
