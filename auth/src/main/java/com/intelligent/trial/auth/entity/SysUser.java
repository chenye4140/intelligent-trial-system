package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Long deptId;
    private String phone;
    private String email;
    private Integer status;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private List<Long> roleIds;
}
