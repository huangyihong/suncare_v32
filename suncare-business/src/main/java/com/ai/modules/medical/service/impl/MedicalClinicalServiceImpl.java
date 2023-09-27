package com.ai.modules.medical.service.impl;

import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.mapper.MedicalClinicalInfoMapper;
import com.ai.modules.medical.mapper.MedicalClinicalMapper;
import com.ai.modules.medical.mapper.MedicalClinicalRangeGroupMapper;
import com.ai.modules.medical.service.IMedicalClinicalService;
import com.ai.modules.medical.vo.MedicalClinicalIOVO;
import com.ai.modules.medical.vo.MedicalClinicalRangeGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 临床路径主体
 * @Author: jeecg-boot
 * @Date: 2020-03-09
 * @Version: V1.0
 */
@Slf4j
@Service
public class MedicalClinicalServiceImpl extends ServiceImpl<MedicalClinicalMapper, MedicalClinical> implements IMedicalClinicalService {
    @Autowired
    private MedicalClinicalInfoMapper medicalClinicalInfoMapper;

    @Autowired
    private MedicalClinicalRangeGroupMapper medicalClinicalRangeGroupMapper;

    @Autowired
    private IMedicalDictService medicalDictService;

    private String[] infoFieldsTile = {
            // 基础字段
            "临床路径编码","临床路径名称","最低费用（元）","最高费用（元）","最小天数","最大天数",
            // 扩展信息字段
            "适用对象","诊断依据-症状体征","诊断依据-影像学检查","诊断依据-实验室检查","诊断依据-病理","公示年份","标准住院日","进入路径标准","必需的检查项目","可选的检查项目","治疗方案的选择","出院标准","变异及原因分析","临床路径附件","备注","临床路径来源"
    };

    private String[] infoFields = {
            // 基础字段
            "clinicalCode","clinicalName","inhospPayMin","inhospPayMax","inhospDaysMin","inhospDaysMax",
            // 扩展信息字段
            "basisTarget","basisSymptom","basisCtmri","basisLabExam","basisPathology","publicYear","standerdInhosDays","conformStanderd","requiredCheckItem","optionalCheckItem","treatmentOptions","dischargeStandard","variationCauseAnalyse","clinicalFile","remark","clinicalSource"
    };

    private String[] rangeFieldsTitle = {
            "临床路径编码","临床路径名称","组编码","组名称","组序号","医嘱属性代码","医嘱属性名称","必要属性名称","备注"
    };

    private String[] rangeFields = {
            "clinicalCode","","groupCode","groupName","groupNo","","adviceAttr","requireAttr",""
    };


    @Override
    public Double getMaxOrderNo() {
        return this.baseMapper.queryMaxOrder();
    }

