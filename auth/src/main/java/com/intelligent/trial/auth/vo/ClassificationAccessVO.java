package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ClassificationAccessVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Integer maxLevelSort;
    private List<ClassificationLevelInfo> accessibleLevels;

    @Data
    public static class ClassificationLevelInfo {
        private Long id;
        private String levelCode;
        private String levelName;
        private Integer sort;
    }
}
