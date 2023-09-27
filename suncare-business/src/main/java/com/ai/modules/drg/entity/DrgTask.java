package com.ai.modules.drg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: drg任务表
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Data
@TableName("DRG_TASK")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DRG_TASK对象", description="drg任务表")
public class DrgTask {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**批次号*/
	@Excel(name = "批次号", width = 15)
    @ApiModelProperty(value = "批次号")
	private java.lang.String batchId;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.Object orgs;
	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
    @ApiModelProperty(value = "机构orgid")
	private java.lang.String orgids;
	/**数据开始时间*/
	@Excel(name = "数据开始时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据开始时间")
	private java.util.Date startdate;
	/**数据结束时间*/
	@Excel(name = "数据结束时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据结束时间")
	private java.util.Date enddate;
	/**DRG目录版本*/
	@Excel(name = "DRG目录版本", width = 15)
    @ApiModelProperty(value = "DRG目录版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='DRG_V') t ")
	private java.lang.String drgCatalogV;
	/**逻辑版本*/
	@Excel(name = "逻辑版本", width = 15)
    @ApiModelProperty(value = "逻辑版本")
	@MedicalDict(dicCode = "DRG_LOGIC_V")
	private java.lang.String logicV;
	/**入组病历数量*/
	@Excel(name = "入组病历数量", width = 15)
    @ApiModelProperty(value = "入组病历数量")
	private java.lang.Integer groupNum;
	/**未入组病历数量*/
	@Excel(name = "未入组病历数量", width = 15)
    @ApiModelProperty(value = "未入组病历数量")
	private java.lang.Integer nogroupNum;
	/**总体病例入组率*/
	@Excel(name = "总体病例入组率", width = 15)
    @ApiModelProperty(value = "总体病例入组率")
	private java.math.BigDecimal rate;
	/**运行状态*/
	@Excel(name = "运行状态", width = 15)
    @ApiModelProperty(value = "运行状态")
	private java.lang.String status;
	/**错误信息*/
	@Excel(name = "错误信息", width = 15)
    @ApiModelProperty(value = "错误信息")
	private java.lang.String errorMsg;
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
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String dataSource;
	/**运行开始时间*/
	@Excel(name = "运行开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "运行开始时间")
	private java.util.Date runStartdate;
	/**运行结束时间*/
	@Excel(name = "运行结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "运行结束时间")
	private java.util.Date runEnddate;
}
