package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

// import java.util.List;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
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
        User u = userService.login(user.getUsername(), user.getPassword());
        return Result.success(u);
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
}