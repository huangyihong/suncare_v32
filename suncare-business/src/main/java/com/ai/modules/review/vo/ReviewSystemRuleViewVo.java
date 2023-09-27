package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;


@Data
@ApiModel(value="DWB_MASTER_INFO对象", description="就诊信息")
public class ReviewSystemRuleViewVo {
	/**id*/
    @ApiModelProperty(value = "id")
	private String id;
	/**就诊id*/
	@Excel(name = "就诊ID", width = 15)
    @ApiModelProperty(value = "就诊ID")
	private String visitid;

	/*@Excel(name = "医疗机构名称", width = 15)
    @ApiModelProperty(value = "医疗机构名称")
	private String orgname;

	@Excel(name = "医疗机构级别", width = 15)
    @ApiModelProperty(value = "医疗机构级别")
	private String hosplevel;*/
	/**患者姓名*/
	@Excel(name = "病人姓名", width = 15)
	@ApiModelProperty(value = "病人姓名")
	private String clientname;

	@Excel(name = "性别名称", width = 6)
	@ApiModelProperty(value = "性别名称")
	private String sex;

	@Excel(name = "原始性别名称", width = 6)
	@ApiModelProperty(value = "原始性别名称")
	private String sexSrc;

	@Excel(name = "年龄（岁）", width = 15)
	@ApiModelProperty(value = "年龄（岁）")
	private Double yearage;

	/**年龄（月）*/
	@Excel(name = "年龄（月）", width = 15)
	@ApiModelProperty(value = "年龄（月）")
	private Double monthage;
	/**年龄（日）*/
	@Excel(name = "年龄（日）", width = 15)
	@ApiModelProperty(value = "年龄（日）")
	private Double dayage;

	/*@Excel(name = "就诊类型", width = 8)
	@ApiModelProperty(value = "就诊类型")
	private String visittype;

	@Excel(name = "就诊类型id", width = 8)
	@ApiModelProperty(value = "就诊类型id")
	private String visittypeId;*/

	@Excel(name = "就诊日期", width = 15)
	@ApiModelProperty(value = "就诊日期")
	private String visitdate;

	/*@Excel(name = "住院天数", width = 15)
	@ApiModelProperty(value = "住院天数")
	private Double zyDaysCalculate;*/

	@Excel(name = "诊断疾病名称", width = 30)
	@ApiModelProperty(value = "诊断疾病名称")
	private String diseasename;

	/*@Excel(name = "项目编码", width = 25)
	@ApiModelProperty(value = "项目编码")
	private String caseId;

	@Excel(name = "项目名称", width = 25)
	@ApiModelProperty(value = "项目名称")
	private String caseName;

	@Excel(name = "冲突项目编码", width = 25)
	@ApiModelProperty(value = "项目编码")
	private List<String> mutexItemCode;

	@Excel(name = "冲突项目名称", width = 25)
	@ApiModelProperty(value = "项目名称")
	private List<String> mutexItemName;

	@Excel(name = "出现次数", width = 7)
	@ApiModelProperty(value = "出现次数")
	private Double itemQty;

	@Excel(name = "涉及金额", width = 15)
	@ApiModelProperty(value = "涉及金额")
	private Double actionMoney;*/

	@Excel(name = "就诊金额", width = 15)
	@ApiModelProperty(value = "就诊金额")
	private Double totalfee;

	/*@Excel(name = "提示信息", width = 40)
	@ApiModelProperty(value = "提示信息")
	private String actionDesc;

	@Excel(name = "违反限定范围", width = 15)
	@ApiModelProperty(value = "违反限定范围")
//	@MedicalDict(dicCode = "LIMIT_SCOPE")
	private List<String> ruleScope;

	@Excel(name = "违反限定范围数组", width = 15)
	@ApiModelProperty(value = "违反限定范围数组")
	private List<String> ruleScopeName;*/

	@Excel(name = "推送状态", width = 40)
	@ApiModelProperty(value = "推送状态")
	private String pushStatus;

	@Excel(name = "判定结果", width = 40)
	@ApiModelProperty(value = "判定结果")
	private String firReviewStatus;

	@Excel(name = "审核人", width = 40)
	@ApiModelProperty(value = "审核人")
	private String firReviewUsername;


}
