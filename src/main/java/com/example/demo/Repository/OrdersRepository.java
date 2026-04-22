package com.example.demo.Repository;

import com.example.demo.Entity.Orders;
import com.example.demo.Enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {

    List<Orders> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o WHERE o.status = :status")
    BigDecimal sumTotalPriceByStatus(@Param("status") OrderStatus status);

    @Query("SELECT FUNCTION('DATE', o.createTime) as date, COUNT(o), COALESCE(SUM(o.totalPrice), 0) " +
            "FROM Orders o WHERE o.createTime >= :startDate GROUP BY FUNCTION('DATE', o.createTime) ORDER BY date")
    List<Object[]> getLast7DaysOrderTrend(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o.status, COUNT(o) FROM Orders o GROUP BY o.status")
    List<Object[]> getOrderStatusDistribution();
}
