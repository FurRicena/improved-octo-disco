package com.example.demo.Asp;

import com.example.demo.Annotation.Log;
import com.example.demo.Entity.OperationLog;
import com.example.demo.Repository.OperationLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);
    private final OperationLogRepository logRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LogAspect(OperationLogRepository logRepository, HttpServletRequest request) {
        this.logRepository = logRepository;
        this.request = request;
    }

    @AfterReturning(pointcut = "@annotation(log)", returning = "result")
    public void logAfter(JoinPoint joinPoint, Log log, Object result) {
        try {
            String username = getCurrentUsername();
            String operation = log.value();
            String target = getTarget(joinPoint);
            String detail = getDetail(joinPoint);
            String ip = request.getRemoteAddr();

            OperationLog entity = new OperationLog();
            entity.setOperator(username);
            entity.setOperation(operation);
            entity.setTarget(target);
            entity.setDetail(detail);
            entity.setIp(ip);
            logRepository.save(entity);
        } catch (Exception e) {
            logger.error("记录操作日志失败，方法: {}", joinPoint.getSignature().getName(), e);
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private String getTarget(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.getSignature().getName();  // 返回方法名
        }
        Object arg = args[0];
        return arg != null ? arg.toString() : "";
    }

    private String getDetail(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "无参数";
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return Arrays.toString(args);
        }
    }
}
