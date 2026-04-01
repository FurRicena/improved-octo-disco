package com.example.demo.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Schema(description = "菜品实体")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "菜品id")
    private Long id;

    @Schema(description = "菜品名字")
    private String name;

    @Schema(description = "菜品价格")
    private BigDecimal price;

    @Schema(description = "菜品类别")
    private String category;

    @Schema(description = "菜品介绍")
    private String description;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "菜品状态")
    private Integer status = 1; // 1=上架, 0=下架

    @Schema(description = "菜品创建时间")
    private LocalDateTime createTime = LocalDateTime.now();
}
