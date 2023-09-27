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
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.PayDurationComputeVO;
import com.ai.modules.engine.model.vo.PayDurationVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：医保药品超过最大持续使用时间规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrPayDurationRuleHandle extends SolrRuleHandle {
	
	public SolrPayDurationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}
		String itemcode = rule.getItemCodes();
		List<String> conditionList = new ArrayList<String>();        
        conditionList.add("ITEMCODE:" + itemcode);
        conditionList.addAll(this.parseCommonCondition(true));
		//基金支出金额>0
		conditionList.add("FUND_COVER:{0 TO *}");
		//自付比例<0
		conditionList.add("SELFPAY_PROP:[ 0 TO 1}");
		if("1".equals(batch.getYbFundRm0())) {
			//过滤掉病例基金支出金额为0的数据
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
			conditionList.add(plugin.parse()+"FUNDPAY:{0 TO *}");
		}
		conditionList.add("PRESCRIPTTIME:?*");
		//添加准入与限定条件
		conditionList.addAll(this.parseCondition());
        //添加过滤掉指标为空值的条件
		RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
  		
  		MedicalRuleConditionSet conditionRule = this.getPayDuration();
  		int limit = Integer.parseInt(conditionRule.getExt1());
  		String timeUnit = conditionRule.getExt2();
  		Set<String> visitidSet = new HashSet<String>();
  		Map<String, PayDurationComputeVO> computeMap = new HashMap<String, PayDurationComputeVO>();
        SolrUtil.exportDocByPager(conditionList, EngineUtil.DWB_CHARGE_DETAIL, (doc, index) -> {
        	//处方日期PRESCRIPTTIME
			Object value = doc.get("PRESCRIPTTIME");
			String prescripttime = value.toString();
			String day = DateUtils.dateformat(prescripttime, "yyyy-MM-dd");
			String visitId = doc.get("VISITID").toString();
			String code = doc.get("ITEMCODE").toString();
			ChargedetailVO vo = new ChargedetailVO();
			vo.setPrescripttime(prescripttime);
			vo.setDay(day);
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
			visitidSet.add(visitId);
			if(computeMap.containsKey(visitId)) {
				PayDurationComputeVO compute = computeMap.get(visitId);
				compute.add(vo);
			} else {
				PayDurationComputeVO compute = new PayDurationComputeVO(visitId, code, doc, limit, timeUnit);
				compute.add(vo);
				computeMap.put(visitId, compute);
			}
        });
        if(computeMap.size()>0) {
        	String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;    		
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
    	    		PayDurationComputeVO computeVO = computeMap.get(visitid);
    	    		PayDurationVO out = computeVO.compute();
                	if(out!=null && out.getAmount().compareTo(BigDecimal.ZERO)>0
                			&& out.getFundConver().compareTo(BigDecimal.ZERO)>0) {
                		//超用药支付时长并且基金支出金额>0
                		JSONObject json = this.parseJSONObject(doc, actionTypeName, actionDictMap);
                		if(out.getItemnameSrcSet()!=null && out.getItemnameSrcSet().size()>0) {
                			//原始项目名称
                			json.put("ITEMNAME_SRC", StringUtils.join(out.getItemnameSrcSet(), ","));
                		}
                		//基金支出金额
                        json.put("MIN_ACTION_MONEY", out.getFundConver());
                        json.put("MAX_ACTION_MONEY", out.getFundConver());
                        //收费项目费用
                        json.put("MIN_MONEY", out.getFee());
                        json.put("MAX_MONEY", out.getFee());
                        //超出数量
                        json.put("AI_OUT_CNT", out.getAmount());
                        //用药天数
                        json.put("AI_ITEM_CNT", out.getPayDay());
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
	
	private MedicalRuleConditionSet getPayDuration() {
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	for(MedicalRuleConditionSet bean : judgeList) {
			if(AbsRuleParser.RULE_CONDI_PAYDURATION.equals(bean.getField())) {
				return bean;
			}
		}
    	return null;
	}
	
	@Override
	protected String parseJudgeCondition() {
		MedicalRuleConditionSet conditionRule = this.getPayDuration();
		if(conditionRule==null || StringUtils.isBlank(conditionRule.getExt1())) {
			throw new RuntimeException(rule.getItemNames()+"规则未配置限制支出时长");
		}
		//黑名单ITEM_QTY>支付时长
  		int limit = Integer.parseInt(conditionRule.getExt1());
  		String timeUnit = conditionRule.getExt2();
  		if("day".equals(timeUnit)) {
  			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, "VISITID", "VISITID");
  			String where = "_query_:\"%sITEMCODE:%s AND ITEM_QTY:{%s TO *}\"";
  			where = String.format(where, plugin.parse(), rule.getItemCodes(), limit);
  			return where;
  		} else {
  			String where = "VISITID:0";
  			return where;
  		}
	}	
}
