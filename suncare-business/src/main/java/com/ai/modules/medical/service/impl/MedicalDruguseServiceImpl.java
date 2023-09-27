package com.ai.modules.medical.service.impl;

import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.MD5Util;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.dto.MedicalDruguseIO;
import com.ai.modules.medical.mapper.MedicalClinicalMapper;
import com.ai.modules.medical.mapper.MedicalDruguseMapper;
import com.ai.modules.medical.service.IMedicalDruguseRuleGroupService;
import com.ai.modules.medical.service.IMedicalDruguseService;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.ai.modules.medical.service.IMedicalValidService;
import com.ai.modules.medical.vo.MedicalChargeRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDruguseRuleGroupVO;
import com.ai.modules.medical.vo.MedicalDruguseVO;
import com.ai.modules.medical.vo.MedicalRuleConditionSetVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 合理用药配置
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Service
@Transactional
public class MedicalDruguseServiceImpl extends ServiceImpl<MedicalDruguseMapper, MedicalDruguse> implements IMedicalDruguseService {

//    @Autowired
//    IMedicalDruguseRuleGroupService medicalRuleConditionSetService;

    @Autowired
    IMedicalRuleConditionSetService medicalRuleConditionSetService;

    @Autowired
    IMedicalValidService medicalValidService;

    @Autowired
    MedicalClinicalMapper medicalClinicalMapper;

    @Autowired
    private IMedicalDictService medicalDictService;

    @Autowired
    private IMedicalOtherDictService medicalOtherDictService;

    @Override
    public void save(MedicalDruguseVO medicalDruguse, List<MedicalRuleConditionSet> ruleGroups) {
        String ruleId = IdUtils.uuid();
        medicalDruguse.setRuleId(ruleId);
        this.saveRuleGroup(ruleGroups, ruleId);
        this.save(medicalDruguse);
    }

    @Override
    public void updateById(MedicalDruguseVO medicalDruguse, List<MedicalRuleConditionSet> ruleGroups) {
        String ruleId = medicalDruguse.getRuleId();
        this.delRuleGroup(ruleId);
        this.saveRuleGroup(ruleGroups, ruleId);
        this.updateById(medicalDruguse);
    }

    private boolean saveRuleGroup(List<MedicalRuleConditionSet> ruleGroups, String ruleId){
//        int index = 0;
        for(MedicalRuleConditionSet bean: ruleGroups){
            bean.setId(IdUtils.uuid());
            bean.setRuleId(ruleId);
            bean.setType("judge");
            bean.setField("indication");
            bean.setCompare("=");
            bean.setOrderNo(0);
//            bean.setLogic("OR");
//            bean.setOrderNo(index++);
        }
        return this.medicalRuleConditionSetService.saveBatch(ruleGroups);
    }

    @Override
    public boolean delRuleGroup(String ruleId){
        return medicalRuleConditionSetService.remove(
                new QueryWrapper<MedicalRuleConditionSet>().eq("RULE_ID", ruleId));
    }

    @Override
    public boolean delRuleGroup(List<String> idList){
        return medicalRuleConditionSetService.remove(
                new QueryWrapper<MedicalRuleConditionSet>().in("RULE_ID", idList));
    }

    /*@Override
    public int[] importExcel(MultipartFile file) throws Exception {
        if(true){
            throw new Exception("未完成");
        }
        return new int[0];
    }

    @Override
    public void exportExcel(QueryWrapper<MedicalDruguse> queryWrapper, OutputStream os) throws Exception {
        if(true){
            throw new Exception("未完成");
        }
    }

    @Override
    public void exportInvalid(QueryWrapper<MedicalDruguse> queryWrapper, OutputStream os) throws Exception {
        if(true){
            throw new Exception("未完成");
        }
    }*/

    private String[] beanFields = {"itemCodes", "itemNames","itemTypes", "age", "ageUnit", "sex", "actionName"
            , "group1Name", "group1DiseaseGroup","group1DiseaseGroupName", "group1TreatGroup", "group1TreatGroupName","group1Treatment"
            , "group2Name", "group2DiseaseGroup", "group2DiseaseGroupName", "group2TreatGroup", "group2TreatGroupName","group2Treatment"
            , "group3Name", "group3DiseaseGroup", "group3DiseaseGroupName", "group3TreatGroup", "group3TreatGroupName","group3Treatment"
            , "message", "ruleBasis", "ruleBasisType",};

