package com.example.demo.config;

import com.example.demo.Hander.JwtAccessDeniedHandler;
import com.example.demo.Hander.JwtAuthenticationEntryPoint;
import com.example.demo.Hander.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, JwtAuthenticationEntryPoint unauthorizedHandler, JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /**
     * 配置安全过滤链（核心安全策略）
     * <p>本配置定义了：
     * <ul>
     *     <li>禁用 CSRF 保护（适用于无状态的 REST API）</li>
     *     <li>自定义认证入口点和权限拒绝处理器</li>
     *     <li>无状态 Session 策略（不创建 Session）</li>
     *     <li>请求授权规则：
     *         <ul>
     *             <li>放行所有 OPTIONS 预检请求</li>
     *             <li>公开接口：登录、注册、错误页无需认证</li>
     *             <li>/admin/** 接口仅限 ADMIN 角色访问</li>
     *             <li>/files/** 静态资源完全公开</li>
     *             <li>其余所有请求需要认证</li>
     *         </ul>
     *     </li>
     *     <li>在用户名密码认证过滤器之前添加 JWT 认证过滤器</li>
     * </ul>
     * </p>
     *
     * @param http HttpSecurity 构建器
     * @return SecurityFilterChain 安全过滤链实例
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                                // 放行预检请求（OPTIONS）
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // 公开接口（无需登录）
                                .requestMatchers("/user/login", "/user/register", "/error").permitAll()
                                // 管理员专用接口（只有 ADMIN 角色可访问）
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                // 普通用户和管理员都可访问（需要登录）
                                .requestMatchers("/user/**").authenticated()

                                .requestMatchers("/files/**").permitAll()
                                .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 获取 Spring Security 的认证管理器
     * <p>用于支持用户名密码认证及 JWT 认证流程中的 AuthenticationManager 实例。</p>
     *
     * @param authenticationConfiguration Spring Security 提供的认证配置对象
     * @return AuthenticationManager 认证管理器实例
     * @throws Exception 若获取失败则抛出异常
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
