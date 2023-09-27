package com.ai.modules.ybChargeSearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @Description: 收费明细查询任务表
 * @Author: jeecg-boot
 * @Date:   2022-10-09
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_search_task")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_search_task对象", description="收费明细查询任务表")
public class YbChargeSearchTask {

	/**医保收费次数*/
	@Excel(name = "医保收费次数", width = 15)
	@ApiModelProperty(value = "医保收费次数")
	private java.lang.Integer chargeQty;
	/**结算结束时间*/
	@Excel(name = "结算结束时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结算结束时间")
	private java.util.Date chargedateEnddate;
	/**结算开始时间*/
	@Excel(name = "结算开始时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结算开始时间")
	private java.util.Date chargedateStartdate;
	/*患者姓名*/
	@Excel(name = "患者姓名", width = 15)
	@ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
	@ApiModelProperty(value = "创建人ID")
	private java.lang.String createUserId;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
	@ApiModelProperty(value = "项目地")
	@MedicalDict(dicCode = "SOLR_DATA_SOURCE")
	private java.lang.String dataSource;
	/**数据来源层级*/
	@Excel(name = "数据来源层级", width = 15)
	@ApiModelProperty(value = "数据来源层级")
	@MedicalDict(dicCode = "DATA_STATICS_LEVEL")
	private java.lang.String dataStaticsLevel;
	/**科室*/
	@Excel(name = "科室", width = 15)
	@ApiModelProperty(value = "科室")
	private java.lang.String deptname;
	/**错误信息*/
	@Excel(name = "错误信息", width = 15)
	@ApiModelProperty(value = "错误信息")
	private java.lang.String errorMsg;
	/**数据来源(医保数据src)*/
	@Excel(name = "数据来源(医保数据src)", width = 15)
	@ApiModelProperty(value = "数据来源(医保数据src)")
	@MedicalDict(dicCode = "SEARCH_ETL_SOURCE")
	private java.lang.String etlSource;
	/**文件完整路径*/
	@Excel(name = "文件完整路径", width = 15)
	@ApiModelProperty(value = "文件完整路径")
	private java.lang.String fileFullpath;
	/**文件名*/
	@Excel(name = "文件名", width = 15)
	@ApiModelProperty(value = "文件名")
	private java.lang.String fileName;
	/**文件路径*/
	@Excel(name = "文件路径", width = 15)
	@ApiModelProperty(value = "文件路径")
	private java.lang.String filePath;
	/**文件大小*/
	@Excel(name = "文件大小", width = 15)
	@ApiModelProperty(value = "文件大小")
	private java.lang.Double fileSize;
	/**医院收费次数*/
	@Excel(name = "医院收费次数", width = 15)
	@ApiModelProperty(value = "医院收费次数")
	private java.lang.Integer hisChargeQty;
	/**his收费项目*/
	@Excel(name = "his收费项目", width = 15)
	@ApiModelProperty(value = "his收费项目")
	private java.lang.String hisItemName;
	/**his收费项目1*/
	@Excel(name = "his收费项目1", width = 15)
	@ApiModelProperty(value = "his收费项目1")
	private java.lang.String hisItemName1;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**一次就诊累计数量超过住院天数的值*/
	@Excel(name = "一次就诊累计数量超过住院天数的值", width = 15)
	@ApiModelProperty(value = "一次就诊累计数量超过住院天数的值")
	private java.lang.Integer inhosQty;
	/**是否包含自费就诊(1是0否)*/
	@Excel(name = "是否包含自费就诊(1是0否)", width = 15)
	@ApiModelProperty(value = "是否包含自费就诊(1是0否)")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String isFundpay;
	/**是否输出同一天的手术项目(1是0否)*/
	@Excel(name = "是否输出同一天的手术项目(1是0否)", width = 15)
	@ApiModelProperty(value = "是否输出同一天的手术项目(1是0否)")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String isSameDay;
	/**收费项目名称类型*/
	@Excel(name = "收费项目名称类型", width = 15)
	@ApiModelProperty(value = "收费项目名称类型")
	@MedicalDict(dicCode = "ITEM1_TYPE")
	private java.lang.String itemType;
	/**收费项目名称*/
	@Excel(name = "收费项目名称", width = 15)
	@ApiModelProperty(value = "收费项目名称")
	private java.lang.String itemname;
	/**同时存在收费项目1*/
	@Excel(name = "同时存在收费项目1", width = 15)
	@ApiModelProperty(value = "同时存在收费项目1")
	private java.lang.String itemname1;
	/**同时存在收费项目2*/
	@Excel(name = "同时存在收费项目2", width = 15)
	@ApiModelProperty(value = "同时存在收费项目2")
	private java.lang.String itemname2;
	/**条件json*/
	@Excel(name = "条件json", width = 15)
	@ApiModelProperty(value = "条件json")
	private java.lang.String jsonStr;
	/**出院时间*/
	@Excel(name = "出院时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "出院时间")
	private java.util.Date leavedate;
	/**显示排序*/
	@Excel(name = "显示排序", width = 15)
	@ApiModelProperty(value = "显示排序")
	private java.lang.String orderby;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
	@ApiModelProperty(value = "机构名称")
	private java.lang.String orgs;
	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgids;
	/**完成时间*/
	@Excel(name = "完成时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "完成时间")
	private java.util.Date overTime;
	/**记录数*/
	@Excel(name = "记录数", width = 15)
	@ApiModelProperty(value = "记录数")
	private java.lang.Integer recordCount;
	/**状态*/
	@Excel(name = "状态", width = 15)
	@ApiModelProperty(value = "状态")
	private java.lang.String status;
	/**算法标签ID*/
	@Excel(name = "算法标签ID", width = 15)
	@ApiModelProperty(value = "算法标签ID")
	private java.lang.String tagId;
	/**任务类型(search查询yearStatistics年度统计)*/
	@Excel(name = "任务类型(search查询yearStatistics年度统计)", width = 15)
	@ApiModelProperty(value = "任务类型(search查询yearStatistics年度统计)")
	private java.lang.String taskType;
	/**重跑时间*/
	@Excel(name = "重跑时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "重跑时间")
	private java.util.Date updateTime;
	/**重跑人*/
	@Excel(name = "重跑人", width = 15)
	@ApiModelProperty(value = "重跑人")
	private java.lang.String updateUser;
	/**重跑人ID*/
	@Excel(name = "重跑人ID", width = 15)
	@ApiModelProperty(value = "重跑人ID")
	private java.lang.String updateUserId;
	/**就诊号*/
	@Excel(name = "就诊号", width = 15)
	@ApiModelProperty(value = "就诊号")
	private java.lang.String visitid;
	/**就诊类型(ZY住院MM门慢)*/
	@Excel(name = "就诊类型(ZY住院MM门慢)", width = 15)
	@ApiModelProperty(value = "就诊类型(ZY住院MM门慢)")
	private java.lang.String visittype;
	/**一次就诊超过的数量*/
	@Excel(name = "一次就诊超过的数量", width = 15)
	@ApiModelProperty(value = "一次就诊超过的数量")
	private java.lang.Integer vistidQty;

	/**收费项目编码*/
	@Excel(name = "收费项目编码", width = 15)
	@ApiModelProperty(value = "收费项目编码")
	private java.lang.String itemcode;

	/**重复收费类型*/
	@Excel(name = "重复收费类型", width = 15)
	@ApiModelProperty(value = "重复收费类型")
	@MedicalDict(dicCode = "ITEM1_TYPE")
	private java.lang.String item1Type;

	/**病案号*/
	@Excel(name = "病案号", width = 15)
	@ApiModelProperty(value = "病案号")
	private java.lang.String caseId;

	/**收费项目B违规判断*/
	@Excel(name = "收费项目B违规判断", width = 15)
	@ApiModelProperty(value = "收费项目B违规判断")
	@MedicalDict(dicCode = "ITEM1_WGTYPE")
	private java.lang.String item1Wgtype;

	/**超量检查的类型*/
	@Excel(name = "超量检查的类型", width = 15)
	@ApiModelProperty(value = "超量检查的类型")
	@MedicalDict(dicCode = "QTY_TYPE")
	private java.lang.String qtyType;

	/**超量的数值(不含)*/
	@Excel(name = "超量的数值(不含)", width = 15)
	@ApiModelProperty(value = "超量的数值(不含)")
	private java.lang.Integer qtyNum;

	/**是否输出月度用量*/
	@Excel(name = "是否输出月度用量", width = 15)
	@ApiModelProperty(value = "是否输出月度用量")
	private java.lang.String isSearchDrug;

	/**算法标签名称*/
	@Excel(name = "算法标签名称", width = 15)
	@ApiModelProperty(value = "算法标签名称")
	private java.lang.String tagName;
	/**患者id*/
	@Excel(name = "患者id", width = 15)
	@ApiModelProperty(value = "患者id")
	private java.lang.String clientid;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
	@ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**医生id*/
	@Excel(name = "医生id", width = 15)
	@ApiModelProperty(value = "医生id")
	private java.lang.String doctorid;
	/**就诊时间*/
	@Excel(name = "就诊时间", width = 15)
	@ApiModelProperty(value = "就诊时间")
	private java.lang.String visitdate;

	/**诊断关键字*/
	@Excel(name = "诊断关键字", width = 15)
	@ApiModelProperty(value = "诊断关键字")
	private java.lang.String diseasename;

	/**违规案例提示*/
	@Excel(name = "违规案例提示", width = 15)
	@ApiModelProperty(value = "违规案例提示")
	private java.lang.String wgRemark;

	/**患者身份证号*/
	@Excel(name = "患者身份证号", width = 15)
	@ApiModelProperty(value = "患者身份证号")
	private java.lang.String idNo;

	/**机构属性*/
	@TableField(exist=false)
	private java.lang.String owntype;

	/**机构所在地*/
	@TableField(exist=false)
	private java.lang.String localTag;

	/**机构等级*/
	@TableField(exist=false)
	private java.lang.String hosplevel;

	/**年度基金支付合计金额比较符*/
	@TableField(exist=false)
	private java.lang.String fundValType;

	/**年度基金支付合计金额*/
	@TableField(exist=false)
	private java.lang.String maxAllfundPay;

	/**手术名称*/
	@TableField(exist=false)
	private java.lang.String surgeryName;

	/**收费清单中不包含*/
	@TableField(exist=false)
	private java.lang.String excludeItemname;

	/**患者名称+出生日期*/
	@TableField(exist=false)
	private java.lang.String name;

	/**在院日期 ，查询使用*/
	@TableField(exist=false)
	private java.lang.String inHospitalDate;

	/**收费日期 ，明细查询使用*/
	@TableField(exist=false)
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private java.util.Date itemChargedate;
}
