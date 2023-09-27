package com.ai.modules.drg.entity;

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
 * @Description: drg规则限定条件表
 * @Author: jeecg-boot
 * @Date:   2023-05-08
 * @Version: V1.0
 */
@Data
@TableName("drg_rule_limites")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="drg_rule_limites对象", description="drg规则限定条件表")
public class DrgRuleLimites {
    
	/**目录编码*/
	@Excel(name = "目录编码", width = 15)
    @ApiModelProperty(value = "目录编码")
	private java.lang.String catalogCode;
	/**目录类型{DRG_V、ADRG_V}*/
	@Excel(name = "目录类型{DRG_V、ADRG_V}", width = 15)
    @ApiModelProperty(value = "目录类型{DRG_V、ADRG_V}")
	private java.lang.String catalogType;
	/**组内关系*/
	@Excel(name = "组内关系", width = 15)
    @ApiModelProperty(value = "组内关系")
	private java.lang.String compareLogic;
	/**比较符*/
	@Excel(name = "比较符", width = 15)
    @ApiModelProperty(value = "比较符")
	private java.lang.String compareType;
	/**比较符2*/
	@Excel(name = "比较符2", width = 15)
    @ApiModelProperty(value = "比较符2")
	private java.lang.String compareType2;
	/**值*/
	@Excel(name = "值", width = 15)
    @ApiModelProperty(value = "值")
	private java.lang.String compareValue;
	/**值2*/
	@Excel(name = "值2", width = 15)
    @ApiModelProperty(value = "值2")
	private java.lang.String compareValue2;
	/**createdBy*/
	@Excel(name = "createdBy", width = 15)
    @ApiModelProperty(value = "createdBy")
	private java.lang.String createdBy;
	/**createdByName*/
	@Excel(name = "createdByName", width = 15)
    @ApiModelProperty(value = "createdByName")
	private java.lang.String createdByName;
	/**createdTime*/
	@Excel(name = "createdTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "createdTime")
	private java.util.Date createdTime;
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**组与组关系*/
	@Excel(name = "组与组关系", width = 15)
    @ApiModelProperty(value = "组与组关系")
	private java.lang.String logic;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private java.lang.Integer seq;
	/**updatedBy*/
	@Excel(name = "updatedBy", width = 15)
    @ApiModelProperty(value = "updatedBy")
	private java.lang.String updatedBy;
	/**updatedByName*/
	@Excel(name = "updatedByName", width = 15)
    @ApiModelProperty(value = "updatedByName")
	private java.lang.String updatedByName;
	/**updatedTime*/
	@Excel(name = "updatedTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "updatedTime")
	private java.util.Date updatedTime;
	/**版本号*/
	@Excel(name = "版本号", width = 15)
    @ApiModelProperty(value = "版本号")
	private java.lang.String versionCode;
	/**条件类型{age:年龄,dayAge:天龄,zyDays:住院天数,weight:出生体重,leavetype:离院方式}*/
	@Excel(name = "条件类型{age:年龄,dayAge:天龄,zyDays:住院天数,weight:出生体重,leavetype:离院方式}", width = 15)
    @ApiModelProperty(value = "条件类型{age:年龄,dayAge:天龄,zyDays:住院天数,weight:出生体重,leavetype:离院方式}")
	private java.lang.String whereType;
}
