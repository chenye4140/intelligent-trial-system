package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private List<SysMenu> children;
}
