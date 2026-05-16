package com.intelligent.trial.casemanage.management.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 案件当事人实体
 */
@Data
@TableName("case_party")
public class CaseParty implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 关联案件ID */
    private Long caseId;

    /** 当事人姓名 */
    private String partyName;

    /** 类型: 1=被调查人, 2=证人, 3=举报人, 4=其他 */
    private Integer partyType;

    /** 性别: 0=女, 1=男 */
    private Integer gender;

    /** 身份证号 */
    private String idNumber;

    /** 所在单位 */
    private String dept;

    /** 职务 */
    private String position;

    /** 联系电话 */
    private String phone;

    /** 与案件关系 */
    private String relation;

    private Date createTime;

    private Date updateTime;
}
