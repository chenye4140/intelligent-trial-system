package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class AuditLogVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String username;
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
}
