package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class DeptTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    private String deptName;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;
    private Date createTime;
    private List<DeptTreeVO> children;
}
