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
 * @Description: 规则依赖字段质量表
 * @Author: jeecg-boot
 * @Date:   2021-03-16
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_COLUMN_QUALITY")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_COLUMN_QUALITY对象", description="规则依赖字段质量表")
public class MedicalColumnQuality {
    
	/**ID*/
	@Excel(name = "ID", width = 15)
    @ApiModelProperty(value = "ID")
	private java.lang.String columnId;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
	private java.lang.String tableName;
	/**表字段名*/
	@Excel(name = "表字段名", width = 15)
    @ApiModelProperty(value = "表字段名")
	private java.lang.String columnName;
	/**表字段中文名*/
	@Excel(name = "表字段中文名", width = 15)
    @ApiModelProperty(value = "表字段中文名")
	private java.lang.String columnCnname;
	/**涉及不合规行为*/
	@Excel(name = "涉及不合规行为", width = 15)
    @ApiModelProperty(value = "涉及不合规行为")
	private java.lang.String actionNames;
	/**涉及不合规行为数量*/
	@Excel(name = "涉及不合规行为数量", width = 15)
    @ApiModelProperty(value = "涉及不合规行为数量")
	private java.lang.Integer actionCnt;
	/**涉及规则数量*/
	@Excel(name = "涉及规则数量", width = 15)
    @ApiModelProperty(value = "涉及规则数量")
	private java.lang.Integer ruleCnt;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	@Excel(name = "表中文名", width = 15)
    @ApiModelProperty(value = "表中文名")
	private java.lang.String tableCnname;
	@Excel(name = "项目ID", width = 15)
    @ApiModelProperty(value = "项目ID")
	private java.lang.String projectId;
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String projectName;
	@Excel(name = "批次号", width = 15)
    @ApiModelProperty(value = "批次号")
	private java.lang.String batchId;
	@Excel(name = "批次名称", width = 15)
    @ApiModelProperty(value = "批次名称")
	private java.lang.String batchName;
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String dataSource;
}
