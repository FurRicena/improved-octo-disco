package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.DTO.Responce.MenuSales;
import com.example.demo.Service.StatisticsService;
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

    @GetMapping("/menu-sales")
    public Result<List<MenuSales>> getMenuSales() {
        return Result.success(statisticsService.getMenuSalesStatistics());
    }
}
