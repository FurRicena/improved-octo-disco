package com.example.demo.Service;

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
            if(dir.mkdirs())
                throw new RuntimeException("创建目录失败");
        }

        File dest = new File(dir, fileName);
        file.transferTo(dest);

        return accessUrl + fileName;
    }
}
