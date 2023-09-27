/**
 * SolrIndicationRuleHandle.java	  V1.0   2021年3月22日 下午3:12:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;

import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.model.vo.ChargeVO;
import com.ai.modules.engine.model.vo.DrugDurationComputeVO;
import com.ai.modules.engine.model.vo.DrugDurationVO;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：用药时限规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrDrugDurationRuleHandle extends SolrRuleHandle {
	
	public SolrDrugDurationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}
		String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		
		String itemcode = rule.getItemCodes();
		List<String> conditionList = new ArrayList<String>();        
        conditionList.add("ITEMCODE:" + itemcode);
        conditionList.addAll(this.parseCommonCondition(true));
		//基金支出金额>0
		conditionList.add("FUND_COVER:{0 TO *}");
		//自付比例<0
		conditionList.add("SELFPAY_PROP:[ 0 TO 1}");
		conditionList.add("PRESCRIPTTIME:?*");
		//添加准入与限定条件
		conditionList.addAll(this.parseCondition());
        //添加过滤掉指标为空值的条件
		RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
  		  		
  		int duration = this.getDuration();
  		Set<String> visitidSet = new HashSet<String>();
  		Map<String, DrugDurationComputeVO> computeMap = new HashMap<String, DrugDurationComputeVO>();
        SolrUtil.exportDocByPager(conditionList, EngineUtil.DWB_CHARGE_DETAIL, (doc, index) -> {
        	//处方日期PRESCRIPTTIME
			String prescripttime = doc.get("PRESCRIPTTIME").toString();
			prescripttime = DateUtils.dateformat(prescripttime, "yyyy-MM-dd");
			//就诊日期
			String visitdate = doc.get("VISITDATE").toString();
			visitdate = DateUtils.dateformat(visitdate, "yyyy-MM-dd");
			boolean out = false;//prescriptime>(visitdate+限定用药天数+1)			
			try {
				Date end = DateUtils.parseDate(prescripttime, "yyyy-MM-dd");
				Date start = DateUtils.parseDate(visitdate, "yyyy-MM-dd");
				Calendar end_calendar = Calendar.getInstance();
				end_calendar.setTime(end);
				Calendar start_calendar = Calendar.getInstance();
				start_calendar.setTime(start);
				int diff = DateUtils.dateDiff('d', end_calendar, start_calendar);
				out = diff>(duration+1);
			} catch(Exception e) {
				
			}
			if(out) {
				String visitId = doc.get("VISITID").toString();
				visitidSet.add(visitId);
				String code = doc.get("ITEMCODE").toString();
				ChargeVO vo = new ChargeVO();
				vo.setItemcode(code);
				vo.setItemname(doc.get("ITEMNAME").toString());
				if(doc.get("AMOUNT")!=null) {
					vo.setAmount(new BigDecimal(doc.get("AMOUNT").toString()));
				}
				if(doc.get("FEE")!=null) {
					vo.setFee(new BigDecimal(doc.get("FEE").toString()));
				}
				//基金支出金额
				if(doc.get("FUND_COVER")!=null) {
					vo.setFundConver(new BigDecimal(doc.get("FUND_COVER").toString()));
				}
				if(doc.get("ITEMNAME_SRC")!=null) {
					vo.setItemnameSrc(doc.get("ITEMNAME_SRC").toString());
				}
				vo.setDay(prescripttime);
				if(computeMap.containsKey(visitId)) {
					DrugDurationComputeVO compute = computeMap.get(visitId);
					compute.add(vo);
				} else {
					DrugDurationComputeVO compute = new DrugDurationComputeVO(visitId, code, doc);
					compute.add(vo);
					computeMap.put(visitId, compute);
				}
			}
        });
        if(computeMap.size()>0) {
        	String desc = null;
      		if(StringUtils.isNotBlank(rule.getActionType())) {        	
    			desc = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
            }
      		String actionTypeName = desc; //不合规行为类型名称
      		//不合规行为字典映射
      		Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
        	// 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/" + drugCollection + "/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            //写文件头
            fileWriter.write("[");
            int pageSize = 1000;
    		int pageNum = (visitidSet.size() + pageSize - 1) / pageSize;
    		//数据分割
    		List<Set<String>> mglist = new ArrayList<Set<String>>();
    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
    	    	mglist.add(visitidSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
    	    });    	    
    	    for(Set<String> subList : mglist) {
    	    	conditionList.clear();
        	    conditionList.add("ITEMCODE:" + itemcode);
    	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
    	    	conditionList.add(visitidFq);
    	    	SolrUtil.exportDocByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, (doc, index) -> {    	            
    	    		String visitid = doc.get("VISITID").toString();
    	    		DrugDurationComputeVO computeVO = computeMap.get(visitid);
                	DrugDurationVO record = computeVO.compute();
                	if(record.getAmount().compareTo(BigDecimal.ZERO)>0
                			&& record.getFundConver().compareTo(BigDecimal.ZERO)>0) {
                		//超用药时限并且基金支出金额>0
                		JSONObject json = this.parseJSONObject(doc, actionTypeName, actionDictMap);                		
                		if(record.getItemnameSrcSet()!=null && record.getItemnameSrcSet().size()>0) {
                			//原始项目名称
                			json.put("ITEMNAME_SRC", StringUtils.join(record.getItemnameSrcSet(), ","));
                		}
                		//基金支出金额
                        json.put("MIN_ACTION_MONEY", record.getFundConver());
                        json.put("MAX_ACTION_MONEY", record.getFundConver());
                        //收费项目费用
                        json.put("MIN_MONEY", record.getFee());
                        json.put("MAX_MONEY", record.getFee());
                        //超出数量
                        json.put("AI_OUT_CNT", record.getAmount());
                        String content = "超出用药时限发生日期：%s";
                    	content = String.format(content, StringUtils.join(record.getDays(), ","));
                    	json.put("BREAK_RULE_CONTENT", content);
        	    		try {
                            fileWriter.write(json.toJSONString());
                            fileWriter.write(',');
                        } catch (Exception e) {

                        }
                	}    	    		
    	        });
    	    }
            // 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            fileWriter.close();

            //导入solr
            SolrUtil.importJsonToSolr(importFilePath, drugCollection);
            String busiType = this.getBusiType();
            //同步数据
            syncUnreasonableAction(rule, busiType, false);
            //不合规行为汇总            
            String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
            engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
        }
	}
	
	private Integer getDuration() {
		//限定条件
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	for(MedicalRuleConditionSet bean : judgeList) {
			return Integer.parseInt(bean.getExt1());
		}
    	return null;
	}

	/**
	 * 
	 * 功能描述：不合规查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月9日 下午5:49:29</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseCondition() {
		List<String> result = new ArrayList<String>();
	    if(ruleConditionList!=null) {
	    	//准入条件
	    	String condition = this.parseAccessCondition();
	    	if(StringUtils.isNotBlank(condition)) {
	    		result.add(condition);
	    	}
	    }	            
	    return result;
	}	
}
