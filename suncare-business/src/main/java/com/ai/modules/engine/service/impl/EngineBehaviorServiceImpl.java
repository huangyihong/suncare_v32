/**
 * EngineDrugServiceImpl.java	  V1.0   2020年1月2日 上午11:07:02
 * <p>
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.service.IEngineBehaviorService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalBehavior;
import com.ai.modules.formal.entity.MedicalFormalCaseBehavior;
import com.ai.modules.formal.service.IMedicalFormalBehaviorService;
import com.ai.modules.formal.service.IMedicalFormalCaseBehaviorService;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.service.ITaskBatchStepItemService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class EngineBehaviorServiceImpl implements IEngineBehaviorService {
    @Value("${solr.importFolder:/home/web/data}")
    private String importFolder;

    @Autowired
    private IMedicalFormalBehaviorService medicalFormalBehaviorService;

    @Autowired
    private IMedicalFormalCaseBehaviorService medicalFormalCaseBehaviorService;

    @Autowired
    private ITaskBatchStepItemService taskBatchStepItemService;


    @Override
    public EngineResult generateUnreasonableBehaviorAll(String batchId) {
        List<MedicalFormalBehavior> behaviorList = medicalFormalBehaviorService.listByOrder(batchId);
        String type;
        if (behaviorList.size() == 0) {
            return new EngineResult(false, "没有可执行的不合规行为");
        } else {
            type = behaviorList.get(0).getActionType();
        }
        // 删除旧的进度
        taskBatchStepItemService.remove(new QueryWrapper<TaskBatchStepItem>().eq("BATCH_ID",batchId));
        //先删除历史数据
        try {
            deleteSolr(batchId);
        } catch (Exception e) {
            return new EngineResult(false, "删除历史数据失败：" + e.getMessage());
        }
        // 增加记录，执行最后一组
        MedicalFormalBehavior lastBean = new MedicalFormalBehavior();
        lastBean.setActionType(Integer.MAX_VALUE + "");
        behaviorList.add(lastBean);
        int i = 0, start = 0;
        for (MedicalFormalBehavior behavior : behaviorList) {
            if (!type.equals(behavior.getActionType())) {
                // 得到相同类型并且已排序完成的列表
                List<MedicalFormalBehavior> list = behaviorList.subList(start, i);
                String finalType = type;
                TaskBatchStepItem batchStepItem = new TaskBatchStepItem();
                batchStepItem.setId(IdUtils.uuid());
                batchStepItem.setBatchId(batchId);
                batchStepItem.setItemId(finalType);
                batchStepItem.setStep(3);
                batchStepItem.setStatus("running");
                batchStepItem.setCreateTime(new Date());
                taskBatchStepItemService.save(batchStepItem);
                try {
                    generateTotalData(list,finalType,batchId);
                    generateData(list, finalType, batchId);
                    batchStepItem.setStatus("normal");
                } catch (Exception e) {
                    e.printStackTrace();
                    batchStepItem.setMsg(e.getMessage());
                    batchStepItem.setStatus("abnormal");
                } finally {
                    batchStepItem.setUpdateTime(new Date());
                    taskBatchStepItemService.updateById(batchStepItem);
                }
                start = i;
                type = behavior.getActionType();
            }
            i++;
        }

        return EngineResult.ok();
    }

    public static Map<String, String[]> FIELDS_MAPPING;

    static {
        FIELDS_MAPPING = new HashMap<>();
        String[] patientFields = new String[]{"CLIENTID", "CLIENTNAME"};
        String[] doctorFields = new String[]{"DOCTORID", "DOCTORNAME"};
        String[] hospitalFields = new String[]{"ORGID", "ORGNAME"};
        FIELDS_MAPPING.put("2", patientFields);
        FIELDS_MAPPING.put("3", doctorFields);
        FIELDS_MAPPING.put("5", hospitalFields);
    }

    private void generateData(List<MedicalFormalBehavior> list, String type, String batchId) throws Exception {
        List<String> behaviorIdList = list.stream().map(MedicalFormalBehavior::getId).collect(Collectors.toList());
        int count = 1;
        for (String behaviorId : behaviorIdList) {
            List<String> caseIds = medicalFormalCaseBehaviorService.list(
                    new QueryWrapper<MedicalFormalCaseBehavior>().eq("BEHAVIOR_ID", behaviorId))
                    .stream().map(MedicalFormalCaseBehavior::getCaseId).collect(Collectors.toList());
            if(caseIds.size() == 0){
                continue;
            }
            String[] fq = new String[]{
                    "BATCH_ID:" + batchId,
                    "REVIEW_CASE_IDS:" + "(\"" + StringUtils.join(caseIds, "\",\"") + "\")",
                    // 黑名单状态
//                    "FIR_REVIEW_STATUS:blank",
                    "PUSH_STATUS:1",
                    // 去重
//                    "{!collapse field=VISITID}" CASE_ID现在为数组形式就诊记录不会重复
            };
            int pageSize = Integer.MAX_VALUE;
            String[] fields = FIELDS_MAPPING.get(type);

            JSONObject facetChild = new JSONObject();
            facetChild.put("pay", "sum(TOTALFEE)");
            facetChild.put("name", "max(" + fields[1] + ")");

            JSONObject jsonFacet = new JSONObject();
            jsonFacet.put("type", "terms");
            jsonFacet.put("field", fields[0]);
            jsonFacet.put("limit", pageSize);
            // 每个分片取的数据数量
            jsonFacet.put("overrequest", Integer.MAX_VALUE);
            jsonFacet.put("facet", facetChild);

            exec(batchId,type,behaviorId,count,fq,jsonFacet.toJSONString());
            count++;
        }


    }

    private void generateTotalData(List<MedicalFormalBehavior> list, String type, String batchId) throws Exception {
        List<String> behaviorIdList = list.stream().map(MedicalFormalBehavior::getId).collect(Collectors.toList());
        List<String> caseIds = medicalFormalCaseBehaviorService.list(
                new QueryWrapper<MedicalFormalCaseBehavior>().in("BEHAVIOR_ID", behaviorIdList))
                .stream().map(MedicalFormalCaseBehavior::getCaseId).collect(Collectors.toList());
        if(caseIds.size() == 0) {
            return;
        }
        String[] fq = new String[]{
                "BATCH_ID:" + batchId,
                "REVIEW_CASE_IDS:" + "(\"" + StringUtils.join(caseIds, "\",\"") + "\")",
                // 黑名单状态
//                "FIR_REVIEW_STATUS:blank",
                "PUSH_STATUS:1",
                // 去重 CASE_ID现在为数组形式就诊记录不会重复
//                "{!collapse field=VISITID}"
        };
        int pageSize = Integer.MAX_VALUE;
        String[] fields = FIELDS_MAPPING.get(type);

        JSONObject facetChild = new JSONObject();
        facetChild.put("pay", "sum(TOTALFEE)");
        facetChild.put("name", "max(" + fields[1] + ")");

        JSONObject jsonFacet = new JSONObject();
        jsonFacet.put("type", "terms");
        jsonFacet.put("field", fields[0]);
        jsonFacet.put("limit", pageSize);
        jsonFacet.put("facet", facetChild);

        exec(batchId,type,null, 0, fq,jsonFacet.toJSONString());
    }

    private void exec(String batchId,String type,String behaviorId, int orderNum,String[] fq, String jsonFacetStr) throws Exception {
        // 数据写入xml
        String importFilePath = importFolder + "/" + EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT + "/" + batchId + "_" + type +"_" + orderNum + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");

        SolrUtil.jsonFacet(EngineUtil.MEDICAL_UNREASONABLE_ACTION, fq,jsonFacetStr, jsonObj -> {
            JSONObject json = this.constructJson(jsonObj, batchId, type, behaviorId, orderNum);
            try {
                fileWriter.write(json.toJSONString());
                fileWriter.write(',');
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fileWriter.write("]");
        fileWriter.close();
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT);
    }

    private JSONObject constructJson( JSONObject itemJson, String batchId, String type, String behaviorId, int orderNum) {
        String targetId = itemJson.getString("val");
        String count = itemJson.getLong("count").toString();
        String pay = itemJson.getBigDecimal("pay").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        JSONObject json = new JSONObject();
        //写ID字段
        json.put("id",batchId + "_" + type + "_" + targetId);
        if(orderNum == 0){
            json.put("BATCH_ID", batchId);
            json.put("TARGET_ID", targetId);
            json.put("TARGET_NAME", itemJson.get("name"));
            json.put("TARGET_TYPE", type);
            // 算合计
            json.put("CASE_NUM", count);
            json.put("CASE_PAY", pay);
        } else {

            json.put("BA" + orderNum + "_ID", initSetJson(behaviorId));
            json.put("BA" + orderNum + "_CASE_NUM", initSetJson(count));
            json.put("BA" + orderNum + "_CASE_PAY", initSetJson(pay));
        }
        return json;
    }

    private JSONObject initSetJson(Object value){
        JSONObject json = new JSONObject();
        json.put("set",value);
        return json;

    }

    public void deleteSolr(String batchId, String type) throws Exception {
        SolrUtil.delete(EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT,
                "TARGET_TYPE:" + type + " AND BATCH_ID:" + batchId );

    }

    public void deleteSolr(String batchId) throws Exception {
        SolrUtil.delete(EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT,
                "BATCH_ID:" + batchId);
    }

}
