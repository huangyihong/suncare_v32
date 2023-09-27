package com.ai.modules.formal.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.*;
import com.ai.modules.config.service.*;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.formal.dto.MedicalFormalCaseImportDTO;
import com.ai.modules.formal.entity.*;
import com.ai.modules.formal.mapper.*;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.formal.vo.CaseNode;
import com.ai.modules.formal.vo.MedicalFormalCaseBusiVO;
import com.ai.modules.formal.vo.MedicalFormalCaseVO;
import com.ai.modules.his.entity.HisFormalFlowRuleGrade;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.his.entity.HisMedicalFormalFlow;
import com.ai.modules.his.entity.HisMedicalFormalFlowRule;
import com.ai.modules.his.mapper.*;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.service.impl.MedicalRuleConfigCommonServiceImpl;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.mapper.MedicalFlowTemplRuleMapper;
import com.ai.modules.probe.service.IMedicalProbeCaseService;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableListMultimap;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 风控模型正式表
 * @Author: jeecg-boot
 * @Date: 2019-11-26
 * @Version: V1.0
 */
@Service
public class MedicalFormalCaseServiceImpl extends ServiceImpl<MedicalFormalCaseMapper, MedicalFormalCase> implements IMedicalFormalCaseService {

    @Autowired
    MedicalFormalFlowRuleMapper medicalFormalFlowRuleMapper;

    @Autowired
    MedicalFormalFlowMapper medicalFormalFlowMapper;

    @Autowired
    MedicalFormalFlowRuleGradeMapper medicalFormalFlowRuleGradeMapper;

    @Autowired
    MedicalFormalCaseItemRelaMapper medicalFormalCaseItemRelaMapper;

    @Autowired
    IMedicalProbeCaseService medicalProbeCaseService;

    @Autowired
    IMedicalProbeFlowRuleService medicalProbeFlowRuleService;

    @Autowired
    IMedicalDictService medicalDictService;

    @Autowired
    IMedicalTreatProjectService medicalTreatProjectService;

    @Autowired
    IMedicalProjectGroupService medicalProjectGroupService;

    @Autowired
    IMedicalStdAtcService medicalStdAtcService;

    @Autowired
    IMedicalDrugGroupService medicalDrugGroupService;

    @Autowired
    IMedicalChineseDrugService medicalChineseDrugService;

    @Autowired
    IMedicalEquipmentService medicalEquipmentService;

    @Autowired
    private BackMapper backMapper;

    @Autowired
    private CopyMapper copyMapper;

    @Autowired
    HisFormalFlowRuleGradeMapper hisFormalFlowRuleGradeMapper;

    @Autowired
    HisMedicalFormalFlowRuleMapper hisMedicalFormalFlowRuleMapper;

    @Autowired
    HisMedicalFormalFlowMapper hisMedicalFormalFlowMapper;

    @Autowired
    HisMedicalFormalCaseMapper hisMedicalFormalCaseMapper;

    @Autowired
    private MedicalFlowTemplRuleMapper medicalFlowTemplRuleMapper;

    @Autowired
    IMedicalOtherDictService medicalOtherDictService;

    @Autowired
    IMedicalActionDictService medicalActionDictService;


    @Override
    @Transactional
    public void addFormalCase(MedicalFormalCaseVO medicalFormalCase) {
        if (this.baseMapper.insert(medicalFormalCase) > 0) {
            String caseId = medicalFormalCase.getCaseId();
            // 创建NODE节点Bean，插入表
            List<MedicalFormalFlow> flowList = parseJsonFlow(medicalFormalCase.getFlowJson());
            Map<String, String> nodeCodeIdMap = new HashMap<>();
            for (MedicalFormalFlow flow : flowList) {
                flow.setNodeId(IdUtils.uuid());
                flow.setCaseId(caseId);
                medicalFormalFlowMapper.insert(flow);
                nodeCodeIdMap.put(flow.getNodeCode(), flow.getNodeId());
            }
            List<MedicalFormalFlowRule> ruleList = medicalFormalCase.getRules();
            List<MedicalFormalFlowRuleGrade> gradeList = medicalFormalCase.getGrades();
            for (MedicalFormalFlowRule rule : ruleList) {
                rule.setRuleId(IdUtils.uuid());
                rule.setNodeId(nodeCodeIdMap.get(rule.getNodeCode()));
                rule.setCaseId(caseId);
                medicalFormalFlowRuleMapper.insert(rule);
            }
            if(gradeList != null) {
                for (MedicalFormalFlowRuleGrade gradeBean : gradeList) {
                    gradeBean.setGradeId(IdUtils.uuid());
                    gradeBean.setCaseId(caseId);
                    medicalFormalFlowRuleGradeMapper.insert(gradeBean);
                }
            }



            if (medicalFormalCase.getRelaItemIds() != null && medicalFormalCase.getRelaItemIds().size() > 0) {
                String idsStr = StringUtils.join(medicalFormalCase.getRelaItemIds(), ",");
                String namesStr = StringUtils.join(medicalFormalCase.getRelaItemNames(), ",");
                MedicalFormalCaseItemRela bean = new MedicalFormalCaseItemRela();
                bean.setCaseId(caseId);
                bean.setType(medicalFormalCase.getRelaItemType());
                bean.setItemIds(idsStr);
                bean.setItemNames(namesStr);
                medicalFormalCaseItemRelaMapper.insert(bean);
            }

        }

    }

