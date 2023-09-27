package com.ai.modules.ybFj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 文件导出模板信息
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_template_export")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_template_export对象", description="文件导出模板信息")
public class YbFjTemplateExport {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**模板id*/
	@Excel(name = "模板id", width = 15)
    @ApiModelProperty(value = "模板id")
	private java.lang.Integer templateCode;

	/**模板文件名称*/
	@Excel(name = "模板文件名称", width = 15)
    @ApiModelProperty(value = "模板文件名称")
	private java.lang.String templateName;

	/**调用python代码编码*/
	@Excel(name = "调用python代码编码", width = 15)
	@ApiModelProperty(value = "调用python代码编码")
	@Dict(dicCode = "code",dicText="value",dictTable="(select t1.* from medical_dict t,medical_dict_item t1 where t.GROUP_ID=t1.GROUP_ID and t.GROUP_CODE='FJ_FILE_TYPE') t ")
	private java.lang.String pythonProcCode;


	/**版本号*/
	@Excel(name = "版本号", width = 15)
    @ApiModelProperty(value = "版本号")
	private java.lang.Float versionNum;
	/**模板文件路径*/
	@Excel(name = "模板文件路径", width = 15)
    @ApiModelProperty(value = "模板文件路径")
	private java.lang.String templatePath;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**在用状态(1在用 0历史)*/
	@Excel(name = "在用状态(1在用 0历史)", width = 15)
    @ApiModelProperty(value = "在用状态(1在用 0历史)")
	private java.lang.String useStatus;
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
