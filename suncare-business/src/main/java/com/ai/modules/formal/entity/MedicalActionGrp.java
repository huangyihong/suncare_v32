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
 * @Description: 不合理行为组表
 * @Author: jeecg-boot
 * @Date:   2019-12-02
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_ACTION_GRP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_ACTION_GRP对象", description="不合理行为组表")
public class MedicalActionGrp {
    
	/**主键ID*/
	@Excel(name = "主键ID", width = 15)
    @ApiModelProperty(value = "主键ID")
	@TableId("ACTION_GRP_ID")
	private java.lang.String actionGrpId;
	/**组名称*/
	@Excel(name = "组名称", width = 15)
    @ApiModelProperty(value = "组名称")
	private java.lang.String actionGrpName;
	/**上一级ID*/
	@Excel(name = "上一级ID", width = 15)
    @ApiModelProperty(value = "上一级ID")
	private java.lang.String actionGrpPid;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @ApiModelProperty(value = "排序号")
	private Double sortNo;
}