    @Override
    @Transactional
    public void updateFormalCase(MedicalFormalCaseVO medicalFormalCase) throws JeecgBootException {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        MedicalFormalCase caseBean = this.baseMapper.selectOne(
                new QueryWrapper<MedicalFormalCase>()
                        .eq("CASE_ID", medicalFormalCase.getCaseId()));
//                        .eq("CASE_STATUS", "wait"));
        if (caseBean != null) {
            String caseId = caseBean.getCaseId();
            Float oldVersion = caseBean.getCaseVersion();
            //备份
            int backCount = hisMedicalFormalCaseMapper.selectCount(new QueryWrapper<HisMedicalFormalCase>()
                    .eq("CASE_ID", caseId).eq("CASE_VERSION", oldVersion));
            if (backCount == 0) {
                String batchId = "version:" + oldVersion;
                backMapper.backMedicalFormalCaseByCaseid(batchId, caseId);
                backMapper.backMedicalFormalFlowByCaseid(batchId, caseId);
                backMapper.backMedicalFormalFlowRuleByCaseid(batchId, caseId);
                backMapper.backMedicalFormalFlowRuleGradeByCaseid(batchId, caseId);
            }
            BeanUtils.copyProperties(medicalFormalCase, caseBean);
            caseBean.setCaseVersion(caseBean.getCaseVersion() + 0.01f);
            caseBean.setCreateTime(new Date());
            caseBean.setCreateUserid(user.getUsername());
            caseBean.setCreateUsername(user.getRealname());
            medicalFormalCase.setCaseVersion(caseBean.getCaseVersion());
            this.baseMapper.updateById(caseBean);
            // 创建NODE节点Bean，插入表
            List<MedicalFormalFlow> flowList = parseJsonFlow(medicalFormalCase.getFlowJson());
            this.removeFlowByCaseId(caseId);
            for (MedicalFormalFlow flow : flowList) {
                flow.setNodeId(IdUtils.uuid());
                flow.setCaseId(medicalFormalCase.getCaseId());
                medicalFormalFlowMapper.insert(flow);
            }
            List<MedicalFormalFlowRule> ruleList = medicalFormalCase.getRules();
            List<MedicalFormalFlowRuleGrade> gradeList = medicalFormalCase.getGrades();
            this.removeRuleByCaseId(caseId);
            for (MedicalFormalFlowRule rule : ruleList) {
                rule.setRuleId(IdUtils.uuid());
                rule.setCaseId(caseId);
                medicalFormalFlowRuleMapper.insert(rule);
            }

            this.removeGradeByCaseId(caseId);
            if(gradeList != null) {
                for (MedicalFormalFlowRuleGrade gradeBean : gradeList) {
                    gradeBean.setGradeId(IdUtils.uuid());
                    gradeBean.setCaseId(caseId);
                    medicalFormalFlowRuleGradeMapper.insert(gradeBean);
                }
            }
            this.removeRelaItemByCaseId(caseId);
            if (medicalFormalCase.getRelaItemIds() != null && medicalFormalCase.getRelaItemIds().size() > 0) {
                String idsStr = StringUtils.join(medicalFormalCase.getRelaItemIds(), ",");
                String namesStr = StringUtils.join(medicalFormalCase.getRelaItemNames(), ",");
                MedicalFormalCaseItemRela bean = new MedicalFormalCaseItemRela();
                bean.setCaseId(caseId);
                bean.setType(medicalFormalCase.getRelaItemType());
                bean.setItemIds(idsStr);
                bean.setItemNames(namesStr);
                medicalFormalCaseItemRelaMapper.insert(bean);
            }

        } else {
            throw new JeecgBootException("更新项不存在");
        }

    }

    @Override
    @Transactional
    public void submitFormalCase(List<String> ids) throws JeecgBootException {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<MedicalFormalCase> queryWrapper = new QueryWrapper<MedicalFormalCase>().in("CASE_ID", ids).eq("CASE_STATUS", "wait");
        // 创建NODE节点Bean，插入表
        List<MedicalFormalCase> list = this.baseMapper.selectList(queryWrapper);
        List<MedicalFormalFlow> allFlowList = new ArrayList<>();
        List<String> caseIds = new ArrayList<>();
        for (MedicalFormalCase medicalFormalCase : list) {
            caseIds.add(medicalFormalCase.getCaseId());
            List<MedicalFormalFlow> flowList = parseJsonFlow(medicalFormalCase.getFlowJson());
            for (MedicalFormalFlow flow : flowList) {
                flow.setNodeId(IdUtils.uuid());
                flow.setCaseId(medicalFormalCase.getCaseId());
            }
            allFlowList.addAll(flowList);
        }
        // 删除驳回的节点
        this.removeFlowByCaseIds(caseIds);
        // 创建完全部节点后插入，防止报错
        for (MedicalFormalFlow flow : allFlowList) {
            medicalFormalFlowMapper.insert(flow);
        }
        // 更新模型库状态
        MedicalFormalCase medicalFormalCase = new MedicalFormalCase();
        medicalFormalCase.setCaseStatus("submited");
        medicalFormalCase.setSubmitTime(new Date());
        medicalFormalCase.setSubmitUserid(user.getId());
        medicalFormalCase.setSubmitUsername(user.getRealname());
        this.baseMapper.update(medicalFormalCase, queryWrapper);

    }


