package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ClassificationLevelDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "密级编码不能为空")
    private String levelCode;
    @NotBlank(message = "密级名称不能为空")
    private String levelName;
    private Integer sort;
    private Integer status;
}
