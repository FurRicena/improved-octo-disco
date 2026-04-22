package com.example.demo.Repository;

import com.example.demo.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    // 统计每个菜品的总销量（所有订单中该菜品的数量之和）
    @Query("SELECT oi.menuId, SUM(oi.quantity) as totalSold FROM OrderItem oi GROUP BY oi.menuId ORDER BY totalSold DESC")
    List<Object[]> getMenuSalesStatistics();

    @Query("SELECT oi.menuId, m.name, m.price, SUM(oi.quantity) " +
            "FROM OrderItem oi JOIN Menu m ON oi.menuId = m.id " +
            "GROUP BY oi.menuId, m.name, m.price " +
            "ORDER BY SUM(oi.quantity) DESC LIMIT 5")
    List<Object[]> getTop5MenuSales();
}
