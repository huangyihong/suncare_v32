package com.ai.modules.medical.entity;

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
 * @Description: 特殊模型归类表
 * @Author: jeecg-boot
 * @Date:   2020-11-24
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_SPECIAL_CASE_CLASSIFY")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_SPECIAL_CASE_CLASSIFY对象", description="特殊模型归类表")
public class MedicalSpecialCaseClassify {

	/**归类ID*/
	@Excel(name = "归类ID", width = 15)
    @ApiModelProperty(value = "归类ID")
	private java.lang.String classifyId;
	/**归类名称*/
	@Excel(name = "归类名称", width = 15)
    @ApiModelProperty(value = "归类名称")
	private java.lang.String classifyName;
	/**可选表，多个用逗号分隔*/
	@Excel(name = "可选表，多个用逗号分隔", width = 15)
    @ApiModelProperty(value = "可选表，多个用逗号分隔")
	private java.lang.String optionalTable;
	/**引擎sql*/
	@Excel(name = "引擎sql", width = 15)
    @ApiModelProperty(value = "引擎sql")
	private java.lang.String engineSql;
	/**引擎处理类，默认使用HiveCaseHandle*/
	@Excel(name = "引擎处理类，默认使用HiveCaseHandle", width = 15)
    @ApiModelProperty(value = "引擎处理类，默认使用HiveCaseHandle")
	private java.lang.String engineClazz;
	/**引擎表之间的关联关系（与主体表关联关系），如[{from:DWB_MASTER_INFO,to:DWB_DIAG,formIndex:VISITID,toIndex:VISITID}]*/
	@Excel(name = "引擎表之间的关联关系（与主体表关联关系），如[{from:DWB_MASTER_INFO,to:DWB_DIAG,formIndex:VISITID,toIndex:VISITID}]", width = 15)
    @ApiModelProperty(value = "引擎表之间的关联关系（与主体表关联关系），如[{from:DWB_MASTER_INFO,to:DWB_DIAG,formIndex:VISITID,toIndex:VISITID}]")
	private java.lang.String engineRelationships;
	/**引擎结果{case:病例,detail:明细,类似药品合规结果}*/
	@Excel(name = "引擎结果{case:病例,detail:明细,类似药品合规结果}", width = 15)
    @ApiModelProperty(value = "引擎结果{case:病例,detail:明细,类似药品合规结果}")
	private java.lang.String engineResult;

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
}
