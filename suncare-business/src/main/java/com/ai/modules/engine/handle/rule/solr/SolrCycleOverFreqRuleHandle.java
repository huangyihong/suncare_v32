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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;

import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.fee.FeeResult;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.RuleOverFrequencyFromDetail;
import com.ai.modules.engine.handle.sync.SyncUtil;
import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：周期类限频次规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrCycleOverFreqRuleHandle extends SolrRuleHandle {
	
	public SolrCycleOverFreqRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
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
		if(this.isProjectGrp()) {
			//项目组
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
			conditionList.add(plugin.parse() + "TREATGROUP_CODE:" + itemcode);
		} else {
			conditionList.add("ITEMCODE:" + itemcode);
		}
        conditionList.add("ITEM_QTY:{0 TO *}");
		conditionList.add("ITEM_AMT:{0 TO *}");
		conditionList.add("PRESCRIPTTIME_DAY:?*");
		conditionList.addAll(this.parseCommonCondition());
		//添加准入与限定条件
		conditionList.addAll(this.parseCondition());
        //添加过滤掉指标为空值的条件
		RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
  		
  		String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		String desc = null;
  		if(StringUtils.isNotBlank(rule.getActionType())) {        	
			desc = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
        }
  		BigDecimal frequency = this.getFrequency();
  		String actionTypeName = desc; //不合规行为类型名称
  		//不合规行为字典映射  		
        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
        // 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + drugCollection + "/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        String collection = "DWS_PATIENT_CHARGEITEM_SUM";
        int count = SolrUtil.exportDocByPager(conditionList, collection, (doc, index) -> {
            // 循环每条数据写入文件
        	writerJson(fileWriter, doc, actionTypeName, actionDictMap, frequency);
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, drugCollection);
  		if(count>0) {
  			//规则二次处理
        	AbsRuleSecondHandle handle = new RuleOverFrequencyFromDetail(task, batch, rule, ruleConditionList, trail);        	
        	handle.execute();
        	String busiType = this.getBusiType();
            //同步数据
            syncUnreasonableAction(rule, busiType, false);
            //不合规行为汇总
            String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
            engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
        }        
	}
	
	protected JSONObject writerJson(BufferedWriter fileWriter, SolrDocument document, 
			String actionTypeName, Map<String, MedicalActionDict> actionDictMap, BigDecimal frequency) {		
		JSONObject json = this.parseJSONObject(document, actionTypeName, actionDictMap);
		//计算超出部分
		FeeResult feeResult = this.over(document, frequency);
		if(feeResult!=null) {
			String content = "超频次/数量发生时间：%s";
	    	content = String.format(content, feeResult.getDuration());
			json.put("ARRAY_ACTION_MONEY", feeResult.getActionMoney());
			json.put("ARRAY_MONEY", feeResult.getMoney());
			json.put("MIN_MONEY", feeResult.getMoney());
			json.put("MAX_MONEY", feeResult.getMoney());
			json.put("MIN_ACTION_MONEY", feeResult.getFundMoney());
			json.put("MAX_ACTION_MONEY", feeResult.getFundMoney());
			json.put("AI_ITEM_CNT", feeResult.getCnt());
			json.put("AI_OUT_CNT", feeResult.getOutCnt());
			json.put("BREAK_RULE_TIME", feeResult.getDuration());
			json.put("BREAK_RULE_CONTENT", content);
		}
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }
	
	/**
	 * 
	 * 功能描述：解析判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 下午3:13:34</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	protected String parseJudgeCondition() {		
		//限定条件
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet bean : judgeList) {
    			String where = this.parseCondition(bean);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		String condition = StringUtils.join(wheres, " OR ");
        	//黑名单取反
    		condition = "*:* -(" + condition + ")";
    		return condition;
    	}    	
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		String condiType = bean.getField();
		String frequency = bean.getExt2();
		if(StringUtils.isBlank(frequency)) {
			return null;
		}
		if(AbsRuleParser.RULE_CONDI_FREQUENCY.equals(condiType)) {
			String compare = bean.getCompare();			
			StringBuilder sb = new StringBuilder();
			sb.append("ITEM_QTY:");
			if(">".equals(compare)) {
				sb.append("{").append(frequency).append(" TO *}");
			} else if(">=".equals(compare)) {
				sb.append("[").append(frequency).append(" TO *}");
			} else if("<".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("}");
			} else if("<=".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("]");
			} else {
				sb.append(frequency);
			}
			return sb.toString();
		}
        return null;
	}
	
	private BigDecimal getFrequency() {
		//限制频次
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
		MedicalRuleConditionSet ruleCondition = judgeList.get(0);
		BigDecimal frequency = new BigDecimal(ruleCondition.getExt2());
		String compare = ruleCondition.getCompare();
		if("<".equals(compare)) {
			frequency = frequency.subtract(BigDecimal.ONE);
		}
		return frequency;
	}
	
	/**
	 * 
	 * 功能描述：计算超出部分的违规金额、数量
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月17日 下午4:47:13</p>
	 *
	 * @param doc
	 * @param frequency
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private FeeResult over(SolrDocument doc, BigDecimal frequency) {
		FeeResult feeResult = new FeeResult();
		//使用平均值方式计算违规金额
		BigDecimal fee = new BigDecimal(doc.get("ITEM_AMT").toString());
		BigDecimal amount = new BigDecimal(doc.get("ITEM_QTY").toString());
		fee = fee.divide(amount,4, BigDecimal.ROUND_HALF_UP);
		fee = fee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		fee = fee.multiply(amount.subtract(frequency));
		BigDecimal ratio = new BigDecimal(doc.get("SELFPAY_PROP_MIN").toString());
		ratio = BigDecimal.ONE.subtract(ratio);
		BigDecimal actionFee = fee.multiply(ratio);
		actionFee = actionFee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		feeResult.setMoney(fee);
		feeResult.setActionMoney(actionFee);
		feeResult.setCnt(amount);
		feeResult.setOutCnt(amount.subtract(frequency));
		feeResult.setDuration(doc.get("DURATION").toString());
		//使用平均值方式计算违规基金支出金额
		BigDecimal fundcover = new BigDecimal(doc.get("FUND_COVER").toString());
        fundcover = fundcover.divide(amount,4, BigDecimal.ROUND_HALF_UP);
        fundcover = fundcover.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        fundcover = fundcover.multiply(amount.subtract(frequency));
        feeResult.setFundMoney(fundcover);
		return feeResult;
	}
}
