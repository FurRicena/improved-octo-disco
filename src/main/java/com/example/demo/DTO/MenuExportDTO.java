package com.example.demo.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuExportDTO {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String statusText;  // "上架" / "下架"
    private String createTime;   // 格式化后的时间
}
