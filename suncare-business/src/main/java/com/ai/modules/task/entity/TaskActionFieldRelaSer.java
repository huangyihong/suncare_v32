package com.ai.modules.task.entity;

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
 * @Description: 动态表单搜索条件关联
 * @Author: jeecg-boot
 * @Date:   2021-03-19
 * @Version: V1.0
 */
@Data
@TableName("TASK_ACTION_FIELD_RELA_SER")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_ACTION_FIELD_RELA_SER对象", description="动态表单搜索条件关联")
public class TaskActionFieldRelaSer {
    
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**主体主键*/
	@Excel(name = "主体主键", width = 15)
    @ApiModelProperty(value = "主体主键")
	private java.lang.String configId;
	/**字段主键*/
	@Excel(name = "字段主键", width = 15)
    @ApiModelProperty(value = "字段主键")
	private java.lang.String colId;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private java.lang.Integer orderNo;
	/**使用编辑字段显示*/
	@Excel(name = "使用编辑字段显示", width = 15)
    @ApiModelProperty(value = "使用编辑字段显示")
	private java.lang.String colCnname;
}
