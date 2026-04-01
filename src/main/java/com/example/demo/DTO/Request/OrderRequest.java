package com.example.demo.DTO.Request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private List<Item> items;
    @Data
    public static class Item {
        private Long menuId;
        private Integer quantity;
    }
}