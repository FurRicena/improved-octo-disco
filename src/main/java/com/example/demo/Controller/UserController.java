package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.DTO.Responce.JwtResponse;
import com.example.demo.DTO.UserExportDTO;
import com.example.demo.Entity.User;
import com.example.demo.Security.UserDetailsImpl;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.ExcelExporter;
import com.example.demo.Utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// import java.util.List;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }


    /**
     * 用户注册
     *
     * @param user 用户注册信息（用户名、密码、角色等，密码为明文）
     * @return 包含注册成功用户对象的统一响应结果
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @Log("用户注册")
    public Result<User> register(@RequestBody User user){
        return Result.success(userService.register(user));
    }

    /**
     * 用户登录（使用 JWT 认证）
     * <p>验证用户名密码，认证通过后生成并返回 JWT Token，同时返回用户基本信息。</p>
     *
     * @param user 包含用户名和明文密码的请求体
     * @return 包含 JWT Token 及用户信息的统一响应结果（密码不返回）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @Log("用户登录")
    public Result<?> login(@RequestBody User user){
        // 1. 用 AuthenticationManager 验证用户名密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()));

        // 2. 验证通过，生成 JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 3. 获取用户详情
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 4. 返回 Token 和用户信息给前端（密码不要返回！）
        return Result.success(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole().name()
        ));
    }

    /**
     * 查询所有用户（仅限管理员）
     *
     * @return 包含所有用户列表的统一响应结果
     */
    @Operation(summary = "查询所有用户")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<?> getAll(){
        return Result.success(userService.getAll());
    }

    /**
     * 根据用户ID修改用户信息（仅限管理员）
     * <p>仅修改请求体中非空字段。</p>
     *
     * @param id   用户ID
     * @param user 包含待修改字段的用户对象
     * @return 包含更新后用户对象的统一响应结果
     */
    @Operation(summary = "按id修改用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("按id修改用户")
    public Result<User> update(@PathVariable Long id, @RequestBody User user){
        return Result.success(userService.update(id, user));
    }

    /**
     * 根据用户ID删除用户（仅限管理员）
     *
     * @param id 用户ID
     * @return 操作成功的统一响应结果（data 为 null）
     */
    @Operation(summary = "按id删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("按id删除用户")
    public Result<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success(null);
    }

    /**
     * 分页查询普通用户列表（仅限管理员）
     * <p>支持按用户名模糊搜索和注册时间范围过滤。</p>
     *
     * @param username  用户名（模糊匹配，可选）
     * @param startTime 注册开始时间（格式 yyyy-MM-dd，可选）
     * @param endTime   注册结束时间（格式 yyyy-MM-dd，可选）
     * @param pageNum   页码（默认1）
     * @param pageSize  每页条数（默认10）
     * @return 包含分页用户数据的统一响应结果
     */
    @Operation(summary = "分页查看所有用户")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Page<User>> getAdminUserPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<User> page = userService.getAdminUserPage(username, startTime, endTime, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 导出用户数据至Excel（仅限管理员）
     * <p>支持按用户名模糊过滤，导出符合条件的全部用户（不分页）。</p>
     * <p>导出成功时直接输出Excel文件流；失败时返回500错误及JSON格式错误信息。</p>
     *
     * @param username 用户名（模糊匹配，可选）
     * @param response HTTP响应对象，用于写入导出文件或错误信息
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void exportUsers(@RequestParam(required = false) String username,
                            HttpServletResponse response) {
        try {
            System.out.println("导出用户参数：username=" + username);
            List<UserExportDTO> exportData = userService.getUsersForExport(username);

            String[] fieldNames = {"id", "username", "role", "createTime"};
            String[] columnHeaders = {"用户ID", "用户名", "角色", "注册时间"};

            ExcelExporter.exportToExcel(exportData, "Users", columnHeaders, fieldNames, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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