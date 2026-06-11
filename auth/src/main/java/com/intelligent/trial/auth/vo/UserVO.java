package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "用户ID")
        private Long id;
    @Schema(description = "用户名")
        private String username;
    @Schema(description = "真实姓名")
        private String realName;
    @Schema(description = "部门ID")
        private Long deptId;
    @Schema(description = "部门名称")
        private String deptName;
    @Schema(description = "手机号")
        private String phone;
    @Schema(description = "邮箱")
        private String email;
    @Schema(description = "状态")
        private Integer status;
    private Date lastLoginTime;
    @Schema(description = "创建时间")
        private Date createTime;
    @Schema(description = "角色ID列表")
        private List<Long> roleIds;
    @Schema(description = "角色列表")
        private List<String> roles;
}
