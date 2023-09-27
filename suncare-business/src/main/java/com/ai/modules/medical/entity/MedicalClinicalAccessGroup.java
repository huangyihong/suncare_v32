package com.ai.modules.medical.entity;

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
 * @Description: 临床路径条件组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_CLINICAL_ACCESS_GROUP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_CLINICAL_ACCESS_GROUP对象", description="临床路径条件组")
public class MedicalClinicalAccessGroup {

	/**条件组ID*/
	@Excel(name = "条件组ID", width = 15)
    @ApiModelProperty(value = "条件组ID")
	@TableId("GROUP_ID")
	private java.lang.String groupId;
	/**条件组名称*/
	@Excel(name = "条件组名称", width = 15)
    @ApiModelProperty(value = "条件组名称")
	private java.lang.String groupName;
	/**条件组名称*/
	@Excel(name = "条件组类型", width = 15)
	@ApiModelProperty(value = "条件组类型approve准入，reject排除")
	private java.lang.String groupType;
	/**条件组名称*/
	@Excel(name = "条件组序号", width = 15)
	@ApiModelProperty(value = "条件组序号")
	private java.lang.Integer groupNo;
	/**临床路径ID*/
	@Excel(name = "临床路径ID", width = 15)
    @ApiModelProperty(value = "临床路径ID")
	private java.lang.String clinicalId;
	/**患者最小年龄*/
	@Excel(name = "患者最小年龄", width = 15)
    @ApiModelProperty(value = "患者最小年龄")
	private java.lang.Integer patientAgeMin;
	/**患者最大年龄*/
	@Excel(name = "患者最大年龄", width = 15)
    @ApiModelProperty(value = "患者最大年龄")
	private java.lang.Integer patientAgeMax;
	/**患者年龄单位，年、月、日*/
	@Excel(name = "患者年龄单位，年、月、日", width = 15)
    @ApiModelProperty(value = "患者年龄单位，年、月、日")
	private java.lang.String patientAgeUnit;
	/**医疗机构隶属关系*/
	@Excel(name = "医疗机构隶属关系", width = 15)
    @ApiModelProperty(value = "医疗机构隶属关系")
	private java.lang.String hospBelongTo;
	/**多个疾病组编码*/
	@Excel(name = "多个疾病组编码", width = 15)
    @ApiModelProperty(value = "多个疾病组编码")
	private java.lang.String diseaseGroups;
	/**多个手术或操作编码*/
	@Excel(name = "多个手术或操作编码", width = 15)
    @ApiModelProperty(value = "多个手术或操作编码")
	private java.lang.String operations;
	/**多个检查项目编码*/
	@Excel(name = "多个检查项目编码", width = 15)
    @ApiModelProperty(value = "多个检查项目编码")
	private java.lang.String checkItems;
	/**多个化验项目编码*/
	@Excel(name = "多个化验项目编码", width = 15)
    @ApiModelProperty(value = "多个化验项目编码")
	private java.lang.String labworkItems;
	/**多个药品组编码*/
	@Excel(name = "多个药品组编码", width = 15)
    @ApiModelProperty(value = "多个药品组编码")
	private java.lang.String drugGroups;
	/**多个病理形态编码*/
	@Excel(name = "多个病理形态编码", width = 15)
    @ApiModelProperty(value = "多个病理形态编码")
	private java.lang.String pathologys;
	/**多个检查项目结果*/
	@Excel(name = "多个检查项目结果", width = 15)
	@ApiModelProperty(value = "多个检查项目结果")
	private java.lang.String checkItemsDesc;
}
