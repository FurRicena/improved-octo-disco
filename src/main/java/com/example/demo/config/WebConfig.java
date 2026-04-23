package com.example.demo.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域资源共享（CORS）
     * <p>允许前端应用（Vue 开发服务器）跨域访问后端 API，支持携带凭证（如 Cookie）。</p>
     * <ul>
     *     <li>允许的源：http://localhost:5173（Vue 默认端口）</li>
     *     <li>允许的方法：所有 HTTP 方法（包括 OPTIONS 预检）</li>
     *     <li>允许的请求头：所有</li>
     *     <li>允许携带凭证：true（支持 Session/Cookie）</li>
     * </ul>
     *
     * @param registry CorsRegistry 用于注册跨域映射
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173") // ⭐ 前端地址
                .allowedMethods("*")                     // ⭐ 包含 OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 配置静态资源处理器
     * <p>将 /files/** 路径映射到本地文件系统目录，用于提供上传文件的访问服务。</p>
     * <p>例如：访问 http://localhost:8080/files/xxx.jpg 将返回 D:/SpringCode/demo/uploads/xxx.jpg 文件。</p>
     *
     * @param registry ResourceHandlerRegistry 用于注册资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:/SpringCode/demo/uploads/");
    }
}