package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.DTO.Responce.JwtResponse;
import com.example.demo.Entity.User;
import com.example.demo.Security.UserDetailsImpl;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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


    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user){
        return Result.success(userService.register(user));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        //return Result.success(userService.login(user.getUsername(),user.getPassword()));
//        User u = userService.login(user.getUsername(), user.getPassword());
//        return Result.success(u);

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

    @Operation(summary = "查询所有用户")
    @GetMapping
    public Result<?> getAll(){
        return Result.success(userService.getAll());
    }

    @Operation(summary = "按id修改用户")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id,@RequestBody User user){
        return Result.success(userService.update(id, user));
    }

    @Operation(summary = "按id删除用户")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/page")
//    @PreAuthorize("hasRole('ADMIN')")
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
}