package com.ai.modules.action.vo;

import com.ai.modules.action.entity.MedicalBreakBehaviorResult;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 不合规行为结果VO
 * @Author: jeecg-boot
 * @Date:   2020-02-14
 * @Version: V1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value="MEDICAL_BREAK_BEHAVIOR_RESULT扩展对象", description="不合规行为结果")
public class MedicalBreakBehaviorResultVO extends MedicalBreakBehaviorResult {

	/**证件号*/
	@Excel(name = "证件号", width = 15)
	@ApiModelProperty(value = "证件号")
	private String idNo;

	/**证件号*/
	@Excel(name = "参保类型", width = 15)
	@ApiModelProperty(value = "参保类型")
	private String insurancetype;

	/**医疗机构名称*/
	@Excel(name = "医疗机构编码", width = 15)
	@ApiModelProperty(value = "医疗机构编码")
	private String orgid;

	/**医疗机构名称*/
	@Excel(name = "医疗机构名称", width = 15)
	@ApiModelProperty(value = "医疗机构名称")
	private String orgname;

}
