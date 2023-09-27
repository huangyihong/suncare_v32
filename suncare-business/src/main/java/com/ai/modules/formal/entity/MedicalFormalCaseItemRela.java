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
 * @Description: 模型关联项目药品或组
 * @Author: jeecg-boot
 * @Date:   2020-07-17
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FORMAL_CASE_ITEM_RELA")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FORMAL_CASE_ITEM_RELA对象", description="模型关联项目药品或组")
public class MedicalFormalCaseItemRela {

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**caseId*/
	@Excel(name = "caseId", width = 15)
    @ApiModelProperty(value = "caseId")
	private java.lang.String caseId;
	/**type*/
	@Excel(name = "type", width = 15)
    @ApiModelProperty(value = "type")
	private java.lang.String type;
	/**itemIds*/
	@Excel(name = "itemIds", width = 15)
    @ApiModelProperty(value = "itemIds")
	private java.lang.String itemIds;
	/**itemNames*/
	@Excel(name = "itemNames", width = 15)
	@ApiModelProperty(value = "itemNames")
	private java.lang.String itemNames;
	/**updateTime*/
	@Excel(name = "updateTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "updateTime")
	private java.util.Date updateTime;
	/**updateUser*/
	@Excel(name = "updateUser", width = 15)
    @ApiModelProperty(value = "updateUser")
	private java.lang.String updateUser;
	/**createTime*/
	@Excel(name = "createTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "createTime")
	private java.util.Date createTime;
	/**createUser*/
	@Excel(name = "createUser", width = 15)
    @ApiModelProperty(value = "createUser")
	private java.lang.String createUser;
}