    private List<MedicalFormalFlow> parseJsonFlow(String jsonFlowStr) {
        JSONObject jsonFlow = (JSONObject) JSONObject.parse(jsonFlowStr);
        JSONArray nodeArray = jsonFlow.getJSONArray("nodeDataArray");
        JSONArray linkArray = jsonFlow.getJSONArray("linkDataArray");
        Map<String, CaseNode> map = new HashMap<>();
        List<String> rootKeys = new ArrayList<>();
        // 初始化树容器，取得所有key值
        for (int i = 0, len = nodeArray.size(); i < len; i++) {
            JSONObject json = nodeArray.getJSONObject(i);
            String key = json.getString("key");
            CaseNode node = new CaseNode();
            node.setKey(key);
            node.setData(json);
            map.put(key, node);
            rootKeys.add(key);
        }
        for (int i = 0, len = linkArray.size(); i < len; i++) {
            JSONObject json = linkArray.getJSONObject(i);
            String from = json.getString("from");
            String to = json.getString("to");
            CaseNode fromNode = map.get(from);
            CaseNode toNode = map.get(to);
            toNode.setParent(fromNode);
            fromNode.addChild(toNode);
            // 节点条件为否
            if (StringUtils.isNotEmpty(json.getString("visible")) && "否".equals(json.get("text"))) {
                toNode.setFromYes(false);
            }
            // 移除有父节点的key
            rootKeys.remove(to);

        }
        List<MedicalFormalFlow> list = new ArrayList<>();
        for (String key : rootKeys) {
            list.addAll(getFlowListFromNodes(map.get(key), null, 1));
        }


        return list;
    }

    private List<MedicalFormalFlow> getFlowListFromNodes(CaseNode caseNode, String parentKey, int order) {
        List<MedicalFormalFlow> list = new ArrayList<>();
        JSONObject json = caseNode.getData();
        String key = caseNode.getKey();

        MedicalFormalFlow flow = new MedicalFormalFlow();
        flow.setNodeCode(key);
        flow.setParamCode(json.getString("param"));
        flow.setNodeType(json.getString("type"));
        flow.setNodeName(json.getString("text"));
        flow.setOrderNo(order++);
        flow.setPrevNodeCode(parentKey);
        flow.setPrevNodeCondition(caseNode.isFromYes() ? "YES" : "NO");

        list.add(flow);
        for (CaseNode node : caseNode.getChildren()) {
            list.addAll(getFlowListFromNodes(node, key, order));
        }
        return list;
    }

    @Override
    @Transactional
    public void removeFormalCaseByIds(List<String> ids) {
        this.baseMapper.deleteBatchIds(ids);
        this.removeRuleByCaseIds(ids);
        this.removeRelaItemByCaseIds(ids);
        // 删除无用备份
        this.hisMedicalFormalCaseMapper.delete(new QueryWrapper<HisMedicalFormalCase>()
                .in("CASE_ID", ids).likeRight("BATCH_ID", "version:"));
        this.hisMedicalFormalFlowMapper.delete(new QueryWrapper<HisMedicalFormalFlow>()
                .in("CASE_ID", ids).likeRight("BATCH_ID", "version:"));
        this.hisMedicalFormalFlowRuleMapper.delete(new QueryWrapper<HisMedicalFormalFlowRule>()
                .in("CASE_ID", ids).likeRight("BATCH_ID", "version:"));
        this.hisFormalFlowRuleGradeMapper.delete(new QueryWrapper<HisFormalFlowRuleGrade>()
                .in("CASE_ID", ids).likeRight("BATCH_ID", "version:"));
    }


    @Override
    @Transactional
    public void removeFormalCaseById(String id) {
        this.baseMapper.deleteById(id);
        this.removeRuleByCaseId(id);
        this.removeRelaItemByCaseId(id);
        // 删除无用备份
        this.hisMedicalFormalCaseMapper.delete(new QueryWrapper<HisMedicalFormalCase>()
                .eq("CASE_ID", id).likeRight("BATCH_ID", "version:"));
        this.hisMedicalFormalFlowMapper.delete(new QueryWrapper<HisMedicalFormalFlow>()
                .eq("CASE_ID", id).likeRight("BATCH_ID", "version:"));
        this.hisMedicalFormalFlowRuleMapper.delete(new QueryWrapper<HisMedicalFormalFlowRule>()
                .eq("CASE_ID", id).likeRight("BATCH_ID", "version:"));
        this.hisFormalFlowRuleGradeMapper.delete(new QueryWrapper<HisFormalFlowRuleGrade>()
                .eq("CASE_ID", id).likeRight("BATCH_ID", "version:"));
    }

