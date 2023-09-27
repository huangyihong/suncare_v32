package com.ai.modules.medical.service.impl;

import com.ai.common.utils.ExcelUtils;
import com.ai.modules.config.entity.*;
import com.ai.modules.config.service.*;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.mapper.MedicalClinicalAccessGroupMapper;
import com.ai.modules.medical.mapper.MedicalClinicalMapper;
import com.ai.modules.medical.service.IMedicalClinicalAccessGroupService;
import com.ai.modules.medical.vo.MedicalClinicalAccessGroupImport;
import com.ai.modules.medical.vo.MedicalClinicalAccessGroupVO;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 临床路径准入条件组
 * @Author: jeecg-boot
 * @Date: 2020-03-09
 * @Version: V1.0
 */
@Service
public class MedicalClinicalAccessGroupServiceImpl extends ServiceImpl<MedicalClinicalAccessGroupMapper, MedicalClinicalAccessGroup> implements IMedicalClinicalAccessGroupService {
    @Autowired
    private MedicalClinicalMapper medicalClinicalMapper;
    @Autowired
    private IMedicalDiseaseGroupService medicalDiseaseGroupService;
    @Autowired
    private IMedicalOperationService medicalOperationService;
    @Autowired
    private IMedicalTreatProjectService medicalTreatProjectService;
    @Autowired
    private IMedicalDrugGroupService medicalDrugGroupService;
    @Autowired
    private IMedicalPathologyService medicalPathologyService;

    @Autowired
    private IMedicalDictService medicalDictService;

    private String[] approveFields = {"clinicalCode", "", "groupNo", "groupName", "patientAgeMin", "patientAgeMax","patientAgeUnit", "hospBelongTo", "", "diseaseGroup", ""
            , "operation", "", "checkItem", "", "checkItemDesc", "drugGroup", "", "pathology", ""};

    private String[] rejectFields = Arrays.copyOf(approveFields,approveFields.length);

/*    private String[] rejectFields = {"clinicalCode", "", "groupNo", "groupName", "patientAgeMin", "patientAgeMax","", "hospBelongTo", "", "diseaseGroup", ""
            , "operation", "", "checkItem", "","checkItemDesc", "drugGroup", "", "pathology", ""};*/

