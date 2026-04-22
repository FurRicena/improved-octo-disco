package com.example.demo.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardData {

    private Long totalUsers;                          // 总用户数
    private Long totalOrders;                         // 总订单数
    private BigDecimal totalSales;                    // 总销售额（已完成订单）
    private List<DailyOrderTrend> last7DaysTrend;  // 近7天每日订单趋势
    private List<MenuSales> top5Menus;             // 热销菜品TOP5
    private Map<String, Long> statusDistribution;     // 订单状态分布（如 PENDING: 3, FINISHED: 20）
}
