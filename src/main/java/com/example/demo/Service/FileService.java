package com.example.demo.Service;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-url}")
    private String accessUrl;

    /**
     * 上传图片文件到服务器本地目录
     * <p>
     * 支持的文件类型：图片（image/*），文件大小受 Spring Boot 配置限制（默认 1MB）修改为了10MB
     * </p>
     *
     * @param file 待上传的 MultipartFile 文件对象
     * @return 可访问的图片 URL（例如：http://localhost:8080/uploads/uuid.jpg）
     * @throws RuntimeException 如果文件为空、不是图片、创建目录失败或文件写入失败
     * @throws Exception 文件传输过程中可能抛出的 IO 异常
     */
    @Schema(description = "上传文件")
    public String upload(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new RuntimeException("只能上传图片");
        }

        String original = file.getOriginalFilename();
        String suffix = Optional.ofNullable(original)
                .filter(s -> s.contains("."))
                .map(s -> s.substring(s.lastIndexOf(".")))
                .orElse("");

        String fileName = UUID.randomUUID() + suffix;

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            if(!dir.mkdirs())
                throw new RuntimeException("创建目录失败");
        }

        File dest = new File(dir, fileName);
        file.transferTo(dest);

        return accessUrl + fileName;
    }
}
