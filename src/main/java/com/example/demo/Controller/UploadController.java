package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.Service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "上传接口")
@RestController
@RequestMapping("/upload")
@PreAuthorize("isAuthenticated()")
public class UploadController {

    private final FileService fileService;

    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 上传图片文件到服务器
     * <p>支持的文件类型：图片（image/*），文件大小受 Spring Boot 配置限制（默认 1MB）</p>
     *
     * @param file 待上传的 MultipartFile 文件对象，必须为图片格式
     * @return 包含可访问的文件 URL 的统一响应结果（例如：http://域名/uploads/uuid.jpg）
     * @throws Exception 文件上传过程中可能发生的 IO 异常
     */
    @Operation(summary = "上传文件")
    @PostMapping
    @Log("上传文件")
    public Result<?> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(fileService.upload(file));
    }
}
