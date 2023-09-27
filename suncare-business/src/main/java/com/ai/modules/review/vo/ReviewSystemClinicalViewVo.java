package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;


@Data
@ApiModel(value="DWB_MASTER_INFO对象", description="就诊信息")
public class ReviewSystemClinicalViewVo {
	/**id*/
    @ApiModelProperty(value = "id")
	private String id;
	/**就诊id*/
	@Excel(name = "就诊ID", width = 15)
    @ApiModelProperty(value = "就诊ID")
	private String visitid;

	@Excel(name = "临川路径编码", width = 25)
	@ApiModelProperty(value = "临川路径编码")
	private String caseId;

	@Excel(name = "临川路径名称", width = 25)
	@ApiModelProperty(value = "临川路径名称")
	private String caseName;

	@Excel(name = "关联项目名称", width = 15)
	@ApiModelProperty(value = "关联项目名称")
	private String itemName;

	@Excel(name = "关联项目时间", width = 30)
	@ApiModelProperty(value = "关联项目时间")
	private String itemTime;

	@Excel(name = "最低涉案金额", width = 25)
	@ApiModelProperty(value = "项目编码")
	private Double minActionMoney;

	@Excel(name = "最高涉案金额", width = 25)
	@ApiModelProperty(value = "最高涉案金额")
	private Double maxActionMoney;

	@Excel(name = "推送状态", width = 40)
	@ApiModelProperty(value = "推送状态")
	private String pushStatus;

/*	@Excel(name = "判定结果", width = 40)
	@ApiModelProperty(value = "判定结果")
	private String firReviewStatus;*/

	@Excel(name = "审核人", width = 40)
	@ApiModelProperty(value = "审核人")
	private String firReviewUsername;


}
