/**
 * EngineClinicalRule.java	  V1.0   2020年4月21日 下午3:31:08
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.clinical;

/**
 * 
 * 功能描述：临床路径准入条件规则
 *
 * @author  zhangly
 * Date: 2020年4月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineClinicalRule {
	//患者最小年龄
	private Integer minAge;
	//患者最大年龄
	private Integer maxAge;
	//年龄单位
	private String ageUnit;
	//疾病组编码，多个之间使用逗号分隔
	private String diseaseGroupCode;
	//手术编码，多个之间使用逗号分隔
	private String operationCode;
	//检查化验项目，多个之间使用逗号分隔
	private String treatCode;
	//药品组，多个之间使用逗号分隔
	private String durgGroupCode;
	//病理形态
	private String pathologys;
	
	public String getDiseaseGroupCode() {
		return diseaseGroupCode;
	}
	public void setDiseaseGroupCode(String diseaseGroupCode) {
		this.diseaseGroupCode = diseaseGroupCode;
	}
	public String getOperationCode() {
		return operationCode;
	}
	public void setOperationCode(String operationCode) {
		this.operationCode = operationCode;
	}
	public String getTreatCode() {
		return treatCode;
	}
	public void setTreatCode(String treatCode) {
		this.treatCode = treatCode;
	}
	public String getDurgGroupCode() {
		return durgGroupCode;
	}
	public void setDurgGroupCode(String durgGroupCode) {
		this.durgGroupCode = durgGroupCode;
	}
	public Integer getMinAge() {
		return minAge;
	}
	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}
	public Integer getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}
	public String getPathologys() {
		return pathologys;
	}
	public void setPathologys(String pathologys) {
		this.pathologys = pathologys;
	}
	public String getAgeUnit() {
		return ageUnit;
	}
	public void setAgeUnit(String ageUnit) {
		this.ageUnit = ageUnit;
	}	
}
