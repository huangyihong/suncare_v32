package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.StringCamelUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalFlowRuleGrade;
import com.ai.modules.review.vo.DwbMasterInfoVo;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewManualVo;
import com.ai.modules.task.dto.TaskReviewAssignManualDTO;
import com.ai.modules.task.service.ITaskReviewAssignService;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "人工审核")
@RestController
@RequestMapping("/reviewManual")
public class ReviewManualController {
    @Autowired
    private ITaskReviewAssignService taskReviewAssignService;

    private static Map<String, String> FIELD_MAPPING = SolrUtil.initFieldMap(ReviewManualVo.class);

    private String initUnreasonableFq(String batchId, String caseId,  String actionFirReviewUserid, String actionFirReviewStatus, String actionPushStatus) {
        String unreasonableFq = "REVIEW_CASE_IDS: * AND BATCH_ID:" + batchId;
        if (StringUtils.isNotBlank(caseId)) {
            unreasonableFq += " AND CASE_ID:" + caseId;
        }
        if(StringUtils.isNotBlank(actionFirReviewUserid)){
            unreasonableFq += " AND FIR_REVIEW_USERID:" + actionFirReviewUserid;
        }
        if(StringUtils.isNotBlank(actionFirReviewStatus)){
            unreasonableFq += " AND FIR_REVIEW_STATUS:" + actionFirReviewStatus;
        }
        if(StringUtils.isNotBlank(actionPushStatus)){
            if("1".equals(actionPushStatus)){
                unreasonableFq += " AND PUSH_STATUS:1";
            } else {
                unreasonableFq += " AND -PUSH_STATUS:1";
            }

        }
        /*if(filterPush){
            unreasonableFq += " AND -FIR_REVIEW_USERID:*";
        }*/
        return unreasonableFq;
    }

