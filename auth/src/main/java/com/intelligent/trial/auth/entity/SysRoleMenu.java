package com.intelligent.trial.auth.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_role_menu")
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long roleId;
    private Long menuId;
    private Date createTime;
    private Date updateTime;
}
