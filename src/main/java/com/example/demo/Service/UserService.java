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

    /**
     * 用户注册
     *
     * @param user 用户注册信息（包含用户名、密码、角色等，密码为明文）
     * @return 注册成功后的用户对象（密码已加密）
     * @throws RuntimeException 如果用户名已存在
     */
    @Schema(description = "用户注册")
    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功后的用户对象
     * @throws RuntimeException 如果用户名或密码错误
     */
    @Schema(description = "用户登录")
    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(()->new RuntimeException("用户名或密码错误"));
    }

    /**
     * 查询所有用户
     *
     * @return 所有用户的列表
     */
    @Schema(description = "查询所有用户")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * 根据用户ID修改用户信息（仅修改非空字段）
     *
     * @param id   用户ID
     * @param user 包含待修改字段的用户对象（密码、角色、用户名可按需设置）
     * @return 修改后的用户对象
     * @throws RuntimeException 如果指定ID的用户不存在
     */
    @Schema(description = "按id修该用户")
    public User update(Long id, User user) {
        User exist = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getPassword() != null) exist.setPassword(user.getPassword());
        if (user.getRole() != null) exist.setRole(user.getRole());
        if (user.getUsername() != null) exist.setUsername(user.getUsername());
        return userRepository.save(exist);
    }

    /**
     * 根据用户ID删除用户
     *
     * @param id 用户ID
     * @throws RuntimeException 如果指定ID的用户不存在
     */
    @Schema(description = "按id删除用户")
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.deleteById(id);
    }

    /**
     * 分页并动态条件查询普通用户（角色为 USER）
     *
     * @param username  用户名（模糊匹配，可选）
     * @param startTime 创建时间起始（格式 yyyy-MM-dd，可选）
     * @param endTime   创建时间结束（格式 yyyy-MM-dd，可选）
     * @param pageNum   页码（从1开始，默认为1）
     * @param pageSize  每页条数（默认10，最大100）
     * @return 分页后的普通用户数据
     */
    @Schema(description = "分页分类查询用户")
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

    /**
     * 导出符合条件的普通用户列表（用于导出Excel等）
     *
     * @param username 用户名（模糊匹配，可选）
     * @return 用户导出DTO列表（包含格式化后的创建时间）
     */
    @Schema(description = "导出用户列表")
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

    /**
     * 将 User 实体转换为导出用的 UserExportDTO
     *
     * @param user 用户实体（必须包含 id, username, role, createTime）
     * @return 导出DTO，createTime 格式化为 "yyyy-MM-dd HH:mm:ss"
     */
    private UserExportDTO convertToExportDTO(User user) {
        UserExportDTO dto = new UserExportDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setCreateTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return dto;
    }
}