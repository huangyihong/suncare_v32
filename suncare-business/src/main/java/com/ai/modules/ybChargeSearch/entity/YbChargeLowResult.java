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
 * @Description: 低标准入院结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_low_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_low_result对象", description="低标准入院结果表")
public class YbChargeLowResult {

	/**床位费*/
	@Excel(name = "床位费", width = 15)
    @ApiModelProperty(value = "床位费")
	private java.math.BigDecimal bedAmt;
	/**检查费*/
	@Excel(name = "检查费", width = 15)
    @ApiModelProperty(value = "检查费")
	private java.math.BigDecimal checkTotalAmt;
	/**中成药费*/
	@Excel(name = "中成药费", width = 15)
    @ApiModelProperty(value = "中成药费")
	private java.math.BigDecimal chimedAmt;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**耗材费*/
	@Excel(name = "耗材费", width = 15)
    @ApiModelProperty(value = "耗材费")
	private java.math.BigDecimal conamteAmt;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
	private java.lang.String deptname;
	/**诊查费*/
	@Excel(name = "诊查费", width = 15)
    @ApiModelProperty(value = "诊查费")
	private java.math.BigDecimal diagAmt;
	/**全部诊断*/
	@Excel(name = "全部诊断", width = 15)
    @ApiModelProperty(value = "全部诊断")
	private java.lang.String dis;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**其他费*/
	@Excel(name = "其他费", width = 15)
    @ApiModelProperty(value = "其他费")
	private java.math.BigDecimal elsefeeAmt;
	/**基金支付金额*/
	@Excel(name = "基金支付金额", width = 15)
    @ApiModelProperty(value = "基金支付金额")
	private java.math.BigDecimal fundpay;
	/**中草药费*/
	@Excel(name = "中草药费", width = 15)
    @ApiModelProperty(value = "中草药费")
	private java.math.BigDecimal herbalAmt;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**参保类型*/
	@Excel(name = "参保类型", width = 15)
    @ApiModelProperty(value = "参保类型")
	private java.lang.String insurancetype;
	/**化验费*/
	@Excel(name = "化验费", width = 15)
    @ApiModelProperty(value = "化验费")
	private java.math.BigDecimal labtestAmt;
	/**出院时间*/
	@Excel(name = "出院时间", width = 15)
    @ApiModelProperty(value = "出院时间")
	private java.lang.String leavedate;
	/**护理费*/
	@Excel(name = "护理费", width = 15)
    @ApiModelProperty(value = "护理费")
	private java.math.BigDecimal nursingAmt;
	/**手术费*/
	@Excel(name = "手术费", width = 15)
    @ApiModelProperty(value = "手术费")
	private java.math.BigDecimal operationAmt;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**算法名称*/
	@Excel(name = "算法名称", width = 15)
    @ApiModelProperty(value = "算法名称")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**总费用*/
	@Excel(name = "总费用", width = 15)
    @ApiModelProperty(value = "总费用")
	private java.math.BigDecimal totalfee;
	/**治疗费*/
	@Excel(name = "治疗费", width = 15)
    @ApiModelProperty(value = "治疗费")
	private java.math.BigDecimal treatAmt;
	/**入院时间*/
	@Excel(name = "入院时间", width = 15)
    @ApiModelProperty(value = "入院时间")
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
	/**西药费*/
	@Excel(name = "西药费", width = 15)
    @ApiModelProperty(value = "西药费")
	private java.math.BigDecimal wmAmt;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;

	/**原主键id*/
	@Excel(name = "原主键id", width = 15)
	@ApiModelProperty(value = "原主键id")
	private java.lang.String srcId;

	/**标注内容*/
	@Excel(name = "标注内容", width = 15)
	@ApiModelProperty(value = "标注内容")
	private java.lang.String labelName;

	/**标注人*/
	@Excel(name = "标注人", width = 15)
	@ApiModelProperty(value = "标注人")
	private java.lang.String labelUser;

	/**标注时间*/
	@Excel(name = "标注时间", width = 15)
	@ApiModelProperty(value = "标注时间")
	private java.lang.String labelTime;
}
