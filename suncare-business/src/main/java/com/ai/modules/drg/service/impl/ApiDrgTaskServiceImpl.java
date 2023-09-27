package com.ai.modules.drg.service.impl;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.service.IApiDrgTaskService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : zhangly
 * @date : 2023/4/6 11:13
 */
//@Service
public class ApiDrgTaskServiceImpl implements IApiDrgTaskService {

    @Override
    public DrgCatalog findDrgCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/drgCatalogByVersion", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findMdcCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.MDC_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findAdrgCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.ADRG_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findAdrgListCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.ADRG_LIST_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findMccCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.MCC_INFO_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findCcCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.CC_INFO_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findMdcDiagCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.MDC_INFO_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findSurgeryCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.SURGERY_INFO_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgCatalog findExcludeCatalog(String version) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("catalogType", DrgCatalogConstants.EXCLUDE_INFO_V);
        busiParams.put("version", version);
        DrgCatalog bean = ApiOauthUtil.response("/oauth/api/drg/catalog", busiParams, "post", DrgCatalog.class);
        return bean;
    }

    @Override
    public DrgTask findDrgTask(String id) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("id", id);
        DrgTask bean = ApiOauthUtil.response("/oauth/api/drg/task/get", busiParams, "post", DrgTask.class);
        return bean;
    }

    @Override
    public DrgTask findDrgTaskByBatch(String batchId) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("batchId", batchId);
        DrgTask bean = ApiOauthUtil.response("/oauth/api/drg/task/getByBatch", busiParams, "post", DrgTask.class);
        return bean;
    }

    @Override
    public void updateDrgTask(String batchId, DrgTask up) {
        Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("batchId", batchId);
        busiParams.put("dataJson", JSON.toJSONString(up));
        ApiOauthUtil.postSuccess("/oauth/api/drg/task/update", busiParams);
    }
}
