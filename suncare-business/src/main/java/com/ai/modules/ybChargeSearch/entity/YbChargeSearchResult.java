package com.ai.modules.ybChargeSearch.entity;

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
 * @Description: 收费明细查询结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_search_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_search_result对象", description="收费明细查询结果表")
public class YbChargeSearchResult {

	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private java.math.BigDecimal amount;
	/**收费日期*/
	@Excel(name = "收费日期", width = 15)
    @ApiModelProperty(value = "收费日期")
	private java.lang.String charge;
	/**收费等级*/
	@Excel(name = "收费等级", width = 15)
    @ApiModelProperty(value = "收费等级")
	private java.lang.String chargeattri;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
	private java.lang.String deptname;
	/**主诊断*/
	@Excel(name = "主诊断", width = 15)
	@ApiModelProperty(value = "主诊断")
	private java.lang.String disMain;
	/**其他诊断*/
	@Excel(name = "其他诊断", width = 15)
    @ApiModelProperty(value = "其他诊断")
	private java.lang.String dis;
	/**原始诊断*/
	@Excel(name = "原始诊断", width = 15)
    @ApiModelProperty(value = "原始诊断")
	private java.lang.String disSrc;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private java.math.BigDecimal fee;
	/**医院项目名称*/
	@Excel(name = "医院项目名称", width = 15)
    @ApiModelProperty(value = "医院项目名称")
	private java.lang.String hisItemname;
	/**原始医院项目名称*/
	@Excel(name = "原始医院项目名称", width = 15)
    @ApiModelProperty(value = "原始医院项目名称")
	private java.lang.String hisItemnameSrc;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**医保项目名称*/
	@Excel(name = "医保项目名称", width = 15)
    @ApiModelProperty(value = "医保项目名称")
	private java.lang.String itemname;
	/**原始医保项目名称*/
	@Excel(name = "原始医保项目名称", width = 15)
    @ApiModelProperty(value = "原始医保项目名称")
	private java.lang.String itemnameSrc;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**患者名称*/
	@Excel(name = "患者名称", width = 15)
    @ApiModelProperty(value = "患者名称")
	private java.lang.String name;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**就诊日期*/
	@Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
	private java.lang.String visitdate;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private java.lang.String visitid;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String year;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;

	/**收费项目编码*/
	@Excel(name = "收费项目编码", width = 15)
	@ApiModelProperty(value = "收费项目编码")
	private java.lang.String itemcode;

	/**病案号*/
	@Excel(name = "病案号", width = 15)
	@ApiModelProperty(value = "病案号")
	private java.lang.String caseId;

	/**违规标志*/
	@Excel(name = "违规标志", width = 15)
	@ApiModelProperty(value = "违规标志")
	private java.lang.String wgFlag;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;

	/**自付比例*/
	@Excel(name = "自付比例", width = 15)
	@ApiModelProperty(value = "自付比例")
	private java.lang.String selfpayProp;

	/**原始规格*/
	@Excel(name = "原始规格", width = 15)
	@ApiModelProperty(value = "原始规格")
	private java.lang.String specificaion;

	/**单价*/
	@Excel(name = "单价", width = 15)
	@ApiModelProperty(value = "单价")
	private java.math.BigDecimal itemprice;

	/**清洗后单价*/
	@Excel(name = "清洗后单价", width = 15)
	@ApiModelProperty(value = "清洗后单价")
	private java.math.BigDecimal itempriceClean;

	/**清洗后数量*/
	@Excel(name = "清洗后数量", width = 15)
	@ApiModelProperty(value = "清洗后数量")
	private java.math.BigDecimal amountClean;

	/**医保类型*/
	@Excel(name = "医保类型", width = 15)
	@ApiModelProperty(value = "医保类型")
	private java.lang.String insurancetypename;



}
