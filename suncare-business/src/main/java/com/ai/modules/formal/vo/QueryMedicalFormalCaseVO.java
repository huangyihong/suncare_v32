/**
 * QueryMedicalFormalCaseVO.java	  V1.0   2022年3月14日 下午3:03:30
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.formal.vo;

import java.util.Date;
import java.util.List;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryMedicalFormalCaseVO {
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	@TableId("CASE_ID")
	private java.lang.String caseId;
	/**编号*/
	@Excel(name = "编号", width = 15)
    @ApiModelProperty(value = "编号")
	private java.lang.String caseCode;
	/**模型名称*/
	@Excel(name = "模型名称", width = 15)
    @ApiModelProperty(value = "模型名称")
	private java.lang.String caseName;

	/**归纳流程编号*/
	@Excel(name = "归纳流程编号", width = 15)
	@ApiModelProperty(value = "归纳流程编号")
	private java.lang.String caseCodes;
	/**归纳流程名称*/
	@Excel(name = "归纳流程名称", width = 15)
	@ApiModelProperty(value = "归纳流程名称")
	private java.lang.String caseNames;
	/**状态{wait:待提交,submited:已提交,reject:驳回,stop:停用,lock:锁定,normal:正常}*/
//	@Excel(name = "状态{wait:待提交,submited:已提交,reject:驳回,stop:停用,lock:锁定,normal:正常}", width = 15)
//    @ApiModelProperty(value = "状态{wait:待提交,submited:已提交,reject:驳回,stop:停用,lock:锁定,normal:正常}")
	@Excel(name = "状态{stop:停用,,normal:启用}", width = 15)
	@ApiModelProperty(value = "状态{stop:停用,,normal:启用}")
	@MedicalDict(dicCode = "SWITCH_STATUS")
	private java.lang.String caseStatus;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String caseRemark;
	@Excel(name = "版本号", width = 15)
	@ApiModelProperty(value = "版本号")
	private java.lang.Float caseVersion;
	@Excel(name = "模型归类", width = 15)
	@ApiModelProperty(value = "模型归类")
	private java.lang.String caseClassify;
	/**流程图json字符串*/
	@Excel(name = "流程图json字符串", width = 15)
    @ApiModelProperty(value = "流程图json字符串")
	private java.lang.String flowJson;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
	private java.lang.String createUserid;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	@Excel(name = "修改备注", width = 15)
	@ApiModelProperty(value = "修改备注")
	private String updateRemark;
	/**提交人ID*/
	@Excel(name = "提交人ID", width = 15)
    @ApiModelProperty(value = "提交人ID")
	private java.lang.String submitUserid;
	/**提交人*/
	@Excel(name = "提交人", width = 15)
    @ApiModelProperty(value = "提交人")
	private java.lang.String submitUsername;
	/**提交时间*/
	@Excel(name = "提交时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "提交时间")
	private java.util.Date submitTime;
	/**所属地区*/
	@Excel(name = "所属地区", width = 15)
	@ApiModelProperty(value = "所属地区")
	private java.lang.String ruleSource;
	/**所属地区编码*/
	@Excel(name = "所属地区编码", width = 15)
	@ApiModelProperty(value = "所属地区编码")
	private java.lang.String ruleSourceCode;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	/**开始时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结束时间")
	private Date endTime;
	/**不合理行为ID*/
	@Excel(name = "不合理行为ID", width = 15)
	@ApiModelProperty(value = "不合理行为ID")
	private java.lang.String actionId;
	/**不合理行为名称*/
	@Excel(name = "不合理行为名称", width = 15)
	@ApiModelProperty(value = "不合理行为名称")
	private java.lang.String actionName;
	/**不合理行为类型*/
	@Excel(name = "不合理行为类型", width = 15)
    @ApiModelProperty(value = "不合理行为类型")
	private java.lang.String actionType;
	/**不合理行为类型名称*/
	@Excel(name = "不合理行为类型名称", width = 15)
	@ApiModelProperty(value = "不合理行为类型名称")
	private java.lang.String actionTypeName;
	/**不合理行为描述*/
	@Excel(name = "不合理行为描述", width = 15)
	@ApiModelProperty(value = "不合理行为描述")
	private java.lang.String actionDesc;
	@Excel(name = "政策依据", width = 15)
	@ApiModelProperty(value = "政策依据")
	private java.lang.String ruleBasis;
	/**序列号*/
	@Excel(name = "序列号", width = 15)
    @ApiModelProperty(value = "序列号")
	private java.lang.Integer orderNo;
	/**数据生成状态(1数据未生成2数据正在生成中3数据已生成)*/
	@Excel(name = "数据生成状态(1数据未生成2数据正在生成中3数据已生成)", width = 15)
    @ApiModelProperty(value = "数据生成状态(1数据未生成2数据正在生成中3数据已生成)")
	@Dict(dicCode = "result_data_status")
	private java.lang.Integer resultDataStatus;
	/**诊断ID数*/
	@Excel(name = "诊断ID数", width = 15)
    @ApiModelProperty(value = "诊断ID数")
	private java.lang.Long idDataCount;
	/**主体数*/
	@Excel(name = "主体数", width = 15)
    @ApiModelProperty(value = "主体数")
	private java.lang.Long objDataCount;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private java.math.BigDecimal dataMoney;
	/**数据生成开始时间*/
	@Excel(name = "数据生成开始时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据生成开始时间")
	private java.util.Date resultStartTime;
	/**数据生成结束时间*/
	@Excel(name = "数据生成结束时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "数据生成结束时间")
	private java.util.Date resultEndTime;
	
	private List<MedicalFormalFlowRule> conditionList;
}