    private String[] beanFieldsTile = {
            "药品编码",	"药品名称",	"药品来源\n（ATC或DRUG）",	"准入年龄\n（数学范围表达式）"
            ,"年龄单位\n（年，月，日）",	"性别（未知的性别、\n未说明的性别、男性、女性）",	"不合规行为"
            ,"判定条件组1", "疾病组1编码", "疾病组1名称", "项目组1编码", "项目组1名称", "化验结果1"
            ,"判定条件组2", "疾病组2编码", "疾病组2名称", "项目组2编码", "项目组2名称", "化验结果2"
            ,"判定条件组3", "疾病组3编码", "疾病组3名称", "项目组3编码", "项目组3名称", "化验结果3"
            ,"提示信息",	"政策依据",	"政策依据类别"
    };

    private String dealEmpty(String obj){
        return StringUtils.isBlank(obj)?"":obj;
    }

    @Override
    @Transactional
    public int[] importExcel(MultipartFile file) throws Exception {

        List<MedicalDruguseIO> list = ExcelXUtils.readSheet(MedicalDruguseIO.class, beanFields, 0, 1, file.getInputStream());

        Map<String, String> sexDictMap = medicalDictService.queryNameMapByType("GB/T2261.1");
        Map<String, String> ageUnitDictMap = medicalDictService.queryNameMapByType("AGE_UNIT");
        Map<String, String> ruleBasisTypeDictMap = medicalOtherDictService.queryNameMapByType("rule_sourcetype");
        Map<String, String> actionListDictMap = medicalDictService.queryNameMapByType("ACTION_LIST");

        // code
        Set<String> diseaseGroupSet = new HashSet<>();
        Set<String> treatGroupSet = new HashSet<>();
//        Set<String> treatmentGroupSet = new HashSet<>();

        List<MedicalRuleConditionSet> ruleGroupList = new ArrayList<>();

        Map<String, MedicalDruguseIO> codeBeanMap = new HashMap<>();
        for(MedicalDruguseIO bean: list) {
//            String id = IdUtils.uuid();
//            bean.setRuleId(id);
            bean.setActionType("DRUGUSE");
            // 字典翻译
            String sex = bean.getSex();
            String ageUnit = bean.getAgeUnit();
            String ruleBasisType = bean.getRuleBasisType();
            String actionName = bean.getActionName();
            if(StringUtils.isBlank(bean.getItemCodes())){
                throw new Exception("药品编码值不能为空");
            }
            if (sex != null && sex.length() > 0) {
                bean.setSex(sexDictMap.get(sex));
                if (bean.getSex() == null) {
                    throw new Exception("性别值不存在：" + sex);
                }
            }
            if (ageUnit != null && ageUnit.length() > 0) {
                bean.setAgeUnit(ageUnitDictMap.get(ageUnit));
                if (bean.getAgeUnit() == null) {
                    throw new Exception("年龄单位值不存在：" + ageUnit);
                }
            }
            if (ruleBasisType != null && ruleBasisType.length() > 0) {
                bean.setRuleBasisType(ruleBasisTypeDictMap.get(ruleBasisType));
                if (bean.getRuleBasisType() == null) {
                    throw new Exception("政策依据类别不存在：" + ruleBasisType);
                }
            }
            if (actionName != null && actionName.length() > 0) {
                bean.setActionId(actionListDictMap.get(actionName));
                if (bean.getActionId() == null) {
                    throw new Exception("不合规行为不存在：" + ruleBasisType);
                }
            }

            String ruleCodeDecodeBefore = bean.getItemCodes()
                    + "_" + dealEmpty(bean.getActionId())
                    + "_" + dealEmpty(bean.getSex())
                    + "_" + dealEmpty(bean.getAge())
                    + "_" + dealEmpty(bean.getAgeUnit());

            String ruleCode = MD5Util.getMD5(
                    (ruleCodeDecodeBefore).replaceAll(" ","")
            );
            bean.setRuleCode(ruleCode);
            codeBeanMap.put(ruleCode, bean);

        }

        // 根据code匹配ID
        List<String> codeList = Arrays.asList(codeBeanMap.keySet().toArray(new String[0]));
        for(int i = 0,j, len = codeList.size(); i < len; i = j){
            j = i + 1000;
            if(j > len){
                j = len;
            }
            List<Map<String, Object>> codeIdMapList = this.listMaps(new QueryWrapper<MedicalDruguse>()
                    .select("RULE_ID", "RULE_CODE").in("RULE_CODE", codeList.subList(i, j)));
            for(Map<String, Object> map :codeIdMapList){
                String ruleId = map.get("RULE_ID").toString();
                String ruleCode = map.get("RULE_CODE").toString();
                codeBeanMap.get(ruleCode).setRuleId(ruleId);
            }

        }

        List<MedicalDruguse> addList = new ArrayList<>();
        List<MedicalDruguse> updateList = new ArrayList<>();

        for(MedicalDruguseIO bean: list){
            String ruleId = bean.getRuleId();
            if(ruleId == null){
                bean.setRuleId(ruleId = IdUtils.uuid());
                addList.add(bean);
            } else {
                updateList.add(bean);
            }

            MedicalRuleConditionSet ruleGroup = new MedicalRuleConditionSet();
            int orderNo = -1;
            boolean groupIsBlank = true;
            // 组1
            if(StringUtils.isNotBlank(bean.getGroup1DiseaseGroup())){
                ruleGroup.setExt2(bean.getGroup1DiseaseGroup().replaceAll(" ","").replaceAll("，",","));
                String[] diseaseGroups = ruleGroup.getExt2().split("[|,]");
                diseaseGroupSet.addAll(Arrays.asList(diseaseGroups));
                groupIsBlank = false;
            }

            if(StringUtils.isNotBlank(bean.getGroup1TreatGroup())) {
                ruleGroup.setExt3(bean.getGroup1TreatGroup().replaceAll(" ", "").replaceAll("，", ","));
                String[] treatGroups = ruleGroup.getExt3().split("[|,]");
                treatGroupSet.addAll(Arrays.asList(treatGroups));
                groupIsBlank = false;
            }
            if(StringUtils.isNotBlank(bean.getGroup1Treatment())) {
                ruleGroup.setExt4(bean.getGroup1Treatment());
                groupIsBlank = false;
            }

            if(!groupIsBlank){
                orderNo++;
                ruleGroup.setGroupNo(0);
                ruleGroup.setOrderNo(orderNo);
                ruleGroup.setRuleId(ruleId);
                if(StringUtils.isBlank(bean.getGroup1Name())){
                    ruleGroup.setExt1("条件组" + (orderNo + 1));
                }
                ruleGroupList.add(ruleGroup);
                ruleGroup = new MedicalRuleConditionSet();
                groupIsBlank = true;
            }
            // 组2
            if(StringUtils.isNotBlank(bean.getGroup2DiseaseGroup())){
                ruleGroup.setExt2(bean.getGroup2DiseaseGroup().replaceAll(" ","").replaceAll("，",","));
                String[] diseaseGroups = ruleGroup.getExt2().split("[|,]");
                diseaseGroupSet.addAll(Arrays.asList(diseaseGroups));
                groupIsBlank = false;

            }

            if(StringUtils.isNotBlank(bean.getGroup2TreatGroup())) {
                ruleGroup.setExt3(bean.getGroup2TreatGroup().replaceAll(" ", "").replaceAll("，", ","));
                String[] treatGroups = ruleGroup.getExt3().split("[|,]");
                treatGroupSet.addAll(Arrays.asList(treatGroups));
                groupIsBlank = false;

            }
            if(StringUtils.isNotBlank(bean.getGroup2Treatment())) {
                ruleGroup.setExt4(bean.getGroup2Treatment());
                groupIsBlank = false;
            }
            if(!groupIsBlank){
                orderNo++;
                ruleGroup.setGroupNo(0);
                ruleGroup.setOrderNo(orderNo);
                ruleGroup.setRuleId(ruleId);
                if(StringUtils.isBlank(bean.getGroup2Name())){
                    ruleGroup.setExt1("条件组" + (orderNo + 1));
                }
                ruleGroupList.add(ruleGroup);
                ruleGroup = new MedicalRuleConditionSet();
                groupIsBlank = true;
            }
            // 组3
            if(StringUtils.isNotBlank(bean.getGroup3DiseaseGroup())){
                ruleGroup.setExt2(bean.getGroup3DiseaseGroup().replaceAll(" ","").replaceAll("，",","));
                String[] diseaseGroups = ruleGroup.getExt2().split("[|,]");
                diseaseGroupSet.addAll(Arrays.asList(diseaseGroups));
                groupIsBlank = false;
            }

            if(StringUtils.isNotBlank(bean.getGroup3TreatGroup())) {
                ruleGroup.setExt3(bean.getGroup3TreatGroup().replaceAll(" ", "").replaceAll("，", ","));
                String[] treatGroups = ruleGroup.getExt3().split("[|,]");
                treatGroupSet.addAll(Arrays.asList(treatGroups));
                groupIsBlank = false;
            }
            if(StringUtils.isNotBlank(bean.getGroup3Treatment())) {
                ruleGroup.setExt4(bean.getGroup3Treatment());
                groupIsBlank = false;
            }
            if(!groupIsBlank){
                orderNo++;
                ruleGroup.setGroupNo(0);
                ruleGroup.setOrderNo(orderNo);
                ruleGroup.setRuleId(ruleId);
                if(StringUtils.isBlank(bean.getGroup3Name())){
                    ruleGroup.setExt1("条件组" + (orderNo + 1));
                }
                ruleGroupList.add(ruleGroup);
            }

        }

        String[] diseaseGroupCodes = new ArrayList<>(diseaseGroupSet).stream().sorted(String::compareTo).toArray(String[]::new);
        // 验证编码是否存在
        medicalValidService.validDiseaseGroupCodes(diseaseGroupCodes, "疾病组");

        String[] treatGroupCodes = new ArrayList<>(treatGroupSet).stream().sorted(String::compareTo).toArray(String[]::new);
        // 验证编码是否存在
        medicalValidService.validTreatGroupCodes(treatGroupCodes, "项目组");

//        String[] treatmentCodes = new ArrayList<>(treatmentGroupSet).stream().sorted(String::compareTo).toArray(String[]::new);
        // 验证编码是否存在
//        medicalValidService.validTreatmentCodes(treatmentCodes, "项目");


        List<String> updateRuleIds = updateList.stream().map(MedicalDruguse::getRuleId).collect(Collectors.toList());
        for(int i = 0,j, len = updateRuleIds.size(); i < len; i = j){
            j = i + 1000;
            if(j > len){
                j = len;
            }
            this.medicalRuleConditionSetService.remove(new QueryWrapper<MedicalRuleConditionSet>().in("RULE_ID", updateRuleIds.subList(i, j)));

        }

        if(ruleGroupList.size() > 0){
            // 插入数据库
            this.medicalRuleConditionSetService.saveBatch(ruleGroupList);
        }

        if(addList.size() > 0){
            this.saveBatch(addList);
        }
        if(updateList.size() > 0){
            this.updateBatchById(updateList);
        }


        return new int[]{addList.size(),updateList.size()};
    }

