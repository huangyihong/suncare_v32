package com.ai.modules.config.entity;

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
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 不合规行为字典
 * @Author: jeecg-boot
 * @Date:   2021-03-31
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_ACTION_DICT", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_ACTION_DICT对象", description="不合规行为字典")
public class MedicalActionDict {

	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**不合规行为编码*/
	@Excel(name = "不合规行为编码", width = 15)
    @ApiModelProperty(value = "不合规行为编码")
	private java.lang.String actionId;
	/**不合规行为名称*/
	@Excel(name = "不合规行为名称", width = 15)
    @ApiModelProperty(value = "不合规行为名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String actionName;
	/**不合规行为释义*/
	@Excel(name = "不合规行为释义", width = 15)
    @ApiModelProperty(value = "不合规行为释义")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String actionDesc;
	/**规则级别*/
	@Excel(name = "规则级别", width = 15)
    @ApiModelProperty(value = "规则级别")
	private java.lang.String ruleLevel;
	/**计算逻辑*/
	@Excel(name = "计算逻辑", width = 15)
    @ApiModelProperty(value = "计算逻辑")
	private java.lang.String calculate;
	
	/**政策依据*/
	@Excel(name = "政策依据", width = 15)
    @ApiModelProperty(value = "政策依据")
	private java.lang.String policyBasis;
	
	/**政策依据对应的政策编号 POLICY_BASIS_CODE*/
	@Excel(name = "政策依据对应的政策编号", width = 15)
    @ApiModelProperty(value = "政策依据对应的政策编号")
	private java.lang.String policyBasisCode;
	
	
	/**现场查处需要资料*/
	@Excel(name = "现场查处需要资料", width = 15)
    @ApiModelProperty(value = "现场查处需要资料")
	private java.lang.String information;
	/**现场稽查查处方法*/
	@Excel(name = "现场稽查查处方法", width = 15)
    @ApiModelProperty(value = "现场稽查查处方法")
	private java.lang.String method;
	/**落地难易度*/
	@Excel(name = "落地难易度", width = 15)
    @ApiModelProperty(value = "落地难易度")
	private java.lang.String difficultyLevel;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**规则/模型类别*/
	@Excel(name = "规则/模型类别", width = 15)
    @ApiModelProperty(value = "规则/模型类别")
	private java.lang.String rules;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
	private java.lang.String status;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createStaff;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createStaffName;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateStaff;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateStaffName;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**人工审核标准*/
	@Excel(name = "人工审核标准", width = 15)
	@ApiModelProperty(value = "人工审核标准")
	private java.lang.String auditStandard;

	@Excel(name = "违规说明模板", width = 15)
	@ApiModelProperty(value = "违规说明模板")
	private java.lang.String breakStateTempl;
}
