package com.ai.modules.formal.entity;

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
 * @Description: 不合规行为关联表
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FORMAL_CASE_BEHAVIOR")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FORMAL_CASE_BEHAVIOR对象", description="不合规行为关联表")
public class MedicalFormalCaseBehavior {
    
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String relaId;
	/**不合规行为ID*/
	@Excel(name = "不合规行为ID", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String behaviorId;
	/**模型ID*/
	@Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
	private java.lang.String caseId;
}