    /**
     * 人工审查-分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "人工审核-规则结果分页列表查询")
    @ApiOperation(value = "人工审核-规则结果分页列表查询", notes = "人工审核-规则结果分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(DwbMasterInfoVo dwbMasterInfo,
                                   @RequestParam(name = "batchId") String batchId,
                                   String caseId,
                                   String grades,
                                   String actionPushStatus,
                                   String actionFirReviewUserid,
                                   String actionFirReviewStatus,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) throws Exception {
        String unreasonableFq = initUnreasonableFq(batchId, caseId,actionFirReviewUserid, actionFirReviewStatus , actionPushStatus);
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
        String unreasonableJoinFq = "_query_:\"" + plugin.parse() + unreasonableFq + "\"";
        SolrQuery solrQuery = this.constructListQuery(dwbMasterInfo, unreasonableJoinFq, caseId, grades, req.getParameterMap());

        Page<ReviewManualVo> page = new Page<>(pageNo, pageSize);
        IPage<ReviewManualVo> pageList = SolrQueryGenerator.page(page, ReviewManualVo.class,
                solrQuery, EngineUtil.DWB_MASTER_INFO, FIELD_MAPPING);
        if (pageList.getSize() > 0) {
            Map<String, ReviewManualVo> map = new HashMap<>();
            for (ReviewManualVo bean : pageList.getRecords()) {
                map.put(bean.getVisitid(), bean);
            }
            String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

            SolrQuery solrQuery1 = new SolrQuery("*:*");
            solrQuery1.addFilterQuery(visitIdFq, "BATCH_ID:" + batchId);
            solrQuery1.setFields("id", "VISITID", "CASE_NAME", "REVIEW_CASE_IDS", "FIR_REVIEW_USERNAME", "FIR_REVIEW_USERID", "FIR_REVIEW_STATUS");
            SolrDocumentList unreasonableList = SolrUtil.call(solrQuery1, EngineUtil.MEDICAL_UNREASONABLE_ACTION).getResults();
            for (SolrDocument doc : unreasonableList) {
                ReviewManualVo bean = map.get(doc.getFieldValue("VISITID").toString());
                bean.setId(doc.getFieldValue("id").toString());
                if (doc.containsKey("CASE_NAME")) {
                    bean.setCaseName(doc.getFieldValues("CASE_NAME").stream().map(Object::toString).collect(Collectors.toList()));
                }
                if (doc.containsKey("REVIEW_CASE_IDS")) {
                    bean.setReviewCaseIds(doc.getFieldValues("REVIEW_CASE_IDS").stream().map(Object::toString).collect(Collectors.toList()));
                }
                if (doc.containsKey("FIR_REVIEW_USERNAME")) {
                    bean.setFirReviewUsername(doc.getFieldValue("FIR_REVIEW_USERNAME").toString());
                }
                if (doc.containsKey("FIR_REVIEW_USERID")) {
                    bean.setFirReviewUserid(doc.getFieldValue("FIR_REVIEW_USERID").toString());
                }
                if (doc.containsKey("FIR_REVIEW_STATUS")) {
                    bean.setFirReviewStatus(doc.getFieldValue("FIR_REVIEW_STATUS").toString());
                }

            }
        }

        return Result.ok(pageList);
    }

    /**
     * 保存多个审查结果
     * <p>
     * \     * @return
     *
     * @throws Exception
     */
    @AutoLog(value = "不合理行为就诊记录审查表-保存多个审查结果")
    @ApiOperation(value = "不合理行为就诊记录审查表-保存多个审查结果", notes = "不合理行为就诊记录审查表-保存多个审查结果")
    @PutMapping(value = "/pushReviews")
    public Result<?> pushReviews(DwbMasterInfoVo dwbMasterInfo,
                                 @RequestParam(name = "batchId") String batchId,
                                 String caseId,
                                 String grades,
                                 String ids,
                                 String actionPushStatus,
                                 String actionFirReviewUserid,
                                 String actionFirReviewStatus,
                                 MedicalUnreasonableActionVo pushInfo,
                                 HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        SolrInputDocument commonDoc = initInputDocument(pushInfo);
        //第一次审查信息
        commonDoc.setField("FIR_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonDoc.setField("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonDoc.setField("FIR_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));
        commonDoc.setField("PUSH_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonDoc.setField("PUSH_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
//			commonDoc.setField("ACTION_COUNT", documents.getNumFound());
        commonDoc.setField("SEC_REVIEW_STATUS", SolrUtil.initActionValue("00", "set"));//待审核
        // 获取查询出的ID
        SolrClient solr = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        int count = 0;
        if (ids == null) {
            String unreasonableFq = initUnreasonableFq(batchId, caseId,actionFirReviewUserid, actionFirReviewStatus, actionPushStatus);
            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
            String unreasonableJoinFq = "_query_:\"" + plugin.parse() + unreasonableFq + "\"";
            SolrQuery masterQuery = this.constructListQuery(dwbMasterInfo, unreasonableJoinFq, caseId, grades, req.getParameterMap());
            masterQuery.removeFilterQuery(unreasonableJoinFq);
            // 构造主表条件
            SolrQuery solrQuery = new SolrQuery("*:*");
            solrQuery.setFields("id");
            String[] masterFqs = masterQuery.getFilterQueries();
            if (masterFqs != null && masterFqs.length > 0) {
                String gradeParam = masterQuery.get("GRADE_VALUE");
                if(gradeParam != null){
                    for(int i = 0, len = masterFqs.length; i < len; i++){
                        if(masterFqs[i].contains("$GRADE_VALUE")){
                            masterFqs[i] = masterFqs[i].replace("$GRADE_VALUE",gradeParam);
                            break;
                        }
                    }
                }
                plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
                solrQuery.addFilterQuery(plugin.parse() + StringUtils.join(masterFqs, " AND "));
            }
            solrQuery.addFilterQuery(unreasonableFq);
            // 限定审核人为自己 并且未被推送
            solrQuery.addFilterQuery("FIR_REVIEW_USERID:" + user.getId());
            solrQuery.addFilterQuery("-PUSH_STATUS:1");
            count += SolrUtil.export(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
                String id = String.valueOf(map.get("id"));
                // 构造更新参数
                SolrInputDocument doc = commonDoc.deepCopy();
                doc.setField("id", id);
                try {
                    solr.add(doc);
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            String[] idArray = ids.split(",");
            if (idArray.length == 0) {
                return Result.error("勾选项不能为空");
            }
            for (String id : idArray) {
                SolrInputDocument doc = commonDoc.deepCopy();
                doc.setField("id", id);
                solr.add(doc);
            }
            count+= idArray.length;
        }
        log.info("批量推送数量：" + count);
        // 提交
        solr.commit();
        return Result.ok("保存成功!");
    }

    /**
     * 添加
     *
     * @param taskReviewAssign
     * @return
     */
    @AutoLog(value = "人工审核任务分配-添加")
    @ApiOperation(value = "人工审核任务分配-添加", notes = "人工审核任务分配-添加")
    @PostMapping(value = "/addAssign")
    @Transactional
    public Result<?> addManual(@RequestBody TaskReviewAssignManualDTO taskReviewAssign) {
        // 设置病例归属
        String memberId = taskReviewAssign.getMember();
        String memberName = taskReviewAssign.getMemberName();
        SolrInputDocument commonDoc = new SolrInputDocument();
        commonDoc.setField("FIR_REVIEW_USERID", SolrUtil.initActionValue(memberId, "set"));
        commonDoc.setField("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(memberName, "set"));
        int count = 0;
        try {
            SolrClient solrClient = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
            String[] selectKeys = taskReviewAssign.getSelectKeys();
            // 选中的记录
            if (selectKeys != null && selectKeys.length > 0) {
                for (String id : selectKeys) {
                    // 构造更新参数
                    SolrInputDocument document = commonDoc.deepCopy();
                    document.setField("id", id);
                    solrClient.add(document);
                }
                count += selectKeys.length;
            } else {
                // 全部 或 区间
                cn.hutool.json.JSONObject paramJson = taskReviewAssign.getParams();
                DwbMasterInfoVo paramBean = paramJson.toBean(DwbMasterInfoVo.class);
                // 构造查询参数map
                Map<String, String[]> requestMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : paramJson.entrySet()) {
                    requestMap.put(entry.getKey(), new String[]{entry.getValue().toString()});
                }
                String batchId = paramJson.getStr("batchId");
                String caseId = paramJson.getStr("caseId");
                String actionFirReviewUserid = paramJson.getStr("actionFirReviewUserid");
                String actionFirReviewStatus = paramJson.getStr("actionFirReviewStatus");
                String actionPushStatus = paramJson.getStr("actionPushStatus");
                String unreasonableFq = initUnreasonableFq(batchId, caseId,actionFirReviewUserid, actionFirReviewStatus, actionPushStatus);
                // 构造查询条件
                SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
                String unreasonableJoinFq = "_query_:\"" + plugin.parse() + unreasonableFq + "\"";
                SolrQuery masterQuery = constructListQuery(paramBean, unreasonableJoinFq,
                        caseId, paramJson.getStr("grades"), requestMap);
                // 通过join条件算出最大值最小值后移除未过滤推送的
                masterQuery.removeFilterQuery(unreasonableJoinFq);
                // 增加未推送条件， 过滤就诊ID
                unreasonableFq += " AND -FIR_REVIEW_USERID:*";

                if (taskReviewAssign.getRangeStart() != null || taskReviewAssign.getRangeEnd() != null) {
                    int start = (taskReviewAssign.getRangeStart() == null ? 1 : taskReviewAssign.getRangeStart()) - 1;
                    masterQuery.setStart(start);
                    if (taskReviewAssign.getRangeEnd() != null) {
                        masterQuery.setRows(taskReviewAssign.getRangeEnd() - start);
                    } else {
                        masterQuery.setRows(Integer.MAX_VALUE);
                    }
                    plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
                    unreasonableJoinFq = "_query_:\"" + plugin.parse() + unreasonableFq + "\"";
                    masterQuery.addFilterQuery(unreasonableJoinFq);
                    masterQuery.setFields("VISITID");
                    QueryResponse queryResponse = SolrUtil.call(masterQuery, EngineUtil.DWB_MASTER_INFO);
                    List<Object> visitidList = queryResponse.getResults().stream().map(doc -> doc.getFieldValue("VISITID")).collect(Collectors.toList());
                    if (visitidList.size() == 0) {
                        return Result.error("可分配数量为空");
                    }
                    // solr in 有数量限制，分页
                    List<List<Object>> visitidPages = new ArrayList<>();
                    for (int i = 0, j, len = visitidList.size(); i < len; i = j) {
                        j = i + 500;
                        if (j > len) {
                            j = len;
                        }
                        visitidPages.add(visitidList.subList(i, j));
                    }

                    for (List<Object> visitids : visitidPages) {
                        SolrQuery solrQuery = new SolrQuery("*:*");
                        solrQuery.addFilterQuery("VISTID:(\"" + StringUtils.join(visitids, "\",\"") + "\")");
                        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
                        // 获取查询出的ID
                        count += SolrUtil.export(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
                            String id = String.valueOf(map.get("id"));
                            // 构造更新参数
                            SolrInputDocument document = commonDoc.deepCopy();
                            document.setField("id", id);
                            try {
                                solrClient.add(document);
                            } catch (SolrServerException | IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                } else {
                    // 构造主表条件
                    SolrQuery solrQuery = new SolrQuery("*:*");
                    solrQuery.setFields("id");
                    String[] masterFqs = masterQuery.getFilterQueries();
                    if (masterFqs != null && masterFqs.length > 0) {
                        String gradeParam = masterQuery.get("GRADE_VALUE");
                        if(gradeParam != null){
                            for(int i = 0, len = masterFqs.length; i < len; i++){
                                if(masterFqs[i].contains("$GRADE_VALUE")){
                                    masterFqs[i] = masterFqs[i].replace("$GRADE_VALUE",gradeParam);
                                    break;
                                }
                            }
                        }
                        plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
                        solrQuery.addFilterQuery(plugin.parse() + StringUtils.join(masterFqs, " AND "));
                    }
                    solrQuery.addFilterQuery(unreasonableFq);
                    // 获取查询出的ID
                    count += SolrUtil.export(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
                        String id = String.valueOf(map.get("id"));
                        // 构造更新参数
                        SolrInputDocument doc = commonDoc.deepCopy();
                        doc.setField("id", id);
                        try {
                            solrClient.add(doc);
                        } catch (SolrServerException | IOException e) {
                            e.printStackTrace();
                        }
                    });

                }

            }
            log.info("分配数量：" + count);
            // 提交
            solrClient.commit();
            solrClient.close();
        } catch (Exception e) {
            log.error("", e);
            return Result.error(e.getMessage());
        }
        if (StringUtils.isBlank(taskReviewAssign.getAssignId())) {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            taskReviewAssign.setLeader(user.getId());
            taskReviewAssignService.save(taskReviewAssign);
        }
        return Result.ok("添加成功！");
    }

    private SolrInputDocument initInputDocument(Object obj) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SolrInputDocument doc = new SolrInputDocument();
        List<Field> fieldList = new ArrayList<>();
        Class clzz = obj.getClass();
        do {
            fieldList.addAll(Arrays.asList(clzz.getDeclaredFields()));
            clzz = clzz.getSuperclass(); //得到父类,然后赋给自己
            //当父类为null的时候说明到达了最上层的父类(Object类).
        } while (clzz != null);

        for (Field field : fieldList) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(obj);
            String docName = StringCamelUtils.camel2Underline(name);
            if (value != null) {
                if ("id".equals(name)) {
                    doc.setField(name, value);
                } else {
                    doc.setField(docName, SolrUtil.initActionValue(value, "set"));
                }
            }
        }

        return doc;
    }


    private SolrQuery constructListQuery(DwbMasterInfoVo dwbMasterInfo,
                                         String unreasonableFq,
                                         String caseId,
                                         String grades,
                                         Map<String, String[]> paramMap) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbMasterInfo, paramMap);
        SolrQuery.ORDER gradeOrder = null;
        List<SolrQuery.SortClause> sortList = solrQuery.getSorts();
        for (SolrQuery.SortClause sortClause : sortList) {
            if ("GRADE_VALUE".equals(sortClause.getItem())) {
                solrQuery.removeSort("GRADE_VALUE");
                gradeOrder = sortClause.getOrder();
                break;
            }
        }
        if (StringUtils.isNotBlank(caseId)) {
            solrQuery.removeFilterQuery("CASE_ID:" + caseId);
        }
        // 结果表查询条件
        solrQuery.addFilterQuery(unreasonableFq);

        // 设置返回字段
        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        // 评分
        if (StringUtils.isNotBlank(grades)) {
            List<MedicalFormalFlowRuleGrade> gradeList = JSONArray.parseArray(URLDecoder.decode(grades), MedicalFormalFlowRuleGrade.class);
            List<String> facetList = gradeList.stream().map(grade -> String.format("max-%s:\"max(%s)\"", grade.getEvaluateField(), grade.getEvaluateField())).collect(Collectors.toList());

            solrQuery.set("json.facet", "{" + StringUtils.join(facetList, ",") + "}");
            solrQuery.setRows(0);

            QueryResponse response = SolrUtil.call(solrQuery, EngineUtil.DWB_MASTER_INFO);
            long recordNum = response.getResults().getNumFound();
            if (recordNum == 0) {
                throw new Exception("此次模型条件下的记录为空");
            }
            NestableJsonFacet jsonFacet = response.getJsonFacetingResponse();
            List<String> gradeParams = new ArrayList<>();
            for (MedicalFormalFlowRuleGrade grade : gradeList) {
                String field = grade.getEvaluateField();
                Number valObj = jsonFacet.getStatFacetValue("max-" + field);
                if (valObj != null) {
                    double val = valObj.doubleValue() - grade.getStandardVal().doubleValue();
                    if (val == 0) {
                        throw new Exception("字段：" + field + "的最大值=基准值");
                    }
                    String param = "mul(div(" +
                            "sub(" + field + "," + grade.getStandardVal() + ")," + val +
                            ")," + grade.getWeight() + ")";
                    gradeParams.add(param);
                }
            }


            if (gradeParams.size() > 0) {
                // 评分区间
                String[] gradeMins = paramMap.get("gradeMin");
                String[] gradeMaxs = paramMap.get("gradeMax");
                String gradeMin = gradeMins != null && gradeMins.length > 0 ? (" l=" + gradeMins[0]) : "";
                String gradeMax = gradeMaxs != null && gradeMaxs.length > 0 ? (" u=" + gradeMaxs[0]) : "";
                if (!("".equals(gradeMin) && "".equals(gradeMax))) {
                    String rangeFq = "{!frange " + gradeMin + gradeMax + "}$GRADE_VALUE";
                    solrQuery.addFilterQuery(rangeFq);
                }
                solrQuery.set("GRADE_VALUE", "sum(" + StringUtils.join(gradeParams, ",") + ")");
                solrQuery.addField("GRADE_VALUE:$GRADE_VALUE");
                if (gradeOrder != null) {
                    solrQuery.addSort("$GRADE_VALUE", gradeOrder);
                } else if (sortList.size() == 0) {
                    solrQuery.addSort("$GRADE_VALUE", SolrQuery.ORDER.desc);
                }
            }
            solrQuery.remove("json.facet");
        }

        return solrQuery;

    }
}
