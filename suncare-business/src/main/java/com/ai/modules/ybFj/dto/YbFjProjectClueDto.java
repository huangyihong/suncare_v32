package com.ai.modules.ybFj.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @Description: 飞检项目线索
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
@Data
public class YbFjProjectClueDto {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private String clueId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private String projectOrgId;
	/**问题类别（一级）*/
	@Excel(name = "问题类别（一级）", width = 15)
    @ApiModelProperty(value = "问题类别（一级）")
	private String issueType;
	/**问题类型（二级）*/
	@Excel(name = "问题类型（二级）", width = 15)
    @ApiModelProperty(value = "问题类型（二级）")
	private String issueSubtype;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private String clueName;
	/**项目类别*/
	@Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
	private String clueType;
	/**涉及数量*/
	@Excel(name = "涉及数量", width = 15)
    @ApiModelProperty(value = "涉及数量")
	private Integer caseAmount;
	/**涉及人次*/
	@Excel(name = "涉及人次", width = 15)
    @ApiModelProperty(value = "涉及人次")
	private Integer casePersonCnt;
	/**涉及金额（单位：元）*/
	@Excel(name = "涉及金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及金额（单位：元）")
	private java.math.BigDecimal caseFee;
	/**涉及医保基金金额（单位：元）*/
	@Excel(name = "涉及医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及医保基金金额（单位：元）")
	private java.math.BigDecimal caseFundFee;
	/**违规说明*/
	@Excel(name = "违规说明", width = 15)
    @ApiModelProperty(value = "违规说明")
	private String caseRemark;

	@ApiModelProperty(value = "列表选中ID")
	private String selections;
}
