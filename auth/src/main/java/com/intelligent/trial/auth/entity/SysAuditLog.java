package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_audit_log")
public class SysAuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long userId;
    private String module;
    private String action;
    private String description;
    private String ip;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    private String params;
    private Integer result;
    private Integer duration;
    private Date createTime;
    private Date updateTime;
}
