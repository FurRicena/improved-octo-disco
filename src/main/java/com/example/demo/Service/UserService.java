package com.example.demo.Service;

import com.example.demo.DTO.UserExportDTO;
import com.example.demo.Entity.User;
import com.example.demo.Enums.UserRole;
import com.example.demo.Repository.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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

    public Page<User> getAdminUserPage(String username, String startTime, String endTime, Integer pageNum, Integer pageSize) {
        int currentPage = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Pageable pageable = PageRequest.of(currentPage - 1, size, Sort.by(Sort.Direction.ASC, "id"));

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 只查询普通用户
            predicates.add(cb.equal(root.get("role"), "USER"));
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (StringUtils.hasText(startTime)) {
                LocalDateTime start = LocalDate.parse(startTime).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start));
            }
            if (StringUtils.hasText(endTime)) {
                LocalDateTime end = LocalDate.parse(endTime).atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable);
    }


    // UserService.java
    public List<UserExportDTO> getUsersForExport(String username) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 只导出普通用户（可选，如果只想导 USER）
            predicates.add(cb.equal(root.get("role"), "USER"));
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<User> users = userRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createTime"));
        return users.stream().map(this::convertToExportDTO).collect(Collectors.toList());
    }

    private UserExportDTO convertToExportDTO(User user) {
        UserExportDTO dto = new UserExportDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setCreateTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return dto;
    }
}