    @Override
    public JSONObject getFormalCaseById(String id) {
        MedicalFormalCase medicalFormalCase = this.baseMapper.selectById(id);
        if(medicalFormalCase == null){
            return null;
        }
        List<MedicalFormalFlowRule> ruleList = medicalFormalFlowRuleMapper.selectList(new QueryWrapper<MedicalFormalFlowRule>().eq("CASE_ID", id));
        List<HisMedicalFormalCase> versionList = hisMedicalFormalCaseMapper.selectList(new QueryWrapper<HisMedicalFormalCase>()
                .eq("CASE_ID", id).select("CASE_VERSION").orderByAsc("CASE_VERSION"));

        // 虚拟节点
        List<MedicalFormalFlow> virtualFlowList = this.medicalFormalFlowMapper.selectList(
                new QueryWrapper<MedicalFormalFlow>()
                        .eq("CASE_ID", id)
                        .like("NODE_TYPE", "_v"));
        if(virtualFlowList.size() > 0){
            Map<String, MedicalFormalFlow> idFlowMap = new HashMap<>();
            virtualFlowList.forEach(r -> idFlowMap.put(r.getParamCode(), r));
            List<MedicalFlowTemplRule> templRuleList = medicalFlowTemplRuleMapper.selectList(new QueryWrapper<MedicalFlowTemplRule>().in("NODE_ID", idFlowMap.keySet()));
            ruleList.addAll(templRuleList.stream().map(r -> {
                MedicalFormalFlowRule flowRule = new MedicalFormalFlowRule();
                BeanUtils.copyProperties(r, flowRule);
                MedicalFormalFlow formalFlow = idFlowMap.get(r.getNodeId());
                flowRule.setCaseId(id);
                flowRule.setNodeId(formalFlow.getNodeId());
                flowRule.setNodeCode(formalFlow.getNodeCode());
                return flowRule;
            }).collect(Collectors.toList()));

        }
        JSONObject jsonData = (JSONObject) JSONObject.toJSON(medicalFormalCase);
        jsonData.put("rules", ruleList);
        jsonData.put("versions", versionList.stream().map(HisMedicalFormalCase::getCaseVersion).distinct().toArray(Float[]::new));
        return jsonData;
    }

    private void removeRelaItemByCaseId(String caseId) {
        medicalFormalCaseItemRelaMapper.delete(new QueryWrapper<MedicalFormalCaseItemRela>().eq("CASE_ID", caseId));
    }

    private void removeRelaItemByCaseIds(List<String> ids) {
        medicalFormalCaseItemRelaMapper.delete(new QueryWrapper<MedicalFormalCaseItemRela>().in("CASE_ID", ids));
    }

    private void removeRuleByCaseId(String caseId) {
        medicalFormalFlowRuleMapper.delete(new QueryWrapper<MedicalFormalFlowRule>().eq("CASE_ID", caseId));
    }
    private void removeRuleByCaseIds(List<String> ids) {
        medicalFormalFlowRuleMapper.delete(new QueryWrapper<MedicalFormalFlowRule>().in("CASE_ID", ids));

    }

    private void removeGradeByCaseId(String caseId) {
        medicalFormalFlowRuleGradeMapper.delete(new QueryWrapper<MedicalFormalFlowRuleGrade>().eq("CASE_ID", caseId));
    }

    private void removeFlowByCaseId(String caseId) {
        medicalFormalFlowMapper.delete(new QueryWrapper<MedicalFormalFlow>().eq("CASE_ID", caseId));
    }

    private void removeFlowByCaseIds(List<String> caseIds) {
        medicalFormalFlowMapper.delete(new QueryWrapper<MedicalFormalFlow>().in("CASE_ID", caseIds));
    }

    @Override
    public List<MedicalFormalCaseBusiVO> selectCaseBusiVOPage(IPage page, MedicalFormalCaseBusiVO voParams) {
        return this.baseMapper.selectCaseBusiVOPage(page, voParams);
    }

    @Override
    public List<String> selectCaseIdByBatchId(String batchId) {
        return this.baseMapper.selectCaseIdByBatchId(batchId);
    }

    @Override
    public List<String> selectCaseIdByBusiId(String busiId) {
        return this.baseMapper.selectCaseIdByBusiId(busiId);
    }

    @Override
    public List<String> selectCaseIdByBehaviorId(String behaviorId) {
        return this.baseMapper.selectCaseIdByBehaviorId(behaviorId);
    }

