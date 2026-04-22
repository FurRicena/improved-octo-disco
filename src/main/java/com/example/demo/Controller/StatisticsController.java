package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.DTO.DashboardData;
import com.example.demo.DTO.MenuSales;
import com.example.demo.Service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "满意度接口")
@RestController
@RequestMapping("/admin/statistics")
@PreAuthorize("hasAuthority('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "查询销售量")
    @GetMapping("/menu-sales")
    public Result<List<MenuSales>> getMenuSales() {
        return Result.success(statisticsService.getMenuSalesStatistics());
    }

    @Operation(summary = "查看仪表盘")
    @GetMapping("/dashboard")
    public Result<DashboardData> getDashboardData() {
        DashboardData data = new DashboardData();
        data.setTotalUsers(statisticsService.getTotalUserCount());
        data.setTotalOrders(statisticsService.getTotalOrderCount());
        data.setTotalSales(statisticsService.getTotalSales());
        data.setLast7DaysTrend(statisticsService.getLast7DaysOrderTrend());
        data.setTop5Menus(statisticsService.getTop5MenuSales());
        data.setStatusDistribution(statisticsService.getOrderStatusDistribution());
        return Result.success(data);
    }
}
