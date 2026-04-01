package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Schema(description = "用户注册")
    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Schema(description = "用户登录")
    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(()->new RuntimeException("用户名或密码错误"));
    }

    @Schema(description = "查询所有用户")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Schema(description = "按id修该用户")
    public User update(Long id, User user) {
        User exist = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getPassword() != null) exist.setPassword(user.getPassword());
        if (user.getRole() != null) exist.setRole(user.getRole());
        if (user.getUsername() != null) exist.setUsername(user.getUsername());
        return userRepository.save(exist);
    }

    @Schema(description = "按id删除用户")
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.deleteById(id);
    }
}