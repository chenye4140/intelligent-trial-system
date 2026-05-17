package com.intelligent.trial.document.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 来文登记实体类
 * 对应数据库表: incoming_doc
 *
 * @author intelligent-trial
 */
@Data
@TableName("incoming_doc")
public class IncomingDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 来文文号
     */
    @TableField("doc_no")
    private String docNo;

    /**
     * 来文单位
     */
    @TableField("from_unit")
    private String fromUnit;

    /**
     * 来文标题
     */
    @TableField("title")
    private String title;

    /**
     * 收到日期
     */
    @TableField("receive_date")
    private Date receiveDate;

    /**
     * 事由/主题
     */
    @TableField("subject")
    private String subject;

    /**
     * OCR识别内容
     */
    @TableField("ocr_content")
    private String ocrContent;

    /**
     * 状态：0=待处理, 1=处理中, 2=已办结, 3=已归档
     */
    @TableField("status")
    private Integer status;

    /**
     * 当前处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
