package com.ai.modules.drg.service.impl;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.service.IApiDrgTaskService;
import com.ai.modules.drg.service.IDrgCatalogService;
import com.ai.modules.drg.service.IDrgTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : zhangly
 * @date : 2023/4/6 11:13
 */
@Service
public class DbDrgTaskServiceImpl implements IApiDrgTaskService {

    @Autowired
    private IDrgCatalogService drgCatalogService;
    @Autowired
    private IDrgTaskService drgTaskService;

    @Autowired
    private WebSocket webSocket;

    @Override
    public DrgCatalog findDrgCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findDrgCatalog(version);
        return bean;
    }

    @Override
    public DrgCatalog findMdcCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.MDC_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findAdrgCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.ADRG_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findAdrgListCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.ADRG_LIST_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findMccCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.MCC_INFO_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findCcCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.CC_INFO_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findMdcDiagCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.MDC_INFO_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findSurgeryCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.SURGERY_INFO_V, version);
        return bean;
    }

    @Override
    public DrgCatalog findExcludeCatalog(String version) {
        DrgCatalog bean = drgCatalogService.findCatalog(DrgCatalogConstants.EXCLUDE_INFO_V, version);
        return bean;
    }

    @Override
    public DrgTask findDrgTask(String id) {
        DrgTask bean = drgTaskService.getById(id);
        return bean;
    }

    @Override
    public DrgTask findDrgTaskByBatch(String batchId) {
        DrgTask bean = drgTaskService.getOne(new QueryWrapper<DrgTask>().eq("batch_id", batchId));
        return bean;
    }

    @Override
    public void updateDrgTask(String batchId, DrgTask up) {
        QueryWrapper<DrgTask> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_id", batchId);
        drgTaskService.update(up, wrapper);
        //websocket通知状态改变
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cmd", "drg");
        jsonObject.put("batchId", batchId);
        webSocket.sendAllMessage(JSON.toJSONString(jsonObject));
    }
}
