package com.example.demo.Service;

import com.example.demo.DTO.OrderExportDTO;
import com.example.demo.DTO.Request.OrderRequest;
import com.example.demo.DTO.Responce.OrderResponse;
import com.example.demo.Entity.Menu;
import com.example.demo.Entity.Orders;
import com.example.demo.Entity.OrderItem;
import com.example.demo.Entity.User;
import com.example.demo.Enums.OrderStatus;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Repository.OrderItemRepository;
import com.example.demo.Repository.OrdersRepository;
import com.example.demo.Repository.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public OrderService(OrdersRepository ordersRepository,
                        OrderItemRepository orderItemRepository,
                        MenuRepository menuRepository, UserRepository userRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
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
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new RuntimeException("请登录"));

        Orders orders = new Orders();
//        orders.setUserId(request.getUserId());
        orders.setUser(user);
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

    @Schema(description = "查询用户订单(分页)")
    public Page<Orders> getUserOrdersByPage(Long userId, Integer pageNum, Integer pageSize, String status) {
        // 1. 参数默认值处理
        int currentPage = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int pageSizeVal = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100); // 限制最大100条
        // 分页参数：PageRequest 的页码从0开始，所以需要 -1
        Pageable pageable = PageRequest.of(currentPage - 1, pageSizeVal, Sort.by(Sort.Direction.ASC, "id"));

        // 2. 动态构建查询条件 (Specification)
        Specification<Orders> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 必须条件：userId
            predicates.add(cb.equal(root.get("userId"), userId));
            // 可选条件：status
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 3. 执行分页查询
        return ordersRepository.findAll(spec, pageable);
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
        orderResponse.setStatus(orders.getStatus());
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
    @Transactional
    public Orders updateOrderStatus(Long orderId, OrderStatus newStatus){

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        OrderStatus current = order.getStatus();

        // 状态流转校验
        boolean validTransition = false;


        // 状态流转校验（非常重要🔥）
        if(current == OrderStatus.PENDING && newStatus == OrderStatus.ACCEPTED){
            validTransition = true;
        } else if(current == OrderStatus.ACCEPTED && newStatus == OrderStatus.COOKING){
            validTransition = true;
        } else if(current == OrderStatus.COOKING && newStatus == OrderStatus.FINISHED){
            validTransition = true;
        }
        // 新增取消逻辑：允许从 PENDING、ACCEPTED、COOKING 取消
        else if (newStatus == OrderStatus.CANCELLED
                && (current == OrderStatus.PENDING
                || current == OrderStatus.ACCEPTED
                || current == OrderStatus.COOKING)) {
            validTransition = true;
        }

        if (!validTransition) {
            throw new RuntimeException("非法状态变更: 从 " + current + " 到 " + newStatus);
        }

        order.setStatus(newStatus);
        return ordersRepository.save(order);
    }

    @Schema(description = "查询全部订单")
    public List<Orders> getAllOrders(){
        return ordersRepository.findAll();
    }

    @Schema(description = "查询全部订单(分页)")
    public Page<Orders> getAdminOrdersPage(String username, OrderStatus status, Integer pageNum, Integer pageSize) {
        int currentPage = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Pageable pageable = PageRequest.of(currentPage - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        Specification<Orders> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(username)) {
                Join<Orders, User> userJoin = root.join("user");
                predicates.add(cb.like(userJoin.get("username"), "%" + username + "%"));
            }

            // 不能调用name方法
            if (status != null){
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return ordersRepository.findAll(spec, pageable);
    }

    public List<OrderExportDTO> getOrdersForExport(String username, String status) {
        // 复用之前的查询逻辑（不分页）
        Specification<Orders> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(username)) {
                Join<Orders, User> userJoin = root.join("user");
                predicates.add(cb.like(userJoin.get("username"), "%" + username + "%"));
            }

            // 不能调用name方法
            if (status != null){
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Orders> orders = ordersRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createTime"));
        return orders.stream().map(this::convertToExportDTO).collect(Collectors.toList());
    }

    private OrderExportDTO convertToExportDTO(Orders order) {
        OrderExportDTO dto = new OrderExportDTO();
        dto.setId(order.getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatusText(convertStatus(order.getStatus()));
        dto.setCreateTime(order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return dto;
    }

    private String convertStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> "待接单";
            case ACCEPTED -> "已接单";
            case COOKING -> "制作中";
            case FINISHED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }
}