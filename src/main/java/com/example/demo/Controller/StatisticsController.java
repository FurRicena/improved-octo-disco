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

    /**
     * 查询所有菜品的销售数量统计（用于销量排行榜或管理报表）
     *
     * @return 包含菜品销量统计列表的统一响应结果
     */
    @Operation(summary = "查询销售量")
    @GetMapping("/menu-sales")
    public Result<List<MenuSales>> getMenuSales() {
        return Result.success(statisticsService.getMenuSalesStatistics());
    }

    /**
     * 获取运营仪表盘数据，汇总系统核心指标
     * <p>返回数据包含：
     * <ul>
     *     <li>总用户数（仅普通用户）</li>
     *     <li>总订单数</li>
     *     <li>已完成订单总销售额</li>
     *     <li>近7天每日订单趋势（订单数及金额）</li>
     *     <li>热门菜品TOP5（按销量）</li>
     *     <li>订单状态分布（各状态订单数量）</li>
     * </ul>
     * </p>
     *
     * @return 包含仪表盘聚合数据的统一响应结果
     */
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
