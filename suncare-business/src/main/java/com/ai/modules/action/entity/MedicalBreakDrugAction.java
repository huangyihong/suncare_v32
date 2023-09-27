package com.ai.modules.action.entity;

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
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 不合规结果
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_BREAK_DRUG_ACTION")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_BREAK_DRUG_ACTION对象", description="不合规结果")
public class MedicalBreakDrugAction {

	/**ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "ID")
	private java.lang.String id;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	private java.lang.String ruleId ;
	/**规则名称*/
	@Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
	private java.lang.String ruleName;
	/**规则类型*/
	@Excel(name = "规则类型", width = 15)
    @ApiModelProperty(value = "规则类型")
//    @MedicalDict(dicCode = "DRUG_RULE_TYPE")
    private java.lang.String ruleType;
	/**就诊流水号*/
	@Excel(name = "就诊流水号", width = 15)
    @ApiModelProperty(value = "就诊流水号")
	private java.lang.String visitid;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype ;
	/**项目编码*/
	@Excel(name = "项目编码", width = 15)
    @ApiModelProperty(value = "项目编码")
	private java.lang.String itemcode;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String itemname;
	/**项目类别*/
	@Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
	private java.lang.String itemclass;
	/**项目类别编码*/
	@Excel(name = "项目类别编码", width = 15)
    @ApiModelProperty(value = "项目类别编码")
	private java.lang.String itemclassid;
	/**规格*/
	@Excel(name = "规格", width = 15)
    @ApiModelProperty(value = "规格")
	private java.lang.String specificaion ;
	/**单位*/
	@Excel(name = "单位", width = 15)
    @ApiModelProperty(value = "单位")
	private java.lang.String chargeunit;
	/**患者编号*/
	@Excel(name = "患者编号", width = 15)
    @ApiModelProperty(value = "患者编号")
	private java.lang.String clientid;
	/**就诊机构编码*/
	@Excel(name = "就诊机构编码", width = 15)
    @ApiModelProperty(value = "就诊机构编码")
	private java.lang.String orgid;
	/**就诊机构名称*/
	@Excel(name = "就诊机构名称", width = 15)
    @ApiModelProperty(value = "就诊机构名称")
	private java.lang.String orgname;
	/**就诊机构名称*/
	@Excel(name = "处方日期", width = 15)
    @ApiModelProperty(value = "处方日期")
	private java.lang.String prescripttime;
}
