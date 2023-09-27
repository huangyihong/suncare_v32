package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;
import java.util.Set;


@Data
@ApiModel(value="DWB_MASTER_INFO对象", description="就诊信息")
public class ReviewSystemViewVo {
	/**id*/
    @ApiModelProperty(value = "id")
	private String id;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private String visitid;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private String clientname;
	/**性别名称*/
	@Excel(name = "性别名称", width = 15)
    @ApiModelProperty(value = "性别名称")
	private String sex;
	/**年龄（岁）*/
	@Excel(name = "年龄（岁）", width = 15)
    @ApiModelProperty(value = "年龄（岁）")
	private Double yearage;
	/**医疗费用总金额*/
	@Excel(name = "医疗费用总金额", width = 15)
	@ApiModelProperty(value = "医疗费用总金额")
	private Double totalfee;
	/**就诊日期*/
	@Excel(name = "就诊日期", width = 15)
	@ApiModelProperty(value = "就诊日期")
	private String visitdate;
	/**医疗费用总金额*/
	@Excel(name = "审核通过模型ID", width = 15)
	@ApiModelProperty(value = "审核通过模型ID")
	private List<String> reviewCaseIds;
	/**诊断疾病名称*/
	@Excel(name = "诊断疾病名称", width = 15)
	@ApiModelProperty(value = "诊断疾病名称")
	private String diseasename;
	/**评分*/
	@Excel(name = "评分", width = 15)
	@ApiModelProperty(value = "评分")
	private Float gradeValue;
	/**规则名称*/
	/*@Excel(name = "规则名称", width = 15)
	@ApiModelProperty(value = "评分")
	private Set<String> ruleNames;*/

	/**评分*/
	@Excel(name = "提示信息", width = 15)
	@ApiModelProperty(value = "提示信息")
	private Set<String> ruleDesc;

}
