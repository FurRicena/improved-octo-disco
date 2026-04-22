package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.Entity.OperationLog;
import com.example.demo.Repository.OperationLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "日志接口")
@RestController
@RequestMapping
@PreAuthorize("hasAuthority('ADMIN')")
public class LogController {

    private final OperationLogRepository logRepository;

    public LogController(OperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/admin/logs")
    public Result<Page<OperationLog>> getLogs(@RequestParam(required = false) String operator,
                                              @RequestParam(required = false) String operation,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        // 1. 分页参数健壮处理（与 Menu 示例完全一致）
        int currentPage = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Pageable pageable = PageRequest.of(currentPage - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 2. 动态条件构建（与 Menu 风格一致，使用 StringUtils.hasText）
        Specification<OperationLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(operator)) {
                predicates.add(cb.like(root.get("operator"), "%" + operator + "%"));
            }
            if (StringUtils.hasText(operation)) {
                predicates.add(cb.like(root.get("operation"), "%" + operation + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OperationLog> page = logRepository.findAll(spec, pageable);
        return Result.success(page);
    }
}
