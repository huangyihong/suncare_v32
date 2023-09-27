package com.ai.modules.task.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.review.vo.DwbMasterInfoVo;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskActionFieldConfigService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 不同不合规行为显示字段配置
 * @Author: jeecg-boot
 * @Date: 2020-10-12
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "项目批次数据预览")
@RestController
@RequestMapping("/apiTask/taskDataPreview")
public class ApiTaskDataPreviewController extends JeecgController<TaskActionFieldConfig, ITaskActionFieldConfigService> {


    @AutoLog(value = "项目批次数据预览-按照医院统计")
    @ApiOperation(value = "项目批次数据预览-按照医院统计", notes = "项目批次数据预览-按照医院统计")
    @GetMapping(value = "/hospitalStatistic")
    public Result<?> hospitalStatistic(DwbMasterInfoVo searchObj,
                                       String fundpayGt0,
                                       String diseaseMappingFilter,
                                       String diseasecodeCustom,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) throws Exception {
        Page<Object> page = new Page<>(pageNo, pageSize);
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        String[] dwbMasterFqs = solrQuery.getFilterQueries();
        // 排除条件
        List<String> neFields = new ArrayList<>(Arrays.asList("VISITTYPE_ID", "PAYWAY_ID", "FUN_SETTLEWAY_ID"));
        if(dwbMasterFqs != null){
            for(String fq: dwbMasterFqs){
                if(neFields.stream().anyMatch(fq::startsWith)){
                    solrQuery.removeFilterQuery(fq);
                    solrQuery.addFilterQuery("*:* -" + fq);
                }
            }
        }


        if("1".equals(fundpayGt0)){
            solrQuery.addFilterQuery("FUNDPAY:{0 TO *]");
        }
        if(StringUtils.isNotBlank(diseasecodeCustom)){
            List<String> templs = Arrays.asList("*,%s", "%s,*", "*,%s,*");
            String diseasecodeFq =  Arrays.stream(diseasecodeCustom.split("\\|"))
                    .map(code -> templs.stream().map(r -> String.format(r, code)).toArray(String[]::new))
                    .flatMap(Arrays::stream)
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("*:* -DISEASECODE:(" + diseasecodeFq + ")");

        }
        dwbMasterFqs = solrQuery.getFilterQueries();
        if("1".equals(diseaseMappingFilter)){
            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWS_1VISIT_TAG, "id", "VISITID");
            solrQuery.addFilterQuery("*:* -" + plugin.parse() + "DISEASE_MISS_FLAG:1");
        }
        // 统计字段
        JSONObject facetJson =  new JSONObject()
                .fluentPut("sumTotalfee", "sum(TOTALFEE)")
                .fluentPut("sumFundpay", "sum(FUNDPAY)")
                .fluentPut("visitidCount", "unique(VISITID)");
        // 以医院名称分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", "ORGNAME");
        json1.put("numBuckets", true);
//        json1.put("offset", (pageNo - 1) * pageSize);
//        json1.put("limit", pageSize);
        // 每个分片取的数据数量
                json1.put("limit", Integer.MAX_VALUE);

        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", facetJson);
        JSONObject json = new JSONObject();
        json.put("body", json1);
        json.putAll(facetJson);
        // facet查询
        JSONObject totalData = SolrUtil.jsonFacet(EngineUtil.DWB_MASTER_INFO, solrQuery.getFilterQueries(), json.toJSONString());
        long totalCount = totalData.getLongValue("count");
        if(totalCount == 0){
            return Result.ok(page);
        }
        JSONObject bodyJson = (JSONObject)totalData.remove("body");
        JSONArray statisticData = bodyJson.getJSONArray("buckets");
        Long totalPage = bodyJson.getLong("numBuckets");
        // 添加 全部 行
        totalData.put("val", "全部");
        statisticData.add(0, totalData);
        for(int i = 0, j, len = statisticData.size(); i < len; i = j){
            if((j = i + 1000) > len){
                j = len;
            }

//                    getChargeIdCount(statisticData.subList(i, j), dwbMasterFqs, "1".equals(diseaseMappingFilter));
            getChargeIdCountDws(statisticData.subList(i, j), dwbMasterFqs, "1".equals(diseaseMappingFilter));
//            getChargeIdCountDwsPage(statisticData.subList(i, j), dwbMasterFqs, "1".equals(diseaseMappingFilter));
        }

