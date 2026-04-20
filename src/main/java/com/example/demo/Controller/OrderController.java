package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.DTO.Request.OrderRequest;
import com.example.demo.DTO.Responce.AdminOrderResponse;
import com.example.demo.DTO.Responce.OrderResponse;
import com.example.demo.Entity.Orders;
import com.example.demo.Enums.OrderStatus;
import com.example.demo.Service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单接口")
@RestController
@RequestMapping("/order")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Orders> create(@RequestBody OrderRequest request){
        return Result.success(orderService.createOrder(request));
    }

    // 不分页
    @Operation(summary = "查询用户订单列表")
    @GetMapping("/user/{userId}")
    public Result<List<Orders>> getUserOrders(@PathVariable Long userId){
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    // 新增：分页查询接口
    @Operation(summary = "分页查询用户订单列表")
    @GetMapping("/user/{userId}/page")
    public Result<Page<Orders>> getUserOrdersByPage(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {

        Page<Orders> page = orderService.getUserOrdersByPage(userId, pageNum, pageSize, status);
        return Result.success(page);
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderResponse> getOrderDetail(@PathVariable Long id){
        return Result.success(orderService.getOrderDetail(id));
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Orders> updateStatus(@PathVariable Long id,
                                       @RequestParam OrderStatus status){
        return Result.success(orderService.updateOrderStatus(id, status));
    }

    @Operation(summary = "查询所有订单")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<List<Orders>> getAllOrders(){
        return Result.success(orderService.getAllOrders());
    }

    // 新增,分页查询所有订单
    @Operation(summary = "分页查询所有订单")
    @GetMapping("/adminpage")
    @PreAuthorize("hasAuthority('ADMIN')")
//    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AdminOrderResponse>> getAdminOrdersPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<Orders> page = orderService.getAdminOrdersPage(username, status, pageNum, pageSize);
        Page<AdminOrderResponse> dtoPage = page.map(order -> {
            AdminOrderResponse dto = new AdminOrderResponse();
            BeanUtils.copyProperties(order, dto);
            // 需要用户名：从订单关联的用户获取
            dto.setUsername(order.getUser().getUsername());
            return dto;
        });
        return Result.success(dtoPage);
    }
}
