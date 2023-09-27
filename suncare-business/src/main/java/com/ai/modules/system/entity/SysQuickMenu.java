package com.ai.modules.system.entity;

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
 * @Description: 用户快捷菜单
 * @Author: jeecg-boot
 * @Date:   2019-12-17
 * @Version: V1.0
 */
@Data
@TableName("SYS_QUICK_MENU")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="SYS_QUICK_MENU对象", description="用户快捷菜单")
public class SysQuickMenu {

	/**ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "ID")
	private java.lang.String id;
	/**用户ID*/
	@Excel(name = "用户ID", width = 15)
    @ApiModelProperty(value = "用户ID")
	private java.lang.String userId;
	/**菜单ID*/
	@Excel(name = "菜单ID", width = 15)
    @ApiModelProperty(value = "菜单ID")
	private java.lang.String menuId;
	/**排序值*/
	@Excel(name = "排序值", width = 15)
    @ApiModelProperty(value = "排序值")
	private java.lang.Integer orderNo;
}
