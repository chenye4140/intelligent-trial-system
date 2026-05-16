package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MenuTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
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
    private List<MenuTreeVO> children;
}
