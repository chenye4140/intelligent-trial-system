package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class ClassificationLevelVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer sort;
    private Integer status;
    private Date createTime;
}
