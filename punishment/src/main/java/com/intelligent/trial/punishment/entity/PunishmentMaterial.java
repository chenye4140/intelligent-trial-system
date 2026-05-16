package com.intelligent.trial.punishment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 处分材料实体
 */
@Data
@TableName("punishment_material")
public class PunishmentMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联处分执行ID
     */
    private Long executionId;

    /**
     * 材料类型（决定书/送达回证/执行报告等）
     */
    private String materialType;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 上传人ID
     */
    private Long uploaderId;

    private Date createTime;

    private Date updateTime;
}