    @Override
    public int importExcel(MultipartFile file) throws Exception {
        String[] fields = {"caseCode", "caseName", "caseRemark", "actionId", "actionName", "actionTypeName", "actionDesc","ruleBasis", "relaItemTypeName", "relaItems"};
        List<MedicalFormalCaseImportDTO> list = ExcelXUtils.readSheet(MedicalFormalCaseImportDTO.class, fields, 0, 1, file.getInputStream());

        Map<String, MedicalFormalCaseImportDTO> codeMap = new HashMap<>();
        for (MedicalFormalCaseImportDTO bean : list) {
            if (StringUtils.isBlank(bean.getCaseCode())) {
                throw new Exception("探查编码不能为空");
            }
            codeMap.put(bean.getCaseCode(), bean);
        }

        Map<String, String> relaDictMap = new HashMap<>();
        List<MedicalDictItemVO> relaDictList = medicalDictService.queryByType("CASE_RELA_TYPE");
        for (MedicalDictItemVO bean : relaDictList) {
            relaDictMap.put(bean.getValue(), bean.getCode());
        }

        Map<String, String> actionTypeMap = new HashMap<>();
        List<MedicalDictItemVO> actionTypeList = medicalDictService.queryByType("ACTION_TYPE");
        for (MedicalDictItemVO bean : actionTypeList) {
            actionTypeMap.put(bean.getValue(), bean.getCode());
        }

        MedicalDict actionTypeDict = medicalDictService.getOne(new QueryWrapper<MedicalDict>()
                .eq("GROUP_CODE", "ACTION_TYPE")
                .eq("KIND", MedicalConstant.DICT_KIND_COMMON));

        Integer actionTypeKeyMax = actionTypeMap.values().stream().mapToInt(v -> {
            int val;
            try {
                val = Integer.parseInt(v);
            } catch (Exception ignored) {
                val = 1;
            }
            return val;
        }).max().getAsInt();
        Long actionTypeListOrderMax = actionTypeList.stream().map(MedicalDictItem::getIsOrder).max((a, b) -> a > b ? 1 : -1).get();


        Date nowTime = new Date();
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        Set<String> caseCodeSet = codeMap.keySet();
        List<MedicalProbeCase> probeCaseList = medicalProbeCaseService.list(new QueryWrapper<MedicalProbeCase>().in("CASE_CODE", caseCodeSet));
        if (probeCaseList.size() < list.size()) {
            Set<String> probeCaseCodes = probeCaseList.stream().map(MedicalProbeCase::getCaseCode).collect(Collectors.toSet());
            List<String> noExistCodeList = caseCodeSet.stream().filter(code -> !probeCaseCodes.contains(code)).collect(Collectors.toList());
            throw new Exception("探查流程编码不存在：" + StringUtils.join(noExistCodeList, " , "));
        }

        // 获取不合规行为Map
        List<String> actionNameList = codeMap.values().stream().map(MedicalFormalCase::getActionName).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<String, String> actionMap = medicalActionDictService.getMapByNames(actionNameList);

        List<MedicalDictItem> addActionTypeDict = new ArrayList<>();
        for (MedicalProbeCase probeCase : probeCaseList) {
            MedicalFormalCaseImportDTO formalCase = codeMap.get(probeCase.getCaseCode());
            formalCase.setFlowJson(probeCase.getFlowJson()
                    .replaceAll("\"type\":\"end\"", "\"type\":\"end\",\"submit\":true"));
            formalCase.setStartTime(new Date(2000, 1, 1));
            formalCase.setEndTime(new Date(2099, 12, 31));
            formalCase.setCaseStatus(MedicalConstant.SWITCH_NORMAL);

            formalCase.setCaseId(IdUtils.uuid());
            formalCase.setCaseVersion(1.0f);
            formalCase.setCreateTime(nowTime);
            formalCase.setCreateUserid(user.getId());
            formalCase.setCreateUsername(user.getRealname());

            formalCase.setCaseCodes(formalCase.getCaseCode());
            formalCase.setCaseNames(formalCase.getCaseName());

            // 设置字典翻译值
            String relaType = relaDictMap.get(formalCase.getRelaItemTypeName());
            formalCase.setRelaItemType(relaType);

            String actionType = actionTypeMap.get(formalCase.getActionTypeName());
            if (StringUtils.isNotBlank(formalCase.getActionTypeName()) && actionType == null) {
                MedicalDictItem dictItem = new MedicalDictItem();
                dictItem.setGroupId(actionTypeDict.getGroupId());
                dictItem.setItemId(IdUtils.uuid());
                dictItem.setIsOrder(++actionTypeListOrderMax);
                dictItem.setCode(String.valueOf(++actionTypeKeyMax));
                dictItem.setValue(formalCase.getActionTypeName());
                addActionTypeDict.add(dictItem);
                actionTypeMap.put(dictItem.getValue(), actionType = dictItem.getCode());
            }
            String actionId = actionMap.get(formalCase.getActionName());
            formalCase.setActionId(actionId);
            formalCase.setActionType(actionType);
            if (StringUtils.isNotBlank(formalCase.getRelaItemTypeName()) && relaType == null) {
                throw new Exception("关联项目类型不存在：" + formalCase.getRelaItemTypeName());
            }
            if (formalCase.getActionId() == null) {
                throw new Exception("不合规行为不存在：" + formalCase.getActionName());
            }
            if (formalCase.getActionType() == null) {
                throw new Exception("不合规行为类型不存在：" + formalCase.getActionTypeName());
            }
            String relaItemCodes = formalCase.getRelaItems();
            if (relaType != null && StringUtils.isNotBlank(relaItemCodes)) {
                // 翻译关联项目编码
                List<String> itemCodes = Arrays.asList(relaItemCodes.replaceAll("，", ",").split(","));
                List<String> itemNames = new ArrayList<>();
                if ("peoject".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalTreatProjectService.listMaps(new QueryWrapper<MedicalTreatProject>()
                            .in("code", itemCodes).select("NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("NAME"))).collect(Collectors.toList());
                } else if ("peojectGroup".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalProjectGroupService.listMaps(new QueryWrapper<MedicalProjectGroup>()
                            .in("GROUP_CODE", itemCodes).select("GROUP_NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("GROUP_NAME"))).collect(Collectors.toList());
                } else if ("drug".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalStdAtcService.listMaps(new QueryWrapper<MedicalStdAtc>()
                            .in("CODE", itemCodes).select("NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("NAME"))).collect(Collectors.toList());
                } else if ("drugGroup".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalDrugGroupService.listMaps(new QueryWrapper<MedicalDrugGroup>()
                            .in("GROUP_CODE", itemCodes).select("GROUP_NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("GROUP_NAME"))).collect(Collectors.toList());
                } else if ("chineseDrug".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalChineseDrugService.listMaps(new QueryWrapper<MedicalChineseDrug>()
                            .in("CODE", itemCodes).select("NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("NAME"))).collect(Collectors.toList());
                } else if ("equipment".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalEquipmentService.listMaps(new QueryWrapper<MedicalEquipment>()
                            .in("PRODUCTCODE", itemCodes).select("PRODUCTNAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("PRODUCTNAME"))).collect(Collectors.toList());

                }

                if (itemNames.size() < itemCodes.size()) {
                    for (int i = itemNames.size(), len = itemCodes.size(); i < len; i++) {
                        itemNames.add(itemCodes.get(i));
                    }
                }
                formalCase.setRelaItemIds(itemCodes);
                formalCase.setRelaItemNames(itemNames);
            } else {
                formalCase.setRelaItemIds(new ArrayList<>());
                formalCase.setRelaItemNames(new ArrayList<>());
            }
            // 设置规则和评分
            List<MedicalFormalFlowRule> ruleList = medicalProbeFlowRuleService.listFlowRule(new QueryWrapper<MedicalFormalFlowRule>().eq("CASE_ID", probeCase.getCaseId()));
            formalCase.setRules(ruleList);
            formalCase.setGrades(new ArrayList<>());
        }


        if (addActionTypeDict.size() > 0) {
            medicalDictService.addItems(addActionTypeDict, actionTypeDict.getGroupCode());
        }

        for (MedicalFormalCaseImportDTO bean : list) {
            this.addFormalCase(bean);
        }

        return list.size();
    }


    private String[] caseInfoTitles = {"模型ID", "模型名称", "模型描述",
            "不合规行为编码","不合规行为名称", "不合规行为类型", "不合规行为释义",
            "政策依据", "关联项目类型", "关联项目", "地区", "模型状态", "操作原因", "规则级别", "级别备注" };
    private String[] caseInfoFields = {"caseId", "caseName", "caseRemark",
            "actionId","actionName", "actionTypeName", "actionDesc",
            "ruleBasis", "relaItemTypeName", "relaItems", "ruleSource" ,"caseStatus", "updateRemark", "ruleGrade", "ruleGradeRemark"};

    @Override
    public void exportCaseInfo(QueryWrapper<MedicalFormalCase> queryWrapper, OutputStream os) throws Exception {


        queryWrapper.select("CASE_ID", "CASE_NAME", "CASE_REMARK", "ACTION_ID", "ACTION_NAME", "ACTION_TYPE_NAME", "ACTION_DESC", "RULE_BASIS", "RULE_SOURCE_CODE" ,"CASE_STATUS", "UPDATE_REMARK", "RULE_GRADE", "RULE_GRADE_REMARK");
        List<MedicalFormalCase> caseList = this.list(queryWrapper);

        List<String> caseIdList = caseList.stream().map(MedicalFormalCase::getCaseId).collect(Collectors.toList());
        // 获取不合规行为Map
        List<String> actionIdList = caseList.stream().map(MedicalFormalCase::getActionId).filter(Objects::nonNull).collect(Collectors.toList());
        Map<String, String> actionMap = medicalActionDictService.getMapByCodes(actionIdList);

        List<MedicalFormalCaseItemRela> relaList = medicalFormalCaseItemRelaMapper.selectList(
                new QueryWrapper<MedicalFormalCaseItemRela>().in("CASE_ID", caseIdList));
        Map<String, MedicalFormalCaseItemRela> relaMap = new HashMap<>();
        for(MedicalFormalCaseItemRela bean: relaList){
            relaMap.put(bean.getCaseId(), bean);
        }

        Map<String, String> relaTypeDictMap = medicalDictService.queryMapByType("CASE_RELA_TYPE");
//        Map<String, String> statusDictMap = medicalDictService.queryMapByType("SWITCH_STATUS");

        List<MedicalFormalCaseImportDTO> exportList = new ArrayList<>(caseIdList.size());
        for(MedicalFormalCase bean: caseList){
            String actionName = actionMap.get(bean.getActionId());
            if(actionName != null){
                bean.setActionName(actionName);
            }

            MedicalFormalCaseImportDTO exportDTO = new MedicalFormalCaseImportDTO();
            BeanUtils.copyProperties(bean, exportDTO);
            MedicalFormalCaseItemRela rela = relaMap.get(exportDTO.getCaseId());
            if(rela != null){
                exportDTO.setRelaItemTypeName(relaTypeDictMap.get(rela.getType()));
                exportDTO.setRelaItems(rela.getItemIds());
            }

            String ruleSource = medicalOtherDictService.getValueByCode("region", exportDTO.getRuleSourceCode());
            exportDTO.setRuleSource(ruleSource == null? "中国": ruleSource);
            // 注解自动翻译
//            exportDTO.setCaseStatus(statusDictMap.getOrDefault(exportDTO.getCaseStatus(), "启用"));

            exportList.add(exportDTO);
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(exportList, MedicalFormalCaseImportDTO.class, caseInfoTitles, caseInfoFields, workbook, "模型信息");

        workbook.write(os);
        workbook.dispose();

    }

    @Override
    public int importCaseInfo(MultipartFile file) throws Exception {

        List<MedicalFormalCaseImportDTO> importList = ExcelXUtils.readSheet(MedicalFormalCaseImportDTO.class, caseInfoFields, 0, 1, file.getInputStream());
        // 查询库中存在id
        List<String> caseIdList = importList.stream().map(MedicalFormalCase::getCaseId).filter(Objects::nonNull).collect(Collectors.toList());
        List<MedicalFormalCase> existCases = this.list(new QueryWrapper<MedicalFormalCase>().in("CASE_ID", caseIdList).select("CASE_ID", "CASE_VERSION"));
        Map<String, MedicalFormalCase> existCaseMap = new HashMap<>();
        existCases.forEach(r -> existCaseMap.put(r.getCaseId(), r));


        Map<String, String> statusMap = medicalDictService.queryNameMapByType("SWITCH_STATUS");
        Map<String, String> relaDictMap = new HashMap<>();
        List<MedicalDictItemVO> relaDictList = medicalDictService.queryByType("CASE_RELA_TYPE");
        for (MedicalDictItemVO bean : relaDictList) {
            relaDictMap.put(bean.getValue(), bean.getCode());
        }

        // 获取不合规行为Map
        List<String> actionNameList = importList.stream().map(MedicalFormalCase::getActionName).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<String, String> actionMap = medicalActionDictService.getMapByNames(actionNameList);

        Map<String, String> actionTypeMap = new HashMap<>();
        List<MedicalDictItemVO> actionTypeList = medicalDictService.queryByType("ACTION_TYPE");
        for (MedicalDictItemVO bean : actionTypeList) {
            actionTypeMap.put(bean.getValue(), bean.getCode());
        }
        /*MedicalDict actionListDict = medicalDictService.getOne(new QueryWrapper<MedicalDict>()
                .eq("GROUP_CODE", "ACTION_LIST")
                .eq("KIND", MedicalConstant.DICT_KIND_COMMON));*/
        MedicalDict actionTypeDict = medicalDictService.getOne(new QueryWrapper<MedicalDict>()
                .eq("GROUP_CODE", "ACTION_TYPE")
                .eq("KIND", MedicalConstant.DICT_KIND_COMMON));

        Integer actionTypeKeyMax = actionTypeMap.values().stream().mapToInt(v -> {
            int val;
            try {
                val = Integer.parseInt(v);
            } catch (Exception ignored) {
                val = 1;
            }
            return val;
        }).max().getAsInt();
        Long actionTypeListOrderMax = actionTypeList.stream().map(MedicalDictItem::getIsOrder).max((a, b) -> a > b ? 1 : -1).get();

        // 归纳更新或插入，同时翻译字典值
        List<MedicalFormalCaseImportDTO> addList = new ArrayList<>();
        List<MedicalFormalCaseImportDTO> updateList = new ArrayList<>();

//        List<MedicalDictItem> addActionListDict = new ArrayList<>();
        List<MedicalDictItem> addActionTypeDict = new ArrayList<>();

        int index = 1;
        for(MedicalFormalCaseImportDTO formalCase: importList){
            String indexMsg = "第" + ++index + "行";
            // 设置字典翻译值
            String relaType = relaDictMap.get(formalCase.getRelaItemTypeName());
            formalCase.setRelaItemType(relaType);

            String actionType = actionTypeMap.get(formalCase.getActionTypeName());
            if (StringUtils.isNotBlank(formalCase.getActionTypeName()) && actionType == null) {
                MedicalDictItem dictItem = new MedicalDictItem();
                dictItem.setGroupId(actionTypeDict.getGroupId());
                dictItem.setItemId(IdUtils.uuid());
                dictItem.setIsOrder(++actionTypeListOrderMax);
                dictItem.setCode(String.valueOf(++actionTypeKeyMax));
                dictItem.setValue(formalCase.getActionTypeName());
                addActionTypeDict.add(dictItem);
                actionTypeMap.put(dictItem.getValue(), actionType = dictItem.getCode());
            }

            if (formalCase.getRuleSource() != null && formalCase.getRuleSource().length() > 0) {
                formalCase.setRuleSourceCode(medicalOtherDictService.getCodeByValue("region", formalCase.getRuleSource()));
            }
            if (formalCase.getRuleSourceCode() == null) {
                throw new Exception(indexMsg + "：所属地区为空或不存在");

            }

            if(StringUtils.isNotBlank(formalCase.getCaseStatus())){
                String status = statusMap.get(formalCase.getCaseStatus());
                if(status == null){
                    throw new Exception(indexMsg + "：模型状态不存在-" + formalCase.getCaseStatus());
                }
                formalCase.setCaseStatus(status);
            } else {
                formalCase.setCaseStatus("normal");
            }

            String actionId = actionMap.get(formalCase.getActionName());
            formalCase.setActionId(actionId);
            formalCase.setActionType(actionType);
            if (StringUtils.isNotBlank(formalCase.getRelaItemTypeName()) && relaType == null) {
                throw new Exception(indexMsg + "：关联项目类型不存在：" + formalCase.getRelaItemTypeName() + "，可选项有：" + StringUtils.join(relaDictMap.keySet(), ","));
            }
            if (formalCase.getActionId() == null) {
                throw new Exception(indexMsg + "：不合规行为不存在：" + formalCase.getActionName());
            }
            if (formalCase.getActionType() == null) {
                throw new Exception(indexMsg + "：不合规行为类型不存在：" + formalCase.getActionTypeName() + "，可选项有：" + StringUtils.join(actionTypeMap.keySet(), ","));
            }
            String relaItemCodes = formalCase.getRelaItems();
            if (relaType != null && StringUtils.isNotBlank(relaItemCodes)) {
                // 翻译关联项目编码
                List<String> itemCodes = Arrays.asList(relaItemCodes.replaceAll("，", ",").split(","));
                List<String> itemNames = new ArrayList<>();
                if ("peoject".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalTreatProjectService.listMaps(new QueryWrapper<MedicalTreatProject>()
                            .in("code", itemCodes).select("NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("NAME"))).collect(Collectors.toList());
                } else if ("peojectGroup".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalProjectGroupService.listMaps(new QueryWrapper<MedicalProjectGroup>()
                            .in("GROUP_CODE", itemCodes).select("GROUP_NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("GROUP_NAME"))).collect(Collectors.toList());
                } else if ("drug".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalStdAtcService.listMaps(new QueryWrapper<MedicalStdAtc>()
                            .in("CODE", itemCodes).select("NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("NAME"))).collect(Collectors.toList());
                } else if ("drugGroup".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalDrugGroupService.listMaps(new QueryWrapper<MedicalDrugGroup>()
                            .in("GROUP_CODE", itemCodes).select("GROUP_NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("GROUP_NAME"))).collect(Collectors.toList());
                } else if ("chineseDrug".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalChineseDrugService.listMaps(new QueryWrapper<MedicalChineseDrug>()
                            .in("CODE", itemCodes).select("NAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("NAME"))).collect(Collectors.toList());
                } else if ("equipment".equals(relaType)) {
                    List<Map<String, Object>> itemList = medicalEquipmentService.listMaps(new QueryWrapper<MedicalEquipment>()
                            .in("PRODUCTCODE", itemCodes).select("PRODUCTNAME"));
                    itemNames = itemList.stream().map(map -> String.valueOf(map.get("PRODUCTNAME"))).collect(Collectors.toList());

                }

                if (itemNames.size() < itemCodes.size()) {
                    for (int i = itemNames.size(), len = itemCodes.size(); i < len; i++) {
                        itemNames.add(itemCodes.get(i));
                    }
                }
                formalCase.setRelaItemIds(itemCodes);
                formalCase.setRelaItemNames(itemNames);
            } else {
                formalCase.setRelaItemIds(new ArrayList<>());
                formalCase.setRelaItemNames(new ArrayList<>());
            }

            String caseId = formalCase.getCaseId();
            if(StringUtils.isBlank(caseId) || existCaseMap.get(caseId) == null){
                addList.add(formalCase);
            } else {
                updateList.add(formalCase);
            }
        }

        if(addList.size() > 0){
            throw new Exception("模型ID不存在：" + addList.stream().map(MedicalFormalCaseImportDTO::getCaseId).filter(Objects::nonNull).collect(Collectors.joining(",")));
        }


        if (addActionTypeDict.size() > 0) {
            medicalDictService.addItems(addActionTypeDict, actionTypeDict.getGroupCode());
        }


        Date createTime = new Date();
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String createUserId = user.getUsername();
        String createUserName = user.getRealname();

        // 更新备份
        for (MedicalFormalCaseImportDTO importDTO: updateList) {
            String caseId = importDTO.getCaseId();
            MedicalFormalCase caseBean = existCaseMap.get(caseId);
            Float oldVersion = caseBean.getCaseVersion();
            //备份
            int backCount = hisMedicalFormalCaseMapper.selectCount(new QueryWrapper<HisMedicalFormalCase>()
                    .eq("CASE_ID", caseId).eq("CASE_VERSION", oldVersion));
            if (backCount == 0) {
                String batchId = "version:" + oldVersion;
                backMapper.backMedicalFormalCaseByCaseid(batchId, caseId);
                backMapper.backMedicalFormalFlowByCaseid(batchId, caseId);
                backMapper.backMedicalFormalFlowRuleByCaseid(batchId, caseId);
                backMapper.backMedicalFormalFlowRuleGradeByCaseid(batchId, caseId);
            }
            this.removeRelaItemByCaseId(caseId);
            if (importDTO.getRelaItemIds() != null && importDTO.getRelaItemIds().size() > 0) {
                String idsStr = StringUtils.join(importDTO.getRelaItemIds(), ",");
                String namesStr = StringUtils.join(importDTO.getRelaItemNames(), ",");
                MedicalFormalCaseItemRela bean = new MedicalFormalCaseItemRela();
                bean.setCaseId(caseId);
                bean.setType(importDTO.getRelaItemType());
                bean.setItemIds(idsStr);
                bean.setItemNames(namesStr);
                medicalFormalCaseItemRelaMapper.insert(bean);
            }
            importDTO.setCreateTime(createTime);
            importDTO.setCreateUserid(createUserId);
            importDTO.setCreateUsername(createUserName);
            this.updateById(importDTO);
        }


        return updateList.size();


    }


    @Transactional
    @Override
    public void copyAdd(String[] caseIds) {
        String suffix = System.currentTimeMillis() + "";
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> createInfo = new HashMap<>();
        createInfo.put("createUserid", loginUser.getId());
        createInfo.put("createUsername", loginUser.getRealname());
        createInfo.put("createTime", new Date());

        copyMapper.copyMedicalFormalCaseByCaseIds(suffix, caseIds, createInfo);
        copyMapper.copyMedicalFormalFlowByCaseIds(suffix, caseIds);
        copyMapper.copyMedicalFormalFlowRuleByCaseIds(suffix, caseIds);
        copyMapper.copyMedicalFormalFlowRuleGradeByCaseIds(suffix, caseIds);
        copyMapper.copyMedicalFormalCaseItemRelaByCaseIds(suffix, caseIds, createInfo);
    }

    @Override
    public void updateActionNameByActionId(String actionId, String actionName) {
        QueryWrapper<MedicalFormalCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ACTION_ID",actionId);
        queryWrapper.ne("ACTION_NAME",actionName);
        this.baseMapper.update(new MedicalFormalCase().setActionName(actionName),queryWrapper);

        QueryWrapper<HisMedicalFormalCase> hisQueryWrapper = new QueryWrapper<>();
        hisQueryWrapper.eq("ACTION_ID",actionId);
        hisQueryWrapper.ne("ACTION_NAME",actionName);
        this.hisMedicalFormalCaseMapper.update(new HisMedicalFormalCase().setActionName(actionName),hisQueryWrapper);
    }
}
