package com.ai.modules.review.service;

import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.task.entity.TaskProject;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;


public interface IReviewStatisticNewGpService {
    Map<String,Object> module0Data(QueryWrapper<MedicalUnreasonableAction> queryWrapper) throws Exception;
    Map<String,Object> module0MasterInfoData(TaskProject project) throws Exception;

    Map<String,Object> module0ChargeDetailData(TaskProject project) throws Exception;

    List<JSONObject> module1Data(List<String> batchIdList, String actionId) throws Exception;

    public void export(List<JSONObject> list, String[] titles, String[] fields, String title, OutputStream os) throws Exception;

    public List<JSONObject> module2Data(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String groupBy) throws Exception;

    public List<JSONObject> module2ExportData(List<String> batchIdList, String action_id, String fir_review_status) throws Exception;

    public List<JSONObject> module4Data(List<String> batchIdList) throws Exception;

    public List<JSONObject> module5Data(List<String> batchIdList, String secReviewStatus) throws Exception;

    public List<JSONObject> module5ExportData(List<String> batchIdList) throws Exception;

    public List<JSONObject> module6Data(List<String> batchIdList, String groupBy) throws Exception;

    public List<JSONObject> module6ExportData(List<String> batchIdList, String groupBy) throws Exception;



}
