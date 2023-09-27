package com.ai.modules.review.vo;

import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value="DWB_CLIENT对象", description="病人信息")
public class DwbClientVo {    
	/**id*/
    @ApiModelProperty(value = "id")
	private java.lang.String id;
    /**CLIENTID*/
	@Excel(name = "CLIENTID", width = 15)
    @ApiModelProperty(value = "CLIENTID")
	private java.lang.String clientid;
	/**YX患者编号 */
	@Excel(name = "YX患者编号 ", width = 15)
    @ApiModelProperty(value = "YX患者编号 ")
	private java.lang.String yxClientcode;
	/**YX编号判断方式代码*/
	@Excel(name = "YX编号判断方式代码", width = 15)
    @ApiModelProperty(value = "YX编号判断方式代码")
	private java.lang.String integrateCode;
	/**YX编号判断方式名称*/
	@Excel(name = "YX编号判断方式名称", width = 15)
    @ApiModelProperty(value = "YX编号判断方式名称")
	private java.lang.String integrateType;
	/**身份证件类型代码*/
	@Excel(name = "身份证件类型代码", width = 15)
    @ApiModelProperty(value = "身份证件类型代码")
	private java.lang.String identifytype;
	/**身份证件类型名称*/
	@Excel(name = "身份证件类型名称", width = 15)
    @ApiModelProperty(value = "身份证件类型名称")
	private java.lang.String identifyname;
	/**居民身份证号码*/
	@Excel(name = "居民身份证号码", width = 15)
    @ApiModelProperty(value = "居民身份证号码")
	private java.lang.String idNo;
	/**居民身份证号码2*/
	@Excel(name = "居民身份证号码2", width = 15)
    @ApiModelProperty(value = "居民身份证号码2")
	private java.lang.String idNo2;
	/**签发机关*/
	@Excel(name = "签发机关", width = 15)
    @ApiModelProperty(value = "签发机关")
	private java.lang.String approveorg;
	/**有效期限起始日期*/
	@Excel(name = "有效期限起始日期", width = 15)
    @ApiModelProperty(value = "有效期限起始日期")
	private java.lang.String validDate;
	/**有效期限截止日期*/
	@Excel(name = "有效期限截止日期", width = 15)
    @ApiModelProperty(value = "有效期限截止日期")
	private java.lang.String invalidDate;
	/**姓名*/
	@Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
	private java.lang.String clientname;
	/**曾用名*/
	@Excel(name = "曾用名", width = 15)
    @ApiModelProperty(value = "曾用名")
	private java.lang.String exName;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**出生日期*/
	@Excel(name = "出生日期", width = 15)
    @ApiModelProperty(value = "出生日期")
	private java.lang.String birthday;
	/**民族代码*/
	@Excel(name = "民族代码", width = 15)
    @ApiModelProperty(value = "民族代码")
	private java.lang.String nationcode;
	/**民族名称*/
	@Excel(name = "民族名称", width = 15)
    @ApiModelProperty(value = "民族名称")
	private java.lang.String nation;
	/**国籍代码*/
	@Excel(name = "国籍代码", width = 15)
    @ApiModelProperty(value = "国籍代码")
	private java.lang.String nationalityCode;
	/**国籍名称*/
	@Excel(name = "国籍名称", width = 15)
    @ApiModelProperty(value = "国籍名称")
	private java.lang.String nationality;
	/**身高(cm) */
	@Excel(name = "身高(cm) ", width = 15)
    @ApiModelProperty(value = "身高(cm) ")
	private java.lang.String hight;
	/**体重(kg)*/
	@Excel(name = "体重(kg)", width = 15)
    @ApiModelProperty(value = "体重(kg)")
	private java.lang.String weight;
	/**出生证明编号*/
	@Excel(name = "出生证明编号", width = 15)
    @ApiModelProperty(value = "出生证明编号")
	private java.lang.String birthid;
	/**人员类别*/
	@Excel(name = "人员类别", width = 15)
    @ApiModelProperty(value = "人员类别")
	private java.lang.String clientclass;
	/**死亡标记*/
	@Excel(name = "死亡标记", width = 15)
    @ApiModelProperty(value = "死亡标记")
	private java.lang.String deathSign;
	/**死亡日期*/
	@Excel(name = "死亡日期", width = 15)
    @ApiModelProperty(value = "死亡日期")
	private java.lang.String deathdate;
	/**患者最高学历代码 */
	@Excel(name = "患者最高学历代码 ", width = 15)
    @ApiModelProperty(value = "患者最高学历代码 ")
	private java.lang.String educationcode;
	/**患者最高学历名称*/
	@Excel(name = "患者最高学历名称", width = 15)
    @ApiModelProperty(value = "患者最高学历名称")
	private java.lang.String education;
	/**职业代码 */
	@Excel(name = "职业代码 ", width = 15)
    @ApiModelProperty(value = "职业代码 ")
	private java.lang.String professioncode;
	/**职业名称 */
	@Excel(name = "职业名称 ", width = 15)
    @ApiModelProperty(value = "职业名称 ")
	private java.lang.String profession;
	/**行业类别*/
	@Excel(name = "行业类别", width = 15)
    @ApiModelProperty(value = "行业类别")
	private java.lang.String industryclass;
	/**参保类别 */
	@Excel(name = "参保类别 ", width = 15)
    @ApiModelProperty(value = "参保类别 ")
	private java.lang.String insurancetype;
	/**社保人员状态代码*/
	@Excel(name = "社保人员状态代码", width = 15)
    @ApiModelProperty(value = "社保人员状态代码")
	private java.lang.String participantstatus;
	/**社保人员状态名称*/
	@Excel(name = "社保人员状态名称", width = 15)
    @ApiModelProperty(value = "社保人员状态名称")
	private java.lang.String statusname;
	/**医保卡号*/
	@Excel(name = "医保卡号", width = 15)
    @ApiModelProperty(value = "医保卡号")
	private java.lang.String insurancecardNo;
	/**农合卡号 */
	@Excel(name = "农合卡号 ", width = 15)
    @ApiModelProperty(value = "农合卡号 ")
	private java.lang.String nhCardno;
	/**健康卡号*/
	@Excel(name = "健康卡号", width = 15)
    @ApiModelProperty(value = "健康卡号")
	private java.lang.String medicalcardId;
	/**社会保障号码*/
	@Excel(name = "社会保障号码", width = 15)
    @ApiModelProperty(value = "社会保障号码")
	private java.lang.String socialSecurityno;
	/**社保缴纳单位*/
	@Excel(name = "社保缴纳单位", width = 15)
    @ApiModelProperty(value = "社保缴纳单位")
	private java.lang.String paycorpration;
	/**社保缴纳地（省）代码 */
	@Excel(name = "社保缴纳地（省）代码 ", width = 15)
    @ApiModelProperty(value = "社保缴纳地（省）代码 ")
	private java.lang.String payprovinceCode;
	/**社保缴纳地（省）名称 */
	@Excel(name = "社保缴纳地（省）名称 ", width = 15)
    @ApiModelProperty(value = "社保缴纳地（省）名称 ")
	private java.lang.String payprovince;
	/**社保缴纳地城市代码*/
	@Excel(name = "社保缴纳地城市代码", width = 15)
    @ApiModelProperty(value = "社保缴纳地城市代码")
	private java.lang.String paycityCode;
	/**社保缴纳地城市名称*/
	@Excel(name = "社保缴纳地城市名称", width = 15)
    @ApiModelProperty(value = "社保缴纳地城市名称")
	private java.lang.String paycity;
	/**社保缴纳地（区/县）代码*/
	@Excel(name = "社保缴纳地（区/县）代码", width = 15)
    @ApiModelProperty(value = "社保缴纳地（区/县）代码")
	private java.lang.String paycountyCode;
	/**社保缴纳地（区/县）名称*/
	@Excel(name = "社保缴纳地（区/县）名称", width = 15)
    @ApiModelProperty(value = "社保缴纳地（区/县）名称")
	private java.lang.String paycounty;
	/**社保缴纳基数*/
	@Excel(name = "社保缴纳基数", width = 15)
    @ApiModelProperty(value = "社保缴纳基数")
	private java.lang.String payinsuraceLevel;
	/**贫困业务属性代码*/
	@Excel(name = "贫困业务属性代码", width = 15)
    @ApiModelProperty(value = "贫困业务属性代码")
	private java.lang.String poorSign;
	/**贫困业务属性名称*/
	@Excel(name = "贫困业务属性名称", width = 15)
    @ApiModelProperty(value = "贫困业务属性名称")
	private java.lang.String poorClass;
	/**出生（省）代码*/
	@Excel(name = "出生（省）代码", width = 15)
    @ApiModelProperty(value = "出生（省）代码")
	private java.lang.String birthprovinceCode;
	/**出生（省）名称*/
	@Excel(name = "出生（省）名称", width = 15)
    @ApiModelProperty(value = "出生（省）名称")
	private java.lang.String birthprovince;
	/**出生城市代码*/
	@Excel(name = "出生城市代码", width = 15)
    @ApiModelProperty(value = "出生城市代码")
	private java.lang.String birthcityCode;
	/**出生城市名称*/
	@Excel(name = "出生城市名称", width = 15)
    @ApiModelProperty(value = "出生城市名称")
	private java.lang.String birthcity;
	/**出生（区/县）代码*/
	@Excel(name = "出生（区/县）代码", width = 15)
    @ApiModelProperty(value = "出生（区/县）代码")
	private java.lang.String birthcountyCode;
	/**出生（区/县）名称*/
	@Excel(name = "出生（区/县）名称", width = 15)
    @ApiModelProperty(value = "出生（区/县）名称")
	private java.lang.String birthcounty;
	/**籍贯（省）代码*/
	@Excel(name = "籍贯（省）代码", width = 15)
    @ApiModelProperty(value = "籍贯（省）代码")
	private java.lang.String nativeprovinceCode;
	/**籍贯（省）名称*/
	@Excel(name = "籍贯（省）名称", width = 15)
    @ApiModelProperty(value = "籍贯（省）名称")
	private java.lang.String nativeprovince;
	/**籍贯城市代码*/
	@Excel(name = "籍贯城市代码", width = 15)
    @ApiModelProperty(value = "籍贯城市代码")
	private java.lang.String nativecityCode;
	/**籍贯城市名称*/
	@Excel(name = "籍贯城市名称", width = 15)
    @ApiModelProperty(value = "籍贯城市名称")
	private java.lang.String nativecity;
	/**籍贯（区/县）代码*/
	@Excel(name = "籍贯（区/县）代码", width = 15)
    @ApiModelProperty(value = "籍贯（区/县）代码")
	private java.lang.String nativecountyCode;
	/**籍贯（区/县）名称*/
	@Excel(name = "籍贯（区/县）名称", width = 15)
    @ApiModelProperty(value = "籍贯（区/县）名称")
	private java.lang.String nativecounty;
	/**现住址（省）代码*/
	@Excel(name = "现住址（省）代码", width = 15)
    @ApiModelProperty(value = "现住址（省）代码")
	private java.lang.String addrprovinceCode;
	/**现住址（省）名称*/
	@Excel(name = "现住址（省）名称", width = 15)
    @ApiModelProperty(value = "现住址（省）名称")
	private java.lang.String addrprovince;
	/**现住址城市代码*/
	@Excel(name = "现住址城市代码", width = 15)
    @ApiModelProperty(value = "现住址城市代码")
	private java.lang.String addrcityCode;
	/**现住址城市名称*/
	@Excel(name = "现住址城市名称", width = 15)
    @ApiModelProperty(value = "现住址城市名称")
	private java.lang.String addrcity;
	/**现住址（区/县）代码*/
	@Excel(name = "现住址（区/县）代码", width = 15)
    @ApiModelProperty(value = "现住址（区/县）代码")
	private java.lang.String addrcountyCode;
	/**现住址（区/县）名称*/
	@Excel(name = "现住址（区/县）名称", width = 15)
    @ApiModelProperty(value = "现住址（区/县）名称")
	private java.lang.String addrcounty;
	/**联系人姓名*/
	@Excel(name = "联系人姓名", width = 15)
    @ApiModelProperty(value = "联系人姓名")
	private java.lang.String contactor;
	/**联系人电话*/
	@Excel(name = "联系人电话", width = 15)
    @ApiModelProperty(value = "联系人电话")
	private java.lang.String contactorphone;
	/**患者与联系人关系*/
	@Excel(name = "患者与联系人关系", width = 15)
    @ApiModelProperty(value = "患者与联系人关系")
	private java.lang.String contactorrelation;
	/**监护人1姓名*/
	@Excel(name = "监护人1姓名", width = 15)
    @ApiModelProperty(value = "监护人1姓名")
	private java.lang.String guardianname;
	/**监护人1身份证号*/
	@Excel(name = "监护人1身份证号", width = 15)
    @ApiModelProperty(value = "监护人1身份证号")
	private java.lang.String guardianIdno;
	/**监护人1监护关系*/
	@Excel(name = "监护人1监护关系", width = 15)
    @ApiModelProperty(value = "监护人1监护关系")
	private java.lang.String guardianRelation;
	/**监护人2姓名*/
	@Excel(name = "监护人2姓名", width = 15)
    @ApiModelProperty(value = "监护人2姓名")
	private java.lang.String guardian2name;
	/**监护人2身份证号*/
	@Excel(name = "监护人2身份证号", width = 15)
    @ApiModelProperty(value = "监护人2身份证号")
	private java.lang.String guardian2Idno;
	/**监护人2监护关系*/
	@Excel(name = "监护人2监护关系", width = 15)
    @ApiModelProperty(value = "监护人2监护关系")
	private java.lang.String guardian2Relation;
	/**父亲姓名*/
	@Excel(name = "父亲姓名", width = 15)
    @ApiModelProperty(value = "父亲姓名")
	private java.lang.String father;
	/**父亲身份证号*/
	@Excel(name = "父亲身份证号", width = 15)
    @ApiModelProperty(value = "父亲身份证号")
	private java.lang.String fatherIdno;
	/**母亲姓名*/
	@Excel(name = "母亲姓名", width = 15)
    @ApiModelProperty(value = "母亲姓名")
	private java.lang.String mother;
	/**母亲身份证号*/
	@Excel(name = "母亲身份证号", width = 15)
    @ApiModelProperty(value = "母亲身份证号")
	private java.lang.String motherIdno;
	/**配偶姓名*/
	@Excel(name = "配偶姓名", width = 15)
    @ApiModelProperty(value = "配偶姓名")
	private java.lang.String spouse;
	/**配偶身份证号*/
	@Excel(name = "配偶身份证号", width = 15)
    @ApiModelProperty(value = "配偶身份证号")
	private java.lang.String spouseIdno;
	/**工作单位名称*/
	@Excel(name = "工作单位名称", width = 15)
    @ApiModelProperty(value = "工作单位名称")
	private java.lang.String workplacename;
	/**工作单位电话*/
	@Excel(name = "工作单位电话", width = 15)
    @ApiModelProperty(value = "工作单位电话")
	private java.lang.String workplacephone;
	/**工作单位邮政编码*/
	@Excel(name = "工作单位邮政编码", width = 15)
    @ApiModelProperty(value = "工作单位邮政编码")
	private java.lang.String workplacepostcode;
	/**ABO血型代码*/
	@Excel(name = "ABO血型代码", width = 15)
    @ApiModelProperty(value = "ABO血型代码")
	private java.lang.String aboBloodTypeCode;
	/**ABO血型名称*/
	@Excel(name = "ABO血型名称", width = 15)
    @ApiModelProperty(value = "ABO血型名称")
	private java.lang.String aboBloodType;
	/**Rh血型代码*/
	@Excel(name = "Rh血型代码", width = 15)
    @ApiModelProperty(value = "Rh血型代码")
	private java.lang.String rhBloodTypeCode;
	/**Rh血型名称*/
	@Excel(name = "Rh血型名称", width = 15)
    @ApiModelProperty(value = "Rh血型名称")
	private java.lang.String rhBloodType;
	/**居民公安系统内部ID*/
	@Excel(name = "居民公安系统内部ID", width = 15)
    @ApiModelProperty(value = "居民公安系统内部ID")
	private java.lang.String policeid;
	/**户号*/
	@Excel(name = "户号", width = 15)
    @ApiModelProperty(value = "户号")
	private java.lang.String familyno;
	/**户类型代码*/
	@Excel(name = "户类型代码", width = 15)
    @ApiModelProperty(value = "户类型代码")
	private java.lang.String familyclass;
	/**离休人员类别*/
	@Excel(name = "离休人员类别", width = 15)
    @ApiModelProperty(value = "离休人员类别")
	private java.lang.String retireclass;
	/**离退休日期*/
	@Excel(name = "离退休日期", width = 15)
    @ApiModelProperty(value = "离退休日期")
	private java.lang.String retiredate;
	/**离休地属*/
	@Excel(name = "离休地属", width = 15)
    @ApiModelProperty(value = "离休地属")
	private java.lang.String retireaddr;
	/**干部类别*/
	@Excel(name = "干部类别", width = 15)
    @ApiModelProperty(value = "干部类别")
	private java.lang.String cadreclass;
	/**就业状态*/
	@Excel(name = "就业状态", width = 15)
    @ApiModelProperty(value = "就业状态")
	private java.lang.String employStatus;
	/**军残等级*/
	@Excel(name = "军残等级", width = 15)
    @ApiModelProperty(value = "军残等级")
	private java.lang.String milDisableClass;
	/**参保状态*/
	@Excel(name = "参保状态", width = 15)
    @ApiModelProperty(value = "参保状态")
	private java.lang.String insuranceStatus;
	/**三无人员标识*/
	@Excel(name = "三无人员标识", width = 15)
    @ApiModelProperty(value = "三无人员标识")
	private java.lang.String noranySign;
	/**低收入老年人标识*/
	@Excel(name = "低收入老年人标识", width = 15)
    @ApiModelProperty(value = "低收入老年人标识")
	private java.lang.String poorOldSign;
	/**数据来源编码*/
	@Excel(name = "数据来源编码", width = 15)
    @ApiModelProperty(value = "数据来源编码")
	private java.lang.String dataResouceId;
	/**数据来源名称*/
	@Excel(name = "数据来源名称", width = 15)
    @ApiModelProperty(value = "数据来源名称")
	private java.lang.String dataResouce;
	/**etl数据来源*/
	@Excel(name = "etl数据来源", width = 15)
    @ApiModelProperty(value = "etl数据来源")
	private java.lang.String etlSource;
	/**etl处理时间*/
	@Excel(name = "etl处理时间", width = 15)
    @ApiModelProperty(value = "etl处理时间")
	private java.lang.String etlTime;
}
