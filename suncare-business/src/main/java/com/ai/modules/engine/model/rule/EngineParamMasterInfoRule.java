/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.medical.entity.MedicalDrugRule;

/**
 * 
 * 功能描述：药品、收费、临床路径关联DWB_MASTER_INFO参数
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamMasterInfoRule extends AbsEngineParamRule {	
	//年龄单位
	private String ageUnit;
	//年龄下限
	private String ageLow;
	//年龄上限
	private String ageHigh;
	//年龄下限比较符
	private String ageLowCompare;
	//年龄上限比较符
	private String ageHighCompare;
	//性别
	private String sex;
	//就诊类型
	private String jzlx;
	//医保类型
	private String yblx;
	//医院级别
	private String yyjb;
	//科室
	private String office;
	//医院
	private String org;
	
	//限制范围
	private Set<String> limitScopeSet;
	
	public EngineParamMasterInfoRule() {
		
	}
	
	public EngineParamMasterInfoRule(MedicalDrugRule rule) {
        this.ageUnit = rule.getAgeUnit();
        if(rule.getAgeLow()!=null) {
        	this.ageLow = String.valueOf(rule.getAgeLow());
        }
        this.ageLowCompare = rule.getAgeLowCompare();
        if(rule.getAgeHigh()!=null) {
        	this.ageHigh = String.valueOf(rule.getAgeHigh());
        }
        this.ageHighCompare = rule.getAgeHighCompare();
        this.sex = rule.getSex();
        this.jzlx = rule.getJzlx();
        this.yblx = rule.getYblx();
        this.yyjb = rule.getYyjb();
        this.office = rule.getOffice();
        this.org = rule.getOrg();
        
        String[] limitScope = rule.getLimitScope().split(",");
		limitScopeSet = new HashSet<String>();
		for(String scope : limitScope) {
			limitScopeSet.add(scope);
		}
	}
	
	public EngineParamMasterInfoRule(String tableName, String colName) {
		super(tableName, colName);
	}

	@Override
	public String where() {
		if(!(limitScopeSet.contains("01") 
				|| limitScopeSet.contains("02")
				|| limitScopeSet.contains("03") 
				|| limitScopeSet.contains("04")
				|| limitScopeSet.contains("05") 
				|| limitScopeSet.contains("06")
				|| limitScopeSet.contains("35"))) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWB_MASTER_INFO", "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("VISITID:*");
		/*if(StringUtils.isNotBlank(age)) {
			//年龄
			sb.append(" AND ");
			switch (age) {
			case "1":
				//新生儿
				sb.append("DAYAGE:[0 TO 28]");
				break;
			case "2":
				//小儿
				sb.append("MONTHAGE:[0 TO 1]");
				break;
			case "3":
				//儿童
				sb.append("YEARAGE").append(":[0 TO 18}");
				break;
			case "4":
				//老年人
				sb.append("YEARAGE").append(":[60 TO *]");
				break;
			default:
				//成年人
				sb.append("YEARAGE").append(":[18 TO *]");
				break;
			}
		}*/
		if(limitScopeSet.contains("01")) {
			//年龄
			String field = "YEARAGE";
			if("day".equals(ageUnit)) {
				field = "DAYAGE";
			} else if("month".equals(ageUnit)) {
				field = "MONTHAGE";
			}
			sb.append(" AND ").append(field).append(":");
			String low = "{", high = "}";	
			if("<=".equals(ageLowCompare)) {
				low = "[";
			}
			if("<=".equals(ageHighCompare)) {
				high = "]";
			}
			sb.append(low);
			if(StringUtils.isNotBlank(ageLow)) {
				if(!"-1".equals(ageLow)) {
					sb.append(ageLow);
				} else {
					sb.append("*");
				}
			} else {
				sb.append("*");
			}
			sb.append(" TO ");
			if(StringUtils.isNotBlank(ageHigh)) {
				if(!"-1".equals(ageHigh)) {
					sb.append(ageHigh);
				} else {
					sb.append("*");
				}
			} else {
				sb.append("*");
			}
			sb.append(high);
		}
		if(limitScopeSet.contains("02")) {
			//性别
			sb.append(" AND SEX_CODE:").append(sex);
		}
		if(limitScopeSet.contains("03")) {
			//就诊类型
			sb.append(" AND ").append(this.parseMultParam("VISITTYPE_ID", jzlx, true));
		}
		if(limitScopeSet.contains("04")) {
			//医保类型
			sb.append(" AND ").append(this.parseMultParam("INSURANCETYPE", yblx, false));
		}
		if(limitScopeSet.contains("05")) {
			//机构级别
			sb.append(" AND ").append(this.parseMultParam("HOSPLEVEL", yyjb, false));
		}
		if(limitScopeSet.contains("06")) {
			//科室
			sb.append(" AND ").append(this.parseMultParam("DEPTID", office, false));
		}
		if(limitScopeSet.contains("35")) {
			//医院
			sb.append(" AND ").append(this.parseMultParam("ORGID", org, false));
		}
		sb.append("\"");

		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}
	
	public String ignoreNullWhere() {
		if(!(limitScopeSet.contains("01") 
				|| limitScopeSet.contains("02")
				|| limitScopeSet.contains("03") 
				|| limitScopeSet.contains("04")
				|| limitScopeSet.contains("05") 
				|| limitScopeSet.contains("06")
				|| limitScopeSet.contains("35"))) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWB_MASTER_INFO", "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("VISITID:*");
		
		if(limitScopeSet.contains("01")) {
			//年龄
			String field = "YEARAGE";
			if("day".equals(ageUnit)) {
				field = "DAYAGE";
			} else if("month".equals(ageUnit)) {
				field = "MONTHAGE";
			}
			sb.append(" AND ").append(field).append(":[0 TO *}");
		}
		if(limitScopeSet.contains("02")) {
			//性别
			sb.append(" AND SEX_CODE:?* AND SEX_CODE:(1 OR 2)");
		}
		if(limitScopeSet.contains("03")) {
			//就诊类型
			sb.append(" AND VISITTYPE_ID:?*");
		}
		if(limitScopeSet.contains("04")) {
			//医保类型
			sb.append(" AND INSURANCETYPE:?*");
		}
		if(limitScopeSet.contains("05")) {
			//机构级别
			sb.append(" AND HOSPLEVEL:?*");
		}
		if(limitScopeSet.contains("06")) {
			//科室
			sb.append(" AND DEPTID:?*");
		}
		if(limitScopeSet.contains("35")) {
			//医院
			sb.append(" AND ORGID:?*");
		}
		sb.append("\"");

		return sb.toString();
	}
	
	private String parseMultParam(String key, String mult, boolean like) {
		StringBuilder sb = new StringBuilder();
		if(mult.indexOf("|")==-1) {
			sb.append(key).append(":").append(mult);
			if(like) {
				sb.append("*");
			}
		} else {
			String[] values = StringUtils.split(mult, "|");
			sb.append(key).append(":(");
			int index = 0;
			for(String value : values) {
				if(index>0) {
					sb.append(" OR ");
				}
				sb.append(value);
				if(like) {
					sb.append("*");
				}
				index++;
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public String getAgeLow() {
		return ageLow;
	}

	public void setAgeLow(String ageLow) {
		this.ageLow = ageLow;
	}

	public String getAgeHigh() {
		return ageHigh;
	}

	public void setAgeHigh(String ageHigh) {
		this.ageHigh = ageHigh;
	}

	public String getAgeLowCompare() {
		return ageLowCompare;
	}

	public void setAgeLowCompare(String ageLowCompare) {
		this.ageLowCompare = ageLowCompare;
	}

	public String getAgeHighCompare() {
		return ageHighCompare;
	}

	public void setAgeHighCompare(String ageHighCompare) {
		this.ageHighCompare = ageHighCompare;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getJzlx() {
		return jzlx;
	}

	public void setJzlx(String jzlx) {
		this.jzlx = jzlx;
	}

	public String getYblx() {
		return yblx;
	}

	public void setYblx(String yblx) {
		this.yblx = yblx;
	}

	public String getYyjb() {
		return yyjb;
	}

	public void setYyjb(String yyjb) {
		this.yyjb = yyjb;
	}

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public String getAgeUnit() {
		return ageUnit;
	}

	public void setAgeUnit(String ageUnit) {
		this.ageUnit = ageUnit;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}
}
