package com.example.demo.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Schema(description = "订单实体")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "订单id")
    private Long orderId;

    @Schema(description = "菜品id")
    private Long menuId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "总价")
    private BigDecimal price;
}
