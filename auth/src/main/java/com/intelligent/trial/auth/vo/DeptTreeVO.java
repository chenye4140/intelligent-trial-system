package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "部门树节点")
public class DeptTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "部门ID")
        private Long id;
    @Schema(description = "父部门ID")
        private Long parentId;
    @Schema(description = "部门名称")
        private String deptName;
    @Schema(description = "负责人")
        private String leader;
    private String phone;
    @Schema(description = "排序")
        private Integer sort;
    @Schema(description = "状态")
        private Integer status;
    private Date createTime;
    @Schema(description = "子部门列表")
        private List<DeptTreeVO> children;
}
