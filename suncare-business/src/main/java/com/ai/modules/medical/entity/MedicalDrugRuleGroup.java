package com.ai.modules.medical.entity;

import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 药品合规规则分组
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DRUG_RULE_GROUP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_RULE_GROUP对象", description="药品合规规则分组")
public class MedicalDrugRuleGroup {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**分组编码*/
	@Excel(name = "分组编码", width = 15)
    @ApiModelProperty(value = "分组编码")
	private java.lang.String groupType;
	/**分组名称*/
	@Excel(name = "分组名称", width = 15)
    @ApiModelProperty(value = "分组名称")
	private java.lang.String groupName;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @ApiModelProperty(value = "排序号")
	private java.lang.Long isOrder;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
	private java.lang.String remark;
	/**机构ID*/
	@Excel(name = "机构ID", width = 15)
    @ApiModelProperty(value = "机构ID")
	private java.lang.String orgId;
	/**种类(1治疗项目 2治疗方式 3重复用药 4二线用药 5适用症 6重复诊疗 7治疗用药  8疾病-项目 9住院天数 99其他)*/
	@Excel(name = "种类(1治疗项目 2治疗方式 3重复用药 4二线用药 5适用症 6重复诊疗 7治疗用药  8疾病-项目 9住院天数 99其他)", width = 15)
    @ApiModelProperty(value = "种类(1治疗项目 2治疗方式 3重复用药 4二线用药 5适用症 6重复诊疗 7治疗用药  8疾病-项目 9住院天数 99其他)")
	@MedicalDict(dicCode = "DRUG_GROUP_KIND")
	private java.lang.String kind;
}
