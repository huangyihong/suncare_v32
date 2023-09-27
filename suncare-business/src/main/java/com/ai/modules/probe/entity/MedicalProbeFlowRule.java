package com.ai.modules.probe.entity;

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
 * @Description: 流程图树
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_PROBE_FLOW_RULE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_PROBE_FLOW_RULE对象", description="流程图树")
public class MedicalProbeFlowRule {

	/**规则ID*/
	@TableId("RULE_ID")
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	private java.lang.String ruleId;
	/**流程节点ID*/
	@Excel(name = "流程节点ID", width = 15)
    @ApiModelProperty(value = "流程节点ID")
	private java.lang.String nodeId;
	/**流程节点ID*/
	@Excel(name = "流程节点编号", width = 15)
	@ApiModelProperty(value = "流程节点编号")
	private java.lang.String nodeCode;
	/**模型探查ID*/
	@Excel(name = "模型探查ID", width = 15)
    @ApiModelProperty(value = "模型探查ID")
	private java.lang.String caseId;
	/**所属组编号*/
	@Excel(name = "所属组编号", width = 15)
    @ApiModelProperty(value = "所属组编号")
	private java.lang.String groupNo;
	/**逻辑运算符，组内的第一个作为组之间的关系符，包括and,or*/
	@Excel(name = "逻辑运算符，组内的第一个作为组之间的关系符，包括and,or", width = 15)
    @ApiModelProperty(value = "逻辑运算符，组内的第一个作为组之间的关系符，包括and,or")
	private java.lang.String logic;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
	private java.lang.String tableName;
	/**表字段名*/
	@Excel(name = "表字段名", width = 15)
    @ApiModelProperty(value = "表字段名")
	private java.lang.String colName;
	/**比较运算符*/
	@Excel(name = "比较运算符", width = 15)
    @ApiModelProperty(value = "比较运算符")
	private java.lang.String compareType;
	/**比较值*/
	@Excel(name = "比较值", width = 15)
    @ApiModelProperty(value = "比较值")
	private java.lang.String compareValue;
	/**规则排序*/
	@Excel(name = "规则排序", width = 15)
    @ApiModelProperty(value = "规则排序")
	private java.lang.Integer orderNo;
/*	*//**输入类型*//*
	@Excel(name = "输入类型", width = 15)
	@ApiModelProperty(value = "输入类型")
	private java.lang.String inputType;
	*//**字典type*//*
	@Excel(name = "字典type", width = 15)
	@ApiModelProperty(value = "字典type")
	private java.lang.String selectType;*/
}
