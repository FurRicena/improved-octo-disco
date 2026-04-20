package com.example.demo.Service;

import com.example.demo.DTO.Responce.MenuSales;
import com.example.demo.Entity.Menu;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Repository.OrderItemRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;

    public StatisticsService(OrderItemRepository orderItemRepository, MenuRepository menuRepository) {
        this.orderItemRepository = orderItemRepository;
        this.menuRepository = menuRepository;
    }

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
}