    @Override
    public int importExcel(MultipartFile file) throws Exception {
        List<MedicalClinicalAccessGroupImport> approveList = ExcelUtils.readSheet(MedicalClinicalAccessGroupImport.class, approveFields, 0, 1, file.getInputStream());
        List<MedicalClinicalAccessGroupImport> rejectList = ExcelUtils.readSheet(MedicalClinicalAccessGroupImport.class, rejectFields, 1, 1, file.getInputStream());


        Set<String> approveClinicalCode = approveList.stream().map(MedicalClinicalAccessGroupImport::getClinicalCode).collect(Collectors.toSet());
        Set<String> rejectClinicalCode = rejectList.stream().map(MedicalClinicalAccessGroupImport::getClinicalCode).collect(Collectors.toSet());
        //  获取属性字典项用于验证
        Set<String> ages = medicalDictService.queryByType("AGE_UNIT").stream().map(MedicalDictItemVO::getValue).collect(Collectors.toSet());
        Set<String> belongTos = medicalDictService.queryByType("StartupCorp").stream().map(MedicalDictItemVO::getCode).collect(Collectors.toSet());

        Set<String> allClinicalCode = new HashSet<>();
        allClinicalCode.addAll(approveClinicalCode);
        allClinicalCode.addAll(rejectClinicalCode);

        // 构造code - id 映射 map
        Map<String, String> clinicalCodeIdMap = new HashMap<>();
        List<MedicalClinical> clinicalList = medicalClinicalMapper.selectList(new QueryWrapper<MedicalClinical>().in("CLINICAL_CODE", allClinicalCode));
        for (MedicalClinical clinical : clinicalList) {
            clinicalCodeIdMap.put(clinical.getClinicalCode(), clinical.getClinicalId());
        }
        if (clinicalList.size() != allClinicalCode.size()) {
            List<String> noExistCode = new ArrayList<>();
            for (String code : allClinicalCode) {
                if (clinicalCodeIdMap.get(code) == null) {
                    noExistCode.add(code);
                }
            }
            throw new Exception("临床路径编码不存在：[" + StringUtils.join(noExistCode, ",") + "]");
        }
        // 构造插入数据列表  code - bean map
        Map<String, MedicalClinicalAccessGroupVO> approveGroupMap = new HashMap<>();
        Map<String, MedicalClinicalAccessGroupVO> rejectGroupMap = new HashMap<>();

        for (String code : approveClinicalCode) {
            MedicalClinicalAccessGroupVO groupVO = new MedicalClinicalAccessGroupVO();
            groupVO.setClinicalId(clinicalCodeIdMap.get(code));
            groupVO.setGroupType("approve");
            approveGroupMap.put(code, groupVO);
        }

        for (String code : rejectClinicalCode) {
            MedicalClinicalAccessGroupVO groupVO = new MedicalClinicalAccessGroupVO();
            groupVO.setClinicalId(clinicalCodeIdMap.get(code));
            groupVO.setGroupType("reject");
            rejectGroupMap.put(code, groupVO);
        }

        // bean 添加数据
        for (MedicalClinicalAccessGroupImport groupImport : approveList) {
            MedicalClinicalAccessGroupVO groupVO = approveGroupMap.get(groupImport.getClinicalCode());
            this.initGroup(groupVO, groupImport, ages, belongTos);
        }

        for (MedicalClinicalAccessGroupImport groupImport : rejectList) {
            MedicalClinicalAccessGroupVO groupVO = rejectGroupMap.get(groupImport.getClinicalCode());
            this.initGroup(groupVO, groupImport, ages, belongTos);
        }

        List<MedicalClinicalAccessGroupVO> groupVOList = new ArrayList<>();

        groupVOList.addAll(approveGroupMap.values());
        groupVOList.addAll(rejectGroupMap.values());

        // 验证项目是否存在
        Set<String> diseaseGroupList = new HashSet<>();
        Set<String> operationList = new HashSet<>();
        Set<String> checkItemList = new HashSet<>();
        Set<String> drugGroupList = new HashSet<>();
        Set<String> pathologyList = new HashSet<>();
        groupVOList.forEach(group -> {
            diseaseGroupList.addAll(group.getDiseaseGroupList());
            operationList.addAll(group.getOperationList());
            checkItemList.addAll(group.getCheckItemList());
            drugGroupList.addAll(group.getDrugGroupList());
            pathologyList.addAll(group.getPathologyList());
        });

        if (diseaseGroupList.size() > 0) {
            List<MedicalDiseaseGroup> diseaseGroups = medicalDiseaseGroupService.list(new QueryWrapper<MedicalDiseaseGroup>()
                    .in("GROUP_CODE", diseaseGroupList).select("GROUP_CODE"));
            if (diseaseGroups.size() != diseaseGroupList.size()) {
                List<String> codes = diseaseGroups.stream().map(MedicalDiseaseGroup::getGroupCode).collect(Collectors.toList());
                codes.removeAll(diseaseGroupList);
                throw new Exception("疾病组编码不存在：[" + StringUtils.join(codes, ",") + "]");
            }
        }

        if (operationList.size() > 0) {
            List<MedicalOperation> operations = medicalOperationService.list(new QueryWrapper<MedicalOperation>()
                    .in("CODE", operationList).select("CODE"));
            if (operations.size() != operationList.size()) {
                List<String> codes = operations.stream().map(MedicalOperation::getCode).collect(Collectors.toList());
                codes.removeAll(operationList);
                throw new Exception("手术或操作编码不存在：[" + StringUtils.join(codes, ",") + "]");
            }
        }

        if (checkItemList.size() > 0) {
            List<MedicalTreatProject> checkItems = medicalTreatProjectService.list(new QueryWrapper<MedicalTreatProject>()
                    .in("CODE", checkItemList).select("CODE"));
            if (checkItems.size() != checkItemList.size()) {
                List<String> codes = checkItems.stream().map(MedicalTreatProject::getCode).collect(Collectors.toList());
                codes.removeAll(checkItemList);
                throw new Exception("检查检验项目编码不存在：[" + StringUtils.join(codes, ",") + "]");
            }
        }

        if (drugGroupList.size() > 0) {
            List<MedicalDrugGroup> drugGroups = medicalDrugGroupService.list(new QueryWrapper<MedicalDrugGroup>()
                    .in("GROUP_CODE", drugGroupList).select("GROUP_CODE"));
            if (drugGroups.size() != drugGroupList.size()) {
                List<String> codes = drugGroups.stream().map(MedicalDrugGroup::getGroupCode).collect(Collectors.toList());
                codes.removeAll(drugGroupList);
                throw new Exception("药品组编码不存在：[" + StringUtils.join(codes, ",") + "]");
            }
        }

        if (pathologyList.size() > 0) {
            List<MedicalPathology> pathologys = medicalPathologyService.list(new QueryWrapper<MedicalPathology>()
                    .in("CODE", pathologyList).select("CODE"));
            if (pathologys.size() != pathologyList.size()) {
                List<String> codes = pathologys.stream().map(MedicalPathology::getCode).collect(Collectors.toList());
                codes.removeAll(pathologyList);
                throw new Exception("病理形态编码不存在：[" + StringUtils.join(codes, ",") + "]");
            }
        }

        // 项目数据拼接赋值
        for (MedicalClinicalAccessGroupVO groupVO : groupVOList) {
            if (groupVO.getDiseaseGroupList().size() > 0) {
                groupVO.setDiseaseGroups(StringUtils.join(groupVO.getDiseaseGroupList(), ","));
            }
            if (groupVO.getDrugGroupList().size() > 0) {
                groupVO.setDrugGroups(StringUtils.join(groupVO.getDrugGroupList(), ","));
            }
            if (groupVO.getCheckItemList().size() > 0) {
                groupVO.setCheckItems(StringUtils.join(groupVO.getCheckItemList(), ","));
            }
            if (groupVO.getCheckItemsDescList().size() > 0) {
                groupVO.setCheckItemsDesc(StringUtils.join(groupVO.getCheckItemsDescList(), ","));
            }
            if (groupVO.getOperationList().size() > 0) {
                groupVO.setOperations(StringUtils.join(groupVO.getOperationList(), ","));
            }
            if (groupVO.getPathologyList().size() > 0) {
                groupVO.setPathologys(StringUtils.join(groupVO.getPathologyList(), ","));
            }
        }

        List<MedicalClinicalAccessGroup> groupList = new ArrayList<>(groupVOList);


        List<String> approveClinicalIds = approveClinicalCode.stream().map(clinicalCodeIdMap::get).collect(Collectors.toList());
        List<String> rejectClinicalIds = rejectClinicalCode.stream().map(clinicalCodeIdMap::get).collect(Collectors.toList());
        // 删除数据
        this.remove(new QueryWrapper<MedicalClinicalAccessGroup>()
                .eq("GROUP_TYPE","approve").in("CLINICAL_ID",approveClinicalIds)
                .or(wrapper -> wrapper.eq("GROUP_TYPE","reject").in("CLINICAL_ID",rejectClinicalIds))
        );
        // 插入数据
        this.saveBatch(groupList);

        return groupList.size();

    }

