package com.ai.modules.ybFj.entity;

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
 * @Description: 飞检动态表单
 * @Author: jeecg-boot
 * @Date:   2023-06-07
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_dynamic_config")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_dynamic_config对象", description="飞检动态表单")
public class YbFjDynamicConfig {
    
	/**唯一ID*/
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String configId;
	/**类别{fj:飞检}*/
	@Excel(name = "类别{fj:飞检}", width = 15)
    @ApiModelProperty(value = "类别{fj:飞检}")
	private java.lang.String configCategory;
	/**类型*/
	@Excel(name = "类型", width = 15)
    @ApiModelProperty(value = "类型")
	private java.lang.String configType;
	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @ApiModelProperty(value = "字段名")
	private java.lang.String colName;
	/**字段中文名*/
	@Excel(name = "字段中文名", width = 15)
    @ApiModelProperty(value = "字段中文名")
	private java.lang.String colCname;
	/**字段类型*/
	@Excel(name = "字段类型", width = 15)
    @ApiModelProperty(value = "字段类型")
	private java.lang.String colType;
	/**主键标识{y:是,n:否}*/
	@Excel(name = "主键标识{y:是,n:否}", width = 15)
    @ApiModelProperty(value = "主键标识{y:是,n:否}")
	private java.lang.String pkFlag;
	/**是否显示{y:是,n:否}*/
	@Excel(name = "是否显示{y:是,n:否}", width = 15)
    @ApiModelProperty(value = "是否显示{y:是,n:否}")
	private java.lang.String displayFlag;
	/**宽度*/
	@Excel(name = "宽度", width = 15)
    @ApiModelProperty(value = "宽度")
	private java.lang.Integer colWidth;
	/**字段顺序*/
	@Excel(name = "字段顺序", width = 15)
    @ApiModelProperty(value = "字段顺序")
	private java.math.BigDecimal colSeq;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
}
