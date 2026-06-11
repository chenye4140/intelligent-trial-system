package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "密级创建/更新请求")
public class ClassificationLevelDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "密级编码不能为空")
    @Schema(description = "密级编码")
        private String levelCode;
    @NotBlank(message = "密级名称不能为空")
    @Schema(description = "密级名称")
        private String levelName;
    private Integer sort;
    @Schema(description = "状态")
        private Integer status;
}