    @Override
    public void exportExcel(QueryWrapper<MedicalDruguse> queryWrapper, OutputStream os) throws Exception {

        Map<String, String> sexDictMap = medicalDictService.queryMapByType("GB/T2261.1");
        Map<String, String> ageUnitDictMap = medicalDictService.queryMapByType("AGE_UNIT");
        Map<String, String> ruleBasisTypeDictMap = medicalOtherDictService.queryMapByType("rule_sourcetype");

        List<MedicalDruguseIO> list = this.baseMapper.listIO(queryWrapper);

        Map<String, MedicalDruguseIO> idBeanMap = new HashMap<>();
        for(MedicalDruguseIO bean: list){
            if(StringUtils.isNotBlank(bean.getSex())){
                bean.setSex(sexDictMap.get(bean.getSex()));
            }
            if(StringUtils.isNotBlank(bean.getAgeUnit())) {
                bean.setAgeUnit(ageUnitDictMap.get(bean.getAgeUnit()));
            }
            if(StringUtils.isNotBlank(bean.getRuleBasisType())) {
                bean.setRuleBasisType(ruleBasisTypeDictMap.get(bean.getRuleBasisType()));
            }
            idBeanMap.put(bean.getRuleId(), bean);
        }

        List<String> ruleIds = list.stream().map(MedicalDruguseIO::getRuleId).collect(Collectors.toList());

        List<MedicalRuleConditionSet> conditionSetList = new ArrayList<>();
        // 分页查询条件
        for(int i = 0, j, len = ruleIds.size(); i < len; i = j){
            j = i + 1000;
            if(j > len){
                j = len;
            }
            conditionSetList.addAll(medicalRuleConditionSetService.list(
                    new QueryWrapper<MedicalRuleConditionSet>()
                            .orderByAsc("RULE_ID", "GROUP_NO", "ORDER_NO")
                            .in("RULE_ID" , ruleIds.subList(i, j))
            ));
        }

        // 设置三个条件组值
        if(conditionSetList.size() > 0){
            String ruleId = conditionSetList.get(0).getRuleId();
            List<MedicalRuleConditionSet> sameRuleIdGroup = new ArrayList<>();
            for(MedicalRuleConditionSet bean: conditionSetList){
                if(!ruleId.equals(bean.getRuleId())){
                    MedicalDruguseIO druguse = idBeanMap.get(ruleId);
                    MedicalRuleConditionSet outBean = sameRuleIdGroup.get(0);
                    druguse.setGroup1Name(outBean.getExt1());
                    druguse.setGroup1DiseaseGroup(outBean.getExt2());
                    druguse.setGroup1TreatGroup(outBean.getExt3());
                    druguse.setGroup1Treatment(outBean.getExt4());

                    if(sameRuleIdGroup.size() > 1){
                        outBean = sameRuleIdGroup.get(1);
                        druguse.setGroup2Name(outBean.getExt1());
                        druguse.setGroup2DiseaseGroup(outBean.getExt2());
                        druguse.setGroup2TreatGroup(outBean.getExt3());
                        druguse.setGroup2Treatment(outBean.getExt4());
                    }
                    if(sameRuleIdGroup.size() > 2){
                        outBean = sameRuleIdGroup.get(2);
                        druguse.setGroup3Name(outBean.getExt1());
                        druguse.setGroup3DiseaseGroup(outBean.getExt2());
                        druguse.setGroup3TreatGroup(outBean.getExt3());
                        druguse.setGroup3Treatment(outBean.getExt4());
                    }

                    sameRuleIdGroup.clear();
                    ruleId = bean.getRuleId();
                }
                sameRuleIdGroup.add(bean);
            }
            if(sameRuleIdGroup.size() > 0){
                MedicalDruguseIO druguse = idBeanMap.get(ruleId);
                MedicalRuleConditionSet outBean = sameRuleIdGroup.get(0);
                druguse.setGroup1Name(outBean.getExt1());
                druguse.setGroup1DiseaseGroup(outBean.getExt2());
                druguse.setGroup1TreatGroup(outBean.getExt3());
                druguse.setGroup1Treatment(outBean.getExt4());

                if(sameRuleIdGroup.size() > 1){
                    outBean = sameRuleIdGroup.get(1);
                    druguse.setGroup2Name(outBean.getExt1());
                    druguse.setGroup2DiseaseGroup(outBean.getExt2());
                    druguse.setGroup2TreatGroup(outBean.getExt3());
                    druguse.setGroup2Treatment(outBean.getExt4());
                }
                if(sameRuleIdGroup.size() > 2){
                    outBean = sameRuleIdGroup.get(2);
                    druguse.setGroup3Name(outBean.getExt1());
                    druguse.setGroup3DiseaseGroup(outBean.getExt2());
                    druguse.setGroup3TreatGroup(outBean.getExt3());
                    druguse.setGroup3Treatment(outBean.getExt4());
                }
            }
        }


        List<String> diseaseGroupList = new ArrayList<>();
        List<String> treatGroupList = new ArrayList<>();
//        List<String> treatmentSbList = new ArrayList<>();

        for(MedicalDruguseIO bean: list){
            diseaseGroupList.add(bean.getGroup1DiseaseGroup() + "::" + bean.getGroup2DiseaseGroup() + "::" + bean.getGroup3DiseaseGroup());
            treatGroupList.add(bean.getGroup1TreatGroup()+ "::"+ bean.getGroup2TreatGroup()+ "::"+ bean.getGroup3TreatGroup());
//            treatmentSbList.add(bean.getGroup1Treatment() + "::" + bean.getGroup2Treatment() + "::" + bean.getGroup3Treatment());
        }

        List<String> diseaseGroupTranStr = medicalValidService.transDiseaseGroupCodes(diseaseGroupList, new String[]{"::", "|", ","});
        List<String> treatGroupTranStr = medicalValidService.transTreatGroupCodes(treatGroupList, new String[]{"::", "|", ","});
//        List<String> treatmentTranStr = medicalValidService.transTreatmentCodes(treatmentSbList, new String[]{"::", "|", ","});

        int index = 0;
        String[] trans;
        for(MedicalDruguseIO bean: list){
            trans = (diseaseGroupTranStr.get(index) + "::1").split("::");
            bean.setGroup1DiseaseGroupName(trans[0]);
            bean.setGroup2DiseaseGroupName(trans[1]);
            bean.setGroup3DiseaseGroupName(trans[2]);
            trans = (treatGroupTranStr.get(index) + "::1").split("::");
            bean.setGroup1TreatGroupName(trans[0]);
            bean.setGroup2TreatGroupName(trans[1]);
            bean.setGroup3TreatGroupName(trans[2]);
            /*trans = (treatmentTranStr.get(index) + "::1").split("::");
            bean.setGroup1TreatmentName(trans[0]);
            bean.setGroup2TreatmentName(trans[1]);
            bean.setGroup3TreatmentName(trans[2]);*/
            index++;
        }


        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 导出合理用药配置
        ExportXUtils.exportExl(list,MedicalDruguseIO.class,beanFieldsTile,beanFields,workbook,"合理用药配置");

        workbook.write(os);
        workbook.dispose();
    }


