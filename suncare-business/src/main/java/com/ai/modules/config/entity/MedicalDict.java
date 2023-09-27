package com.ai.modules.config.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DICT")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DICT对象", description="医疗字典")
public class MedicalDict {

	/**分组主键*/
	@Excel(name = "分组主键", width = 15)
    @ApiModelProperty(value = "分组主键")
	@TableId("GROUP_ID")
	private java.lang.String groupId;
	/**分组编码*/
	@Excel(name = "分组编码", width = 15)
    @ApiModelProperty(value = "分组编码")
	private java.lang.String groupCode;
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
	@org.jeecg.common.aspect.annotation.MedicalDict(dicCode = "DRUG_GROUP_KIND")
	private java.lang.String kind;
}
