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

    /**
     * 分页查询操作日志（仅限管理员）
     * <p>支持按操作人（operator）和操作类型（operation）进行模糊搜索，结果按创建时间降序排列。</p>
     *
     * @param operator 操作人用户名（模糊匹配，可选）
     * @param operation 操作描述（模糊匹配，可选，如“用户注册”、“创建订单”）
     * @param pageNum   页码（默认1，从1开始）
     * @param pageSize  每页条数（默认10，最大100）
     * @return 包含分页操作日志数据的统一响应结果
     */
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
