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
 * @Description: 飞检项目线索统计
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_xstj")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_xstj对象", description="飞检项目线索统计")
public class YbFjXstj {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private java.lang.String projectId;
	/**关联的飞检项目机构主键*/
	@Excel(name = "关联的飞检项目机构主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目机构主键")
	private java.lang.String projectOrgId;
	/**关联的上传记录主键*/
	@Excel(name = "关联的上传记录主键", width = 15)
    @ApiModelProperty(value = "关联的上传记录主键")
	private java.lang.String uploadId;
	/**问题类别 （一级指标）*/
	@Excel(name = "问题类别 （一级指标）", width = 15)
    @ApiModelProperty(value = "问题类别 （一级指标）")
	private java.lang.String type1;
	/**问题类别 （二级指标）*/
	@Excel(name = "问题类别 （二级指标）", width = 15)
    @ApiModelProperty(value = "问题类别 （二级指标）")
	private java.lang.String type2;
	/**违规项目*/
	@Excel(name = "违规项目", width = 15)
    @ApiModelProperty(value = "违规项目")
	private java.lang.String wgxmName;
	/**违规说明*/
	@Excel(name = "违规说明", width = 15)
    @ApiModelProperty(value = "违规说明")
	private java.lang.String wgDesc;
	/**涉及数量*/
	@Excel(name = "涉及数量", width = 15)
    @ApiModelProperty(value = "涉及数量")
	private java.lang.Integer wgNum;
	/**违规金额*/
	@Excel(name = "违规金额", width = 15)
    @ApiModelProperty(value = "违规金额")
	private java.math.BigDecimal wgMoney;
	/**医院报销比例*/
	@Excel(name = "医院报销比例", width = 15)
    @ApiModelProperty(value = "医院报销比例")
	private java.math.BigDecimal hospRatio;
	/**项目报销比例*/
	@Excel(name = "项目报销比例", width = 15)
    @ApiModelProperty(value = "项目报销比例")
	private java.math.BigDecimal projectRatio;
	/**支付类别*/
	@Excel(name = "支付类别", width = 15)
    @ApiModelProperty(value = "支付类别")
	private java.lang.String payType;
	/**涉及医保金额*/
	@Excel(name = "涉及医保金额", width = 15)
    @ApiModelProperty(value = "涉及医保金额")
	private java.math.BigDecimal fundpay;
	/**违规认定依据*/
	@Excel(name = "违规认定依据", width = 15)
    @ApiModelProperty(value = "违规认定依据")
	private java.lang.String wgBasis;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
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
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
	@ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
	@ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
}
