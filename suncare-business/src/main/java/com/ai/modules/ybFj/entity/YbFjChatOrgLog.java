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
 * @Description: 飞检项目聊天记录
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_chat_org_log")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_chat_org_log对象", description="飞检项目聊天记录")
public class YbFjChatOrgLog {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String logId;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
    @ApiModelProperty(value = "医疗机构编码")
	private java.lang.String orgId;
	/**传输类型{sys:系统,org:医院}*/
	@Excel(name = "传输类型{sys:系统,org:医院}", width = 15)
    @ApiModelProperty(value = "传输类型{sys:系统,org:医院}")
	private java.lang.String transferType;
	/**消息*/
	@Excel(name = "消息", width = 15)
    @ApiModelProperty(value = "消息")
	private java.lang.String chatMsg;
	/**消息类型{txt:文本,file:文件}*/
	@Excel(name = "消息类型{txt:文本,file:文件}", width = 15)
    @ApiModelProperty(value = "消息类型{txt:文本,file:文件}")
	private java.lang.String chatType;
	/**是否已读*/
	@Excel(name = "是否已读", width = 15)
	@ApiModelProperty(value = "是否已读")
	private java.lang.String readState;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
}
