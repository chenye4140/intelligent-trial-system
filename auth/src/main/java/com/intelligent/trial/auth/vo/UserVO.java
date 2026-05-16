package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
    private String phone;
    private String email;
    private Integer status;
    private Date lastLoginTime;
    private Date createTime;
    private List<Long> roleIds;
    private List<String> roles;
}
