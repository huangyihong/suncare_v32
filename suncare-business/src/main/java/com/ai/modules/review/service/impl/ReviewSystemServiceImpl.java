/**
 * EngineServiceImpl.java	  V1.0   2019年11月29日 上午11:06:14
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.review.service.impl;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IReviewSystemService;
import com.ai.modules.review.vo.ReviewSystemDrugViewVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewSystemServiceImpl implements IReviewSystemService {


	@Override
	public void operateReviewCaseId(String[] ids, String caseId, String action) throws Exception {
		SolrClient solr = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);

		for(String id: ids){
			if(StringUtils.isBlank(id)){
				continue;
			}
			SolrInputDocument document = new SolrInputDocument();
			document.setField("id", id);
			document.setField("REVIEW_CASE_IDS",SolrUtil.initActionValue(caseId,action));
			solr.add(document);
		}
		solr.commit();
		solr.close();
	}

	@Override
	public void exportDrugList(SolrQuery solrQuery, String collection, Map<String, String> fieldMap, String title,String ruleType, OutputStream os) throws Exception {

		solrQuery.setRows(1000000);
		List<ReviewSystemDrugViewVo> list = SolrQueryGenerator.list(collection, solrQuery, ReviewSystemDrugViewVo.class
				, fieldMap);

		NumberFormat nbf = NumberFormat.getInstance();
		nbf.setMinimumFractionDigits(2);

		if (list.size() > 0) {

			for(int i = 0,j = 0, len = list.size(); j  < len; i+=500){
				j = i + 500;
				if(j > len){
					j = len;
				}

				Map<String, ReviewSystemDrugViewVo> map = new HashMap<>();
				for (ReviewSystemDrugViewVo bean : list.subList(i, j)) {
					/*if(bean.getRuleFdesc() != null){
						List<String> descs = bean.getRuleFdesc().stream().map(desc -> desc.substring(desc.indexOf("::") + 2)).collect(Collectors.toList());
						for(int i1 = 0,len1 = descs.size(); i1 < len1; i1++){
							descs.set(i1,(i1 + 1) + "." + descs.get(i1));
						}
						bean.setRuleDesc(StringUtils.join(descs,"\n"));
					}*/
					/*if(bean.getRuleScope() != null){
						bean.setRuleScopes(StringUtils.join(bean.getRuleScope(),","));
					}
					bean.setItemAmt((Math.round(bean.getItemAmt() * 100)) / 100.0);
					map.put(bean.getVisitid(), bean);*/
				}
				String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

				SolrQuery solrQuery1 = new SolrQuery("*:*");
				solrQuery1.addFilterQuery(visitIdFq);
				solrQuery1.setFields("VISITID","SEX", "YEARAGE","MONTHAGE","DAYAGE", "DISEASENAME", "DISEASECODE","ZY_DAYS_CALCULATE");
				SolrDocumentList masterList = SolrUtil.call(solrQuery1, EngineUtil.DWB_MASTER_INFO).getResults();
				for (SolrDocument doc : masterList) {
					ReviewSystemDrugViewVo bean = map.get(doc.getFieldValue("VISITID").toString());
					Object sexObj = doc.getFieldValue("SEX");
					Object yearageObj = doc.getFieldValue("YEARAGE");
					Object monthageObj = doc.getFieldValue("MONTHAGE");
					Object dayageObj = doc.getFieldValue("DAYAGE");
					Object diseasenameObj = doc.getFieldValue("DISEASENAME");
					Object diseasecodeObj = doc.getFieldValue("DISEASECODE");
					Object zyDaysCalculate = doc.getFieldValue("ZY_DAYS_CALCULATE");
					if (sexObj != null) {
						bean.setSex(sexObj.toString());
					}
					if (yearageObj != null) {
						bean.setYearage((Double) yearageObj);
					}
					if (monthageObj != null) {
						bean.setMonthage((Double) monthageObj);
					}
					if (dayageObj != null) {
						bean.setDayage((Double) dayageObj);
					}
					if (diseasenameObj != null) {
						if(diseasecodeObj != null){
							bean.setDiseasename(diseasenameObj + "(" + diseasecodeObj +")");
						} else {
							bean.setDiseasename(diseasenameObj.toString());
						}
					}
					if (zyDaysCalculate != null) {
						bean.setZyDaysCalculate((Double)zyDaysCalculate);
					}

				}
			}

		}

		String[] titles = new String[]{"就诊ID", "医疗机构名称", "病人姓名", "性别", "年龄(岁)","年龄(月)","年龄(天)", "就诊类型", "就诊日期", "住院天数", "诊断疾病名称", "项目编码", "项目名称", "出现次数", "涉及金额", "提示信息", "违反限定范围"};
		String[] fields = new String[]{"visitid", "orgname", "clientname", "sex", "yearage","monthage", "dayage", "visittype", "visitdate","zyDaysCalculate", "diseasename", "itemcode", "itemname", "itemQty", "itemAmt", "ruleDesc", "ruleScopes"};

		// 创建文件输出流
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		// 生成一个表格
		ExportXUtils.exportExl(list, ReviewSystemDrugViewVo.class, titles, fields, workbook,title);

		workbook.write(os);
		workbook.dispose();
	}


}
