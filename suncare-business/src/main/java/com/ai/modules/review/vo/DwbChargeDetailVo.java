package com.ai.modules.review.vo;

import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value="DWB_CHARGE_DETAIL对象", description="收费明细表")
public class  DwbChargeDetailVo {
	/**审核状态*/
	@Excel(name = "审核状态", width = 15)
    @ApiModelProperty(value = "审核状态")
	private java.lang.String checkstatus;
	/**执行天数*/
	@Excel(name = "执行天数", width = 15)
    @ApiModelProperty(value = "执行天数")
	private java.lang.Double days;
	/**数据来源编码*/
	@Excel(name = "数据来源编码", width = 15)
    @ApiModelProperty(value = "数据来源编码")
	private java.lang.String dataResouceId;
	/**数据来源名称*/
	@Excel(name = "数据来源名称", width = 15)
    @ApiModelProperty(value = "数据来源名称")
	private java.lang.String dataResouce;
	/**etl来源编码*/
	@Excel(name = "etl来源编码", width = 15)
    @ApiModelProperty(value = "etl来源编码")
	private java.lang.String etlSource;
	/**etl来源名称*/
	@Excel(name = "etl来源名称", width = 15)
    @ApiModelProperty(value = "etl来源名称")
	private java.lang.String etlSourceName;
	/**etl时间*/
	@Excel(name = "etl时间", width = 15)
    @ApiModelProperty(value = "etl时间")
	private java.lang.String etlTime;
	/**id*/
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**收费明细表唯一标识*/
	@Excel(name = "收费明细表唯一标识", width = 15)
    @ApiModelProperty(value = "收费明细表唯一标识")
	private java.lang.String chargeid;
	/**结算主表唯一标识*/
	@Excel(name = "结算主表唯一标识", width = 15)
    @ApiModelProperty(value = "结算主表唯一标识")
	private java.lang.String settlementid;
	/**结算日期*/
	@Excel(name = "结算日期", width = 15)
    @ApiModelProperty(value = "结算日期")
	private java.lang.String settlementdate;
	/**医保/农合收费明细id*/
	@Excel(name = "医保/农合收费明细id", width = 15)
    @ApiModelProperty(value = "医保/农合收费明细id")
	private java.lang.String ybChargeid;
	/**his收费明细id*/
	@Excel(name = "his收费明细id", width = 15)
    @ApiModelProperty(value = "his收费明细id")
	private java.lang.String hisChargeid;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private java.lang.String visitid;
	/**医保/农合就诊id*/
	@Excel(name = "医保/农合就诊id", width = 15)
    @ApiModelProperty(value = "医保/农合就诊id")
	private java.lang.String ybVisitid;
	/**his就诊id*/
	@Excel(name = "his就诊id", width = 15)
    @ApiModelProperty(value = "his就诊id")
	private java.lang.String hisVisitid;
	/**就诊类型id*/
	@Excel(name = "就诊类型id", width = 15)
    @ApiModelProperty(value = "就诊类型id")
	private java.lang.String visittypeId;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**yx患者编号*/
	@Excel(name = "yx患者编号", width = 15)
    @ApiModelProperty(value = "yx患者编号")
	private java.lang.String clientid;
	/**yx患者名称*/
	@Excel(name = "yx患者名称", width = 15)
    @ApiModelProperty(value = "yx患者名称")
	private java.lang.String clientname;
	/**单据号*/
	@Excel(name = "单据号", width = 15)
    @ApiModelProperty(value = "单据号")
	private java.lang.String recieptno;
	/**定点医疗机构编号*/
	@Excel(name = "定点医疗机构编号", width = 15)
    @ApiModelProperty(value = "定点医疗机构编号")
	private java.lang.String orgid;
	/**就诊医疗机构名称*/
	@Excel(name = "就诊医疗机构名称", width = 15)
    @ApiModelProperty(value = "就诊医疗机构名称")
	private java.lang.String orgname;
	/**就诊科室编码*/
	@Excel(name = "就诊科室编码", width = 15)
    @ApiModelProperty(value = "就诊科室编码")
	private java.lang.String deptid;
	/**就诊科室名称*/
	@Excel(name = "就诊科室名称", width = 15)
    @ApiModelProperty(value = "就诊科室名称")
	private java.lang.String deptname;
	/**处方医师编码*/
	@Excel(name = "处方医师编码", width = 15)
    @ApiModelProperty(value = "处方医师编码")
	private java.lang.String doctorid;
	/**处方医师名称*/
	@Excel(name = "处方医师名称", width = 15)
    @ApiModelProperty(value = "处方医师名称")
	private java.lang.String doctorname;
	/**处方号*/
	@Excel(name = "处方号", width = 15)
    @ApiModelProperty(value = "处方号")
	private java.lang.String prescriptno;
	/**医嘱编号*/
	@Excel(name = "医嘱编号", width = 15)
    @ApiModelProperty(value = "医嘱编号")
	private java.lang.String orderid;
	/**处方日期*/
	@Excel(name = "处方日期", width = 15)
    @ApiModelProperty(value = "处方日期")
	private java.lang.String prescripttime;
	@Excel(name = "收费日期", width = 15)
    @ApiModelProperty(value = "收费日期")
	private java.lang.String chargedate;
	/**项目编码*/
	@Excel(name = "项目编码", width = 15)
    @ApiModelProperty(value = "项目编码")
	private java.lang.String itemcode;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String itemname;
	/**项目编码_src*/
	@Excel(name = "项目编码_src", width = 15)
	@ApiModelProperty(value = "项目编码_src")
	private java.lang.String itemcodeSrc;
	/**项目名称_src*/
	@Excel(name = "项目名称_src", width = 15)
	@ApiModelProperty(value = "项目名称_src")
	private java.lang.String itemnameSrc;
	@Excel(name = "医院项目名称_src", width = 15)
	@ApiModelProperty(value = "医院项目名称_src")
	private java.lang.String hisItemnameSrc;
	/**处方药标志*/
	@Excel(name = "处方药标志", width = 15)
    @ApiModelProperty(value = "处方药标志")
	private java.lang.String presdrugSign;
	/**特殊材料标志*/
	@Excel(name = "特殊材料标志", width = 15)
    @ApiModelProperty(value = "特殊材料标志")
	private java.lang.String specialmaterialSign;
	/**草药单复方标志*/
	@Excel(name = "草药单复方标志", width = 15)
    @ApiModelProperty(value = "草药单复方标志")
	private java.lang.String herbcountSign;
	/**剂型代码*/
	@Excel(name = "剂型代码", width = 15)
    @ApiModelProperty(value = "剂型代码")
	private java.lang.String dosageform;
	/**剂型名称*/
	@Excel(name = "剂型名称", width = 15)
    @ApiModelProperty(value = "剂型名称")
	private java.lang.String dosagename;
	/**规格*/
	@Excel(name = "规格", width = 15)
    @ApiModelProperty(value = "规格")
	private java.lang.String specificaion;
	/**计价单位*/
	@Excel(name = "计价单位", width = 15)
    @ApiModelProperty(value = "计价单位")
	private java.lang.String chargeunit;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
	private Double itemprice;
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private Double amount;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private Double fee;
	/**项目类别代码*/
	@Excel(name = "项目类别代码", width = 15)
    @ApiModelProperty(value = "项目类别代码")
	private java.lang.String itemclassId;
	/**项目类别*/
	@Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
	private java.lang.String itemclass;
	/**收费类别编码*/
	@Excel(name = "收费类别编码", width = 15)
    @ApiModelProperty(value = "收费类别编码")
	private java.lang.String chargeclassId;
	/**收费类别名称*/
	@Excel(name = "收费类别名称", width = 15)
    @ApiModelProperty(value = "收费类别名称")
	private java.lang.String chargeclass;
	/**收费项目等级代码*/
	@Excel(name = "收费项目等级", width = 15)
    @ApiModelProperty(value = "收费项目等级")
	private java.lang.String chargeattri;
	@Excel(name = "收费项目等级代码", width = 15)
    @ApiModelProperty(value = "收费项目等级代码")
	private java.lang.String chargeattriId;
	/**用法*/
	@Excel(name = "用法", width = 15)
    @ApiModelProperty(value = "用法")
	private java.lang.String usage;
	/**每次用量*/
	@Excel(name = "每次用量", width = 15)
    @ApiModelProperty(value = "每次用量")
	private Double dosage;
	/**用量单位*/
	@Excel(name = "用量单位", width = 15)
    @ApiModelProperty(value = "用量单位")
	private java.lang.String dosageunit;
	/**使用频次*/
	@Excel(name = "使用频次", width = 15)
    @ApiModelProperty(value = "使用频次")
	private java.lang.String frequency;
	/**有效标志*/
	@Excel(name = "有效标志", width = 15)
    @ApiModelProperty(value = "有效标志")
	private java.lang.String valid;
	/**生育记账类别*/
	@Excel(name = "生育记账类别", width = 15)
    @ApiModelProperty(value = "生育记账类别")
	private java.lang.String birthinsuranceSign;
	/**全额自费标志*/
	@Excel(name = "全额自费标志", width = 15)
    @ApiModelProperty(value = "全额自费标志")
	private java.lang.String allselfpaySign;
	/**自付类别*/
	@Excel(name = "自付类别", width = 15)
    @ApiModelProperty(value = "自付类别")
	private java.lang.String selfpayClass;
	/**自付比例*/
	@Excel(name = "自付比例", width = 15)
    @ApiModelProperty(value = "自付比例")
	private Double selfpayProp;
	/**自付金额*/
	@Excel(name = "自付金额", width = 15)
    @ApiModelProperty(value = "自付金额")
	private Double selfpayFee;
	/**丙类自费金额*/
	@Excel(name = "丙类自费金额", width = 15)
    @ApiModelProperty(value = "丙类自费金额")
	private Double selfpay3;
	/**最高限价*/
	@Excel(name = "最高限价", width = 15)
    @ApiModelProperty(value = "最高限价")
	private Double limitprice;
	/**计费科室编码*/
	@Excel(name = "计费科室编码", width = 15)
    @ApiModelProperty(value = "计费科室编码")
	private java.lang.String chargedepartId;
	/**执行科室编码*/
	@Excel(name = "执行科室编码", width = 15)
    @ApiModelProperty(value = "执行科室编码")
	private java.lang.String impledepartId;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String checkperson;
	/**审核日期*/
	@Excel(name = "审核日期", width = 15)
    @ApiModelProperty(value = "审核日期")
	private java.lang.String checkdate;
}
