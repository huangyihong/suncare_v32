package com.ai.modules.ybFj.entity;

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
 * @Description: 飞检项目线索推送医院
 * @Author: jeecg-boot
 * @Date:   2023-03-14
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_project_clue_push")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project_clue_push对象", description="飞检项目线索推送医院")
public class YbFjProjectCluePush {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String pushId;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private java.lang.String projectId;
	/**关联yb_fj_project_clue.clue_id*/
	@Excel(name = "关联yb_fj_project_clue.clue_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_clue.clue_id")
	private java.lang.String clueId;
	/**推送时间*/
	@Excel(name = "推送时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "推送时间")
	private java.util.Date pushTime;
	/**推送人姓名*/
	@Excel(name = "推送人姓名", width = 15)
    @ApiModelProperty(value = "推送人姓名")
	private java.lang.String pushUsername;
	/**推送人*/
	@Excel(name = "推送人", width = 15)
    @ApiModelProperty(value = "推送人")
	private java.lang.String pushUser;
}
