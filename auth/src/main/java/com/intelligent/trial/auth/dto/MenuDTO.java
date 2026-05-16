package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class MenuDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
