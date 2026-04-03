package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.DTO.Request.OrderRequest;
import com.example.demo.DTO.Responce.OrderResponse;
import com.example.demo.Entity.Orders;
import com.example.demo.Enums.OrderStatus;
import com.example.demo.Service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单接口")
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Orders> create(@RequestBody OrderRequest request){
        return Result.success(orderService.createOrder(request));
    }

    @Operation(summary = "查询用户订单列表")
    @GetMapping("/user/{userId}")
    public Result<List<Orders>> getUserOrders(@PathVariable Long userId){
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderResponse> getOrderDetail(@PathVariable Long id){
        return Result.success(orderService.getOrderDetail(id));
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Orders> updateStatus(@PathVariable Long id,
                                       @RequestBody OrderStatus request){
        return Result.success(orderService.updateOrderStatus(id, request));
    }

    @Operation(summary = "查询所有订单")
    @GetMapping
    public Result<List<Orders>> getAllOrders(){
        return Result.success(orderService.getAllOrders());
    }
}
