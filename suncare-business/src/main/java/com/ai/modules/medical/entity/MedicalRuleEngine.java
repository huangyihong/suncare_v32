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
 * @Description: 规则计算引擎表
 * @Author: jeecg-boot
 * @Date:   2020-11-13
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_RULE_ENGINE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_RULE_ENGINE对象", description="规则计算引擎表")
public class MedicalRuleEngine {
    
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	private java.lang.String ruleId;
	/**规则类型{DRUG_USE:合理用药}*/
	@Excel(name = "规则类型{DRUG_USE:合理用药}", width = 15)
    @ApiModelProperty(value = "规则类型{DRUG_USE:合理用药}")
	private java.lang.String ruleType;
	/**状态{1:有效,0:无效}*/
	@Excel(name = "状态{1:有效,0:无效}", width = 15)
    @ApiModelProperty(value = "状态{1:有效,0:无效}")
	private java.lang.String status;
}
