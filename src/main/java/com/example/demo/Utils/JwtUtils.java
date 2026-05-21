package com.example.demo.Utils;

import com.example.demo.Security.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private  String jwtSecret;   // 从配置文件读取密钥

    @Value("${jwt.expiration}")
    private  int jwtExpirationMs;    // 过期时间（毫秒）

    @PostConstruct
    public void init() {
        System.out.println("jwtSecret = " + jwtSecret);
    }

    // 生成 Token，里面包含用户 id 和角色
    public  String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())      // 用户 id 存到 subject
                .claim("username", userPrincipal.getUsername())    // 额外存用户名
                .claim("role", userPrincipal.getRole())            // 额外存角色
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private  Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // 从 Token 中解析用户 id
    public Long getUserIdFromJwtToken(String token) {
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    // 校验 Token 有效性
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 签名错误、过期等异常
        }
        return false;
    }
}

