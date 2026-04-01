package com.example.demo.Entity;

import com.example.demo.Enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Schema(description = "订单本体")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "总价")
    private BigDecimal totalPrice;

    @Schema(description = "订单状态")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING; // PENDING...

    @Schema(description = "创建时间")
    private LocalDateTime createTime = LocalDateTime.now();
}
