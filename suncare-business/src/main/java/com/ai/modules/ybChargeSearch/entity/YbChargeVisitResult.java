package com.ai.modules.ybChargeSearch.entity;

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
 * @Description: 住院门慢清单结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_visit_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_visit_result对象", description="住院门慢清单结果表")
public class YbChargeVisitResult {

	/**科室*/
	@Excel(name = "科室", width = 15)
    @ApiModelProperty(value = "科室")
	private java.lang.String deptname;
	/**诊断*/
	@Excel(name = "诊断", width = 15)
    @ApiModelProperty(value = "诊断")
	private java.lang.String dis;
	/**医生*/
	@Excel(name = "医生", width = 15)
    @ApiModelProperty(value = "医生")
	private java.lang.String doctorname;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**患者名称*/
	@Excel(name = "患者名称", width = 15)
    @ApiModelProperty(value = "患者名称")
	private java.lang.String name;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**基金支付总额*/
	@Excel(name = "基金支付总额", width = 15)
    @ApiModelProperty(value = "基金支付总额")
	private java.math.BigDecimal sumFundpay;
	/**住院总费用*/
	@Excel(name = "住院总费用", width = 15)
    @ApiModelProperty(value = "住院总费用")
	private java.math.BigDecimal sumTotalfee;
	/**标签*/
	@Excel(name = "标签", width = 15)
    @ApiModelProperty(value = "标签")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**入院日期*/
	@Excel(name = "入院日期", width = 15)
    @ApiModelProperty(value = "入院日期")
	private java.lang.String visitdate;
	/**住院天数*/
	@Excel(name = "住院天数", width = 15)
    @ApiModelProperty(value = "住院天数")
	private java.lang.Integer visitdays;
	/**就诊号*/
	@Excel(name = "就诊号", width = 15)
    @ApiModelProperty(value = "就诊号")
	private java.lang.String visitid;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;

	/**单位地址*/
	@Excel(name = "单位地址", width = 15)
	@ApiModelProperty(value = "单位地址")
	private java.lang.String  workplacename;

	/**本地/异地*/
	@Excel(name = "本地/异地", width = 15)
	@ApiModelProperty(value = "本地/异地")
	private java.lang.String  ifClientLocal;

    /**医保类型*/
    @Excel(name = "医保类型", width = 15)
    @ApiModelProperty(value = "医保类型")
    private java.lang.String insurancetypename;
}
