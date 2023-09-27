package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalClinical;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 临床路径资料信息
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalClinicalIOVO extends MedicalClinical {

	/**信息文字模板*/
	@Excel(name = "信息文字模板", width = 15)
    @ApiModelProperty(value = "信息文字模板")
	private String templWord;
	/**编码规则提示模板*/
	@Excel(name = "编码规则提示模板", width = 15)
    @ApiModelProperty(value = "编码规则提示模板")
	private String templPrompt;
	/**依据适用对象*/
	@Excel(name = "依据适用对象", width = 15)
	@ApiModelProperty(value = "依据适用对象")
	private String basisTarget;
	/**症状体征依据*/
	@Excel(name = "症状体征依据", width = 15)
    @ApiModelProperty(value = "症状体征依据")
	private String basisSymptom;
	/**影像学检查依据*/
	@Excel(name = "影像学检查依据", width = 15)
    @ApiModelProperty(value = "影像学检查依据")
	private String basisCtmri;
	/**实验室检查依据*/
	@Excel(name = "实验室检查依据", width = 15)
    @ApiModelProperty(value = "实验室检查依据")
	private String basisLabExam ;
	/**病理依据*/
	@Excel(name = "病理依据", width = 15)
    @ApiModelProperty(value = "病理依据")
	private String basisPathology;
	/**标准住院日*/
	@Excel(name = "标准住院日", width = 15)
    @ApiModelProperty(value = "标准住院日")
	private Integer standerdInhosDays;
	/**进入路径标准*/
	@Excel(name = "进入路径标准", width = 15)
    @ApiModelProperty(value = "进入路径标准")
	private String conformStanderd;
	/**必须的检查项目*/
	@Excel(name = "必须的检查项目", width = 15)
    @ApiModelProperty(value = "必须的检查项目")
	private String requiredCheckItem;
	/**可选的检查项目*/
	@Excel(name = "可选的检查项目", width = 15)
    @ApiModelProperty(value = "可选的检查项目")
	private String optionalCheckItem;
	/**治疗方案的选择*/
	@Excel(name = "治疗方案的选择", width = 15)
    @ApiModelProperty(value = "治疗方案的选择")
	private String treatmentOptions;
	/**出院标准*/
	@Excel(name = "出院标准", width = 15)
    @ApiModelProperty(value = "出院标准")
	private String dischargeStandard;
	/**变异原因及分析*/
	@Excel(name = "变异原因及分析", width = 15)
    @ApiModelProperty(value = "变异原因及分析")
	private String variationCauseAnalyse;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private String remark;
	/**临床路径来源*/
	@Excel(name = "临床路径来源", width = 15)
	@ApiModelProperty(value = "临床路径来源")
	private String clinicalSource;
	/**临床路径附件*/
	@Excel(name = "临床路径附件", width = 15)
    @ApiModelProperty(value = "临床路径附件")
	private String clinicalFile;
}
