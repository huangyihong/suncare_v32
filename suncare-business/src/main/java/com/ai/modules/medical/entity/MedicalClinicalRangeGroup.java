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
 * @Description: 临床路径范围组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_CLINICAL_RANGE_GROUP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_CLINICAL_RANGE_GROUP对象", description="临床路径范围组")
public class MedicalClinicalRangeGroup {

	/**临床路径ID*/
	@Excel(name = "临床路径ID", width = 15)
    @ApiModelProperty(value = "临床路径ID")
	private java.lang.String clinicalId;
	/**组ID*/
	@Excel(name = "组编码", width = 15)
    @ApiModelProperty(value = "组编码")
	private java.lang.String groupCode;
	/**组名称*/
	@Excel(name = "组名称", width = 15)
    @ApiModelProperty(value = "组名称")
	private java.lang.String groupName;
	/**组类别，drug或project*/
	@Excel(name = "组类别，drug或project", width = 15)
    @ApiModelProperty(value = "组类别，drug或project")
	private java.lang.String groupType;
	/**医嘱属性（long长期，short临时，unlimit不限）*/
	@Excel(name = "医嘱属性（long长期，short临时，inHosp住院）", width = 15)
    @ApiModelProperty(value = "医嘱属性（long长期，short临时，inHosp住院）")
	private java.lang.String adviceAttr;
	/**必要属性（require必须，optional可选择，unlimit不限）*/
	@Excel(name = "必要属性（require必须，optional可选择）", width = 15)
    @ApiModelProperty(value = "必要属性（require必须，optional可选择")
	private java.lang.String requireAttr;
	@Excel(name = "组序号", width = 15)
	@ApiModelProperty(value = "组序号")
	private java.lang.Integer groupNo;
}
