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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 通用条件设置
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
@Data
@TableName("TASK_COMMON_CONDITION_SET")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_COMMON_CONDITION_SET对象", description="通用条件设置")
public class TaskCommonConditionSet {
    
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	private java.lang.String ruleId;
	/**类型(access:准入条件judge:判断条件)*/
	@Excel(name = "类型(access:准入条件judge:判断条件)", width = 15)
    @ApiModelProperty(value = "类型(access:准入条件judge:判断条件)")
	private java.lang.String type;
	/**逻辑运算符，组内的第一个作为组之间的关系符，包括and,or*/
	@Excel(name = "逻辑运算符，组内的第一个作为组之间的关系符，包括and,or", width = 15)
    @ApiModelProperty(value = "逻辑运算符，组内的第一个作为组之间的关系符，包括and,or")
	private java.lang.String logic;
	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @ApiModelProperty(value = "字段名")
	private java.lang.String field;
	/**比较符*/
	@Excel(name = "比较符", width = 15)
    @ApiModelProperty(value = "比较符")
	private java.lang.String compare;
	/**组内序号*/
	@Excel(name = "组内序号", width = 15)
    @ApiModelProperty(value = "组内序号")
	private java.lang.Integer orderNo;
	/**组序号*/
	@Excel(name = "组序号", width = 15)
    @ApiModelProperty(value = "组序号")
	private java.lang.Integer groupNo;
	/**参数1*/
	@Excel(name = "参数1", width = 15)
    @ApiModelProperty(value = "参数1")
	private java.lang.String ext1;
	/**参数2*/
	@Excel(name = "参数2", width = 15)
    @ApiModelProperty(value = "参数2")
	private java.lang.String ext2;
	/**参数3*/
	@Excel(name = "参数3", width = 15)
    @ApiModelProperty(value = "参数3")
	private java.lang.String ext3;
	/**参数4*/
	@Excel(name = "参数4", width = 15)
    @ApiModelProperty(value = "参数4")
	private java.lang.String ext4;
	/**参数5*/
	@Excel(name = "参数5", width = 15)
    @ApiModelProperty(value = "参数5")
	private java.lang.String ext5;
	/**参数6*/
	@Excel(name = "参数6", width = 15)
    @ApiModelProperty(value = "参数6")
	private java.lang.String ext6;
	/**参数7*/
	@Excel(name = "参数7", width = 15)
    @ApiModelProperty(value = "参数7")
	private java.lang.String ext7;
	/**参数8*/
	@Excel(name = "参数8", width = 15)
    @ApiModelProperty(value = "参数8")
	private java.lang.String ext8;
}