        totalData.put("val", "全部（" + totalPage + "）");

        page.setRecords(statisticData);
        page.setTotal(totalPage);

        return Result.ok(page);
    }

    /**
     * 有分页的时候统计总数
     * @param statisticData
     * @param dwbMasterFqs
     * @param isDiseaseMappingFilter
     * @throws Exception
     */
    private void getChargeIdCountDwsPage(List<Object> statisticData, String[] dwbMasterFqs, boolean isDiseaseMappingFilter) throws Exception {

        if(statisticData.size() == 0){
            return;
        }
        SolrQuery solrQuery = new SolrQuery("*:*");
        // 添加主表条件关联
        if(dwbMasterFqs != null && dwbMasterFqs.length > 0){
            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "id");
            solrQuery.addFilterQuery(plugin.parse() + "(" + StringUtils.join(dwbMasterFqs, " AND ") + ")");
        }

        // 疾病映射不全过滤 条件
        if(isDiseaseMappingFilter){
            solrQuery.addFilterQuery("DISEASE_MISS_FLAG:1");
        }



        List<String> orgnames = statisticData.stream().map(r -> ((JSONObject)r).getString("val")).collect(Collectors.toList());
        // 移除全部
        orgnames.remove(0);

        JSONObject json = new JSONObject().fluentPut("chargeidCount", "sum(CHARGE_ITEM_COUNT)");
        for(String orgname: orgnames){
            json.put(orgname, new JSONObject()
                            .fluentPut("type", "query")
                            .fluentPut("q", "ORGNAME:" + EngineUtil.escapeQueryChars(orgname))
                            .fluentPut("facet", new JSONObject()
                                .fluentPut("chargeidCount", "sum(CHARGE_ITEM_COUNT)")
                            )
            );
        }
        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(EngineUtil.DWS_1VISIT_TAG, solrQuery.getFilterQueries(), json.toJSONString());

        jsonObject.put("全部", new JSONObject().fluentPut("chargeidCount", jsonObject.getLongValue("chargeidCount")));
        statisticData.forEach(r -> {
            JSONObject statisticObj = (JSONObject)r;
            String val = statisticObj.getString("val");
            statisticObj.put("chargeidCount", jsonObject.getJSONObject(val).getLongValue("chargeidCount"));
        });


    }

    /**
     * 无分页
     * @param statisticData
     * @param dwbMasterFqs
     * @param isDiseaseMappingFilter
     * @throws Exception
     */
    private void getChargeIdCountDws(List<Object> statisticData, String[] dwbMasterFqs, boolean isDiseaseMappingFilter) throws Exception {

        if(statisticData.size() == 0){
            return;
        }

        SolrQuery solrQuery = new SolrQuery("*:*");

       /* List<String> orgnames = statisticData.stream().map(r -> ((JSONObject)r).getString("val")).collect(Collectors.toList());
        List<String> dwbMasterFqList = dwbMasterFqs == null?new ArrayList<>():new ArrayList<>(Arrays.asList(dwbMasterFqs));
        dwbMasterFqList.add("ORGNAME:(" + orgnames.stream().map(EngineUtil::escapeQueryChars).collect(Collectors.joining(" OR ")) + ")");
        // 添加主表条件关联
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "id");
        solrQuery.addFilterQuery(plugin.parse() + "(" + StringUtils.join(dwbMasterFqList, " AND ") + ")");
*/
        // 添加主表条件关联
        if(dwbMasterFqs != null && dwbMasterFqs.length > 0){
            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "id");
            solrQuery.addFilterQuery(plugin.parse() + "(" + StringUtils.join(dwbMasterFqs, " AND ") + ")");
        }
        // 疾病映射不全过滤 条件
        if(isDiseaseMappingFilter){
            solrQuery.addFilterQuery("DISEASE_MISS_FLAG:1");
        }

        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", "ORGNAME");
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", new JSONObject()
                .fluentPut("chargeidCount", "sum(CHARGE_ITEM_COUNT)")
        );
        JSONObject json = new JSONObject()
                .fluentPut("body", json1)
                .fluentPut("chargeidCount", "sum(CHARGE_ITEM_COUNT)");
        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(EngineUtil.DWS_1VISIT_TAG, solrQuery.getFilterQueries(), json.toJSONString());
        long totalChargeidCount = jsonObject.getLongValue("chargeidCount");
        if(totalChargeidCount == 0){
            return;
        }
        JSONArray data = jsonObject.getJSONObject("body").getJSONArray("buckets");
        Map<String, Long> map = new HashMap<>();
        data.forEach(r -> {
            JSONObject rJson = (JSONObject)r;
            map.put(rJson.getString("val"), rJson.getLongValue("chargeidCount"));
        });

        map.put("全部", jsonObject.getLongValue("chargeidCount"));
        statisticData.forEach(r -> {
            JSONObject statisticObj = (JSONObject)r;
            String val = statisticObj.getString("val");
            statisticObj.put("chargeidCount", map.get(val));
        });


    }

    private void getChargeIdCount(List<Object> statisticData, String[] dwbMasterFqs, boolean isDiseaseMappingFilter) throws Exception {

        if(statisticData.size() == 0){
            return;
        }
        SolrQuery solrQuery = new SolrQuery("*:*");
        // 添加主表条件关联
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
        solrQuery.addFilterQuery(plugin.parse() + "(" + StringUtils.join(dwbMasterFqs, " AND ") + ")");
        // 疾病映射不全过滤 条件
        if(isDiseaseMappingFilter){
            plugin = new SolrJoinParserPlugin(EngineUtil.DWB_DIAG, "VISITID", "VISITID");
            solrQuery.addFilterQuery("*:* -" + plugin.parse() + "-DISEASENAME:?*");
        }

        List<String> orgnames = statisticData.stream().map(r -> ((JSONObject)r).getString("val")).collect(Collectors.toList());

        // 添加分页的机构关联
        String orgnameFq = "ORGNAME:(" + StringUtils.join(orgnames, " OR ") + ")";

        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", "ORGNAME");
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", new JSONObject()
                .fluentPut("chargeidCount", "unique(CHARGEID)")
        );

        JSONObject json = new JSONObject();
               /* .fluentPut("body", new JSONObject()
                        .fluentPut("type", "query")
                        .fluentPut("q", orgnameFq)
                        .fluentPut("facet", new JSONObject().fluentPut("group",json1))
                );*/
        for(String orgname: orgnames){
            json.put(orgname, new JSONObject()
                    .fluentPut("type", "query")
                    .fluentPut("q", "ORGNAME:" + orgname)
//                    .fluentPut("facet", "ORGNAME:" + orgname)
            );
        }
        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(EngineUtil.DWB_CHARGE_DETAIL, solrQuery.getFilterQueries(), json.toJSONString());
        /*JSONArray orgStatisticData = jsonObject.getJSONObject("body").getJSONObject("group").getJSONArray("buckets");

        Map<String, Long> orgChargeCountMap = new HashMap<>();
        for(int i = 0, len = statisticData.size(); i < len; i++){
            JSONObject statisticObj = orgStatisticData.getJSONObject(i);
            orgChargeCountMap.put(statisticObj.getString("val"), statisticObj.getLongValue("chargeidCount"));
        }*/
        jsonObject.put("全部", new JSONObject().fluentPut("count", jsonObject.getLongValue("count")));
        statisticData.forEach(r -> {
            JSONObject statisticObj = (JSONObject)r;
            String val = statisticObj.getString("val");
            statisticObj.put("chargeidCount", jsonObject.getJSONObject(val).getLongValue("count"));
        });


    }



}