    @Override
    public void exportInvalid(QueryWrapper<MedicalDruguse> queryWrapper, OutputStream os) throws Exception {
        List<MedicalRuleConditionSetVO> ruleGroupVOList = this.medicalRuleConditionSetService.listVOJoinDruguse(queryWrapper);

        // code
        Set<String> diseaseGroupSet = new HashSet<>();
        Set<String> treatGroupSet = new HashSet<>();
//        Set<String> treatmentSet = new HashSet<>();
        for(MedicalRuleConditionSetVO bean: ruleGroupVOList){
            if(StringUtils.isNotBlank(bean.getExt2())){
                String[] diseaseGroups = bean.getExt2().split("[|,]");
                diseaseGroupSet.addAll(Arrays.asList(diseaseGroups));
            }

            if(StringUtils.isNotBlank(bean.getExt3())) {
                String[] treatGroups = bean.getExt3().split("[|,]");
                treatGroupSet.addAll(Arrays.asList(treatGroups));
            }
        }

        // code -> name
//        Map<String, String> diseaseGroupMap = new HashMap<>();
//        Map<String, String> treatGroupMap = new HashMap<>();
//        Map<String, String> treatmentMap = new HashMap<>();

        Set<String> invalidDiseaseGroupSet = new HashSet<>(medicalValidService.invalidDiseaseGroupCodes(diseaseGroupSet.toArray(new String[0])));
        Set<String> invalidTreatGroupSet = new HashSet<>(medicalValidService.invalidTreatGroupCodes(treatGroupSet.toArray(new String[0])));

        List<MedicalRuleConditionSetVO> invalidGroupList = new ArrayList<>();

        for(MedicalRuleConditionSetVO bean: ruleGroupVOList){
            boolean isInvalid = false;
            if(StringUtils.isNotBlank(bean.getExt2())){
                if(invalidDiseaseGroupSet.size() > 0){
                    String[] codes = bean.getExt2().split("[|,]");
                    List<String> list = Arrays.stream(codes).filter(invalidDiseaseGroupSet::contains).collect(Collectors.toList());
                    if(list.size() > 0){
                        isInvalid = true;
                        bean.setExt2(StringUtils.join(list, ","));
                    }else {
                        bean.setExt2(null);
                    }
                } else {
                    bean.setExt2(null);
                }
            }
            if(StringUtils.isNotBlank(bean.getExt3())) {
                if(invalidTreatGroupSet.size() > 0){
                    String[] codes = bean.getExt3().split("[|,]");
                    List<String> list = Arrays.stream(codes).filter(invalidTreatGroupSet::contains).collect(Collectors.toList());
                    if(list.size() > 0){
                        isInvalid = true;
                        bean.setExt3(StringUtils.join(list, ","));
                    } else {
                        bean.setExt3(null);
                    }
                } else {
                    bean.setExt3(null);
                }

            }
            if(isInvalid){
                bean.setOrderNo(bean.getOrderNo() + 1);
                invalidGroupList.add(bean);
            }
        }

        if(invalidGroupList.size() == 0){
            throw new Exception("不存在失效判定条件组");
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 导出失效判定条件组数据
        ExportXUtils.exportExl(invalidGroupList,MedicalRuleConditionSetVO.class,invalidRuleGroupFieldsTitle,ruleGroupFields,workbook,"失效判定条件组");

        workbook.write(os);
        workbook.dispose();

    }

    private String[] invalidRuleGroupFieldsTitle = {
            "药品编码",	"药品名称",
            "判定条件组序号","判定条件组名称","失效疾病组","失效项目组",
    };

    private String[] ruleGroupFields = {"itemCodes","itemNames", "orderNo", "ext1", "ext2", "ext3",};



}
