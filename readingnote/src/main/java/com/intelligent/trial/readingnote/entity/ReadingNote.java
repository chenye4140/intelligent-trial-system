package com.intelligent.trial.readingnote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("reading_note")
public class ReadingNote implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String caseId;
    private String title;
    private String content;
    private String tags;
    private Integer noteType;
    private Integer isShared;
    private Long userId;
    private Date createTime;
    private Date updateTime;
}
