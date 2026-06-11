package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "密级信息")
public class ClassificationLevelVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
        private Long id;
    @Schema(description = "密级编码")
        private String levelCode;
    @Schema(description = "密级名称")
        private String levelName;
    private Integer sort;
    @Schema(description = "状态")
        private Integer status;
    private Date createTime;
}
