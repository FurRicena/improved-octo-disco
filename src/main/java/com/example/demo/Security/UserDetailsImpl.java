package com.example.demo.Security;

import com.example.demo.Entity.User;
import com.example.demo.Enums.UserRole;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private UserRole role;
    private LocalDateTime createTime;

    // 构造器（全参或从 User 构建）
    public UserDetailsImpl(Long id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * 从 User 实体构建 UserDetailsImpl 实例的静态工厂方法
     *
     * @param user 用户实体对象（包含 id、username、password、role）
     * @return UserDetailsImpl 对象，用于 Spring Security 认证和授权
     */
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }

    /**
     * 获取用户授予的权限集合
     * <p>将用户的角色（role）转换为 Spring Security 的 GrantedAuthority 对象，
     * 通常权限名称会添加 "ROLE_" 前缀，此处根据实际存储格式直接使用角色名。</p>
     *
     * @return 包含单个权限（用户角色）的集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将 role 转换为 GrantedAuthority（通常加 "ROLE_" 前缀，根据你的存储格式调整）
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * 获取用户密码（已加密）
     *
     * @return 加密后的密码字符串
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账号是否未过期（本实现始终返回 true，表示永不过期）
     *
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /**
     * 账号是否未锁定（本实现始终返回 true，表示永不锁定）
     *
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /**
     * 凭证（密码）是否未过期（本实现始终返回 true，表示永不过期）
     *
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * 账号是否启用（本实现始终返回 true，表示始终启用）
     *
     * @return true
     */
    @Override
    public boolean isEnabled() { return true; }
}
