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

    // 静态工厂方法：从 User 实体构建
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }

    // 必须实现的 UserDetails 方法
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将 role 转换为 GrantedAuthority（通常加 "ROLE_" 前缀，根据你的存储格式调整）
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
