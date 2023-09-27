/**
 * EngineController.java	  V1.0   2019年12月2日 上午11:37:44
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.engine.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.model.EchartsEntity;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.dto.CompareCaseFlowDTO;
import com.ai.modules.engine.model.dto.EchartCaseFlowDTO;
import com.ai.modules.engine.model.dto.EchartCompareCaseFlowDTO;
import com.ai.modules.engine.model.dto.EngineCaseFlowDTO;
import com.ai.modules.engine.model.vo.MedicalCaseVO;
import com.ai.modules.engine.service.IEngineDwsService;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.IEngineTrialService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.vo.CaseNode;
import com.ai.modules.medical.entity.MedicalFlowTrial;
import com.ai.modules.medical.service.IMedicalFlowTrialService;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "模型试算引擎")
@RestController
@RequestMapping("/engine")
public class EngineController {
    @Autowired
    private IEngineService service;
    @Autowired
    private IEngineDwsService dwsService;
    @Autowired
    private IMedicalFlowTrialService medicalFlowTrialService;

    @Autowired
    private IEngineTrialService engineTrialService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AutoLog(value = "病例试算")
    @ApiOperation(value = "病例试算", notes = "病例试算")
    @PostMapping(value = "/trail")
    public Result<?> trail(@RequestBody EngineCaseFlowDTO dto, HttpServletRequest req) throws Exception {
        Page<SolrDocument> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        if (dto.isDWS()) {
            IPage<SolrDocument> pageList = dwsService.trial(page, dto);
            return Result.ok(pageList);
        }
        IPage<SolrDocument> pageList = service.trial(page, dto);
        return Result.ok(pageList);
    }

    @AutoLog(value = "病例试算分组统计")
    @ApiOperation(value = "病例试算分组统计", notes = "病例试算分组统计")
    @PostMapping(value = "/trail/echart")
    public Result<?> echart(@RequestBody EchartCaseFlowDTO dto, HttpServletRequest req) throws Exception {
        EchartsEntity echarts = service.echart(dto);
        return Result.ok(echarts);
    }

    @AutoLog(value = "试算两个节点病例的交集或差集")
    @ApiOperation(value = "试算两个节点病例的交集或差集", notes = "试算两个节点病例的交集或差集")
    @PostMapping(value = "/trail/compare")
    public Result<?> compare(@RequestBody CompareCaseFlowDTO dto, HttpServletRequest req) throws Exception {
        Page<MedicalCaseVO> page = new Page<MedicalCaseVO>(dto.getPageNo(), dto.getPageSize());
        IPage<MedicalCaseVO> pageList = service.compare(page, dto);
        return Result.ok(pageList);
    }

    @AutoLog(value = "试算两个节点病例的交集或差集后分组统计")
    @ApiOperation(value = "试算两个节点病例的交集或差集后分组统计", notes = "试算两个节点病例的交集或差集后分组统计")
    @PostMapping(value = "/trail/compare/echart")
    public Result<?> compare(@RequestBody EchartCompareCaseFlowDTO dto, HttpServletRequest req) throws Exception {
        EchartsEntity echarts = service.echart(dto);
        return Result.ok(echarts);
    }


    @AutoLog(value = "病例试算导出")
    @ApiOperation(value = "病例试算导出", notes = "病例试算导出")
    @RequestMapping(value = "/trailExportMasterInfo")
    public Result<?> trailExportMasterInfo(String param, HttpServletRequest req, HttpServletResponse response) throws Exception {
        param = URLDecoder.decode(param, "UTF-8");
        EngineCaseFlowDTO dto = JSONObject.parseObject(param, EngineCaseFlowDTO.class);
        Set<String> fqSet = this.service.constructTrialFq(dto);
        SolrQuery solrQuery = new SolrQuery("*:*");
        // 设定查询字段
        fqSet.forEach(solrQuery::addFilterQuery);
        int count = (int) SolrQueryGenerator.count(EngineUtil.DWB_MASTER_INFO, solrQuery);
        String title = "试算_病例";

        if (count < 20000) {
//			response.reset();
//			response.setContentType("application/octet-stream; charset=utf-8");
//			response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "导出" + System.currentTimeMillis() + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            OutputStream os = response.getOutputStream();
            this.service.trialExportMasterInfo(dto, solrQuery, os);
            return null;
        } else {

            List<SolrQuery> queries = new ArrayList<>();

            for (int i = 0, j; i < count; i = j) {
                j = i + 500000;
                if (j > count) {
                    j = count;
                }
                SolrQuery query = solrQuery.getCopy();
                query.setSorts(solrQuery.getSorts());
                query.setStart(i);
                query.setRows(j - i);
                queries.add(query);
            }
            int i = 1, len = queries.size();
            for (SolrQuery query : queries) {
                ThreadUtils.EXPORT_POOL.addRemote(title + "_导出" + (len == 1 ? "" : ("(" + i++ + ")")), "xlsx", query.getRows(), (os) -> {
                    try {
                        this.service.trialExportMasterInfo(dto, query, os);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                    return Result.ok();
                });
            }
            return Result.ok("等待导出,共有 " + len + " 个文件");
        }

    }

    @AutoLog(value = "试算项目明细导出")
    @ApiOperation(value = "试算项目明细导出", notes = "试算项目明细导出")
    @PostMapping(value = "/trailExportThread")
    public Result<?> trailExportThread(@RequestBody EngineCaseFlowDTO dto, HttpServletRequest req) throws Exception {
        String excelName = "试算_项目明细";
        ThreadUtils.EXPORT_POOL.addRemote(excelName + "_导出", "xlsx", -1, (os) -> {
            // 创建文件输出流
            Result<Object> result = Result.ok();
            try {
                Integer count = service.trialExport(dto, os);
                result.setResult(count);
            } catch (Exception e) {
                e.printStackTrace();
                ;
                result = Result.error(e.getMessage());
            }
            return result;
        });


        return Result.ok("等待导出，最大限制就诊记录前100万条，可排序");
    }

    @AutoLog(value = "节点统计结果查看")
    @ApiOperation(value = "节点统计结果查看", notes = "节点统计结果查看")
    @GetMapping(value = "/statisticTotalView")
    public Result<?> statisticTotalView(Boolean probe, HttpServletRequest req) throws Exception {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = sysUser.getDataSource();
/*
		int total = medicalFlowTrialService.count(new QueryWrapper<MedicalFlowTrial>()
				.eq("PROJECT", sysUser.getDataSource())
				.select("distinct CASE_ID")
		);

		int runTotal = medicalFlowTrialService.count(new QueryWrapper<MedicalFlowTrial>()
				.select("distinct CASE_ID")
				.eq("PROJECT", sysUser.getDataSource())
				.eq("STATUS", "running")
		);*/
        //		JSONObject json = new JSONObject().fluentPut("total", total).fluentPut("running", runTotal);

        String typeQuery = probe == null?"":(probe? " AND TYPE = 'probe'": " AND TYPE = 'formal'");
        String projectQuery = "PROJECT= '" + dataSource + "'";

        Map<String, Object> map = jdbcTemplate.queryForMap(
                "SELECT\n" +
                        "	max(t.END_TIME) END_TIME,\n" +
                        "	count(t.CASE_ID) TOTAL,\n" +
//                        "	sum( DECODE( t1.CASE_ID, NULL, 0, 1 ) ) running,\n" +
                        "	sum( IF( t1.CASE_ID is NULL, 0, 1 ) ) RUNNING,\n" +
//                        "	sum( DECODE( t2.CASE_ID, NULL, 0, 1 ) ) abnormal \n" +
                        "	sum( IF( t2.CASE_ID is NULL, 0, 1 ) ) ABNORMAL \n" +
                        "FROM\n" +
                        "	( SELECT CASE_ID,max(END_TIME) END_TIME FROM MEDICAL_FLOW_TRIAL where " + projectQuery + typeQuery + " GROUP BY CASE_ID  ) t\n" +
                        "	LEFT JOIN ( SELECT DISTINCT CASE_ID AS CASE_ID FROM MEDICAL_FLOW_TRIAL where " + projectQuery + typeQuery + " AND STATUS in ('running','wait') ) t1 ON t.CASE_ID = t1.CASE_ID\n" +
                        "	LEFT JOIN ( SELECT DISTINCT CASE_ID AS CASE_ID FROM MEDICAL_FLOW_TRIAL where " + projectQuery + typeQuery + " AND STATUS = 'abnormal' ) t2 ON t.CASE_ID = t2.CASE_ID");
        return Result.ok(map);
    }

    @AutoLog(value = "节点统计结果查看")
    @ApiOperation(value = "节点统计结果查看", notes = "节点统计结果查看")
    @GetMapping(value = "/statisticCaseView")
    public Result<?> statisticCaseView(@RequestParam(name = "caseId") String caseId, HttpServletRequest req) throws Exception {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<MedicalFlowTrial> list = medicalFlowTrialService.list(new QueryWrapper<MedicalFlowTrial>()
                .eq("CASE_ID", caseId)
                .eq("PROJECT", sysUser.getDataSource())
        );
        return Result.ok(list);
    }

    @AutoLog(value = "节点统计结果执行-异步全部")
    @ApiOperation(value = "节点统计结果执行-异步全部", notes = "节点统计结果执行-异步全部")
    @PostMapping(value = "/statisticExec")
    public Result<?> statisticExec(@RequestParam(name = "probe") Boolean probe, HttpServletRequest req) throws Exception {
//        log.info("probe: " + probe);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        new Thread(new EngineFunctionRunnable(sysUser.getDataSource(), sysUser.getToken(), () -> {
            engineTrialService.trialCaseFlowCnt(probe);
        })).start();

        Thread.sleep(3000);
        return Result.ok();
    }

    @AutoLog(value = "节点统计结果执行-实时")
    @ApiOperation(value = "节点统计结果执行-实时", notes = "节点统计结果执行-实时")
    @PostMapping(value = "/statisticCaseExec")
    public Result<?> statisticCaseExec(@RequestBody EngineCaseFlowDTO dto, HttpServletRequest req) throws Exception {
        if (dto.isDWS()) {
//			IPage<SolrDocument> pageList = dwsService.trial(page, dto);
            return Result.ok();
        }
        List<EngineNode> nodes = service.parseEngineCaseDTO(dto);
        Map<String, EngineNode> nodeMap = new HashMap<>();
        nodes.stream().filter(r -> !"start".equals(r.getNodeType()) && !"end".equals(r.getNodeType()))
                .forEach(node -> {
//			String condition = EngineUtil.parseConditionExpression(node);
                    nodeMap.put(node.getNodeCode(), node);
                });

        JSONObject jsonObject = (JSONObject) JSONObject.parse(dto.getFlowJson());
        JSONArray linkArray = jsonObject.getJSONArray("linkDataArray");

        Map<String, CaseNode> map = new HashMap<>();
        for (int i = 0, len = linkArray.size(); i < len; i++) {
            JSONObject json = linkArray.getJSONObject(i);
            String from = json.getString("from");
            String to = json.getString("to");
            CaseNode fromNode = map.computeIfAbsent(from, k -> {
                CaseNode caseNode = new CaseNode();
                caseNode.setKey(from);
                return caseNode;
            });
            CaseNode toNode = map.computeIfAbsent(to, k -> {
                CaseNode caseNode = new CaseNode();
                caseNode.setKey(to);
                return caseNode;
            });
            toNode.setParent(fromNode);
            fromNode.addChild(toNode);
            // 节点条件为否
            if (StringUtils.isNotEmpty(json.getString("visible"))
                    && "否".equals(json.get("text"))) {
                toNode.setFromYes(false);
            }
        }

        List<CaseNode> rootNodes = map.values().stream().filter(r -> r.getParent() == null).collect(Collectors.toList());
        JSONObject json = this.initJson(rootNodes, nodeMap);

        log.debug(json.toJSONString());

        JSONObject resultJson = SolrUtil.jsonFacet(EngineUtil.DWB_MASTER_INFO, new String[0], json.toJSONString());

        return Result.ok(resultJson);
    }

    private JSONObject initJson(List<CaseNode> nodes, Map<String, EngineNode> nodeMap) {
        JSONObject totalJson = new JSONObject();
        for (CaseNode node : nodes) {
            String key = node.getKey();
            List<CaseNode> children = node.getChildren();
            List<CaseNode> yesChildren = children.stream().filter(CaseNode::isFromYes).collect(Collectors.toList());
            JSONObject yesChildJson = yesChildren.size() > 0 ? this.initJson(yesChildren, nodeMap) : new JSONObject();

            EngineNode engineNode = nodeMap.get(key);
            if (engineNode == null) {
                totalJson.putAll(yesChildJson);
                continue;
            }

            engineNode.setCondition("YES");
            String condition = EngineUtil.parseConditionExpression(engineNode);
            if (StringUtils.isBlank(condition)) {
                totalJson.putAll(yesChildJson);
                continue;
            }
            JSONObject json = new JSONObject();
            json.put("type", "query");
            json.put("q", condition);
            if (yesChildJson.size() > 0) {
                json.put("facet", yesChildJson);
            }

            totalJson.put(key, json);

            if (children.size() != yesChildren.size()) {
                engineNode.setCondition("NO");
                condition = EngineUtil.parseConditionExpression(engineNode);
                if (StringUtils.isNotBlank(condition)) {
                    List<CaseNode> noChildren = children.stream().filter(r -> !r.isFromYes()).collect(Collectors.toList());
                    JSONObject noChildJson = this.initJson(noChildren, nodeMap);
                    if (noChildJson.size() > 0) {
                        JSONObject jsonNo = new JSONObject();
                        jsonNo.put("type", "query");
                        jsonNo.put("q", condition);
                        jsonNo.put("facet", noChildJson);
                        totalJson.put(key + "-no", jsonNo);
                    }
                }
            }

        }
        return totalJson;

    }
}
