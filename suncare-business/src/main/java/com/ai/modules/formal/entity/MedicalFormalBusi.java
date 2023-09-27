package com.ai.modules.formal.entity;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 业务组表
 * @Author: jeecg-boot
 * @Date:   2019-11-28
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FORMAL_BUSI")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FORMAL_BUSI对象", description="业务组表")
public class MedicalFormalBusi {

	/**主键ID*/
	@Excel(name = "主键ID", width = 15)
    @ApiModelProperty(value = "主键ID")
	@TableId("BUSI_ID")
	private java.lang.String busiId;
	/**业务组名称*/
	@Excel(name = "业务组名称", width = 15)
    @ApiModelProperty(value = "业务组名称")
	private java.lang.String busiName;
	/**客户名称*/
	@Excel(name = "客户名称", width = 15)
    @ApiModelProperty(value = "客户名称")
	private java.lang.String custName;
	/**状态{wait:待提交,submited:提交,stop:停用,normal:启用}*/
	@Excel(name = "状态{wait:待提交,submited:提交,stop:停用,normal:启用}", width = 15)
    @ApiModelProperty(value = "状态{wait:待提交,submited:提交,stop:停用,normal:启用}")
	@MedicalDict(dicCode = "SWITCH_STATUS")
	private java.lang.String busiStatus;
	/**数据源*/
	@Excel(name = "数据源", width = 15)
    @ApiModelProperty(value = "数据源")
	private java.lang.String dataSourceId;
	/**数据开始时间*/
	@Excel(name = "数据开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据开始时间")
	private java.util.Date etlStartTime;
	/**数据结束时间*/
	@Excel(name = "数据结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据结束时间")
	private java.util.Date etlEndTime;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
	private java.lang.String createUserid;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
}