    private void initGroup(MedicalClinicalAccessGroupVO groupVO, MedicalClinicalAccessGroupImport groupImport,Set<String> ages, Set<String> belongTos) throws Exception {
        if (!groupVO.isInit()) {
            groupVO.setInit(true);
            groupVO.setGroupNo(groupImport.getGroupNo());
            groupVO.setGroupName(StringUtils.isBlank(groupImport.getGroupName())?
                    "准入条件组" + (groupImport.getGroupNo() == null? "1": groupImport.getGroupNo()):groupImport.getGroupName());
            groupVO.setPatientAgeMin(groupImport.getPatientAgeMin());
            groupVO.setPatientAgeMax(groupImport.getPatientAgeMax());
            String ageUnit = groupImport.getPatientAgeUnit();
            if(ageUnit != null){
                ageUnit = ageUnit.trim();
                if(!ages.contains(ageUnit)){
                    throw new Exception("年龄单位不存在：" + ageUnit + " ，可选值有：" + StringUtils.join(ages,","));
                }

            } else {
                ageUnit = "年";
            }
            groupVO.setPatientAgeUnit(ageUnit);
            String belongTo = groupImport.getHospBelongTo();
            if(belongTo != null){
                belongTo = belongTo.trim();
                if(!belongTos.contains(belongTo)){
                    throw new Exception("就诊医疗机构隶属关系编码不存在：" + belongTo + " ，可选值有：" + StringUtils.join(belongTos,","));
                }
                groupVO.setHospBelongTo(belongTo);
            }
        }
        if (!StringUtils.isBlank(groupImport.getDiseaseGroup())) {
            groupVO.addDiseaseGroup(groupImport.getDiseaseGroup());
        }
        if (!StringUtils.isBlank(groupImport.getOperation())) {
            groupVO.addOperation(groupImport.getOperation());
        }
        if (!StringUtils.isBlank(groupImport.getCheckItem())) {
            if (groupVO.addCheckItem(groupImport.getCheckItem())) {
                groupVO.addCheckItemsDesc(groupImport.getCheckItemDesc());
            }
        }
        if (!StringUtils.isBlank(groupImport.getDrugGroup())) {
            groupVO.addDrugGroup(groupImport.getDrugGroup());
        }
        if (!StringUtils.isBlank(groupImport.getPathology())) {
            groupVO.addPathology(groupImport.getPathology());
        }
    }
}