    @Override
    @Transactional
    public int[] importExcel(MultipartFile file) throws Exception {
        List<MedicalClinicalIOVO> list = ExcelUtils.readSheet(MedicalClinicalIOVO.class, infoFields, 0, 1, file.getInputStream());
        List<MedicalClinicalRangeGroupVO> drugGroupList = ExcelUtils.readSheet(MedicalClinicalRangeGroupVO.class, rangeFields, 1, 1, file.getInputStream());
        List<MedicalClinicalRangeGroupVO> projectGroupList = ExcelUtils.readSheet(MedicalClinicalRangeGroupVO.class, rangeFields, 2, 1, file.getInputStream());
        // 校验编码空值
        List<Integer> blankIndex = new ArrayList<>();
        int line = 0;
        for(MedicalClinicalRangeGroupVO bean: drugGroupList){
            if(StringUtils.isBlank(bean.getGroupCode())){
                blankIndex.add(line);
            }
            line++;
        }
        if (blankIndex.size() > 0) {
            throw new Exception("药品组编码不能为空,行数：" + StringUtils.join(blankIndex, ","));
        }
        line = 0;
        for(MedicalClinicalRangeGroupVO bean: projectGroupList){
            if(StringUtils.isBlank(bean.getGroupCode())){
                blankIndex.add(line);
            }
            line++;
        }
        if (blankIndex.size() > 0) {
            throw new Exception("服务项目组编码不能为空,行数：" + StringUtils.join(blankIndex, ","));
        }

        // 校验分组是否存在
        List<String> notExistCode = new ArrayList<>();
        // 药品组
        String[] drugGroupCodes = drugGroupList.stream().map(MedicalClinicalRangeGroup::getGroupCode).distinct().sorted().toArray(String[]::new);
        // 排序，跟数据库取的对应
        if (drugGroupCodes.length > 0) {
            List<MedicalCodeNameVO> drugGroupMaps = this.baseMapper.queryGroupCodeIdInCodes(drugGroupCodes, "7");
            int index = 0;
            for (MedicalCodeNameVO map : drugGroupMaps) {
//                String id = (String) map.get("ID");
                String code = drugGroupCodes[index++];
                String mapCode = map.getCode();
                while(!mapCode.equals(code)){
                    notExistCode.add(code);
                    code = drugGroupCodes[index++];
                }
            }
            if (notExistCode.size() > 0) {
                throw new Exception("药品组编码不存在：[" +
                        StringUtils.join(notExistCode, ",") + "]");
            }
        }
        // 服务项目组
        String[] projectGroupCodes = projectGroupList.stream().map(MedicalClinicalRangeGroup::getGroupCode).distinct().sorted().toArray(String[]::new);
        // 排序，跟数据库取的对应
        if (projectGroupCodes.length > 0) {
            List<MedicalCodeNameVO> projectGroupMaps = this.baseMapper.queryGroupCodeIdInCodes(projectGroupCodes, "1");
            int index = 0;
            for (MedicalCodeNameVO map : projectGroupMaps) {
//                String id = (String) map.get("ID");
                String code = projectGroupCodes[index++];
                String mapCode = map.getCode();
                while(!mapCode.equals(code)){
                    notExistCode.add(code);
                    code = projectGroupCodes[index++];
                }
            }
            if (notExistCode.size() > 0) {
                throw new Exception("服务项目组编码不存在：[" +
                        StringUtils.join(notExistCode, ",") + "]");
            }
        }

        // 拼接所有临床路径编码
        String[] clinicalCodes = list.stream().map(MedicalClinicalIOVO::getClinicalCode).distinct().toArray(String[]::new);
//        clinicalCodes.addAll(drugGroupList.stream().map(MedicalClinicalRangeGroupVO::getClinicalCode).collect(Collectors.toList()));
//        clinicalCodes.addAll(projectGroupList.stream().map(MedicalClinicalRangeGroupVO::getClinicalCode).collect(Collectors.toList()));
        // 查找编码对应的id
        List<MedicalCodeNameVO> idCodeList = this.baseMapper.queryIdByCode(clinicalCodes);
        // 构造code-id  Map
        Map<String, String> codeIdMap = new HashMap<>();
        for (MedicalCodeNameVO map : idCodeList) {
            codeIdMap.put(map.getCode(), map.getId());
        }
        // 根据id判断是新增还是覆盖，把新增的code加入到map里
        for (MedicalClinicalIOVO bean : list) {
            String id = codeIdMap.get(bean.getClinicalCode());
            if (id == null) {
                bean.setClinicalId(IdUtils.uuid());
                codeIdMap.put(bean.getClinicalCode(), bean.getClinicalId());
            } else {
                bean.setClinicalId(id);
            }
        }


        Map<String, String> adviceDictMap = medicalDictService.queryNameMapByType("DOC_ADVICE_ATTR");
        Map<String, String> requireDictMap = medicalDictService.queryNameMapByType("REQUIRE_ATTR");

        // 存在编码的列表
        List<MedicalClinicalRangeGroup> rangeGroupList = new ArrayList<>();
        // 不存在编码的列表
        List<MedicalClinicalRangeGroupVO> unFilledGroup = new ArrayList<>();
        for (MedicalClinicalRangeGroupVO group : drugGroupList) {
            String code = group.getClinicalCode();
            String id = codeIdMap.get(code);
            if (id == null) {
                unFilledGroup.add(group);
            } else {
                // 属性转码
                group.setAdviceAttr(adviceDictMap.getOrDefault(group.getAdviceAttr(), group.getAdviceAttr()));
                group.setRequireAttr(requireDictMap.getOrDefault(group.getRequireAttr(),group.getRequireAttr()));
                group.setClinicalId(id);
                group.setGroupType("drug");
                if(group.getGroupNo() == null){
                    group.setGroupNo(0);
                }
                rangeGroupList.add(group);
            }
        }
        for (MedicalClinicalRangeGroupVO group : projectGroupList) {
            // 读取文件的时候code放在id里
            String code = group.getClinicalCode();
            String id = codeIdMap.get(code);
            if (id == null) {
                unFilledGroup.add(group);
            } else {
                // 属性转码
                group.setAdviceAttr(adviceDictMap.getOrDefault(group.getAdviceAttr(), group.getAdviceAttr()));
                group.setRequireAttr(requireDictMap.getOrDefault(group.getRequireAttr(),group.getRequireAttr()));
                group.setClinicalId(id);
                group.setGroupType("project");
                if(group.getGroupNo() == null){
                    group.setGroupNo(0);
                }
                rangeGroupList.add(group);
            }
        }
        if (unFilledGroup.size() > 0) {
            throw new Exception("范围组关联的临床路径编码不存在：" +
                    unFilledGroup.stream()
                            .map(MedicalClinicalRangeGroupVO::getClinicalCode)
                            .collect(Collectors.joining(", ", "[", "]")));
        }

        // 删除 文件里包含的临床路径主体
        List<String> clinicalIds = list.stream().map(MedicalClinicalIOVO::getClinicalId).collect(Collectors.toList());
        this.baseMapper.deleteBatchIds(clinicalIds);
        this.medicalClinicalInfoMapper.deleteBatchIds(clinicalIds);
        // 当前最大序号+1
        Double maxOrder = this.baseMapper.queryMaxOrder();
        int order = maxOrder == null?1:maxOrder.intValue() + 1;
        double orderAdd = 0.00;
        for (MedicalClinicalIOVO bean : list) {
            bean.setDataStatus("1");
            bean.setPublicStatus("1");
            bean.setOrderNo(order + orderAdd);
        /*    bean.setCreateTime(); 自动注入
            bean.setCreateUser();
            bean.setCreateUsername();*/
            this.baseMapper.insert(bean);
            orderAdd += 1.0;
            // 插入扩展信息表
            MedicalClinicalInfo clinicalInfo = new MedicalClinicalInfo();
            BeanUtils.copyProperties(bean,clinicalInfo);
            this.medicalClinicalInfoMapper.insert(clinicalInfo);
        }
        // 删除范围组
        this.medicalClinicalRangeGroupMapper.delete(
                new QueryWrapper<MedicalClinicalRangeGroup>().in("CLINICAL_ID", clinicalIds));
        for (MedicalClinicalRangeGroup rangeGroup : rangeGroupList) {
            this.medicalClinicalRangeGroupMapper.insert(rangeGroup);
        }
        return new int[]{list.size(),drugGroupList.size(),projectGroupList.size()};
    }

