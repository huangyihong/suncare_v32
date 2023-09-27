package com.ai.modules.his.service;

import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 风控模型正式备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface IHisMedicalFormalCaseService extends IService<HisMedicalFormalCase> {

    JSONObject getFormalCaseById(String id, String batchId);

    List<HisMedicalFormalCase> queryByBusiId(String batchId, String busiId);

    HisMedicalFormalCase queryByCaseId(String batchId, String caseId);

    JSONObject getFormalCaseByVersion(String id, Float version);
}
