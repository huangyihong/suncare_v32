package com.ai.modules.medical.entity;

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
 * @Description: 合理用药配置条件组
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DRUGUSE_RULE_GROUP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUGUSE_RULE_GROUP对象", description="合理用药配置条件组")
public class MedicalDruguseRuleGroup {

	/**条件组ID*/
	@TableId("GROUP_ID")
	@Excel(name = "条件组ID", width = 15)
    @ApiModelProperty(value = "条件组ID")
	private java.lang.String groupId;
	/**条件组名称*/
	@Excel(name = "条件组名称", width = 15)
    @ApiModelProperty(value = "条件组名称")
	private java.lang.String groupName;
	/**组序号*/
	@Excel(name = "组序号", width = 15)
    @ApiModelProperty(value = "组序号")
	private java.lang.Integer groupNo;
	/**所属规则ID*/
	@Excel(name = "所属规则ID", width = 15)
    @ApiModelProperty(value = "所属规则ID")
	private java.lang.String ruleId;
	/**疾病组*/
	@Excel(name = "疾病组", width = 15)
    @ApiModelProperty(value = "疾病组")
	private java.lang.String diseaseGroups;
	/**项目组*/
	@Excel(name = "项目组", width = 15)
    @ApiModelProperty(value = "项目组")
	private java.lang.String treatGroups;
	/**化验结果组*/
	@Excel(name = "化验结果组", width = 15)
    @ApiModelProperty(value = "化验结果组")
	private java.lang.String treatmentGroups;
}
