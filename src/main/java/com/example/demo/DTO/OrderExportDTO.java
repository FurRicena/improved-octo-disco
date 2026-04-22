package com.example.demo.DTO;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderExportDTO {
    private Long id;
    private String username;
    private BigDecimal totalPrice;
    private String statusText;  // 转换后的中文状态
    private String createTime;
}