    @Override
    public void exportExcel(QueryWrapper<MedicalClinical> queryWrapper, OutputStream os) throws Exception {
        List<MedicalClinicalIOVO> list = this.baseMapper.listWholeInfo(queryWrapper);
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("基础资料", 0);
        ExcelUtils.writeSheets(list,infoFieldsTile,infoFields,sheet,0);

        Map<String, String> codeNameMap = new HashMap<>();
        for(MedicalClinicalIOVO bean: list){
            codeNameMap.put(bean.getClinicalCode(), bean.getClinicalName());
        }

        List<String> clinicalIds = list.stream().map(MedicalClinicalIOVO::getClinicalId).collect(Collectors.toList());

        Map<String, String> adviceDictMap = medicalDictService.queryMapByType("DOC_ADVICE_ATTR");
        Map<String, String> requireDictMap = medicalDictService.queryMapByType("REQUIRE_ATTR");

        QueryWrapper<MedicalClinicalRangeGroup> rangeQueryWrapper = new QueryWrapper<MedicalClinicalRangeGroup>()
                .in("t.CLINICAL_ID",clinicalIds).orderByAsc("CLINICAL_CODE");
        // 药品组
        List<MedicalClinicalRangeGroupVO> drugGroupList = medicalClinicalRangeGroupMapper.listDetailMore(rangeQueryWrapper, "drug");
        // 字典翻译
        drugGroupList.forEach(bean -> {
            bean.setAdviceAttr(adviceDictMap.get(bean.getAdviceAttr()));
            bean.setRequireAttr(requireDictMap.get(bean.getRequireAttr()));
            bean.setClinicalName(codeNameMap.get(bean.getClinicalCode()));
        });

        WritableSheet sheet1 = wwb.createSheet("药品组", 1);
        ExcelUtils.writeSheets(drugGroupList,rangeFieldsTitle,rangeFields,sheet1,0);
        // 服务项目组
        List<MedicalClinicalRangeGroupVO> projectGroupList = medicalClinicalRangeGroupMapper.listDetailMore(rangeQueryWrapper, "project");
        // 字典翻译
        projectGroupList.forEach(bean -> {
            bean.setAdviceAttr(adviceDictMap.get(bean.getAdviceAttr()));
            bean.setRequireAttr(requireDictMap.get(bean.getRequireAttr()));
            bean.setClinicalName(codeNameMap.get(bean.getClinicalCode()));

        });

        WritableSheet sheet2 = wwb.createSheet("医疗服务项目组", 2);
        ExcelUtils.writeSheets(projectGroupList,rangeFieldsTitle,rangeFields,sheet2,0);

        wwb.write();
        wwb.close();
    }


}
