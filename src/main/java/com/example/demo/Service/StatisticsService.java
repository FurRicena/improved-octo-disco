package com.example.demo.Service;

import com.example.demo.DTO.DailyOrderTrend;
import com.example.demo.DTO.MenuSales;
import com.example.demo.Entity.Menu;
import com.example.demo.Enums.OrderStatus;
import com.example.demo.Enums.UserRole;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Repository.OrderItemRepository;
import com.example.demo.Repository.OrdersRepository;
import com.example.demo.Repository.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;

    public StatisticsService(OrderItemRepository orderItemRepository, MenuRepository menuRepository, UserRepository userRepository, OrdersRepository ordersRepository) {
        this.orderItemRepository = orderItemRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.ordersRepository = ordersRepository;
    }

    /**
     * 统计所有菜品的销售数量（按订单项汇总）
     *
     * @return 菜品销售统计列表，包含菜单ID、名称、单价和总销量
     */
    @Schema(description = "统计销量")
    public List<MenuSales> getMenuSalesStatistics() {
        List<Object[]> results = orderItemRepository.getMenuSalesStatistics();
        List<MenuSales> list = new ArrayList<>();
        for (Object[] row : results) {
            Long menuId = (Long) row[0];
            Long totalSold = ((Number) row[1]).longValue();
            Menu menu = menuRepository.findById(menuId).orElse(null);
            if (menu != null) {
                MenuSales dto = new MenuSales();
                dto.setMenuId(menuId);
                dto.setMenuName(menu.getName());
                dto.setPrice(menu.getPrice());
                dto.setTotalSold(totalSold);
                list.add(dto);
            }
        }
        return list;
    }

    /**
     * 获取普通用户（角色为 USER）的总数量
     *
     * @return 普通用户总数
     */
    @Schema(description = "获取用户总数")
    public long getTotalUserCount() {
        return userRepository.countByRole(UserRole.USER);
    }

    /**
     * 获取订单总数
     *
     * @return 订单总数量（包含所有状态）
     */
    @Schema(description = "获取订单总数")
    public long getTotalOrderCount() {
        return ordersRepository.count();
    }

    /**
     * 获取已完成订单的总销售额
     *
     * @return 已完成订单的销售额总和
     */
    @Schema(description = "获取总销售额（已完成）")
    public BigDecimal getTotalSales() {
        return ordersRepository.sumTotalPriceByStatus(OrderStatus.FINISHED);
    }

    /**
     * 获取近7天的订单趋势（每日订单数量及销售额）
     *
     * @return 包含近7天每日订单数及总金额的列表，按日期升序排列
     */
    @Schema(description = "获取近7天的订单趋势（每日订单数量及销售额）")
    public List<DailyOrderTrend> getLast7DaysOrderTrend() {
        List<Object[]> results = ordersRepository.getLast7DaysOrderTrend(LocalDateTime.now().minusDays(7).toLocalDate().atStartOfDay());
        // 转换为 DTO
        List<DailyOrderTrend> trendList = new ArrayList<>();
        for (Object[] row : results) {
            // 根据实际返回类型转换，注意 FUNCTION('DATE', ...) 返回的是 java.sql.Date
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Long orderCount = ((Number) row[1]).longValue();
            BigDecimal totalAmount = (BigDecimal) row[2];
            trendList.add(new DailyOrderTrend(date, orderCount, totalAmount));
        }
        return trendList;
    }

    /**
     * 获取销量最高的前5个菜品（热门菜品TOP5）
     *
     * @return 按销量降序排列的前5个菜品销售统计
     */
    @Schema(description = "获取销量最高的前5个菜品")
    public List<MenuSales> getTop5MenuSales() {
        List<Object[]> results = orderItemRepository.getTop5MenuSales();
        List<MenuSales> menuSales = new ArrayList<>();
        for (Object[] row : results) {
            Long menuId = ((Number) row[0]).longValue();
            String menuName = (String) row[1];
            BigDecimal price = (BigDecimal) row[2];
            Long totalSold = ((Number) row[3]).longValue();

            MenuSales sales = new MenuSales();
            sales.setMenuId(menuId);
            sales.setMenuName(menuName);
            sales.setPrice(price);
            sales.setTotalSold(totalSold);
            menuSales.add(sales);
        }
        return menuSales;
    }

    /**
     * 统计订单状态分布（各状态对应的订单数量）
     *
     * @return Map，键为订单状态名称（如 "PENDING", "FINISHED"），值为对应订单数量
     */
    @Schema(description = "统计订单状态分布")
    public Map<String, Long> getOrderStatusDistribution() {
        List<Object[]> results = ordersRepository.getOrderStatusDistribution();
        Map<String, Long> distribution = new HashMap<>();
        for (Object[] row : results) {
            // row[0] 是 OrderStatus 枚举实例
            String status = row[0].toString();  // 或者 ((OrderStatus) row[0]).name()
            Long count = ((Number) row[1]).longValue();
            distribution.put(status, count);
        }
        return distribution;
    }

}
