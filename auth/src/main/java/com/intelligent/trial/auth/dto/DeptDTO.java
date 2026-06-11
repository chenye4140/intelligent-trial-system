package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "部门创建/更新请求")
public class DeptDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @Schema(description = "父部门ID")
        private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    @Schema(description = "部门名称")
        private String deptName;
    @Schema(description = "负责人")
        private String leader;
    @Schema(description = "联系电话")
        private String phone;
    @Schema(description = "排序")
        private Integer sort;
    @Schema(description = "状态")
        private Integer status;
}
