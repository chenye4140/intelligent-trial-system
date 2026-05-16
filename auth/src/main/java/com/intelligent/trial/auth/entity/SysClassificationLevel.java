package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_classification_level")
public class SysClassificationLevel implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer sort;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
