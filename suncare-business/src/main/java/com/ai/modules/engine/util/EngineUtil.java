/**
 * EngineUtil.java	  V1.0   2019年11月28日 下午3:34:22
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.ai.modules.engine.handle.cases.node.AbsNodeHandle;
import com.ai.modules.engine.handle.cases.node.AbsNodeRuleHandle;
import com.ai.modules.engine.handle.cases.node.AbsTemplateNodeRuleHandle;
import com.ai.modules.engine.handle.cases.node.JoinNodeRuleHandle;
import com.ai.modules.engine.handle.cases.node.NodeRuleHandleFactory;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;

public class EngineUtil {
	//实体对象字段与solr字段映射关系
	public final static Map<String, String> FIELD_MAPPING = new HashMap<String, String>();
	//病例主表
	public final static String DWB_MASTER_INFO = "DWB_MASTER_INFO";
	public final static String DWB_DOCTOR = "DWB_DOCTOR";
	public final static String DWB_CLIENT = "DWB_CLIENT";
	public final static String DWB_CHARGE_DETAIL = "DWB_CHARGE_DETAIL";
	public final static String DWB_SETTLEMENT = "DWB_SETTLEMENT";
//	public final static String DWB_ORGANIZATION = "DWB_ORGANIZATION";
	public final static String DWB_ORGANIZATION = "STD_ORGANIZATION";
	public final static String STD_ORGANIZATION = "STD_ORGANIZATION";
	public final static String DWB_DIAG = "DWB_DIAG";
	public final static String DWB_DEPARTMENT = "DWB_DEPARTMENT";
	public final static String YW_JZXX00 = "ywjzxx";
	// 病例-医疗机构统计表
	public final static String DWS_1VISIT_TAG = "DWS_1VISIT_TAG";
	//一次就诊明细统计表
	public final static String DWS_PATIENT_1VISIT_ITEMSUM = "DWS_PATIENT_1VISIT_ITEMSUM";
	//一次就诊明细按天统计表
	public final static String DWS_PATIENT_1VISIT_1DAY_ITEMSUM = "DWS_PATIENT_1VISIT_1DAY_ITEMSUM​";

	//病例不合理行为表
	public final static String MEDICAL_UNREASONABLE_ACTION = "MEDICAL_UNREASONABLE_ACTION";
	//药品不合规行为表
	public final static String MEDICAL_UNREASONABLE_DRUG_ACTION = "MEDICAL_UNREASONABLE_DRUG_ACTION";
	//药品不合规行为试算表
	public final static String MEDICAL_TRAIL_DRUG_ACTION = "MEDICAL_TRAIL_DRUG_ACTION";
	public final static String MEDICAL_TRAIL_ACTION = "MEDICAL_TRAIL_ACTION";
	//不合规行为结果表
	public final static String MEDICAL_BREAK_BEHAVIOR_RESULT = "MEDICAL_BREAK_BEHAVIOR_RESULT";
	// 医嘱信息表
	public final static String DWB_ORDER = "DWB_ORDER";

	// 初审修改solr导入文件夹
	/*public final static String SOLR_IMPORT_FIRST_STEP = "SOLR_IMPORT_FIRST_STEP";
	public final static String SOLR_IMPORT_SECOND_STEP = "SOLR_IMPORT_SECOND_STEP";*/
	public static final String SOLR_CACHE_DATA = "SOLR_CACHE_DATA";
	public static final String SOLR_IMPORT_STEP = "SOLR_IMPORT_STEP";
	public static final String SOLR_IMPORT_GROUP_STEP = "SOLR_IMPORT_GROUP_STEP";

	public final static String MEDICAL_CHARGE_DETAIL = "MEDICAL_CHARGE_DETAIL";
	public final static String MEDICAL_PATIENT_1VISIT_ITEMSUM = "MEDICAL_PATIENT_1VISIT_ITEMSUM";

	public final static String DWB_DISCHARGE = "DWB_DISCHARGE";
	public final static String DWB_ADMMISION = "DWB_ADMMISION";

	//病例主表与业务表关联关系
	public final static Map<String, EngineMapping> ENGIME_MAPPING = new HashMap<String, EngineMapping>();

	static {
		FIELD_MAPPING.put("jzid", "VISITID");
		FIELD_MAPPING.put("patientid", "CLIENTID");
		FIELD_MAPPING.put("patientname", "CLIENTNAME");
		FIELD_MAPPING.put("sex", "SEX");
		FIELD_MAPPING.put("age", "YEARAGE");
		FIELD_MAPPING.put("orgid", "ORGID");
		FIELD_MAPPING.put("orgname", "ORGNAME");
		FIELD_MAPPING.put("deptname", "DEPTNAME");
		FIELD_MAPPING.put("deptid", "DEPTID");
		FIELD_MAPPING.put("visitdate", "VISITDATE");
		FIELD_MAPPING.put("doctorid", "DOCTORID");
		FIELD_MAPPING.put("doctorname", "DOCTORNAME");
		FIELD_MAPPING.put("pathonogyDiseasecode", "PATHONOGY_DISEASECODE");
		FIELD_MAPPING.put("pathonogyDisease", "PATHONOGY_DISEASE");
		FIELD_MAPPING.put("visittypeId", "VISITTYPE_ID");
		FIELD_MAPPING.put("visittype", "VISITTYPE");
		FIELD_MAPPING.put("insurancetype", "INSURANCETYPE");
		FIELD_MAPPING.put("leavedate", "LEAVEDATE");
		FIELD_MAPPING.put("totalfee", "TOTALFEE");

        FIELD_MAPPING.put("id", "id");
        FIELD_MAPPING.put("batchId", "BATCH_ID");
        FIELD_MAPPING.put("ruleId", "RULE_ID");
        FIELD_MAPPING.put("ruleName", "RULE_NAME");
        FIELD_MAPPING.put("ruleType", "RULE_TYPE");
        FIELD_MAPPING.put("visitid", "VISITID");
        FIELD_MAPPING.put("itemcode", "ITEMCODE");
        FIELD_MAPPING.put("itemname", "ITEMNAME");
        FIELD_MAPPING.put("itemclass", "ITEMCLASS");
        FIELD_MAPPING.put("itemclassid", "ITEMCLASSID");
        FIELD_MAPPING.put("specificaion", "SPECIFICAION");
        FIELD_MAPPING.put("chargeunit", "CHARGEUNIT");
        FIELD_MAPPING.put("clientid", "CLIENTID");
		FIELD_MAPPING.put("prescripttime", "PRESCRIPTTIME");

		EngineMapping mapping = new EngineMapping(DWB_DOCTOR, "DOCTORID", "DOCTORID");
		ENGIME_MAPPING.put(DWB_DOCTOR, mapping);
		mapping = new EngineMapping(DWB_CLIENT, "CLIENTID", "CLIENTID");
		ENGIME_MAPPING.put(DWB_CLIENT, mapping);
		mapping = new EngineMapping(DWB_CHARGE_DETAIL, "VISITID", "VISITID");
		ENGIME_MAPPING.put(DWB_CHARGE_DETAIL, mapping);
		mapping = new EngineMapping("DWB_SETTLEMENT", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
//		mapping = new EngineMapping("DWB_ORGANIZATION", "ORGID", "ORGID");
		mapping = new EngineMapping("STD_ORGANIZATION", "ORGID", "ORGID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DEPARTMENT", "DEPTID", "DEPTID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_CHRONIC_PATIENT", "CLIENTID", "CLIENTID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DIAG", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_ORDER", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_化验结果
		mapping = new EngineMapping("DWB_TEST_RESULT", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_化验主记录
		mapping = new EngineMapping("DWB_TEST", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_影像检查主记录
		mapping = new EngineMapping("DWB_PACS_INFO", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_影像检查报告
		mapping = new EngineMapping("DWB_PACS_REPORT", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//DWS层-直接关联dwb_master_info.visitid
		//病人一次就诊某项目统计
		mapping = new EngineMapping("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//病人+单次就诊+药品分类
		mapping = new EngineMapping("DWS_PATIENT_1VISIT_DRUGCLASSSUM", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//病人+单次就诊+诊疗项目分类
		mapping = new EngineMapping("DWS_PATIENT_1VISIT_TREATCLASSSUM", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//业务规则-相邻两次门急诊相隔天数
		mapping = new EngineMapping("DWS_MZAPART_DAYS", "MZ_ID_THIS", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//业务规则-相邻两次住院相隔天数
		mapping = new EngineMapping("DWS_ZYAPART_DAYS", "ZY_ID_THIS", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-住院期间门诊就诊
		mapping = new EngineMapping("DWS_CLINIC_INHOSPITAL", "MZ_ID_INHOSPITAL", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-同一次就诊化验执行日期晚于出院日期
		mapping = new EngineMapping("DWS_TESTDATE_LEAVEDATE", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-同一次就诊检查执行日期晚于出院日期
		mapping = new EngineMapping("DWS_CHECKDATE_LEAVEDATE", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-重叠住院
		mapping = new EngineMapping("DWS_INHOSPITAL_OVERLAP", "ZY_ID_OTHER", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则_his和医保数据源的比较结果
		mapping = new EngineMapping("DWS_TAG_COMPARE_HIS_YB", "YB_VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则_节假日期间住院
		mapping = new EngineMapping("DWS_INHOSPITAL_VACATION", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-患者死亡后就诊
		mapping = new EngineMapping("DWS_CLIENTDEATH_VISIT_DETAIL", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-医生死亡后出诊
		mapping = new EngineMapping("DWS_DOCTORDEATH_PRACTICE_DETAIL", "VISITID", "VISITID");
		ENGIME_MAPPING.put(mapping.getFromIndex(), mapping);
	}

    /**节点类型*/
    //开始
	public final static String NODE_TYPE_START = "start";
	//结束
	public final static String NODE_TYPE_END = "end";
	//条件
	public final static String NODE_TYPE_CONDITIONAL = "diam";
	//按病人筛查
	public final static String NODE_TYPE_PATIENT = "rect_patient_dws";
	//按医生筛查
	public final static String NODE_TYPE_DOCTOR = "rect_doctor_dws";
	//按医院筛查
	public final static String NODE_TYPE_ORG = "rect_org_dws";
	//按科室筛查
	public final static String NODE_TYPE_DEPT = "rect_dept_dws";
	//交集
	public final static String COMPARE_INTERSECT = "intersect";
	//差集
	public final static String COMPARE_DIFF = "diff";
	//并集
	public final static String COMPARE_UNION= "union";

	public final static int MAX_ROW = 200000;
	//solr每页查询条数
	public final static int PAGE_SIZE = 5000;
	//分隔符
	public static String SPLIT_KEY = "::";

	//核心线程数
	public static int CORE_POOL_SIZE = 4;
	//最大线程数
	public static int MAXIMUM_POOL_SIZ = 4;
	//线程空闲时间，单位分钟
	public static int KEEP_ALIVE_TIME = 10;

	/**
	 *
	 * 功能描述：流程节点单条规则解析成查询条件字符串
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年11月28日 下午5:02:55</p>
	 *
	 * @param rule
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String parseConditionExpression(EngineNodeRule rule) {
		AbsNodeRuleHandle handle = NodeRuleHandleFactory.getNodeRuleHandle(rule);
		return handle.where();
	}

	/**
	 *
	 * 功能描述：流程节点单条规则组解析成查询条件字符串
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年11月28日 下午5:04:32</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String parseConditionExpression(EngineNodeRuleGrp grp) {
		if(grp.mergeRuleEnabled()) {
			//组内查询条件允许合并
			return parseConditionExpressionMerge(grp);
		}

		StringBuilder sb = new StringBuilder();

		if(StringUtils.isNotBlank(grp.getLogic())) {
			sb.append(grp.getLogic().toUpperCase()).append(" ");
		}

		int size = grp.getRuleList().size();
		if(size>1) {
			sb.append("(");
		}
		String condition = null;
		for(EngineNodeRule rule : grp.getRuleList()) {
			condition = parseConditionExpression(rule);
			sb.append(condition).append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		if(size>1) {
			sb.append(")");
		}

		return sb.toString();
	}

	/**
	 * 
	 * 功能描述：节点某组内所有的查询条件是否允许合并
	 *
	 * @author  zhangly
	 *
	 * @param grp
	 * @return
	 */
	public static String parseConditionExpressionMerge(EngineNodeRuleGrp grp) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(grp.getLogic())) {
			sb.append(grp.getLogic().toUpperCase()).append(" ");
		}
		//组内所有的查询条件合并
		EngineNodeRule first = new EngineNodeRule();
		BeanUtils.copyProperties(grp.getRuleList().get(0), first);
		boolean notExists = "notlike".equals(first.getCompareType()) || "<>".equals(first.getCompareType());
		AbsNodeRuleHandle templateHandle = NodeRuleHandleFactory.getNodeRuleHandle(first);
		if(templateHandle instanceof AbsTemplateNodeRuleHandle) {
			//使用脚本模板，再替换查询条件
			AbsTemplateNodeRuleHandle porcess = (AbsTemplateNodeRuleHandle)templateHandle;
			String template = porcess.template();
			StringBuilder whereSb = new StringBuilder();
			for(EngineNodeRule nodeRule : grp.getRuleList()) {
				AbsNodeRuleHandle handle = new JoinNodeRuleHandle(nodeRule);
				if(StringUtils.isNotBlank(nodeRule.getLogic())) {
					String logic = nodeRule.getLogic().toUpperCase();
					if(notExists) {
						//不包含条件合并后组内之间关系改成or
						logic = "OR";
					}
					whereSb.append(" ").append(logic).append(" ");
				}
				String where = handle.handler();
				whereSb.append(where);
			}
			template = StringUtils.replace(template, "$where", whereSb.toString());
			sb.append(template);
		} else {
			boolean isJoin = isJoin(first.getTableName().toUpperCase());
			if(isJoin) {
				//使用脚本模板，再替换查询条件
				JoinNodeRuleHandle porcess = new JoinNodeRuleHandle(first);
				String template = porcess.template();
				StringBuilder whereSb = new StringBuilder();
				for(EngineNodeRule nodeRule : grp.getRuleList()) {
					AbsNodeRuleHandle handle = new JoinNodeRuleHandle(nodeRule);
					if(StringUtils.isNotBlank(nodeRule.getLogic())) {
						String logic = nodeRule.getLogic().toUpperCase();
						if(notExists) {
							//不包含条件合并后组内之间关系改成or
							logic = "OR";
						}
						whereSb.append(" ").append(logic).append(" ");
					}
					String where = handle.handler();
					whereSb.append(where);
				}
				template = StringUtils.replace(template, "$where", whereSb.toString());
				sb.append(template);
			} else {
				List<EngineNodeRule> ruleList = grp.getRuleList();
				if(ruleList.size()>1) {
					sb.append("(");
				}
				String condition = null;
				for(int i=0,len=ruleList.size(); i<len; i++) {
					EngineNodeRule nodeRule = ruleList.get(i);
					AbsNodeRuleHandle handle = NodeRuleHandleFactory.getNodeRuleHandle(nodeRule);
					condition = handle.where(true);
					sb.append(condition);
					if(i<len-1) {
						sb.append(" ");
					}
				}
				if(ruleList.size()>1) {
					sb.append(")");
				}
			}
		}
		return sb.toString();
	}
	
	protected static boolean isJoin(String tableName) {
		Set<String> filter = new HashSet<String>();
		filter.add(EngineUtil.DWB_MASTER_INFO);
		filter.add("STD_DIAGGROUP");
		filter.add("STD_TREATMENT");
		filter.add("STD_TREATGROUP");
		filter.add("STD_DRUGGROUP");
		return !filter.contains(tableName);
	}

	/**
	 *
	 * 功能描述：流程节点解析成查询条件字符串
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年11月28日 下午6:07:38</p>
	 *
	 * @param node
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String parseConditionExpression(EngineNode node) {
		if(NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
				|| NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
			//开始或条件节点
			return null;
		}
		//节点处理查询条件工厂
		AbsNodeHandle handle = NodeRuleHandleFactory.getNodeHandle(node);
		return handle.parseConditionExpression();
	}

	/**
	 *
	 * 功能描述：solr查询条件特殊字符处理
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年6月5日 下午9:04:23</p>
	 *
	 * @param s
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String escapeQueryChars(String s) {
		StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < s.length(); i++) {
	    	char c = s.charAt(i);
	    	// These characters are part of the query syntax and must be escaped
	    	if (c == '\\' || c == '+' || c == '!'  || c == '(' || c == ')' || c == ':'
	    			|| c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
	    			|| c == '*' || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'
	    			|| Character.isWhitespace(c)) { // || c == '*'
				sb.append("\\");
	    		/*if(c == '[' || c == ']') {
	    			sb.append("\\");
	    		}*/
	    	}
	    	sb.append(c);
	    }
	    return sb.toString();
	}

	public static String parseMultParam(String key, String mult, String split, boolean like) {
		StringBuilder sb = new StringBuilder();
		if(mult.indexOf(split)==-1) {
			sb.append(key).append(":").append(mult);
			if(like) {
				sb.append("*");
			}
		} else {
			String[] values = StringUtils.split(mult, split);
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

	public static String parseMultParam(String key, String mult, boolean like) {
		return parseMultParam(key, mult, "|", like);
	}

	public static String getDwsPeriod(String period) {
		String attr = "D";
		switch(period) {
			case "day":
				attr = "D";
				break;
			case "1day":
				attr = "D";
				break;
			case "week":
				attr = "W";
				break;
			case "7day":
				attr = "W";
				break;
			case "month":
				attr = "M";
				break;
			case "1month":
				attr = "M";
				break;
			case "quarter":
				attr = "Q";
				break;
			case "3month":
				attr = "Q";
				break;
			case "year":
				attr = "Y";
				break;
			case "1year":
				attr = "Y";
				break;
			case "freq4":
				attr = "W";
				break;
			case "freq5":
				attr = "M";
				break;
			case "freq6":
				attr = "Q";
				break;
			case "freq7":
				attr = "Y";
				break;
			default:
				attr = "D";
				break;
		}
		return attr;
	}

	/**
	 *
	 * 功能描述：新版规则是否存在疾病诊断判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月3日 下午5:26:52</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static boolean existsDisease(List<MedicalRuleConditionSet> ruleConditionList) {
		Set<String> set = new HashSet<String>();
		set.add(AbsRuleParser.RULE_CONDI_DISEASE);
		set.add(AbsRuleParser.RULE_CONDI_DISEASEGRP);
		set.add(AbsRuleParser.RULE_CONDI_ACCESS_DISEASEGRP);
		for(MedicalRuleConditionSet record : ruleConditionList) {
			if(set.contains(record.getField())) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * 功能描述：规则是否存在疾病诊断判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月3日 上午10:18:38</p>
	 *
	 * @param ruleList
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static boolean ruleExistsDisease(List<MedicalDrugRule> ruleList) {
		for(MedicalDrugRule rule : ruleList) {
			if(StringUtils.isNotBlank(rule.getIndication())
					|| StringUtils.isNotBlank(rule.getUnIndication())) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * 功能描述：模型是否存在疾病诊断判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月3日 上午10:19:01</p>
	 *
	 * @param nodeList
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static boolean caseExistsDisease(List<EngineNode> nodeList) {
		Set<String> set = new HashSet<String>();
		set.add("DISEASENAME");
		set.add("DISEASECODE");
		set.add("DISEASECODEGROUP");
		set.add("DIAGGROUP_CODE");
		set.add("DISEASENAME_PRIMARY");
		for(EngineNode node : nodeList) {
			if(node.getWheres()!=null) {
				for(EngineNodeRuleGrp grp : node.getWheres()) {
					for(EngineNodeRule rule : grp.getRuleList()) {
						if(set.contains(rule.getColName())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		List<EngineNodeRule> list = new ArrayList<EngineNodeRule>();
		EngineNodeRule rule = new EngineNodeRule();
		rule.setTableName("DWB_CLIENT");
		rule.setColName("CLIENTID");
		rule.setCompareType("=");
		rule.setCompareValue("091228000011847");
		list.add(rule);
		rule = new EngineNodeRule();
		rule.setLogic("OR");
		rule.setTableName("DWB_CLIENT");
		rule.setColName("CLIENTID");
		rule.setCompareType("=");
		rule.setCompareValue("111216000009060");
		list.add(rule);
		EngineNodeRuleGrp grp = new EngineNodeRuleGrp();
		grp.setRuleList(list);
		String text = EngineUtil.parseConditionExpression(rule);
		System.out.println(text);
		text = EngineUtil.parseConditionExpression(grp);
		System.out.println(text);


		List<EngineNodeRuleGrp> wheres = new ArrayList<EngineNodeRuleGrp>();
		wheres.add(grp);
		List<EngineNodeRule> list2 = new ArrayList<EngineNodeRule>();
		rule = new EngineNodeRule();
		rule.setTableName("MEDICAL_MZ_ZY_MASTER_INFO");
		rule.setColName("VISITTYPE");
		rule.setCompareType("=");
		rule.setCompareValue("门诊");
		list2.add(rule);
		rule = new EngineNodeRule();
		rule.setLogic("and");
		rule.setTableName("MEDICAL_MZ_ZY_MASTER_INFO");
		rule.setColName("YEARAGE");
		rule.setCompareType(">=");
		rule.setCompareValue("55");
		list2.add(rule);
		grp = new EngineNodeRuleGrp();
		grp.setLogic("and");
		grp.setRuleList(list2);
		wheres.add(grp);
		EngineNode node = new EngineNode();
		node.setNodeType("rectangle");
		node.setWheres(wheres);
		text = EngineUtil.parseConditionExpression(node);
		System.out.println(text);
	}
}
