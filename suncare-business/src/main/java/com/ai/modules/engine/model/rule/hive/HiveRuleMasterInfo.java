/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;

/**
 * 
 * 功能描述：规则关联DWB_MASTER_INFO参数
 *
 * @author  zhangly
 * Date: 2020年12月18日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveRuleMasterInfo extends AbsHiveParamRule {	
	//年龄单位
	private String ageUnit;
	//年龄范围
	private String ageRange;
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
	
	public HiveRuleMasterInfo() {
		
	}
	
	public HiveRuleMasterInfo(String fromTable) {
		this.fromTable = fromTable;
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from $table where");
		if(reverse) {
			sb.append(" not");
		}
		sb.append(" exists(select 1 from DWB_MASTER_INFO x1 where $table.visitid=x1.visitid");
		
		if(StringUtils.isNotBlank(ageUnit)) {
			//年龄
			String field = "YEARAGE";
			if("day".equals(ageUnit)) {
				field = "DAYAGE";
			} else if("month".equals(ageUnit)) {
				field = "MONTHAGE";
			}
			
			ageRange = StringUtils.replace(ageRange, ",)", ",*)");
			ageRange = StringUtils.replace(ageRange, "(,", "(*,");
			ageRange = StringUtils.replace(ageRange, ",]", ",*]");
			ageRange = StringUtils.replace(ageRange, "[,", "[*,");
			String[] array = StringUtils.split(ageRange, ",");
			String min = array[0];
			min = StringUtils.replace(min, "(", ">");
			min = StringUtils.replace(min, "[", ">=");
			if(!min.contains("*")) {
				sb.append(" AND ").append(field).append(min);
			}
			String max = array[1];
			max = StringUtils.replace(max, ")", ">");
			max = StringUtils.replace(max, "]", ">=");
			if(!max.contains("*")) {
				sb.append(" AND ").append(max).append(field);
			}
		}
		if(StringUtils.isNotBlank(sex)) {
			//性别
			sb.append(" AND SEX_CODE='").append(sex).append("'");
		}
		if(StringUtils.isNotBlank(jzlx)) {
			//就诊类型
			sb.append(" AND ").append(this.parseMultParam("VISITTYPE_ID", jzlx, true));
		}
		if(StringUtils.isNotBlank(yblx)) {
			//医保类型
			sb.append(" AND ").append(this.parseMultParam("INSURANCETYPE", yblx, false));
		}
		if(StringUtils.isNotBlank(yyjb)) {
			//机构级别
			sb.append(" AND ").append(this.parseMultParam("HOSPLEVEL", yyjb, false));
		}
		if(StringUtils.isNotBlank(office)) {
			//科室
			sb.append(" AND ").append(this.parseMultParam("DEPTID", office, false));
		}
		if(StringUtils.isNotBlank(org)) {
			//医院
			sb.append(" AND ").append(this.parseMultParam("ORGID", org, false));
		}
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}
	
	public String ignoreNullWhere(String alias) {
		List<String> wheres = new ArrayList<String>();
		if(StringUtils.isNotBlank(ageUnit)) {
			//年龄
			String field = "YEARAGE";
			if("day".equals(ageUnit)) {
				field = "DAYAGE";
			} else if("month".equals(ageUnit)) {
				field = "MONTHAGE";
			}
			wheres.add("$alias."+field+">=0");
		}
		if(StringUtils.isNotBlank(sex)) {
			//性别
			wheres.add("$alias.SEX_CODE in('1','2')");
		}
		if(StringUtils.isNotBlank(jzlx)) {
			//就诊类型
			wheres.add("$alias.VISITTYPE_ID is not null");
		}
		if(StringUtils.isNotBlank(yblx)) {
			//医保类型
			wheres.add("$alias.INSURANCETYPE is not null");
		}
		if(StringUtils.isNotBlank(yyjb)) {
			//机构级别
			wheres.add("$alias.HOSPLEVEL is not null");
		}
		if(StringUtils.isNotBlank(office)) {
			//科室
			wheres.add("$alias.DEPTID is not null");
		}
		if(StringUtils.isNotBlank(org)) {
			//医院
			wheres.add("$alias.ORGID is not null");
		}

		String where = StringUtils.join(wheres, " and ");
		if(StringUtils.isBlank(alias)) {
			where = StringUtils.replace(where, "$alias.", "");
		} else {
			where = StringUtils.replace(where, "$alias", alias);
		}
		return where;
	}
	
	private String parseMultParam(String key, String mult, boolean like) {
		StringBuilder sb = new StringBuilder();
		if(mult.indexOf("|")==-1) {
			if(like) {
				sb.append(key).append(" like '").append(mult).append("%'");
			} else {
				sb.append(key).append("='").append(mult).append("'");
			}
		} else {
			String[] values = StringUtils.split(mult, "|");
			if(like) {
				sb.append("(");
				int index = 0;
				for(String value : values) {
					if(index>0) {
						sb.append(" OR ");
					}
					sb.append(key).append(" like '").append(value).append("%'");
					index++;
				}
				sb.append(")");
			} else {
				sb.append(key).append(" in('").append(StringUtils.join(values, "','")).append("')");
			}
		}
		return sb.toString();
	}
	
	public HiveRuleMasterInfo with(MedicalRuleConditionSet condition) {
		String condiType = condition.getField();
		if(AbsRuleParser.RULE_CONDI_SEX.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_SEX.equals(condiType)) {
			//性别
			setSex(condition.getExt1());			
		} else if(AbsRuleParser.RULE_CONDI_AGE.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_AGE.equals(condiType)) {
			//年龄
			setAgeUnit(condition.getExt2());
			setAgeRange(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_JZLX.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_JZLX.equals(condiType)) {
			//就诊类型
			setJzlx(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_YBLX.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_YBLX.equals(condiType)) {
			//医保类型
			setYblx(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_YYJB.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_YYJB.equals(condiType)) {
			//医院级别
			setYyjb(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_OFFICE.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_OFFICE.equals(condiType)) {
			//科室
			setOffice(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_ORG.equals(condiType)) {
			//医院
			setOrg(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_HOSPLEVELTYPE.equals(condiType)) {
			if(StringUtils.isNotBlank(condition.getExt1())) {
				//医院级别
				setYyjb(condition.getExt1());
			}
		}
		return this;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = ageRange;
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
