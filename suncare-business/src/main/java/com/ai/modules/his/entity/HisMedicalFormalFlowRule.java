package com.ai.modules.his.entity;

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
 * @Description: 模型流程节点规则备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Data
@TableName(value="HIS_MEDICAL_FORMAL_FLOW_RULE", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="HIS_MEDICAL_FORMAL_FLOW_RULE对象", description="模型流程节点规则备份")
public class HisMedicalFormalFlowRule {
    
	/**batchId*/
	@Excel(name = "batchId", width = 15)
    @ApiModelProperty(value = "batchId")
	private java.lang.String batchId;
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	private java.lang.String ruleId;
	/**流程节点ID*/
	@Excel(name = "流程节点ID", width = 15)
    @ApiModelProperty(value = "流程节点ID")
	private java.lang.String nodeId;
	/**模型ID*/
	@Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
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
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String compareType;
	/**比较值*/
	@Excel(name = "比较值", width = 15)
    @ApiModelProperty(value = "比较值")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String compareValue;
	/**规则排序*/
	@Excel(name = "规则排序", width = 15)
    @ApiModelProperty(value = "规则排序")
	private java.lang.Integer orderNo;
	/**流程节点编码*/
	@Excel(name = "流程节点编码", width = 15)
    @ApiModelProperty(value = "流程节点编码")
	private java.lang.String nodeCode;
	/**backTime*/
	@Excel(name = "backTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "backTime")
	private java.util.Date backTime;
}