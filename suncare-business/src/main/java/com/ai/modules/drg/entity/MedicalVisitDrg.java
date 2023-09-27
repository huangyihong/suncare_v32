package com.ai.modules.drg.entity;

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
 * @Description: 病历drg标识
 * @Author: jeecg-boot
 * @Date:   2023-04-07
 * @Version: V1.0
 */
@Data
@TableName("medical_visit_drg")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="medical_visit_drg对象", description="病历drg标识")
public class MedicalVisitDrg {
    
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**批次*/
	@Excel(name = "批次", width = 15)
    @ApiModelProperty(value = "批次")
	private java.lang.String batchId;
	/**计算逻辑版本号*/
	@Excel(name = "计算逻辑版本号", width = 15)
    @ApiModelProperty(value = "计算逻辑版本号")
	private java.lang.String logicV;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private java.lang.String visitid;
	/**医院id*/
	@Excel(name = "医院id", width = 15)
    @ApiModelProperty(value = "医院id")
	private java.lang.String orgid;
	/**MDC*/
	@Excel(name = "MDC", width = 15)
    @ApiModelProperty(value = "MDC")
	private java.lang.String mdc;
	/**MDC名称*/
	@Excel(name = "MDC名称", width = 15)
    @ApiModelProperty(value = "MDC名称")
	private java.lang.String mdcName;
	/**ADRG编码*/
	@Excel(name = "ADRG编码", width = 15)
    @ApiModelProperty(value = "ADRG编码")
	private java.lang.String adrg;
	/**ADRG名称*/
	@Excel(name = "ADRG名称", width = 15)
    @ApiModelProperty(value = "ADRG名称")
	private java.lang.String adrgName;
	/**DRG*/
	@Excel(name = "DRG", width = 15)
    @ApiModelProperty(value = "DRG")
	private java.lang.String drg;
	/**DRG名称*/
	@Excel(name = "DRG名称", width = 15)
    @ApiModelProperty(value = "DRG名称")
	private java.lang.String drgName;
	/**满足ADRG分组步骤*/
	@Excel(name = "满足ADRG分组步骤", width = 15)
    @ApiModelProperty(value = "满足ADRG分组步骤")
	private java.lang.String adrgStep;
	/**ADRG分组步骤描述*/
	@Excel(name = "ADRG分组步骤描述", width = 15)
    @ApiModelProperty(value = "ADRG分组步骤描述")
	private java.lang.String adrgStepDesc;
	/**满足DRG分组步骤*/
	@Excel(name = "满足DRG分组步骤", width = 15)
    @ApiModelProperty(value = "满足DRG分组步骤")
	private java.lang.String drgStep;
	/**DRG分组步骤描述*/
	@Excel(name = "DRG分组步骤描述", width = 15)
    @ApiModelProperty(value = "DRG分组步骤描述")
	private java.lang.String drgStepDesc;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String project;
	/**createdBy*/
	@Excel(name = "createdBy", width = 15)
    @ApiModelProperty(value = "createdBy")
	private java.lang.String createdBy;
	/**createdByName*/
	@Excel(name = "createdByName", width = 15)
    @ApiModelProperty(value = "createdByName")
	private java.lang.String createdByName;
	/**createdTime*/
	@Excel(name = "createdTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "createdTime")
	private java.util.Date createdTime;
	/**满足MDC疾病编码*/
	@Excel(name = "满足MDC疾病编码", width = 15)
	@ApiModelProperty(value = "满足MDC疾病编码")
	private java.lang.String mdcDiagCode;
	/**满足MDC疾病名称*/
	@Excel(name = "满足MDC疾病名称", width = 15)
	@ApiModelProperty(value = "满足MDC疾病名称")
	private java.lang.String mdcDiagName;
	/**满足ADRG疾病编码*/
	@Excel(name = "满足ADRG疾病编码", width = 15)
	@ApiModelProperty(value = "满足ADRG疾病编码")
	private java.lang.String adrgDiagCode;
	/**满足ADRG疾病名称*/
	@Excel(name = "满足ADRG疾病名称", width = 15)
	@ApiModelProperty(value = "满足ADRG疾病名称")
	private java.lang.String adrgDiagName;
	/**满足ADRG手术编码*/
	@Excel(name = "满足ADRG手术编码", width = 15)
	@ApiModelProperty(value = "满足ADRG手术编码")
	private java.lang.String adrgSurgeryCode;
	/**满足ADRG手术名称*/
	@Excel(name = "满足ADRG手术名称", width = 15)
	@ApiModelProperty(value = "满足ADRG手术名称")
	private java.lang.String adrgSurgeryName;
	/**DRG主诊断编码*/
	@Excel(name = "DRG主诊断编码", width = 15)
	@ApiModelProperty(value = "DRG主诊断编码")
	private java.lang.String drgDiagCode;
	/**DRG主诊断名称*/
	@Excel(name = "DRG主诊断名称", width = 15)
	@ApiModelProperty(value = "DRG主诊断名称")
	private java.lang.String drgDiagName;
	/**DRG主手术编码*/
	@Excel(name = "DRG主手术编码", width = 15)
	@ApiModelProperty(value = "DRG主手术编码")
	private java.lang.String drgSurgeryCode;
	/**DRG主手术名称*/
	@Excel(name = "DRG主手术名称", width = 15)
	@ApiModelProperty(value = "DRG主手术名称")
	private java.lang.String drgSurgeryName;
}
