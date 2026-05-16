package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private List<Long> menuIds;
}
