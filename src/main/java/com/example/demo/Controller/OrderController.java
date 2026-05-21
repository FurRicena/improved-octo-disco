package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.DTO.OrderExportDTO;
import com.example.demo.DTO.Request.OrderRequest;
import com.example.demo.DTO.Responce.AdminOrderResponse;
import com.example.demo.DTO.Responce.OrderResponse;
import com.example.demo.Entity.Orders;
import com.example.demo.Enums.OrderStatus;
import com.example.demo.Service.OrderService;
import com.example.demo.Utils.ExcelExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
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


    /**
     * 创建订单
     *
     * @param request 订单请求对象（包含用户ID及菜品列表）
     * @return 包含创建成功订单对象的统一响应结果
     */
    @Operation(summary = "创建订单")
    @PostMapping
    @Log("创建订单")
    public Result<Orders> create(@RequestBody OrderRequest request){
        return Result.success(orderService.createOrder(request));
    }

    /**
     * 查询指定用户的所有订单（不分页）
     *
     * @param userId 用户ID
     * @return 包含该用户订单列表的统一响应结果
     */
    @Operation(summary = "查询用户订单列表")
    @GetMapping("/user/{userId}")
    public Result<List<Orders>> getUserOrders(@PathVariable Long userId){
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    /**
     * 分页查询指定用户的订单列表，支持按订单状态过滤
     *
     * @param userId   用户ID
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param status   订单状态（可选，如 PENDING、FINISHED）
     * @return 包含分页订单数据的统一响应结果
     */
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

    /**
     * 查询订单详情（包含订单信息及所有菜品明细）
     *
     * @param id 订单ID
     * @return 包含订单详情响应对象的统一响应结果
     */
    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderResponse> getOrderDetail(@PathVariable Long id){
        return Result.success(orderService.getOrderDetail(id));
    }

    /**
     * 更新订单状态（仅限管理员）
     *
     * @param id     订单ID
     * @param status 目标订单状态（如 ACCEPTED、COOKING、FINISHED、CANCELLED）
     * @return 包含更新后订单对象的统一响应结果
     */
    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
//    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("更新订单状态")
    public Result<Orders> updateStatus(@PathVariable Long id,
                                       @RequestParam OrderStatus status){
        return Result.success(orderService.updateOrderStatus(id, status));
    }

    /**
     * 查询所有订单（不分页，仅限管理员）
     *
     * @return 包含所有订单列表的统一响应结果
     */
    @Operation(summary = "查询所有订单")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<List<Orders>> getAllOrders(){
        return Result.success(orderService.getAllOrders());
    }

    /**
     * 分页查询所有订单（仅限管理员），支持按用户名模糊搜索和订单状态过滤
     *
     * @param username 用户名（模糊匹配，可选）
     * @param status   订单状态（可选）
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @return 包含分页订单DTO的统一响应结果（每个订单包含用户名信息）
     */
    @Operation(summary = "分页查询所有订单")
    @GetMapping("/adminpage")
    @PreAuthorize("hasAuthority('ADMIN')")
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

    /**
     * 导出订单数据至Excel（仅限管理员）
     * <p>支持按用户名和订单状态过滤，导出符合条件的全部订单（不分页）。</p>
     * <p>导出成功时直接输出Excel文件流；失败时返回500错误及JSON格式错误信息。</p>
     *
     * @param username 用户名（模糊匹配，可选）
     * @param status   订单状态（可选，字符串形式）
     * @param response HTTP响应对象，用于写入导出文件或错误信息
     */
    @Operation(summary = "导出订单数据")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void exportOrders(@RequestParam(required = false) String username,
                             @RequestParam(required = false) String status,
                             HttpServletResponse response) {
        try {
            System.out.println("导出订单参数：username=" + username + ", status=" + status);
            // 获取数据（不分页，获取全部符合条件的订单）
            List<OrderExportDTO> exportData = orderService.getOrdersForExport(username, status);

            // 定义导出字段和列头
            String[] fieldNames = {"id", "username", "totalPrice", "statusText", "createTime"};
            String[] columnHeaders = {"订单ID", "用户名", "总金额(元)", "状态", "下单时间"};

            ExcelExporter.exportToExcel(exportData, "Orders", columnHeaders, fieldNames, response);
        }catch (Exception e){
            // 打印完整的错误堆栈到控制台（关键步骤）
            e.printStackTrace();  // 或者 System.err.println("导出失败: " + e.getMessage()); e.printStackTrace();

            // 重置 response（清除可能已写入的内容）
            response.reset();
            // 设置响应类型为 JSON
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500

            // 构造 JSON 错误信息
            String errorJson = String.format("{\"code\":500,\"msg\":\"导出失败: %s\"}", e.getMessage());
            try (PrintWriter writer = response.getWriter()) {
                writer.write(errorJson);
                writer.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
