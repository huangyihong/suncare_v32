package com.ai.modules.ybFj.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 飞检项目线索现场核查
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_project_clue_onsite")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project_clue_onsite对象", description="飞检项目线索现场核查")
public class YbFjProjectClueOnsite {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String clueId;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private java.lang.String projectId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private java.lang.String projectOrgId;
	/**问题类别（一级）*/
	@Excel(name = "问题类别（一级）", width = 15)
    @ApiModelProperty(value = "问题类别（一级）")
	private java.lang.String issueType;
	/**问题类型（二级）*/
	@Excel(name = "问题类型（二级）", width = 15)
    @ApiModelProperty(value = "问题类型（二级）")
	private java.lang.String issueSubtype;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String clueName;
	/**项目类别*/
	@Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
	private java.lang.String clueType;
	/**涉及数量*/
	@Excel(name = "涉及数量", width = 15)
    @ApiModelProperty(value = "涉及数量")
	private java.lang.Integer caseAmount;
	/**涉及人次*/
	@Excel(name = "涉及人次", width = 15)
    @ApiModelProperty(value = "涉及人次")
	private java.lang.Integer casePersonCnt;
	/**涉及金额（单位：元）*/
	@Excel(name = "涉及金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及金额（单位：元）")
	private java.math.BigDecimal caseFee;
	/**涉及医保基金金额（单位：元）*/
	@Excel(name = "涉及医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及医保基金金额（单位：元）")
	private java.math.BigDecimal caseFundFee;
	/**违规说明*/
	@Excel(name = "违规说明", width = 15)
    @ApiModelProperty(value = "违规说明")
	private java.lang.String caseRemark;
	/**核减数量*/
	@Excel(name = "核减数量", width = 15)
    @ApiModelProperty(value = "核减数量")
	private java.lang.Integer cutAmount;
	/**核减人次*/
	@Excel(name = "核减人次", width = 15)
    @ApiModelProperty(value = "核减人次")
	private java.lang.Integer cutPersonCnt;
	/**核减金额（单位：元）*/
	@Excel(name = "核减金额（单位：元）", width = 15)
    @ApiModelProperty(value = "核减金额（单位：元）")
	private java.math.BigDecimal cutFee;
	/**核减医保基金金额（单位：元）*/
	@Excel(name = "核减医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "核减医保基金金额（单位：元）")
	private java.math.BigDecimal cutFundFee;
	/**报销比例*/
	@Excel(name = "报销比例", width = 15)
	@ApiModelProperty(value = "报销比例")
	private java.math.BigDecimal bxRate;
	/**认定依据*/
	@Excel(name = "认定依据", width = 15)
	@ApiModelProperty(value = "认定依据")
	private java.lang.String sureBasis;
	/**审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)*/
	@Excel(name = "审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)", width = 15)
    @ApiModelProperty(value = "审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)")
	private java.lang.String auditState;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String auditUser;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String auditUserName;
	/**审核时间*/
	@Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private java.lang.String auditOpinion;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
	/**明细上传状态{y:已上传,n:未上传}*/
	@Excel(name = "明细上传状态{y:已上传,n:未上传}", width = 15)
	@ApiModelProperty(value = "明细上传状态{y:已上传,n:未上传}")
	private java.lang.String dtlState;
	/**序号*/
	@Excel(name = "序号", width = 15)
	@ApiModelProperty(value = "序号")
	private java.lang.Integer seq;
	/**明细项目数量*/
	@Excel(name = "明细项目数量", width = 15)
	@ApiModelProperty(value = "明细项目数量")
	private java.lang.Integer dtlAmount;
	/**明细违规金额（单位：元）*/
	@Excel(name = "明细违规金额（单位：元）", width = 15)
	@ApiModelProperty(value = "明细违规金额（单位：元）")
	private java.math.BigDecimal dtlWgFee;
}
