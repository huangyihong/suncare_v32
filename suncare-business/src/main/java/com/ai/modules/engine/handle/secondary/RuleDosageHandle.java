/**
 * SecondLineDrugHandle.java	  V1.0   2020年12月4日 上午10:05:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.DosageComputeVO;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：医保限定用药量规则二次处理
 *
 * @author  zhangly
 * Date: 2021年4月14日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleDosageHandle extends AbsRuleSecondHandle {

	public RuleDosageHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList, Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {
  		String batchId = batch.getBatchId();
  		int temp = 0;
  		String valueunit = ""; //计价单位
    	for(MedicalRuleConditionSet bean : ruleConditionList) {
			if(AbsRuleParser.RULE_CONDI_DOSAGE.equals(bean.getField())) {
				temp = Integer.parseInt(bean.getExt1());
				valueunit = bean.getExt2();
				break;
			}
		}
    	int limit = temp;
    	ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalOtherDictService dictSV = context.getBean(IMedicalOtherDictService.class);
		valueunit = dictSV.getValueByCode("dosage_unit", valueunit);
  		String[] array = StringUtils.split(rule.getItemCodes(), ",");
  		for(String itemCode : array) {
  			List<String> conditionList = new ArrayList<String>();
  	    	conditionList.add("ITEMCODE:"+itemCode);
  	    	conditionList.add("RULE_ID:"+rule.getRuleId());
  	    	conditionList.add("BATCH_ID:"+batchId);
  	    	List<String> visitidList = new ArrayList<String>();
  	    	//一个病例的明细map
  	    	Map<String, DosageComputeVO> computeMap = new HashMap<String, DosageComputeVO>();
  	    	String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
  	    	boolean slave = false;
  	    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
  	    		String visitid = doc.get("VISITID").toString();
  	    		visitidList.add(visitid);
  	    		String itemcode = doc.get("ITEMCODE").toString();
  	    		if(!computeMap.containsKey(visitid)) {
  	    			DosageComputeVO vo = new DosageComputeVO(visitid, itemcode, doc, limit);
  	    			computeMap.put(visitid, vo);
  	    		}
  	        });
  	    	if(count>0) {
  	    		int pageSize = 500;
  	    		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
  	    		//数据分割
  	    		List<List<String>> mglist = new ArrayList<>();
  	    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
  	    	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
  	    	    });
  	    	    
  	    	    conditionList.clear();
  	    	    conditionList.add("PRESCRIPTTIME:?*");
  	    	    conditionList.add("ITEMCODE:"+itemCode);
    	    	conditionList.add("CHARGEUNIT:"+valueunit);
    	    	//基金支出金额>0
    			conditionList.add("FUND_COVER:{0 TO *}");
    			//自付比例<0
    			conditionList.add("SELFPAY_PROP:[0 TO 1}");
  	    	    for(List<String> subList : mglist) {
  	    	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
  	    	    	SolrQuery solrQuery = new SolrQuery("*:*");
  	    			// 设定查询字段
  	    			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
  	    			solrQuery.addFilterQuery(visitidFq);
  	    			solrQuery.setStart(0);
  	    			solrQuery.setRows(EngineUtil.MAX_ROW);
  	    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
  	    			SolrUtil.export(solrQuery, EngineUtil.DWB_CHARGE_DETAIL, slave, (map, index) -> {
  	    				//处方日期PRESCRIPTTIME
  	    				Object value = map.get("PRESCRIPTTIME");
  	    				String prescripttime = value.toString();
  	    				String day = DateUtils.dateformat(prescripttime, "yyyy-MM-dd");
  	    				String visitId = map.get("VISITID").toString();
  	    				String code = map.get("ITEMCODE").toString();
  	    				ChargedetailVO vo = new ChargedetailVO();
  	    				vo.setPrescripttime(prescripttime);
  	    				vo.setDay(day);
  	    				vo.setItemcode(code);
  	    				vo.setItemname(map.get("ITEMNAME").toString());
  	    				if(map.get("AMOUNT")!=null) {
  	    					vo.setAmount(new BigDecimal(map.get("AMOUNT").toString()));
  	    				}
  	    				if(map.get("FEE")!=null) {
  	    					vo.setFee(new BigDecimal(map.get("FEE").toString()));
  	    				}
  	    				//基金支出金额
  	    				if(map.get("FUND_COVER")!=null) {
  	    					vo.setFundConver(new BigDecimal(map.get("FUND_COVER").toString()));
  	    				}
  	    				DosageComputeVO compute = computeMap.get(visitId);
  	    				compute.add(vo);
  	    	        });
  	    	    }
  	    	    
  	    	    int index = 0;
	    	    //超用量
	    	    List<DosageComputeVO> dosageList = new ArrayList<DosageComputeVO>();    	    
	    	    //非超用量
	    	    List<String> notDosageList = new ArrayList<String>();
	    	    StringBuilder sb = new StringBuilder();
	    	    for(Map.Entry<String, DosageComputeVO> entry : computeMap.entrySet()) {
	    	    	DosageComputeVO computeVO = entry.getValue();
	    	    	computeVO.compute();
	    	    	if(computeVO.getDosage()!=null 
	    	    			&& computeVO.getDosage().getAmount().compareTo(BigDecimal.ZERO)>0) {
	    	    		dosageList.add(computeVO);
	    	    	} else {
	    	    		notDosageList.add(computeVO.getVisitid());
	    	    	}
	    	    	index++;
	    	    	if(index==pageSize) {
	    	    		if(notDosageList.size()>0) {	    	    			
	    	    			sb.setLength(0);
	    				    sb.append("BATCH_ID:").append(batchId);
	    				    sb.append(" AND ITEMCODE:").append(itemCode);
	    				    sb.append(" AND VISITID:(\"");
	    			    	sb.append(StringUtils.join(notDosageList, "\",\""));
	    			    	sb.append("\")");
	    			    	SolrUtil.delete(collection, sb.toString(), slave);
	    			    	notDosageList.clear();
	    	    		}
				    	index = 0;
	    	    	}
	    	    }
	    	    if(notDosageList.size()>0) {
	    	    	sb.setLength(0);
				    sb.append("BATCH_ID:").append(batchId);
				    sb.append(" AND ITEMCODE:").append(itemCode);
				    sb.append(" AND VISITID:(\"");
			    	sb.append(StringUtils.join(notDosageList, "\",\""));
			    	sb.append("\")");
			    	SolrUtil.delete(collection, sb.toString(), slave);
			    	notDosageList.clear();
	    	    }
	    	        	    
	    	    if(dosageList.size()>0) {
	    	    	//数据写入文件
	    	    	String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + ".json";
	                BufferedWriter fileWriter = new BufferedWriter(
	                        new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	        		
	                //写文件头
	                fileWriter.write("[");
	    	    	for(DosageComputeVO computeVO : dosageList) {
	    	    		JSONObject json = new JSONObject();
	    	    		json.put("id", computeVO.getDocument().get("id").toString());
	    	    		//医保基金支付金额
	    	    		json.put("MIN_ACTION_MONEY", SolrUtil.initActionValue(computeVO.getDosage().getFundConver(), "set"));
	    	    		json.put("MAX_ACTION_MONEY", SolrUtil.initActionValue(computeVO.getDosage().getFundConver(), "set"));
	    	    		//违规金额
	    	    		json.put("MIN_MONEY", SolrUtil.initActionValue(computeVO.getDosage().getFee(), "set"));
	    	    		json.put("MAX_MONEY", SolrUtil.initActionValue(computeVO.getDosage().getFee(), "set"));
	    	    		//超出数量
	    				json.put("AI_OUT_CNT", SolrUtil.initActionValue(computeVO.getDosage().getAmount(), "set"));
	    				try {
	    		            fileWriter.write(json.toJSONString());
	    		            fileWriter.write(',');
	    		        } catch (IOException e) {
	    		        }
	    	    	}
	    	    	// 文件尾
	                fileWriter.write("]");
	                fileWriter.flush();
	                fileWriter.close();
	                //导入solr
	                SolrUtil.importJsonToSolr(importFilePath, collection, slave);
	    	    }
  	    	}
  		}
	}
}
