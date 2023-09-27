package com.ai.modules.action.entity;

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
 * @Description: 不合规行为结果
 * @Author: jeecg-boot
 * @Date:   2020-02-14
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_BREAK_BEHAVIOR_RESULT")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_BREAK_BEHAVIOR_RESULT对象", description="不合规行为结果")
public class MedicalBreakBehaviorResult {


	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
	@ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
	@ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**主体ID*/
	@Excel(name = "主体ID", width = 15)
	@ApiModelProperty(value = "主体ID")
	private java.lang.String targetId;
	/**主体名称*/
	@Excel(name = "主体名称", width = 15)
	@ApiModelProperty(value = "主体名称")
	private java.lang.String targetName;
	/**主体类型*/
	@Excel(name = "主体类型", width = 15)
	@ApiModelProperty(value = "主体类型")
	private java.lang.String targetType;
	/**病例数*/
	@Excel(name = "病例数", width = 15)
	@ApiModelProperty(value = "病例数")
	private java.lang.Long caseNum;
	/**病例涉及金额*/
	@Excel(name = "病例涉及金额", width = 15)
	@ApiModelProperty(value = "病例涉及金额")
	private Double casePay;
	/**不合规行为1ID*/
	@Excel(name = "不合规行为1ID", width = 15)
	@ApiModelProperty(value = "不合规行为1ID")
	private java.lang.String ba1Id;
	/**不合规行为1病例数*/
	@Excel(name = "不合规行为1病例数", width = 15)
	@ApiModelProperty(value = "不合规行为1病例数")
	private java.lang.Long ba1CaseNum;
	/**不合规行为1涉及金额*/
	@Excel(name = "不合规行为1涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为1涉及金额")
	private Double ba1CasePay;
	/**不合规行为2ID*/
	@Excel(name = "不合规行为2ID", width = 15)
	@ApiModelProperty(value = "不合规行为2ID")
	private java.lang.String ba2Id;
	/**不合规行为2病例数*/
	@Excel(name = "不合规行为2病例数", width = 15)
	@ApiModelProperty(value = "不合规行为2病例数")
	private java.lang.Long ba2CaseNum;
	/**不合规行为2涉及金额*/
	@Excel(name = "不合规行为2涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为2涉及金额")
	private Double ba2CasePay;
	/**不合规行为3ID*/
	@Excel(name = "不合规行为3ID", width = 15)
	@ApiModelProperty(value = "不合规行为3ID")
	private java.lang.String ba3Id;
	/**不合规行为3病例数*/
	@Excel(name = "不合规行为3病例数", width = 15)
	@ApiModelProperty(value = "不合规行为3病例数")
	private java.lang.Long ba3CaseNum;
	/**不合规行为3涉及金额*/
	@Excel(name = "不合规行为3涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为3涉及金额")
	private Double ba3CasePay;
	/**不合规行为4ID*/
	@Excel(name = "不合规行为4ID", width = 15)
	@ApiModelProperty(value = "不合规行为4ID")
	private java.lang.String ba4Id;
	/**不合规行为4病例数*/
	@Excel(name = "不合规行为4病例数", width = 15)
	@ApiModelProperty(value = "不合规行为4病例数")
	private java.lang.Long ba4CaseNum;
	/**不合规行为4涉及金额*/
	@Excel(name = "不合规行为4涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为4涉及金额")
	private Double ba4CasePay;
	/**不合规行为5ID*/
	@Excel(name = "不合规行为5ID", width = 15)
	@ApiModelProperty(value = "不合规行为5ID")
	private java.lang.String ba5Id;
	/**不合规行为5病例数*/
	@Excel(name = "不合规行为5病例数", width = 15)
	@ApiModelProperty(value = "不合规行为5病例数")
	private java.lang.Long ba5CaseNum;
	/**不合规行为5涉及金额*/
	@Excel(name = "不合规行为5涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为5涉及金额")
	private Double ba5CasePay;
	/**不合规行为6ID*/
	@Excel(name = "不合规行为6ID", width = 15)
	@ApiModelProperty(value = "不合规行为6ID")
	private java.lang.String ba6Id;
	/**不合规行为6病例数*/
	@Excel(name = "不合规行为6病例数", width = 15)
	@ApiModelProperty(value = "不合规行为6病例数")
	private java.lang.Long ba6CaseNum;
	/**不合规行为6涉及金额*/
	@Excel(name = "不合规行为6涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为6涉及金额")
	private Double ba6CasePay;
	/**不合规行为7ID*/
	@Excel(name = "不合规行为7ID", width = 15)
	@ApiModelProperty(value = "不合规行为7ID")
	private java.lang.String ba7Id;
	/**不合规行为7病例数*/
	@Excel(name = "不合规行为7病例数", width = 15)
	@ApiModelProperty(value = "不合规行为7病例数")
	private java.lang.Long ba7CaseNum;
	/**不合规行为7涉及金额*/
	@Excel(name = "不合规行为7涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为7涉及金额")
	private Double ba7CasePay;
	/**不合规行为8ID*/
	@Excel(name = "不合规行为8ID", width = 15)
	@ApiModelProperty(value = "不合规行为8ID")
	private java.lang.String ba8Id;
	/**不合规行为8病例数*/
	@Excel(name = "不合规行为8病例数", width = 15)
	@ApiModelProperty(value = "不合规行为8病例数")
	private java.lang.Long ba8CaseNum;
	/**不合规行为8涉及金额*/
	@Excel(name = "不合规行为8涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为8涉及金额")
	private Double ba8CasePay;
	/**不合规行为9ID*/
	@Excel(name = "不合规行为9ID", width = 15)
	@ApiModelProperty(value = "不合规行为9ID")
	private java.lang.String ba9Id;
	/**不合规行为9病例数*/
	@Excel(name = "不合规行为9病例数", width = 15)
	@ApiModelProperty(value = "不合规行为9病例数")
	private java.lang.Long ba9CaseNum;
	/**不合规行为9涉及金额*/
	@Excel(name = "不合规行为9涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为9涉及金额")
	private Double ba9CasePay;
	/**不合规行为10ID*/
	@Excel(name = "不合规行为10ID", width = 15)
	@ApiModelProperty(value = "不合规行为10ID")
	private java.lang.String ba10Id;
	/**不合规行为10病例数*/
	@Excel(name = "不合规行为10病例数", width = 15)
	@ApiModelProperty(value = "不合规行为10病例数")
	private java.lang.Long ba10CaseNum;
	/**不合规行为10涉及金额*/
	@Excel(name = "不合规行为10涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为10涉及金额")
	private Double ba10CasePay;
	/**不合规行为11ID*/
	@Excel(name = "不合规行为11ID", width = 15)
	@ApiModelProperty(value = "不合规行为11ID")
	private java.lang.String ba11Id;
	/**不合规行为11病例数*/
	@Excel(name = "不合规行为11病例数", width = 15)
	@ApiModelProperty(value = "不合规行为11病例数")
	private java.lang.Long ba11CaseNum;
	/**不合规行为11涉及金额*/
	@Excel(name = "不合规行为11涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为11涉及金额")
	private Double ba11CasePay;
	/**不合规行为12ID*/
	@Excel(name = "不合规行为12ID", width = 15)
	@ApiModelProperty(value = "不合规行为12ID")
	private java.lang.String ba12Id;
	/**不合规行为12病例数*/
	@Excel(name = "不合规行为12病例数", width = 15)
	@ApiModelProperty(value = "不合规行为12病例数")
	private java.lang.Long ba12CaseNum;
	/**不合规行为12涉及金额*/
	@Excel(name = "不合规行为12涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为12涉及金额")
	private Double ba12CasePay;
	/**不合规行为13ID*/
	@Excel(name = "不合规行为13ID", width = 15)
	@ApiModelProperty(value = "不合规行为13ID")
	private java.lang.String ba13Id;
	/**不合规行为13病例数*/
	@Excel(name = "不合规行为13病例数", width = 15)
	@ApiModelProperty(value = "不合规行为13病例数")
	private java.lang.Long ba13CaseNum;
	/**不合规行为13涉及金额*/
	@Excel(name = "不合规行为13涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为13涉及金额")
	private Double ba13CasePay;
	/**不合规行为14ID*/
	@Excel(name = "不合规行为14ID", width = 15)
	@ApiModelProperty(value = "不合规行为14ID")
	private java.lang.String ba14Id;
	/**不合规行为14病例数*/
	@Excel(name = "不合规行为14病例数", width = 15)
	@ApiModelProperty(value = "不合规行为14病例数")
	private java.lang.Long ba14CaseNum;
	/**不合规行为14涉及金额*/
	@Excel(name = "不合规行为14涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为14涉及金额")
	private Double ba14CasePay;
	/**不合规行为15ID*/
	@Excel(name = "不合规行为15ID", width = 15)
	@ApiModelProperty(value = "不合规行为15ID")
	private java.lang.String ba15Id;
	/**不合规行为15病例数*/
	@Excel(name = "不合规行为15病例数", width = 15)
	@ApiModelProperty(value = "不合规行为15病例数")
	private java.lang.Long ba15CaseNum;
	/**不合规行为15涉及金额*/
	@Excel(name = "不合规行为15涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为15涉及金额")
	private Double ba15CasePay;
	/**不合规行为16ID*/
	@Excel(name = "不合规行为16ID", width = 15)
	@ApiModelProperty(value = "不合规行为16ID")
	private java.lang.String ba16Id;
	/**不合规行为16病例数*/
	@Excel(name = "不合规行为16病例数", width = 15)
	@ApiModelProperty(value = "不合规行为16病例数")
	private java.lang.Long ba16CaseNum;
	/**不合规行为16涉及金额*/
	@Excel(name = "不合规行为16涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为16涉及金额")
	private Double ba16CasePay;
	/**不合规行为17ID*/
	@Excel(name = "不合规行为17ID", width = 15)
	@ApiModelProperty(value = "不合规行为17ID")
	private java.lang.String ba17Id;
	/**不合规行为17病例数*/
	@Excel(name = "不合规行为17病例数", width = 15)
	@ApiModelProperty(value = "不合规行为17病例数")
	private java.lang.Long ba17CaseNum;
	/**不合规行为17涉及金额*/
	@Excel(name = "不合规行为17涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为17涉及金额")
	private Double ba17CasePay;
	/**不合规行为18ID*/
	@Excel(name = "不合规行为18ID", width = 15)
	@ApiModelProperty(value = "不合规行为18ID")
	private java.lang.String ba18Id;
	/**不合规行为18病例数*/
	@Excel(name = "不合规行为18病例数", width = 15)
	@ApiModelProperty(value = "不合规行为18病例数")
	private java.lang.Long ba18CaseNum;
	/**不合规行为18涉及金额*/
	@Excel(name = "不合规行为18涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为18涉及金额")
	private Double ba18CasePay;
	/**不合规行为19ID*/
	@Excel(name = "不合规行为19ID", width = 15)
	@ApiModelProperty(value = "不合规行为19ID")
	private java.lang.String ba19Id;
	/**不合规行为19病例数*/
	@Excel(name = "不合规行为19病例数", width = 15)
	@ApiModelProperty(value = "不合规行为19病例数")
	private java.lang.Long ba19CaseNum;
	/**不合规行为19涉及金额*/
	@Excel(name = "不合规行为19涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为19涉及金额")
	private Double ba19CasePay;
	/**不合规行为20ID*/
	@Excel(name = "不合规行为20ID", width = 15)
	@ApiModelProperty(value = "不合规行为20ID")
	private java.lang.String ba20Id;
	/**不合规行为20病例数*/
	@Excel(name = "不合规行为20病例数", width = 15)
	@ApiModelProperty(value = "不合规行为20病例数")
	private java.lang.Long ba20CaseNum;
	/**不合规行为20涉及金额*/
	@Excel(name = "不合规行为20涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为20涉及金额")
	private Double ba20CasePay;
	/**不合规行为21ID*/
	@Excel(name = "不合规行为21ID", width = 15)
	@ApiModelProperty(value = "不合规行为21ID")
	private java.lang.String ba21Id;
	/**不合规行为21病例数*/
	@Excel(name = "不合规行为21病例数", width = 15)
	@ApiModelProperty(value = "不合规行为21病例数")
	private java.lang.Long ba21CaseNum;
	/**不合规行为21涉及金额*/
	@Excel(name = "不合规行为21涉及金额", width = 15)
	@ApiModelProperty(value = "不合规行为21涉及金额")
	private Double ba21CasePay;
	/**不合规行为22ID*/
	@Excel(name = "不合规行为22ID", width = 15)
	@ApiModelProperty(value = "不合规行为22ID")
	private java.lang.String ba22Id;
	/**不合规行为22病例数*/
	@Excel(name = "不合规行为22病例数", width = 15)
	@ApiModelProperty(value = "不合规行为22病例数")
	private java.lang.Long ba22CaseNum;
	/**不合规行为22涉及金额*/
	@Excel(name = "不合规行为22涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为22涉及金额")
	private Double ba22CasePay;
	/**不合规行为23ID*/
	@Excel(name = "不合规行为23ID", width = 15)
    @ApiModelProperty(value = "不合规行为23ID")
	private java.lang.String ba23Id;
	/**不合规行为23病例数*/
	@Excel(name = "不合规行为23病例数", width = 15)
    @ApiModelProperty(value = "不合规行为23病例数")
	private java.lang.Long ba23CaseNum;
	/**不合规行为23涉及金额*/
	@Excel(name = "不合规行为23涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为23涉及金额")
	private Double ba23CasePay;
	/**不合规行为24ID*/
	@Excel(name = "不合规行为24ID", width = 15)
    @ApiModelProperty(value = "不合规行为24ID")
	private java.lang.String ba24Id;
	/**不合规行为24病例数*/
	@Excel(name = "不合规行为24病例数", width = 15)
    @ApiModelProperty(value = "不合规行为24病例数")
	private java.lang.Long ba24CaseNum;
	/**不合规行为24涉及金额*/
	@Excel(name = "不合规行为24涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为24涉及金额")
	private Double ba24CasePay;
	/**不合规行为25ID*/
	@Excel(name = "不合规行为25ID", width = 15)
    @ApiModelProperty(value = "不合规行为25ID")
	private java.lang.String ba25Id;
	/**不合规行为25病例数*/
	@Excel(name = "不合规行为25病例数", width = 15)
    @ApiModelProperty(value = "不合规行为25病例数")
	private java.lang.Long ba25CaseNum;
	/**不合规行为25涉及金额*/
	@Excel(name = "不合规行为25涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为25涉及金额")
	private Double ba25CasePay;
	/**不合规行为26ID*/
	@Excel(name = "不合规行为26ID", width = 15)
    @ApiModelProperty(value = "不合规行为26ID")
	private java.lang.String ba26Id;
	/**不合规行为26病例数*/
	@Excel(name = "不合规行为26病例数", width = 15)
    @ApiModelProperty(value = "不合规行为26病例数")
	private java.lang.Long ba26CaseNum;
	/**不合规行为26涉及金额*/
	@Excel(name = "不合规行为26涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为26涉及金额")
	private Double ba26CasePay;
	/**不合规行为27ID*/
	@Excel(name = "不合规行为27ID", width = 15)
    @ApiModelProperty(value = "不合规行为27ID")
	private java.lang.String ba27Id;
	/**不合规行为27病例数*/
	@Excel(name = "不合规行为27病例数", width = 15)
    @ApiModelProperty(value = "不合规行为27病例数")
	private java.lang.Long ba27CaseNum;
	/**不合规行为27涉及金额*/
	@Excel(name = "不合规行为27涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为27涉及金额")
	private Double ba27CasePay;
	/**不合规行为28ID*/
	@Excel(name = "不合规行为28ID", width = 15)
    @ApiModelProperty(value = "不合规行为28ID")
	private java.lang.String ba28Id;
	/**不合规行为28病例数*/
	@Excel(name = "不合规行为28病例数", width = 15)
    @ApiModelProperty(value = "不合规行为28病例数")
	private java.lang.Long ba28CaseNum;
	/**不合规行为28涉及金额*/
	@Excel(name = "不合规行为28涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为28涉及金额")
	private Double ba28CasePay;
	/**不合规行为29ID*/
	@Excel(name = "不合规行为29ID", width = 15)
    @ApiModelProperty(value = "不合规行为29ID")
	private java.lang.String ba29Id;
	/**不合规行为29病例数*/
	@Excel(name = "不合规行为29病例数", width = 15)
    @ApiModelProperty(value = "不合规行为29病例数")
	private java.lang.Long ba29CaseNum;
	/**不合规行为29涉及金额*/
	@Excel(name = "不合规行为29涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为29涉及金额")
	private Double ba29CasePay;
	/**不合规行为30ID*/
	@Excel(name = "不合规行为30ID", width = 15)
    @ApiModelProperty(value = "不合规行为30ID")
	private java.lang.String ba30Id;
	/**不合规行为30病例数*/
	@Excel(name = "不合规行为30病例数", width = 15)
    @ApiModelProperty(value = "不合规行为30病例数")
	private java.lang.Long ba30CaseNum;
	/**不合规行为30涉及金额*/
	@Excel(name = "不合规行为30涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为30涉及金额")
	private Double ba30CasePay;
	/**不合规行为31ID*/
	@Excel(name = "不合规行为31ID", width = 15)
    @ApiModelProperty(value = "不合规行为31ID")
	private java.lang.String ba31Id;
	/**不合规行为31病例数*/
	@Excel(name = "不合规行为31病例数", width = 15)
    @ApiModelProperty(value = "不合规行为31病例数")
	private java.lang.Long ba31CaseNum;
	/**不合规行为31涉及金额*/
	@Excel(name = "不合规行为31涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为31涉及金额")
	private Double ba31CasePay;
	/**不合规行为32ID*/
	@Excel(name = "不合规行为32ID", width = 15)
    @ApiModelProperty(value = "不合规行为32ID")
	private java.lang.String ba32Id;
	/**不合规行为32病例数*/
	@Excel(name = "不合规行为32病例数", width = 15)
    @ApiModelProperty(value = "不合规行为32病例数")
	private java.lang.Long ba32CaseNum;
	/**不合规行为32涉及金额*/
	@Excel(name = "不合规行为32涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为32涉及金额")
	private Double ba32CasePay;
	/**不合规行为33ID*/
	@Excel(name = "不合规行为33ID", width = 15)
    @ApiModelProperty(value = "不合规行为33ID")
	private java.lang.String ba33Id;
	/**不合规行为33病例数*/
	@Excel(name = "不合规行为33病例数", width = 15)
    @ApiModelProperty(value = "不合规行为33病例数")
	private java.lang.Long ba33CaseNum;
	/**不合规行为33涉及金额*/
	@Excel(name = "不合规行为33涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为33涉及金额")
	private Double ba33CasePay;
	/**不合规行为34ID*/
	@Excel(name = "不合规行为34ID", width = 15)
    @ApiModelProperty(value = "不合规行为34ID")
	private java.lang.String ba34Id;
	/**不合规行为34病例数*/
	@Excel(name = "不合规行为34病例数", width = 15)
    @ApiModelProperty(value = "不合规行为34病例数")
	private java.lang.Long ba34CaseNum;
	/**不合规行为34涉及金额*/
	@Excel(name = "不合规行为34涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为34涉及金额")
	private Double ba34CasePay;
	/**不合规行为35ID*/
	@Excel(name = "不合规行为35ID", width = 15)
    @ApiModelProperty(value = "不合规行为35ID")
	private java.lang.String ba35Id;
	/**不合规行为35病例数*/
	@Excel(name = "不合规行为35病例数", width = 15)
    @ApiModelProperty(value = "不合规行为35病例数")
	private java.lang.Long ba35CaseNum;
	/**不合规行为35涉及金额*/
	@Excel(name = "不合规行为35涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为35涉及金额")
	private Double ba35CasePay;
	/**不合规行为36ID*/
	@Excel(name = "不合规行为36ID", width = 15)
    @ApiModelProperty(value = "不合规行为36ID")
	private java.lang.String ba36Id;
	/**不合规行为36病例数*/
	@Excel(name = "不合规行为36病例数", width = 15)
    @ApiModelProperty(value = "不合规行为36病例数")
	private java.lang.Long ba36CaseNum;
	/**不合规行为36涉及金额*/
	@Excel(name = "不合规行为36涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为36涉及金额")
	private Double ba36CasePay;
	/**不合规行为37ID*/
	@Excel(name = "不合规行为37ID", width = 15)
    @ApiModelProperty(value = "不合规行为37ID")
	private java.lang.String ba37Id;
	/**不合规行为37病例数*/
	@Excel(name = "不合规行为37病例数", width = 15)
    @ApiModelProperty(value = "不合规行为37病例数")
	private java.lang.Long ba37CaseNum;
	/**不合规行为37涉及金额*/
	@Excel(name = "不合规行为37涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为37涉及金额")
	private Double ba37CasePay;
	/**不合规行为38ID*/
	@Excel(name = "不合规行为38ID", width = 15)
    @ApiModelProperty(value = "不合规行为38ID")
	private java.lang.String ba38Id;
	/**不合规行为38病例数*/
	@Excel(name = "不合规行为38病例数", width = 15)
    @ApiModelProperty(value = "不合规行为38病例数")
	private java.lang.Long ba38CaseNum;
	/**不合规行为38涉及金额*/
	@Excel(name = "不合规行为38涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为38涉及金额")
	private Double ba38CasePay;
	/**不合规行为39ID*/
	@Excel(name = "不合规行为39ID", width = 15)
    @ApiModelProperty(value = "不合规行为39ID")
	private java.lang.String ba39Id;
	/**不合规行为39病例数*/
	@Excel(name = "不合规行为39病例数", width = 15)
    @ApiModelProperty(value = "不合规行为39病例数")
	private java.lang.Long ba39CaseNum;
	/**不合规行为39涉及金额*/
	@Excel(name = "不合规行为39涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为39涉及金额")
	private Double ba39CasePay;
	/**不合规行为40ID*/
	@Excel(name = "不合规行为40ID", width = 15)
    @ApiModelProperty(value = "不合规行为40ID")
	private java.lang.String ba40Id;
	/**不合规行为40病例数*/
	@Excel(name = "不合规行为40病例数", width = 15)
    @ApiModelProperty(value = "不合规行为40病例数")
	private java.lang.Long ba40CaseNum;
	/**不合规行为40涉及金额*/
	@Excel(name = "不合规行为40涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为40涉及金额")
	private Double ba40CasePay;
	/**不合规行为41ID*/
	@Excel(name = "不合规行为41ID", width = 15)
    @ApiModelProperty(value = "不合规行为41ID")
	private java.lang.String ba41Id;
	/**不合规行为41病例数*/
	@Excel(name = "不合规行为41病例数", width = 15)
    @ApiModelProperty(value = "不合规行为41病例数")
	private java.lang.Long ba41CaseNum;
	/**不合规行为41涉及金额*/
	@Excel(name = "不合规行为41涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为41涉及金额")
	private Double ba41CasePay;
	/**不合规行为42ID*/
	@Excel(name = "不合规行为42ID", width = 15)
    @ApiModelProperty(value = "不合规行为42ID")
	private java.lang.String ba42Id;
	/**不合规行为42病例数*/
	@Excel(name = "不合规行为42病例数", width = 15)
    @ApiModelProperty(value = "不合规行为42病例数")
	private java.lang.Long ba42CaseNum;
	/**不合规行为42涉及金额*/
	@Excel(name = "不合规行为42涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为42涉及金额")
	private Double ba42CasePay;
	/**不合规行为43ID*/
	@Excel(name = "不合规行为43ID", width = 15)
    @ApiModelProperty(value = "不合规行为43ID")
	private java.lang.String ba43Id;
	/**不合规行为43病例数*/
	@Excel(name = "不合规行为43病例数", width = 15)
    @ApiModelProperty(value = "不合规行为43病例数")
	private java.lang.Long ba43CaseNum;
	/**不合规行为43涉及金额*/
	@Excel(name = "不合规行为43涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为43涉及金额")
	private Double ba43CasePay;
	/**不合规行为44ID*/
	@Excel(name = "不合规行为44ID", width = 15)
    @ApiModelProperty(value = "不合规行为44ID")
	private java.lang.String ba44Id;
	/**不合规行为44病例数*/
	@Excel(name = "不合规行为44病例数", width = 15)
    @ApiModelProperty(value = "不合规行为44病例数")
	private java.lang.Long ba44CaseNum;
	/**不合规行为44涉及金额*/
	@Excel(name = "不合规行为44涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为44涉及金额")
	private Double ba44CasePay;
	/**不合规行为45ID*/
	@Excel(name = "不合规行为45ID", width = 15)
    @ApiModelProperty(value = "不合规行为45ID")
	private java.lang.String ba45Id;
	/**不合规行为45病例数*/
	@Excel(name = "不合规行为45病例数", width = 15)
    @ApiModelProperty(value = "不合规行为45病例数")
	private java.lang.Long ba45CaseNum;
	/**不合规行为45涉及金额*/
	@Excel(name = "不合规行为45涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为45涉及金额")
	private Double ba45CasePay;
	/**不合规行为46ID*/
	@Excel(name = "不合规行为46ID", width = 15)
    @ApiModelProperty(value = "不合规行为46ID")
	private java.lang.String ba46Id;
	/**不合规行为46病例数*/
	@Excel(name = "不合规行为46病例数", width = 15)
    @ApiModelProperty(value = "不合规行为46病例数")
	private java.lang.Long ba46CaseNum;
	/**不合规行为46涉及金额*/
	@Excel(name = "不合规行为46涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为46涉及金额")
	private Double ba46CasePay;
	/**不合规行为47ID*/
	@Excel(name = "不合规行为47ID", width = 15)
    @ApiModelProperty(value = "不合规行为47ID")
	private java.lang.String ba47Id;
	/**不合规行为47病例数*/
	@Excel(name = "不合规行为47病例数", width = 15)
    @ApiModelProperty(value = "不合规行为47病例数")
	private java.lang.Long ba47CaseNum;
	/**不合规行为47涉及金额*/
	@Excel(name = "不合规行为47涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为47涉及金额")
	private Double ba47CasePay;
	/**不合规行为48ID*/
	@Excel(name = "不合规行为48ID", width = 15)
    @ApiModelProperty(value = "不合规行为48ID")
	private java.lang.String ba48Id;
	/**不合规行为48病例数*/
	@Excel(name = "不合规行为48病例数", width = 15)
    @ApiModelProperty(value = "不合规行为48病例数")
	private java.lang.Long ba48CaseNum;
	/**不合规行为48涉及金额*/
	@Excel(name = "不合规行为48涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为48涉及金额")
	private Double ba48CasePay;
	/**不合规行为49ID*/
	@Excel(name = "不合规行为49ID", width = 15)
    @ApiModelProperty(value = "不合规行为49ID")
	private java.lang.String ba49Id;
	/**不合规行为49病例数*/
	@Excel(name = "不合规行为49病例数", width = 15)
    @ApiModelProperty(value = "不合规行为49病例数")
	private java.lang.Long ba49CaseNum;
	/**不合规行为49涉及金额*/
	@Excel(name = "不合规行为49涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为49涉及金额")
	private Double ba49CasePay;
	/**不合规行为50ID*/
	@Excel(name = "不合规行为50ID", width = 15)
    @ApiModelProperty(value = "不合规行为50ID")
	private java.lang.String ba50Id;
	/**不合规行为50病例数*/
	@Excel(name = "不合规行为50病例数", width = 15)
    @ApiModelProperty(value = "不合规行为50病例数")
	private java.lang.Long ba50CaseNum;
	/**不合规行为50涉及金额*/
	@Excel(name = "不合规行为50涉及金额", width = 15)
    @ApiModelProperty(value = "不合规行为50涉及金额")
	private Double ba50CasePay;

}
