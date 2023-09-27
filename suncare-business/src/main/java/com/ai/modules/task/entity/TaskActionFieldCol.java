package com.ai.modules.task.entity;

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
 * @Description: 不合规行为表字段信息配置
 * @Author: jeecg-boot
 * @Date:   2021-02-22
 * @Version: V1.0
 */
@Data
@TableName("TASK_ACTION_FIELD_COL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_ACTION_FIELD_COL对象", description="不合规行为表字段信息配置")
public class TaskActionFieldCol {

	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	@TableId("COL_ID")
	private String colId;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
	private String tableName;
	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @ApiModelProperty(value = "字段名")
	private String colName;
	/**字段中文名*/
	@Excel(name = "字段中文名", width = 15)
    @ApiModelProperty(value = "字段中文名")
	private String colCnname;
	/**字段含义*/
	@Excel(name = "字段含义", width = 15)
	@ApiModelProperty(value = "字段含义")
	private String colDesc;
	/**字段列宽*/
	@Excel(name = "字段列宽", width = 15)
	@ApiModelProperty(value = "字段列宽")
	private Double colWidth;
	/**字段列宽*/
	@Excel(name = "字段列表对齐方式", width = 15)
	@ApiModelProperty(value = "字段列表对齐方式")
	private String colAlign;
	/**适用平台*/
	@Excel(name = "适用平台", width = 15)
    @ApiModelProperty(value = "适用平台")
	@MedicalDict(dicCode = "ACTION_FIELD_PALTFORM")
	private String platform;
	/**状态(0 关闭)*/
	@Excel(name = "状态(stop normal)", width = 15)
    @ApiModelProperty(value = "状态(stop normal)")
	private String status;
	/**是否DWS字段*/
	@Excel(name = "是否DWS字段", width = 15)
	@ApiModelProperty(value = "是否DWS字段")
	private String dwsCol;
	/**是否默认选中*/
	@Excel(name = "是否默认选中", width = 15)
	@ApiModelProperty(value = "是否默认选中")
	@MedicalDict(dicCode = "YESNO")
	private String defSelect;
	/**是否可以作为查询参数*/
	@Excel(name = "是否可以作为查询参数", width = 15)
	@ApiModelProperty(value = "是否可以作为查询参数")
	@MedicalDict(dicCode = "YESNO")
	private String toSearch;

	@Excel(name = "作为查询条件时的输入方式", width = 15)
	@ApiModelProperty(value = "作为查询条件时的输入方式")
	private String serInputType;

	@Excel(name = "作为查询条件时的下拉字典编码", width = 15)
	@ApiModelProperty(value = "作为查询条件时的下拉字典编码")
	private String serSelectType;

	@Excel(name = "作为查询条件时的数据类型", width = 15)
	@ApiModelProperty(value = "作为查询条件时的数据类型")
	private String serDataType;

	@Excel(name = "作为默认查询条件", width = 15)
	@ApiModelProperty(value = "作为默认查询条件")
	private String defSearch;

	@Excel(name = "默认字段排序", width = 15)
	@ApiModelProperty(value = "默认字段排序")
	private String orderNo;

	@Excel(name = "默认条件排序", width = 15)
	@ApiModelProperty(value = "默认条件排序")
	private String serOrderNo;

	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
	@ApiModelProperty(value = "修改人")
	private String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
	@ApiModelProperty(value = "修改人姓名")
	private String updateUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private String createUser;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
	@ApiModelProperty(value = "创建人姓名")
	private String createUsername;
}
