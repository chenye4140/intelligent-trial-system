package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class DeptDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;
}
