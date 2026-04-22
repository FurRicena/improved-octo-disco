package com.example.demo.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuSales {
    private Long menuId;
    private String menuName;
    private BigDecimal price;
    private Long totalSold; // 总销量
}
