package com.ai.modules.review.vo;

import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value="DWB_MASTER_INFO对象", description="就诊信息")
public class DwbMasterInfoVo {

	/**补偿机构名称*/
	@Excel(name = "补偿机构名称", width = 15)
    @ApiModelProperty(value = "补偿机构名称")
	private java.lang.String compOrgname;
	/**费用结算方式代码*/
	@Excel(name = "费用结算方式代码", width = 15)
    @ApiModelProperty(value = "费用结算方式代码")
	private java.lang.String settleway;
	/**费用结算方式名称*/
	@Excel(name = "费用结算方式名称", width = 15)
    @ApiModelProperty(value = "费用结算方式名称")
	private java.lang.String settlewayId;
	/**数据来源编码*/
	@Excel(name = "数据来源编码", width = 15)
    @ApiModelProperty(value = "数据来源编码")
	private java.lang.String dataResouceId;
	/**数据来源名称*/
	@Excel(name = "数据来源名称", width = 15)
    @ApiModelProperty(value = "数据来源名称")
	private java.lang.String dataResouce;
	/**etl来源编码*/
	@Excel(name = "etl来源编码", width = 15)
    @ApiModelProperty(value = "etl来源编码")
	private java.lang.String etlSource;
	/**etl来源名称*/
	@Excel(name = "etl来源名称", width = 15)
    @ApiModelProperty(value = "etl来源名称")
	private java.lang.String etlSourceName;
	/**etl时间*/
	@Excel(name = "etl时间", width = 15)
    @ApiModelProperty(value = "etl时间")
	private java.lang.String etlTime;
	/**id*/
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private java.lang.String visitid;
	/**医保/农合就诊id*/
	@Excel(name = "医保/农合就诊id", width = 15)
    @ApiModelProperty(value = "医保/农合就诊id")
	private java.lang.String ybVisitid;
	/**his就诊id*/
	@Excel(name = "his就诊id", width = 15)
    @ApiModelProperty(value = "his就诊id")
	private java.lang.String hisVisitid;
	/**虚拟就诊id*/
	@Excel(name = "虚拟就诊id", width = 15)
	@ApiModelProperty(value = "虚拟就诊id")
	private java.lang.String visitidDummy;
	/**his和医保/农合关联的visitid*/
	@Excel(name = "his和医保/农合关联的visitid", width = 15)
	@ApiModelProperty(value = "his和医保/农合关联的visitid")
	private java.lang.String visitidConnect;
	/**病案号*/
	@Excel(name = "病案号", width = 15)
    @ApiModelProperty(value = "病案号")
	private java.lang.String caseId;
	/**yx患者编号*/
	@Excel(name = "yx患者编号", width = 15)
    @ApiModelProperty(value = "yx患者编号")
	private java.lang.String clientid;
	/**身份证件类型代码*/
	@Excel(name = "身份证件类型代码", width = 15)
    @ApiModelProperty(value = "身份证件类型代码")
	private java.lang.String identifytype;
	/**身份证件类型名称*/
	@Excel(name = "身份证件类型名称", width = 15)
    @ApiModelProperty(value = "身份证件类型名称")
	private java.lang.String identifyname;
	/**身份证件号码*/
	@Excel(name = "身份证件号码", width = 15)
    @ApiModelProperty(value = "身份证件号码")
	private java.lang.String idNo;
	/**参保个人编号*/
	@Excel(name = "参保个人编号", width = 15)
    @ApiModelProperty(value = "参保个人编号")
	private java.lang.String ybClientid;
	/**就诊卡号*/
	@Excel(name = "就诊卡号", width = 15)
    @ApiModelProperty(value = "就诊卡号")
	private java.lang.String cardno;
	/**健康卡号*/
	@Excel(name = "健康卡号", width = 15)
    @ApiModelProperty(value = "健康卡号")
	private java.lang.String medicalcardId;
	/**参保类别*/
	@Excel(name = "参保类别", width = 15)
    @ApiModelProperty(value = "参保类别")
	private java.lang.String insurancetype;
	/**医保卡号/农合卡号*/
	@Excel(name = "医保卡号/农合卡号", width = 15)
    @ApiModelProperty(value = "医保卡号/农合卡号")
	private java.lang.String insurancecardNo;
	/**诊断疾病名称*/
	@Excel(name = "诊断疾病名称", width = 15)
	@ApiModelProperty(value = "诊断疾病名称")
	private String diseasename;
	/**疾病编码*/
	@Excel(name = "疾病编码", width = 15)
	@ApiModelProperty(value = "疾病编码")
	private java.lang.String diseasecode;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**性别代码*/
	@Excel(name = "性别代码", width = 15)
    @ApiModelProperty(value = "性别代码")
	private java.lang.String sexCode;
	/**性别名称*/
	@Excel(name = "性别名称", width = 15)
    @ApiModelProperty(value = "性别名称")
	private java.lang.String sex;
	/**出生日期*/
	@Excel(name = "出生日期", width = 15)
    @ApiModelProperty(value = "出生日期")
	private java.lang.String birthday;
	/**年龄（岁）*/
	@Excel(name = "年龄（岁）", width = 15)
    @ApiModelProperty(value = "年龄（岁）")
	private java.lang.Double yearage;
	/**年龄（月）*/
	@Excel(name = "年龄（月）", width = 15)
    @ApiModelProperty(value = "年龄（月）")
	private java.lang.Double monthage;
	/**年龄（日）*/
	@Excel(name = "年龄（日）", width = 15)
	@ApiModelProperty(value = "年龄（日）")
	private java.lang.Double dayage;
	/**异地就医标志*/
	@Excel(name = "异地就医标志", width = 15)
    @ApiModelProperty(value = "异地就医标志")
	private java.lang.String nonlocalHospSign;
	/**就诊类型代码*/
	@Excel(name = "就诊类型代码", width = 15)
    @ApiModelProperty(value = "就诊类型代码")
	private java.lang.String visittypeId;
	/**就诊类型名称*/
	@Excel(name = "就诊类型名称", width = 15)
    @ApiModelProperty(value = "就诊类型名称")
	private java.lang.String visittype;
	/**就诊日期时间*/
	@Excel(name = "就诊日期时间", width = 15)
    @ApiModelProperty(value = "就诊日期时间")
	private java.lang.String visitdate;
	/**挂号方式代码*/
	@Excel(name = "挂号方式代码", width = 15)
    @ApiModelProperty(value = "挂号方式代码")
	private java.lang.String registertypeId;
	/**挂号方式名称*/
	@Excel(name = "挂号方式名称", width = 15)
    @ApiModelProperty(value = "挂号方式名称")
	private java.lang.String registerway;
	/**预约途径代码*/
	@Excel(name = "预约途径代码", width = 15)
    @ApiModelProperty(value = "预约途径代码")
	private java.lang.String appointwayCode;
	/**预约途径名称*/
	@Excel(name = "预约途径名称", width = 15)
    @ApiModelProperty(value = "预约途径名称")
	private java.lang.String appointway;
	/**入院途径代码*/
	@Excel(name = "入院途径代码", width = 15)
    @ApiModelProperty(value = "入院途径代码")
	private java.lang.String admitwayCode;
	/**入院途径名称*/
	@Excel(name = "入院途径名称", width = 15)
    @ApiModelProperty(value = "入院途径名称")
	private java.lang.String admitway;
	/**复诊标志*/
	@Excel(name = "复诊标志", width = 15)
    @ApiModelProperty(value = "复诊标志")
	private java.lang.String returnVisit;
	/**就诊标志*/
	@Excel(name = "就诊标志", width = 15)
    @ApiModelProperty(value = "就诊标志")
	private java.lang.String visitSign;
	/**连续住院标志*/
	@Excel(name = "连续住院标志", width = 15)
    @ApiModelProperty(value = "连续住院标志")
	private java.lang.String continueZySign;
	/**就诊医疗机构编码*/
	@Excel(name = "就诊医疗机构编码", width = 15)
    @ApiModelProperty(value = "就诊医疗机构编码")
	private java.lang.String orgid;
	/**就诊医疗机构名称*/
	@Excel(name = "就诊医疗机构名称", width = 15)
    @ApiModelProperty(value = "就诊医疗机构名称")
	private java.lang.String orgname;
	/**医院级别*/
	@Excel(name = "医院级别", width = 15)
    @ApiModelProperty(value = "医院级别")
	private java.lang.String hosplevel;
	/**医疗机构等级*/
	@Excel(name = "医疗机构等级", width = 15)
    @ApiModelProperty(value = "医疗机构等级")
	private java.lang.String hospgrade;
	/**就诊科室编码*/
	@Excel(name = "就诊科室编码", width = 15)
    @ApiModelProperty(value = "就诊科室编码")
	private java.lang.String deptid;
	/**就诊科室名称*/
	@Excel(name = "就诊科室名称", width = 15)
    @ApiModelProperty(value = "就诊科室名称")
	private java.lang.String deptname;
	/**就诊科室编码_src*/
	@Excel(name = "就诊科室编码_src", width = 15)
	@ApiModelProperty(value = "就诊科室编码_src")
	private java.lang.String deptidSrc;
	/**就诊科室名称_src*/
	@Excel(name = "就诊科室名称_src", width = 15)
	@ApiModelProperty(value = "就诊科室名称_src")
	private java.lang.String deptnameSrc;
	/**入院病房名称*/
	@Excel(name = "入院病房名称", width = 15)
    @ApiModelProperty(value = "入院病房名称")
	private java.lang.String admitroomNo;
	/**床位号*/
	@Excel(name = "床位号", width = 15)
    @ApiModelProperty(value = "床位号")
	private java.lang.String bedno;
	/**就诊医师编码*/
	@Excel(name = "就诊医师编码", width = 15)
    @ApiModelProperty(value = "就诊医师编码")
	private java.lang.String doctorid;
	/**就诊医师姓名*/
	@Excel(name = "就诊医师姓名", width = 15)
    @ApiModelProperty(value = "就诊医师姓名")
	private java.lang.String doctorname;
	/**科主任姓名*/
	@Excel(name = "科主任姓名", width = 15)
    @ApiModelProperty(value = "科主任姓名")
	private java.lang.String directordoctor;
	/**主任（副主任）医师姓名*/
	@Excel(name = "主任（副主任）医师姓名", width = 15)
    @ApiModelProperty(value = "主任（副主任）医师姓名")
	private java.lang.String vicedirector;
	/**执业药师编码*/
	@Excel(name = "执业药师编码", width = 15)
    @ApiModelProperty(value = "执业药师编码")
	private java.lang.String pharmacistid;
	/**执业药师姓名*/
	@Excel(name = "执业药师姓名", width = 15)
    @ApiModelProperty(value = "执业药师姓名")
	private java.lang.String pharmacistname;
	/**入院状态*/
	@Excel(name = "入院状态", width = 15)
    @ApiModelProperty(value = "入院状态")
	private java.lang.String admitstatus;
	/**门诊主要症状代码*/
	@Excel(name = "门诊主要症状代码", width = 15)
    @ApiModelProperty(value = "门诊主要症状代码")
	private java.lang.String symptonCode;
	/**门诊主要症状名称*/
	@Excel(name = "门诊主要症状名称", width = 15)
    @ApiModelProperty(value = "门诊主要症状名称")
	private java.lang.String sympton;
	/**并发症_疾病编码*/
	@Excel(name = "并发症_疾病编码", width = 15)
    @ApiModelProperty(value = "并发症_疾病编码")
	private java.lang.String comorbidityCode;
	/**并发症_疾病名称*/
	@Excel(name = "并发症_疾病名称", width = 15)
    @ApiModelProperty(value = "并发症_疾病名称")
	private java.lang.String comorbidity;
	/**损伤、中毒的外部原因名称*/
	@Excel(name = "损伤、中毒的外部原因名称", width = 15)
    @ApiModelProperty(value = "损伤、中毒的外部原因名称")
	private java.lang.String injureReason;
	/**损伤、中毒的外部原因疾病编码*/
	@Excel(name = "损伤、中毒的外部原因疾病编码", width = 15)
    @ApiModelProperty(value = "损伤、中毒的外部原因疾病编码")
	private java.lang.String injureReasonCode;
	/**病理号*/
	@Excel(name = "病理号", width = 15)
    @ApiModelProperty(value = "病理号")
	private java.lang.String pathonogyTestId;
	/**病理诊断名称*/
	@Excel(name = "病理诊断名称", width = 15)
    @ApiModelProperty(value = "病理诊断名称")
	private java.lang.String pathonogyDisease;
	/**病理诊断疾病编码*/
	@Excel(name = "病理诊断疾病编码", width = 15)
    @ApiModelProperty(value = "病理诊断疾病编码")
	private java.lang.String pathonogyDiseasecode;
	/**药物过敏标志*/
	@Excel(name = "药物过敏标志", width = 15)
    @ApiModelProperty(value = "药物过敏标志")
	private java.lang.String drugAllergyStatus;
	/**过敏药物名称*/
	@Excel(name = "过敏药物名称", width = 15)
    @ApiModelProperty(value = "过敏药物名称")
	private java.lang.String allergyDrug;
	/**治疗方式编码*/
	@Excel(name = "治疗方式编码", width = 15)
    @ApiModelProperty(value = "治疗方式编码")
	private java.lang.String treateMode;
	/**处置*/
	@Excel(name = "处置", width = 15)
    @ApiModelProperty(value = "处置")
	private java.lang.String treatmode;
	/**其他医学处置*/
	@Excel(name = "其他医学处置", width = 15)
    @ApiModelProperty(value = "其他医学处置")
	private java.lang.String othertreat;
	/**转入医院编码*/
	@Excel(name = "转入医院编码", width = 15)
    @ApiModelProperty(value = "转入医院编码")
	private java.lang.String referHospid;
	/**转入医院名称*/
	@Excel(name = "转入医院名称", width = 15)
    @ApiModelProperty(value = "转入医院名称")
	private java.lang.String referHospname;
	/**转入医院级别*/
	@Excel(name = "转入医院级别", width = 15)
    @ApiModelProperty(value = "转入医院级别")
	private java.lang.String referhospLevel;
	/**转入科室编码*/
	@Excel(name = "转入科室编码", width = 15)
    @ApiModelProperty(value = "转入科室编码")
	private java.lang.String admitdepartId;
	/**转入科室名称*/
	@Excel(name = "转入科室名称", width = 15)
    @ApiModelProperty(value = "转入科室名称")
	private java.lang.String admitdepartment;
	/**本次转诊类型*/
	@Excel(name = "本次转诊类型", width = 15)
    @ApiModelProperty(value = "本次转诊类型")
	private java.lang.String transfertype;
	/**转出日期*/
	@Excel(name = "转出日期", width = 15)
    @ApiModelProperty(value = "转出日期")
	private java.lang.String transoutDate;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**实际住院天数*/
	@Excel(name = "实际住院天数", width = 15)
    @ApiModelProperty(value = "实际住院天数")
	private java.lang.Double zyDays;
	/**住院天数计算值*/
	@Excel(name = "住院天数计算值", width = 15)
	@ApiModelProperty(value = "住院天数计算值")
	private java.lang.String zyDaysCalculate;
	/**离院方式代码*/
	@Excel(name = "离院方式代码", width = 15)
    @ApiModelProperty(value = "离院方式代码")
	private java.lang.String leavetypeCode;
	/**离院方式名称*/
	@Excel(name = "离院方式名称", width = 15)
    @ApiModelProperty(value = "离院方式名称")
	private java.lang.String leavetype;
	/**出院原因*/
	@Excel(name = "出院原因", width = 15)
    @ApiModelProperty(value = "出院原因")
	private java.lang.String leavereason;
	/**治疗结果*/
	@Excel(name = "治疗结果", width = 15)
    @ApiModelProperty(value = "治疗结果")
	private java.lang.String result;
	/**死亡患者尸检标志*/
	@Excel(name = "死亡患者尸检标志", width = 15)
    @ApiModelProperty(value = "死亡患者尸检标志")
	private java.lang.String autopsyStatus;
	/**出院医嘱信息*/
	@Excel(name = "出院医嘱信息", width = 15)
    @ApiModelProperty(value = "出院医嘱信息")
	private java.lang.String doctoradvice;
	/**是否有出院31天内再住院计划*/
	@Excel(name = "是否有出院31天内再住院计划", width = 15)
    @ApiModelProperty(value = "是否有出院31天内再住院计划")
	private java.lang.String ifZyIn31day;
	/**再住院目的*/
	@Excel(name = "再住院目的", width = 15)
    @ApiModelProperty(value = "再住院目的")
	private java.lang.String reZyPurpose;
	/**医疗付费方式代码*/
	@Excel(name = "医疗付费方式代码", width = 15)
    @ApiModelProperty(value = "医疗付费方式代码")
	private java.lang.String paywayId;
	/**医疗付费方式名称*/
	@Excel(name = "医疗付费方式名称", width = 15)
    @ApiModelProperty(value = "医疗付费方式名称")
	private java.lang.String payway;
	/**是否单病种*/
	@Excel(name = "是否单病种", width = 15)
    @ApiModelProperty(value = "是否单病种")
	private java.lang.String isdrgs;
	/**医疗费用总金额*/
	@Excel(name = "医疗费用总金额", width = 15)
    @ApiModelProperty(value = "医疗费用总金额")
	private Double totalfee;
	/**其中药品总费用*/
	@Excel(name = "其中药品总费用", width = 15)
    @ApiModelProperty(value = "其中药品总费用")
	private Double drugfee;
	/**本次医保/农合可补费用*/
	@Excel(name = "本次医保/农合可补费用", width = 15)
    @ApiModelProperty(value = "本次医保/农合可补费用")
	private Double fundCover;
	/**本次医保/农合可补药费*/
	@Excel(name = "本次医保/农合可补药费", width = 15)
    @ApiModelProperty(value = "本次医保/农合可补药费")
	private Double fundCoverDrug;
	/**本次基金支付金额*/
	@Excel(name = "本次基金支付金额", width = 15)
    @ApiModelProperty(value = "本次基金支付金额")
	private Double fundpay;
	/**本次门诊农合统筹账户支付*/
	@Excel(name = "本次门诊农合统筹账户支付", width = 15)
    @ApiModelProperty(value = "本次门诊农合统筹账户支付")
	private Double poolAcctPay;
	/**单病种费用定额*/
	@Excel(name = "单病种费用定额", width = 15)
    @ApiModelProperty(value = "单病种费用定额")
	private Double drgsLimitfee;
	/**单病种个人自付金额*/
	@Excel(name = "单病种个人自付金额", width = 15)
    @ApiModelProperty(value = "单病种个人自付金额")
	private Double drgsSelfpay;
	/**高额材料限价超额费用*/
	@Excel(name = "高额材料限价超额费用", width = 15)
    @ApiModelProperty(value = "高额材料限价超额费用")
	private Double beyondMateriallimit;
	/**超过最高收费限额时的补偿金额*/
	@Excel(name = "超过最高收费限额时的补偿金额", width = 15)
    @ApiModelProperty(value = "超过最高收费限额时的补偿金额")
	private Double beyondCompFee;
	/**民政救助补偿金额*/
	@Excel(name = "民政救助补偿金额", width = 15)
    @ApiModelProperty(value = "民政救助补偿金额")
	private Double civilAdmSubsidy;
	/**财政兜底金额*/
	@Excel(name = "财政兜底金额", width = 15)
    @ApiModelProperty(value = "财政兜底金额")
	private Double financeCompensate;
	/**其他保障金额*/
	@Excel(name = "其他保障金额", width = 15)
    @ApiModelProperty(value = "其他保障金额")
	private Double otherComp;
	/**“180”补偿金额*/
	@Excel(name = "“180”补偿金额", width = 15)
    @ApiModelProperty(value = "“180”补偿金额")
	private Double comp180Fee;
	/**本次大病可补费用*/
	@Excel(name = "本次大病可补费用", width = 15)
    @ApiModelProperty(value = "本次大病可补费用")
	private Double seriousillCover;
	/**本次大病补偿费用*/
	@Excel(name = "本次大病补偿费用", width = 15)
    @ApiModelProperty(value = "本次大病补偿费用")
	private Double seriousillComp;
	/**本年度基本农合累计补偿*/
	@Excel(name = "本年度基本农合累计补偿", width = 15)
    @ApiModelProperty(value = "本年度基本农合累计补偿")
	private Double annCompensate;
	/**不合规费用*/
	@Excel(name = "不合规费用", width = 15)
    @ApiModelProperty(value = "不合规费用")
	private Double illegalcost;
	/**增减补偿额*/
	@Excel(name = "增减补偿额", width = 15)
    @ApiModelProperty(value = "增减补偿额")
	private Double adjustfee;
	/**增减原因*/
	@Excel(name = "增减原因", width = 15)
    @ApiModelProperty(value = "增减原因")
	private java.lang.String adjustrease;
	/**个人帐户支付金额*/
	@Excel(name = "个人帐户支付金额", width = 15)
    @ApiModelProperty(value = "个人帐户支付金额")
	private Double indivAcctPay;
	/**本次农合家庭账户支付的金额*/
	@Excel(name = "本次农合家庭账户支付的金额", width = 15)
    @ApiModelProperty(value = "本次农合家庭账户支付的金额")
	private Double famAcctPay;
	/**实际补偿费用*/
	@Excel(name = "实际补偿费用", width = 15)
    @ApiModelProperty(value = "实际补偿费用")
	private Double compensateTotal;
	/**乙类自付*/
	@Excel(name = "乙类自付", width = 15)
    @ApiModelProperty(value = "乙类自付")
	private Double selfpay2;
	/**丙类自费金额*/
	@Excel(name = "丙类自费金额", width = 15)
    @ApiModelProperty(value = "丙类自费金额")
	private Double selfpay3;
	/**个人现金支付金额*/
	@Excel(name = "个人现金支付金额", width = 15)
    @ApiModelProperty(value = "个人现金支付金额")
	private Double selfpay;
	/**医疗机构承担金额*/
	@Excel(name = "医疗机构承担金额", width = 15)
    @ApiModelProperty(value = "医疗机构承担金额")
	private Double hospPay;
	/**医保/农合机构编码*/
	@Excel(name = "医保/农合机构编码", width = 15)
    @ApiModelProperty(value = "医保/农合机构编码")
	private java.lang.String insuranceOrgid;
	/**医保/农合机构名称*/
	@Excel(name = "医保/农合机构名称", width = 15)
    @ApiModelProperty(value = "医保/农合机构名称")
	private java.lang.String insuranceOrgname;
	/**补偿机构编码*/
	@Excel(name = "补偿机构编码", width = 15)
    @ApiModelProperty(value = "补偿机构编码")
	private java.lang.String compOrgid;
	@Excel(name = "医保结算方式代码", width = 15)
    @ApiModelProperty(value = "医保结算方式代码")
	private java.lang.String funSettlewayId;
	@Excel(name = "医保结算方式名称", width = 15)
    @ApiModelProperty(value = "医保结算方式名称")
	private java.lang.String funSettlewayName;

}
