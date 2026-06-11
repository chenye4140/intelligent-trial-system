package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户可访问密级")
public class ClassificationAccessVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Integer maxLevelSort;
    private List<ClassificationLevelInfo> accessibleLevels;

    @Data
    public static class ClassificationLevelInfo {
        @Schema(description = "ID")
        private Long id;
        @Schema(description = "密级编码")
        private String levelCode;
        @Schema(description = "密级名称")
        private String levelName;
        private Integer sort;
    }
}
