package com.example.demo.Service;

import com.example.demo.DTO.Request.OrderRequest;
import com.example.demo.DTO.Responce.OrderResponse;
import com.example.demo.Entity.Menu;
import com.example.demo.Entity.Orders;
import com.example.demo.Entity.OrderItem;
import com.example.demo.Enums.OrderStatus;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Repository.OrderItemRepository;
import com.example.demo.Repository.OrdersRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;

    public OrderService(OrdersRepository ordersRepository,
                        OrderItemRepository orderItemRepository,
                        MenuRepository menuRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuRepository = menuRepository;
    }

    @Schema(description = "创建订单")
    @Transactional // 非常关键🔥
    public Orders createOrder(OrderRequest request) {

        BigDecimal total = BigDecimal.ZERO;

        // 1. 计算总价
        for (OrderRequest.Item item : request.getItems()) {
            Menu menu = menuRepository.findById(item.getMenuId())
                    .orElseThrow(() -> new RuntimeException("菜品不存在"));

            BigDecimal price = menu.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 2. 创建订单
        Orders orders = new Orders();
        orders.setUserId(request.getUserId());
        orders.setTotalPrice(total);
        orders.setStatus(OrderStatus.PENDING);

        orders = ordersRepository.save(orders);

        // 3. 创建订单明细
        for (OrderRequest.Item item : request.getItems()) {
            Menu menu = menuRepository.findById(item.getMenuId()).get();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orders.getId());
            orderItem.setMenuId(menu.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(menu.getPrice());

            orderItemRepository.save(orderItem);
        }

        return orders;
    }

    @Schema(description = "查询用户订单")
    public List<Orders> getOrdersByUserId(Long userId){
        return ordersRepository.findByUserId(userId);
    }

    @Schema(description = "查询订单详情")
    public OrderResponse getOrderDetail(Long orderId){
        Orders orders = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(orders.getId());
        orderResponse.setUserId(orders.getUserId());
        orderResponse.setTotalPrice(orders.getTotalPrice());
        orderResponse.setStatus(String.valueOf(orders.getStatus()));
        orderResponse.setCreateTime(orders.getCreateTime());

        List<OrderResponse.Item> itemVOList = new ArrayList<>();

        for(OrderItem item : items){
            Menu menu = menuRepository.findById(item.getMenuId())
                    .orElseThrow(() -> new RuntimeException("菜品不存在"));

            OrderResponse.Item itemVO = new OrderResponse.Item();
            itemVO.setMenuId(menu.getId());
            itemVO.setName(menu.getName());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setPrice(item.getPrice());

            itemVOList.add(itemVO);
        }

        orderResponse.setItems(itemVOList);
        return orderResponse;
    }

    @Schema(description = "更新订单状态")
    public Orders updateOrderStatus(Long orderId, OrderStatus newStatus){

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        OrderStatus current = order.getStatus();

        // 状态流转校验（非常重要🔥）
        if(current == OrderStatus.PENDING && newStatus == OrderStatus.ACCEPTED){
            order.setStatus(newStatus);
        } else if(current == OrderStatus.ACCEPTED && newStatus == OrderStatus.COOKING){
            order.setStatus(newStatus);
        } else if(current == OrderStatus.COOKING && newStatus == OrderStatus.FINISHED){
            order.setStatus(newStatus);
        } else {
            throw new RuntimeException("非法状态变更");
        }

        return ordersRepository.save(order);
    }

    @Schema(description = "查询全部订单")
    public List<Orders> getAllOrders(){
        return ordersRepository.findAll();
    }
}