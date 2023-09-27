package com.ai.modules.ybFj.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 飞检项目审核任务表
 * @Author: jeecg-boot
 * @Date:   2023-03-10
 * @Version: V1.0
 */
@Data
public class YbFjProjectTaskDto {
    
	/**唯一ID*/
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private String taskId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private String projectOrgId;
	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
	private String taskName;
	/**线索数量*/
	@Excel(name = "线索数量", width = 15)
    @ApiModelProperty(value = "线索数量")
	private Integer caseAmount;
	/**线索金额（单位：元）*/
	@Excel(name = "线索金额（单位：元）", width = 15)
    @ApiModelProperty(value = "线索金额（单位：元）")
	private java.math.BigDecimal caseFee;
	/**线索医保基金金额（单位：元）*/
	@Excel(name = "线索医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "线索医保基金金额（单位：元）")
	private java.math.BigDecimal caseFundFee;
	/**任务说明*/
	@Excel(name = "任务说明", width = 15)
    @ApiModelProperty(value = "任务说明")
	private String taskRemark;
	/**紧急程度{low:低,medium:中,high:高}*/
	@Excel(name = "紧急程度{low:低,medium:中,high:高}", width = 15)
    @ApiModelProperty(value = "紧急程度{low:低,medium:中,high:高}")
	private String urgentLevel;
	/**期望审核完成时间*/
	@Excel(name = "期望审核完成时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "期望审核完成时间")
	private Date hopedAuditTime;
	/**线索审核人*/
	@Excel(name = "线索审核人", width = 15)
    @ApiModelProperty(value = "线索审核人")
	private String auditUser;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private String auditUserName;

	/**飞检项目线索id*/
	private String clueIds;
}
