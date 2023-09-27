package com.ai.modules.medical.entity.vo;

import org.jeecgframework.poi.excel.annotation.Excel;

import com.ai.modules.medical.entity.MedicalDrugRule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MedicalDrugRuleExportVO extends MedicalDrugRule {
	@Excel(name = "治疗项目(名称)", width = 15)
    private String treatProjectStr;
	@Excel(name = "治疗方式(名称)", width = 15)
    private String treatmentStr;
	@Excel(name = "二限用药(名称)", width = 15)
    private String twoLimitDrugStr;
	@Excel(name = "适应症(名称)", width = 15)
    private String indicationStr;
	@Excel(name = "治疗用药(名称)", width = 15)
    private String treatDrugStr;
	@Excel(name = "收费分类编码", width = 15)
	private String  chargeTypeCodesStr;
	@Excel(name = "合用不予支付药品(名称)", width = 15)
    private String twoLimitDrug2Str;
	@Excel(name = "合规项目组(名称)", width = 15)
    private String fitGroupCodesStr;
	@Excel(name = "互斥项目组(名称)", width = 15)
    private String unfitGroupCodesStr;
	@Excel(name = "更新标志", width = 15)
	private java.lang.String importActionType;
    @Excel(name = "试算状态", width = 15)
    @ApiModelProperty(value = "试算状态")
    private String trailStatus;
    @Excel(name = "禁忌症(名称)", width = 15)
    private String unIndicationStr;
    @Excel(name = "互斥项目组(名称)", width = 15)
    private String unfitGroupCodesDayStr;
    @Excel(name = "医疗机构(名称)", width = 15)
    private String orgStr;
    @Excel(name = "频率疾病组(名称)", width = 15)
    private String diseasegroupCodesStr;
    @Excel(name = "数据时间", width = 15)
    private String startEndTimeStr;
    /**定性选择*/
    @Excel(name = "定性选择", width = 15)
    @ApiModelProperty(value = "定性选择")
    private String testResultValue2;
}
