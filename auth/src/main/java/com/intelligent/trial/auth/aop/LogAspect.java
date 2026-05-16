package com.intelligent.trial.auth.aop;

import com.alibaba.fastjson2.JSON;
import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.mapper.SysAuditLogMapper;
import com.intelligent.trial.auth.util.HttpContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysAuditLogMapper auditLogMapper;

    @Around("@annotation(requireLog)")
    public Object around(ProceedingJoinPoint joinPoint, RequireLog requireLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        SysAuditLog auditLog = new SysAuditLog();
        auditLog.setModule(requireLog.module());
        auditLog.setAction(requireLog.action());
        auditLog.setDescription(requireLog.description());

        if (request != null) {
            auditLog.setIp(HttpContextUtil.getIp(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setRequestUrl(request.getRequestURI());
            auditLog.setRequestMethod(request.getMethod());
            try {
                String params = JSON.toJSONString(joinPoint.getArgs());
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
                auditLog.setParams(params);
            } catch (Exception e) {
                auditLog.setParams("[参数序列化失败]");
            }
        }

        // 从请求属性中获取用户信息（由 JwtInterceptor 设置）
        if (request != null && request.getAttribute("userId") != null) {
            auditLog.setUserId((Long) request.getAttribute("userId"));
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            auditLog.setResult(1);
        } catch (Throwable e) {
            auditLog.setResult(0);
            throw e;
        } finally {
            auditLog.setDuration((int) (System.currentTimeMillis() - startTime));
            auditLog.setCreateTime(new Date());
            auditLog.setUpdateTime(new Date());
            try {
                auditLogMapper.insert(auditLog);
            } catch (Exception e) {
                log.error("保存审计日志失败", e);
            }
        }

        return result;
    }
}
