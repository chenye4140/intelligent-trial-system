package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "审计日志信息")
public class AuditLogVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "日志ID")
        private Long id;
    @Schema(description = "用户ID")
        private Long userId;
    private String username;
    @Schema(description = "模块")
        private String module;
    @Schema(description = "操作")
        private String action;
    private String description;
    @Schema(description = "IP地址")
        private String ip;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    @Schema(description = "参数")
        private String params;
    private Integer result;
    @Schema(description = "耗时(ms)")
        private Integer duration;
    @Schema(description = "创建时间")
        private Date createTime;
}
