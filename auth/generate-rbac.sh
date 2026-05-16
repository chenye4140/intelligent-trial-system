#!/bin/bash
# Generate all RBAC files for the auth module
BASE="/home/chenye/intelligent-trial-system/auth/src/main/java/com/intelligent/trial/auth"

# Create directories
mkdir -p $BASE/{entity,mapper,service/impl,controller,dto,vo,annotation,config,interceptor,aop,util}
mkdir -p $BASE/../resources/mapper

###############################################
# 1. util/PasswordEncoderUtil.java
###############################################
cat > $BASE/util/PasswordEncoderUtil.java << 'JAVAEOF'
package com.intelligent.trial.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密工具类
 */
public class PasswordEncoderUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
JAVAEOF

###############################################
# 2. aop/LogAspect.java
###############################################
cat > $BASE/aop/LogAspect.java << 'JAVAEOF'
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
JAVAEOF

###############################################
# 3. util/HttpContextUtil.java
###############################################
cat > $BASE/util/HttpContextUtil.java << 'JAVAEOF'
package com.intelligent.trial.auth.util;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 上下文工具类
 */
public class HttpContextUtil {

    /**
     * 获取客户端 IP
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前请求的用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }

    /**
     * 获取当前请求的用户名
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? (String) username : null;
    }
}
JAVAEOF

###############################################
# 4. context/UserContext.java (ThreadLocal)
###############################################
cat > $BASE/context/UserContext.java << 'JAVAEOF'
package com.intelligent.trial.auth.context;

/**
 * 用户上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
JAVAEOF
mkdir -p $BASE/context

echo "Phase 1 done"
