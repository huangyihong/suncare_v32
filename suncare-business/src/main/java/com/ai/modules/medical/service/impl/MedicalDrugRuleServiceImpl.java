package com.ai.modules.medical.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.*;
import com.ai.modules.config.entity.*;
import com.ai.modules.config.service.*;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalDrugRuleId;
import com.ai.modules.medical.entity.vo.MedicalDrugRuleExportVO;
import com.ai.modules.medical.mapper.MedicalDrugRuleMapper;
import com.ai.modules.medical.service.IMedicalDrugRuleIdService;
import com.ai.modules.medical.service.IMedicalDrugRuleService;
import com.ai.modules.medical.vo.MedicalDrugRuleVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description: 药品合规规则
 * @Author: jeecg-boot
 * @Date: 2019-12-19
 * @Version: V1.0
 */
@Service
public class MedicalDrugRuleServiceImpl extends ServiceImpl<MedicalDrugRuleMapper, MedicalDrugRule> implements IMedicalDrugRuleService {

    @Autowired
    private IMedicalDictService medicalDictService;

    @Autowired
    private IMedicalDrugGroupService medicalDrugGroupService;

    @Autowired
    private IMedicalDrugRuleIdService medicalDrugRuleIdService;

    @Autowired
    private IMedicalOtherDictService medicalOtherDictService;

    @Autowired
    private IMedicalOrganService medicalOrganService;

    @Autowired
    private IMedicalDrugService medicalDrugService;

    @Autowired
    private IMedicalStdAtcService medicalStdAtcService;

    @Autowired
    private IMedicalDiseaseGroupItemService diseaseGroupItemService;

    @Autowired
    private IMedicalProjectGroupItemService projectGroupItemService;

    @Autowired
    private IMedicalTreatProjectService medicalTreatProjectService;

    @Autowired
    private IMedicalEquipmentService medicalEquipmentService;

    public void setActionName(MedicalDrugRule bean){
        // 根据限定范围生成不合规行为名称
        List<String> limitScopes = Arrays.asList(bean.getLimitScope().replaceAll(" ","").split(","));
        String actionName = limitScopes.stream()
                .sorted(String::compareTo)
                .map(k -> medicalDictService.queryDictTextByKey("LIMIT_SCOPE", k))
                .collect(Collectors.joining("及", "限", ""));
        bean.setActionName(actionName);
    }

    @Override
    @Transactional
    public void saveMedicalDrugRule(MedicalDrugRule bean) {
        bean.setRuleId(IdUtils.uuid());
        this.setActionName(bean);
        this.baseMapper.insert(bean);
        //插入关联表
        addMedicalDrugRuleId(bean);
    }

    @Override
    @Transactional
    public void updateMedicalDrugRule(MedicalDrugRule bean) {
        this.setActionName(bean);
        this.baseMapper.updateById(bean);
        //插入关联表
        delAndAddMedicalDrugRuleId(bean);
    }

    private void delAndAddMedicalDrugRuleId(MedicalDrugRule bean) {
        medicalDrugRuleIdService.remove(new QueryWrapper<MedicalDrugRuleId>().eq("RULE_ID", bean.getRuleId()));
        addMedicalDrugRuleId(bean);
    }

    private void addMedicalDrugRuleId(MedicalDrugRule bean) {
        String[] drugCodes = new String[0];
        if ("1".equals(bean.getRuleType())) {//药品规则
            drugCodes = bean.getDrugCode().split(",");
        } else if ("2".equals(bean.getRuleType())) {//收费规则
            drugCodes = bean.getChargeItemCodes().split(",");
        } else if ("3".equals(bean.getRuleType())) {//临床路径项目规则
            drugCodes = bean.getDiseaseCodes().split(",");
        } else if ("4".equals(bean.getRuleType())) {//诊疗规则
            drugCodes = bean.getChargeItemCodes().split(",");
        }


        List<MedicalDrugRuleId> list = new ArrayList<>();
        String[] types = StringUtils.isBlank(bean.getDrugTypes()) ? new String[drugCodes.length] :
                bean.getDrugTypes().split(",");

        for (int i = 0, len = drugCodes.length; i < len; i++) {
            MedicalDrugRuleId drugRuleIdBean = new MedicalDrugRuleId();
            drugRuleIdBean.setRuleId(bean.getRuleId());
            drugRuleIdBean.setDrugCode(drugCodes[i]);
            drugRuleIdBean.setDrugType(types[i]);
            list.add(drugRuleIdBean);
        }


        medicalDrugRuleIdService.saveBatch(list);

    }

    @Override
    public void exportExcel(String ruleType, QueryWrapper<MedicalDrugRule> queryWrapper, OutputStream os, String suffix) throws Exception {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<MedicalDrugRuleVO> list = this.baseMapper.selectListVO(queryWrapper, sysUser.getDataSource());
        if ("1".equals(ruleType)) {
            exportDrugRule(os, list, suffix);//药品合规规则导出
        } else if ("2".equals(ruleType)) {
            exportChargeRule(os, list, suffix);//收费合规规则导出
        } else if ("4".equals(ruleType)) {
            exportTreatRule(os, list, suffix);//诊疗合理规则导出
        }
    }

    private void exportDrugRule(OutputStream os, List<MedicalDrugRuleVO> list, String suffix)
            throws IOException, Exception, WriteException {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,YX_药品剂型级别编码,YX_药品名称,限定范围,年龄,年龄下限,是否包含年龄下限,年龄上限,是否包含年龄上限,年龄单位,性别,就医方式,参保类型,适应症编码(疾病组编码),适应症(疾病组名称),禁忌症编码(疾病组编码),禁忌症(疾病组名称),"
                + "二线用药对应的一线用药编码(ATC药品编码),二线用药对应的一线用药名称(ATC药品名称),治疗项目编码(医疗服务项目组),治疗项目名称(医疗服务项目组),医院级别,科室,"
                + "用药量限制,用药量单位,用药时限,时间单位,最大持续使用时间,最大持续时间单位,检验结果项目类型(项目/项目组),项目/项目组编码,项目/项目组名称,定量/定性,定量范围(数学表达式),定量单位,定性选择,"
                + "规则来源,政策依据,治疗用药编码(ATC药品编码),治疗用药名称(ATC药品名称),卫生机构类别,合用不予支付药品编码(ATC药品编码),合用不予支付药品名称(ATC药品名称),"
                + "不能报销,给药途径,相互作用(ATC药品编码),相互作用(ATC药品名称),医嘱,提示信息,数据时间"
                + ",修改时间,修改人,创建时间,创建人,试算状态";
        String[] titles = titleStr.split(",");
        String fieldStr = "ruleId,drugCode,drugNames,limitScope,age,ageLow,ageLowCompare,ageHigh,ageHighCompare,ageUnit,sex,jzlx,yblx,indication,indicationStr,unIndication,unIndicationStr,"
                + "twoLimitDrug,twoLimitDrugStr,treatProject,treatProjectStr,yyjb,office,"
                + "dosageLimit,dosageUnit,takeTimeLimit,timeUnit,maxKeepUseTime,maxKeepTimeUnit,testResultItemType,testResultItemCode,testResultItemName,testResultValueType,testResultValue,testResultUnit,testResultValue2,"
                + "ruleSource,ruleBasis,treatDrug,treatDrugStr,healthOrgKind,twoLimitDrug2,twoLimitDrug2Str,"
                + "unExpense,drugUsage,unfitGroupCodes,unfitGroupCodesStr,docAdvice,message,startEndTimeStr"
                + ",updateTime,updateUsername,createTime,createUsername,trailStatus";//导出的字段
        String[] fields = fieldStr.split(",");
        List<MedicalDrugRuleExportVO> exportList = new ArrayList<MedicalDrugRuleExportVO>();
        for (MedicalDrugRuleVO bean : list) {
            MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
            BeanUtils.copyProperties(bean, dataBean);
            if (StringUtils.isNotBlank(bean.getYblx())) {
                dataBean.setYblx(medicalOtherDictService.getValueByCode("medinsuranceType", bean.getYblx()));
            }
            dataBean.setIndicationStr(getGroupNameStr("5", bean.getIndication()));
            dataBean.setUnIndicationStr(getGroupNameStr("5", bean.getUnIndication()));
            if (StringUtils.isNotBlank(bean.getTwoLimitDrug())) {
                dataBean.setTwoLimitDrugStr(getStdAtcStr(bean.getTwoLimitDrug()));
            }
            dataBean.setTreatProjectStr(getGroupNameStr("1", bean.getTreatProject()));
            if (StringUtils.isNotBlank(bean.getOffice())) {
                dataBean.setOffice(getOtherDictStr("Department", bean.getOffice()));
            }
            if (StringUtils.isNotBlank(bean.getHealthOrgKind())) {
                dataBean.setHealthOrgKind(getOtherDictStr("Medical_Org_type", bean.getHealthOrgKind()));
            }
            //dataBean.setTreatDrugStr(getGroupNameStr("7",bean.getTreatDrug()));
            if (StringUtils.isNotBlank(bean.getTreatDrug())) {
                dataBean.setTreatDrugStr(getStdAtcStr(bean.getTreatDrug()));
            }
            if (StringUtils.isNotBlank(bean.getTwoLimitDrug2())) {
                dataBean.setTwoLimitDrug2Str(getStdAtcStr(bean.getTwoLimitDrug2()));
            }
            if (StringUtils.isNotBlank(bean.getDrugUsage())) {
                dataBean.setDrugUsage(getOtherDictStr("usage", bean.getDrugUsage()));
            }
            if (StringUtils.isNotBlank(bean.getUnfitGroupCodes())) {
                dataBean.setUnfitGroupCodesStr(getStdAtcStr(bean.getUnfitGroupCodes()));
            }
            // 检验结果
            if(StringUtils.isNotBlank(bean.getTestResultItemType())){
                if("ITEM".equals(bean.getTestResultItemType())){
                    dataBean.setTestResultItemType("项目");
                }
                if("GROUP".equals(bean.getTestResultItemType())){
                    dataBean.setTestResultItemType("项目组");
                }
            }
            if(StringUtils.isNotBlank(bean.getTestResultValueType())){
                if("1".equals(bean.getTestResultValueType())){
                    dataBean.setTestResultValueType("定量");
                }
                if("2".equals(bean.getTestResultValueType())){
                    bean.setTestResultValueType("定性");
                    if (StringUtils.isNotBlank(bean.getTestResultValue())) {
                        dataBean.setTestResultValue2(getOtherDictStr("test_tip", bean.getTestResultValue()));
                        dataBean.setTestResultValue("");
                    }
                }
            }
            if (StringUtils.isNotBlank(bean.getTestResultUnit())) {
                dataBean.setTestResultUnit(getOtherDictStr("testvalueunit", bean.getTestResultUnit()));
            }
            dataBean.setTrailStatus(translateMedicalDictValue("RUN_STATUS", bean.getTrailStatus()));
            //年龄处理
            if (StringUtils.isNotBlank(bean.getAge())) {
                if ("<=".equals(bean.getAgeHighCompare())) {
                    dataBean.setAgeHighCompare("是");
                }
                if ("<".equals(bean.getAgeHighCompare())) {
                    dataBean.setAgeHighCompare("否");
                }
                if ("<=".equals(bean.getAgeLowCompare())) {
                    dataBean.setAgeLowCompare("是");
                }
                if ("<".equals(bean.getAgeLowCompare())) {
                    dataBean.setAgeLowCompare("否");
                }
                if (bean.getAgeHigh() == null || bean.getAgeHigh() == -1) {
                    dataBean.setAgeHigh(null);
                    dataBean.setAgeHighCompare(null);
                }
                if (bean.getAgeLow() == null || bean.getAgeLow() == -1) {
                    dataBean.setAgeLow(null);
                    dataBean.setAgeLowCompare(null);
                }
            } else {
                dataBean.setAgeLow(null);
                dataBean.setAgeLowCompare(null);
                dataBean.setAgeHigh(null);
                dataBean.setAgeHighCompare(null);
                dataBean.setAgeUnit(null);
            }
            //数据时间
            dataBean.setStartEndTimeStr(DateUtils.date2Str(bean.getStartTime(),date_sdf)+"到"+DateUtils.date2Str(bean.getEndTime(),date_sdf));
            exportList.add(dataBean);
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, workbook, "药品合规规则");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("药品合规规则", 0);
            ExportUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
    }

    private void exportChargeRule(OutputStream os, List<MedicalDrugRuleVO> list, String suffix)
            throws IOException, Exception, WriteException {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,收费项目编码,收费项目名称,收费分类编码,收费分类名称,数量或频次周期1,数量或频次1,数量或频次周期2,数量或频次2,限定范围,"
                + "合规项目组编码(医疗服务项目组),合规项目组名称(医疗服务项目组),年龄,年龄下限,是否包含年龄下限,年龄上限,是否包含年龄上限,年龄单位,性别,就医方式,参保类型,"
                + "医疗机构(编码),医疗机构(名称),科室,支付时长(就诊周期),支付时长(时长),支付时长(单位),规则来源,政策依据,"
                + "互斥项目组编码(医疗服务项目组),互斥项目组名称(医疗服务项目组),一日互斥项目组编码(医疗服务项目组),一日互斥项目组名称(医疗服务项目组),"
                + "适应症编码(疾病组编码),适应症(疾病组名称),禁忌症编码(疾病组编码),禁忌症(疾病组名称),"
                + "不能报销,不能收费,频率疾病组(频率),频率疾病组(疾病组编码),频率疾病组(疾病组名称),提示信息,数据时间"
                + ",修改时间,修改人,创建时间,创建人,试算状态";
        String[] titles = titleStr.split(",");
        String fieldStr = "ruleId,chargeItemCodes,chargeItems,chargeTypeCodesStr,chargeTypes,period,frequency,twoPeriod,twoFrequency,limitScope,"
                + "fitGroupCodes,fitGroupCodesStr,age,ageLow,ageLowCompare,ageHigh,ageHighCompare,ageUnit,sex,jzlx,yblx,"
                + "org,orgStr,office,payDurationPeriod,payDuration,payDurationUnit,ruleSource,ruleBasis,"
                + "unfitGroupCodes,unfitGroupCodesStr,unfitGroupCodesDay,unfitGroupCodesDayStr,"
                + "indication,indicationStr,unIndication,unIndicationStr,"
                + "unExpense,unCharge,diseasegroupFreq,diseasegroupCodes,diseasegroupCodesStr,message,startEndTimeStr"
                + ",updateTime,updateUsername,createTime,createUsername,trailStatus";//导出的字段

        String[] fields = fieldStr.split(",");

        List<MedicalDrugRuleExportVO> exportList = new ArrayList<MedicalDrugRuleExportVO>();
        for (MedicalDrugRuleVO bean : list) {
            MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
            BeanUtils.copyProperties(bean, dataBean);
            dataBean.setChargeTypeCodesStr(bean.getChargeTypes());
            if (StringUtils.isBlank(bean.getCompare())) {
                bean.setCompare("");
            }
            if (StringUtils.isBlank(bean.getFrequency())) {
                bean.setFrequency("");
            }
            dataBean.setFrequency(bean.getCompare() + bean.getFrequency());
            if (StringUtils.isBlank(bean.getTwoCompare())) {
                bean.setTwoCompare("");
            }
            if (StringUtils.isBlank(bean.getTwoFrequency())) {
                bean.setTwoFrequency("");
            }
            dataBean.setTwoFrequency(bean.getTwoCompare() + bean.getTwoFrequency());
            dataBean.setFitGroupCodesStr(getGroupNameStr("1", bean.getFitGroupCodes()));
            if (StringUtils.isNotBlank(bean.getYblx())) {
                dataBean.setYblx(medicalOtherDictService.getValueByCode("medinsuranceType", bean.getYblx()));
            }

            dataBean.setOrgStr(getMedicalOrgStr(bean.getOrg()));

            dataBean.setUnfitGroupCodesStr(getGroupNameStr("1", bean.getUnfitGroupCodes()));
            dataBean.setUnfitGroupCodesDayStr(getGroupNameStr("1", bean.getUnfitGroupCodesDay()));
            dataBean.setIndicationStr(getGroupNameStr("5", bean.getIndication()));
            dataBean.setUnIndicationStr(getGroupNameStr("5", bean.getUnIndication()));
            dataBean.setTrailStatus(translateMedicalDictValue("RUN_STATUS", bean.getTrailStatus()));

            //年龄处理
            if (StringUtils.isNotBlank(bean.getAge())) {
                if ("<=".equals(bean.getAgeHighCompare())) {
                    dataBean.setAgeHighCompare("是");
                }
                if ("<".equals(bean.getAgeHighCompare())) {
                    dataBean.setAgeHighCompare("否");
                }
                if ("<=".equals(bean.getAgeLowCompare())) {
                    dataBean.setAgeLowCompare("是");
                }
                if ("<".equals(bean.getAgeLowCompare())) {
                    dataBean.setAgeLowCompare("否");
                }
                if (bean.getAgeHigh() == null || bean.getAgeHigh() == -1) {
                    dataBean.setAgeHigh(null);
                    dataBean.setAgeHighCompare(null);
                }
                if (bean.getAgeLow() == null || bean.getAgeLow() == -1) {
                    dataBean.setAgeLow(null);
                    dataBean.setAgeLowCompare(null);
                }
            } else {
                dataBean.setAgeLow(null);
                dataBean.setAgeLowCompare(null);
                dataBean.setAgeHigh(null);
                dataBean.setAgeHighCompare(null);
                dataBean.setAgeUnit(null);
            }
            //频率疾病组
            if ("=".equals(bean.getDiseasegroupFreq())) {
                dataBean.setDiseasegroupFreq("等于");
            }
            if ("<>".equals(bean.getDiseasegroupFreq())) {
                dataBean.setDiseasegroupFreq("不等于");
            }
            dataBean.setDiseasegroupCodesStr(getGroupNameStr("5", bean.getDiseasegroupCodes()));
            dataBean.setStartEndTimeStr(DateUtils.date2Str(bean.getStartTime(),date_sdf)+"到"+DateUtils.date2Str(bean.getEndTime(),date_sdf));
            exportList.add(dataBean);
        }

        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, workbook, "收费合规规则");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("收费合规规则", 0);
            ExportUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
    }

    private void exportTreatRule(OutputStream os, List<MedicalDrugRuleVO> list, String suffix)
            throws IOException, Exception, WriteException {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,诊疗项目编码,诊疗项目名称,诊疗分类编码,诊疗分类名称,数量或频次周期1,数量或频次1,数量或频次周期2,数量或频次2,限定范围,"
                + "合规项目组编码(医疗服务项目组),合规项目组名称(医疗服务项目组),年龄,年龄下限,是否包含年龄下限,年龄上限,是否包含年龄上限,年龄单位,性别,就医方式,参保类型,"
                + "医疗机构(编码),医疗机构(名称),科室,支付时长(就诊周期),支付时长(时长),支付时长(单位),规则来源,政策依据,"
                + "互斥项目组编码(医疗服务项目组),互斥项目组名称(医疗服务项目组),一日互斥项目组编码(医疗服务项目组),一日互斥项目组名称(医疗服务项目组),"
                + "适应症编码(疾病组编码),适应症(疾病组名称),禁忌症编码(疾病组编码),禁忌症(疾病组名称),"
                + "不能报销,不能收费,频率疾病组(频率),频率疾病组(疾病组编码),频率疾病组(疾病组名称),提示信息,数据时间"
                + ",修改时间,修改人,创建时间,创建人,试算状态";
        String[] titles = titleStr.split(",");
        String fieldStr = "ruleId,chargeItemCodes,chargeItems,chargeTypeCodesStr,chargeTypes,period,frequency,twoPeriod,twoFrequency,limitScope,"
                + "fitGroupCodes,fitGroupCodesStr,age,ageLow,ageLowCompare,ageHigh,ageHighCompare,ageUnit,sex,jzlx,yblx,"
                + "org,orgStr,office,payDurationPeriod,payDuration,payDurationUnit,ruleSource,ruleBasis,"
                + "unfitGroupCodes,unfitGroupCodesStr,unfitGroupCodesDay,unfitGroupCodesDayStr,"
                + "indication,indicationStr,unIndication,unIndicationStr,"
                + "unExpense,unCharge,diseasegroupFreq,diseasegroupCodes,diseasegroupCodesStr,message,startEndTimeStr"
                + ",updateTime,updateUsername,createTime,createUsername,trailStatus";//导出的字段

        String[] fields = fieldStr.split(",");

        List<MedicalDrugRuleExportVO> exportList = new ArrayList<MedicalDrugRuleExportVO>();
        for (MedicalDrugRuleVO bean : list) {
            MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
            BeanUtils.copyProperties(bean, dataBean);
            dataBean.setChargeTypeCodesStr(bean.getChargeTypes());
            if (StringUtils.isBlank(bean.getCompare())) {
                bean.setCompare("");
            }
            if (StringUtils.isBlank(bean.getFrequency())) {
                bean.setFrequency("");
            }
            dataBean.setFrequency(bean.getCompare() + bean.getFrequency());
            if (StringUtils.isBlank(bean.getTwoCompare())) {
                bean.setTwoCompare("");
            }
            if (StringUtils.isBlank(bean.getTwoFrequency())) {
                bean.setTwoFrequency("");
            }
            dataBean.setTwoFrequency(bean.getTwoCompare() + bean.getTwoFrequency());
            dataBean.setFitGroupCodesStr(getGroupNameStr("1", bean.getFitGroupCodes()));
            if (StringUtils.isNotBlank(bean.getYblx())) {
                dataBean.setYblx(medicalOtherDictService.getValueByCode("medinsuranceType", bean.getYblx()));
            }

            dataBean.setOrgStr(getMedicalOrgStr(bean.getOrg()));

            dataBean.setUnfitGroupCodesStr(getGroupNameStr("1", bean.getUnfitGroupCodes()));
            dataBean.setUnfitGroupCodesDayStr(getGroupNameStr("1", bean.getUnfitGroupCodesDay()));
            dataBean.setIndicationStr(getGroupNameStr("5", bean.getIndication()));
            dataBean.setUnIndicationStr(getGroupNameStr("5", bean.getUnIndication()));
            dataBean.setTrailStatus(translateMedicalDictValue("RUN_STATUS", bean.getTrailStatus()));

            //年龄处理
            if (StringUtils.isNotBlank(bean.getAge())) {
                if ("<=".equals(bean.getAgeHighCompare())) {
                    dataBean.setAgeHighCompare("是");
                }
                if ("<".equals(bean.getAgeHighCompare())) {
                    dataBean.setAgeHighCompare("否");
                }
                if ("<=".equals(bean.getAgeLowCompare())) {
                    dataBean.setAgeLowCompare("是");
                }
                if ("<".equals(bean.getAgeLowCompare())) {
                    dataBean.setAgeLowCompare("否");
                }
                if (bean.getAgeHigh() == null || bean.getAgeHigh() == -1) {
                    dataBean.setAgeHigh(null);
                    dataBean.setAgeHighCompare(null);
                }
                if (bean.getAgeLow() == null || bean.getAgeLow() == -1) {
                    dataBean.setAgeLow(null);
                    dataBean.setAgeLowCompare(null);
                }
            } else {
                dataBean.setAgeLow(null);
                dataBean.setAgeLowCompare(null);
                dataBean.setAgeHigh(null);
                dataBean.setAgeHighCompare(null);
                dataBean.setAgeUnit(null);
            }
            //频率疾病组
            if ("=".equals(bean.getDiseasegroupFreq())) {
                dataBean.setDiseasegroupFreq("等于");
            }
            if ("<>".equals(bean.getDiseasegroupFreq())) {
                dataBean.setDiseasegroupFreq("不等于");
            }
            dataBean.setDiseasegroupCodesStr(getGroupNameStr("5", bean.getDiseasegroupCodes()));
            dataBean.setStartEndTimeStr(DateUtils.date2Str(bean.getStartTime(),date_sdf)+"到"+DateUtils.date2Str(bean.getEndTime(),date_sdf));
            exportList.add(dataBean);
        }

        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, workbook, "诊疗合理规则");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("诊疗合理规则", 0);
            ExportUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
    }
    private String getGroupNameStr(String kind, String groupCodes) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupCodes)) {
            String codes = groupCodes.replace("|",",");
            Map<String,String> mapData = medicalDrugGroupService.getMapByGroupCode(kind, codes);
            translateGroupArrValue(groupCodes, str, mapData);
        }
        return str.toString();
    }

    private String getOtherDictStr(String dictEname, String groupCodes) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupCodes)) {
            String codes = groupCodes.replace("|",",");
            Map<String,String> mapData = medicalOtherDictService.getMapByCode(dictEname, codes);
            translateGroupArrValue(groupCodes, str, mapData);
        }
        return str.toString();
    }

    private String getOtherDictCodes(String dictEname, String groupValues) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupValues)) {
            String codes = groupValues.replace("|",",");
            Map<String,String> mapData = medicalOtherDictService.getMapByValue(dictEname, codes);
            translateGroupArrValue(groupValues, str, mapData);
        }
        return str.toString();
    }

    private String getMedicalOrgStr(String groupCodes) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupCodes)) {
            String codes = groupCodes.replace("|",",");
            Map<String,String> mapData = medicalOrganService.getMapByCode(codes);
            translateGroupArrValue(groupCodes, str, mapData);
        }
        return str.toString();
    }

    private String getStdAtcStr(String groupCodes) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupCodes)) {
            String codes = groupCodes.replace("|",",");
            Map<String,String> mapData = medicalStdAtcService.getMapByCode(codes);
            translateGroupArrValue(groupCodes, str, mapData);
        }
        return str.toString();
    }



    private String getStdAtcCodes(String groupValues) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupValues)) {
            String values = groupValues.replace("|",",");
            Map<String,String> mapData = medicalStdAtcService.getMapByName(values);
            translateGroupArrValue(groupValues, str, mapData);
        }
        return str.toString();
    }

    private String getMedicalDictCodeStr(Map<String, String> dictMap, String groupCodes) {
        if (dictMap == null) {
            return groupCodes;
        }
        StringBuffer str = new StringBuffer();
        if (StringUtils.isNotBlank(groupCodes)) {
            groupCodes = groupCodes.replace("，", ",");
            translateGroupArrValue(groupCodes.trim(), str, dictMap);
        }
        return str.toString();
    }

    private void translateGroupArrValue(String groupCodes, StringBuffer str, Map<String, String> mapData) {
        String[] groupArr = groupCodes.split("\\|");//组
        int groupNum = 0;
        for (String groupArrStr : groupArr) {
            if (groupNum++ > 0) {
                str.append("|");
            }
            String[] codeArr = groupArrStr.split(",");
            int codeNum = 0;
            for (String code : codeArr) {
                if (codeNum++ > 0) {
                    str.append(",");
                }
                String name = mapData.get(code);
                if (StringUtils.isNotBlank(name)) {
                    str.append(name);
                } else {
                    str.append(code);
                }
            }
        }
    }

    private String translateMedicalDictValue(String code, String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        StringBuilder textValue = new StringBuilder();
        String[] keys_group = key.split("\\|");
        for (int i = 0; i < keys_group.length; i++) {
            String[] keys = keys_group[i].split(",");
            for (int j = 0; j < keys.length; j++) {
                String k = keys[j];
                String tmpValue = null;
                log.debug(" 字典 key : " + k);
                if (k.trim().length() == 0) {
                    continue; //跳过循环
                }
                tmpValue = medicalDictService.queryDictTextByKey(code, k.trim());

                if (tmpValue != null) {
                    textValue.append(tmpValue);
                    if (j != keys.length - 1 && !"".equals(textValue.toString())) {
                        textValue.append(",");
                    }
                }

            }
            if (i != keys_group.length - 1 && !"".equals(textValue.toString())) {
                textValue.append("|");
            }
        }
        return textValue.toString();
    }

    @Override
    @Transactional
    public Result<?> importExcel(MultipartFile file, MultipartHttpServletRequest multipartRequest, LoginUser user)
            throws Exception {
        String ruleType = multipartRequest.getParameter("ruleType");
        if ("1".equals(ruleType)) {
            String mappingFieldStr = "ruleId,drugCode,drugNames,limitScope,age,ageLow,ageLowCompare,ageHigh,ageHighCompare,ageUnit,sex,jzlx,yblx,indication,indicationStr,unIndication,unIndicationStr,"
                    + "twoLimitDrug,twoLimitDrugStr,treatProject,treatProjectStr,yyjb,office,"
                    + "dosageLimit,dosageUnit,takeTimeLimit,timeUnit,maxKeepUseTime,maxKeepTimeUnit,testResultItemType,testResultItemCode,testResultItemName,testResultValueType,testResultValue,testResultUnit,testResultValue2,"
                    + "ruleSource,ruleBasis,treatDrug,treatDrugStr,healthOrgKind,twoLimitDrug2,twoLimitDrug2Str,"
                    + "unExpense,drugUsage,unfitGroupCodes,unfitGroupCodesStr,docAdvice,message,startEndTimeStr,importActionType";//导入的字段
            String[] mappingFields = mappingFieldStr.split(",");
            return importDrugRule(file, user, mappingFields, ruleType);//药品合规规则导入
        } else if ("2".equals(ruleType)) {
            String mappingFieldStr = "ruleId,chargeItemCodes,chargeItems,chargeTypeCodesStr,chargeTypes,period,frequency,twoPeriod,twoFrequency,limitScope,"
                    + "fitGroupCodes,fitGroupCodesStr,age,ageLow,ageLowCompare,ageHigh,ageHighCompare,ageUnit,sex,jzlx,yblx,"
                    + "org,orgStr,office,payDurationPeriod,payDuration,payDurationUnit,ruleSource,ruleBasis,"
                    + "unfitGroupCodes,unfitGroupCodesStr,unfitGroupCodesDay,unfitGroupCodesDayStr,"
                    + "indication,indicationStr,unIndication,unIndicationStr,"
                    + "unExpense,unCharge,diseasegroupFreq,diseasegroupCodes,diseasegroupCodesStr,message,startEndTimeStr,importActionType";//导入的字段
            String[] mappingFields = mappingFieldStr.split(",");
            return importChargeRule(file, user, mappingFields, ruleType);//收费合规规则导入
        } else if ("4".equals(ruleType)) {
            String mappingFieldStr = "ruleId,chargeItemCodes,chargeItems,chargeTypeCodesStr,chargeTypes,period,frequency,twoPeriod,twoFrequency,limitScope,"
                    + "fitGroupCodes,fitGroupCodesStr,age,ageLow,ageLowCompare,ageHigh,ageHighCompare,ageUnit,sex,jzlx,yblx,"
                    + "org,orgStr,office,payDurationPeriod,payDuration,payDurationUnit,ruleSource,ruleBasis,"
                    + "unfitGroupCodes,unfitGroupCodesStr,unfitGroupCodesDay,unfitGroupCodesDayStr,"
                    + "indication,indicationStr,unIndication,unIndicationStr,"
                    + "unExpense,unCharge,diseasegroupFreq,diseasegroupCodes,diseasegroupCodesStr,message,startEndTimeStr,importActionType";//导入的字段
            String[] mappingFields = mappingFieldStr.split(",");
            return importTreatRule(file, user, mappingFields, ruleType);//诊疗合理规则导入
        }
        return Result.error("导入规则类型错误！");

    }

    @Override
    public IPage<com.ai.modules.medical.vo.MedicalDrugRuleVO> pageVO(Page<com.ai.modules.medical.vo.MedicalDrugRuleVO> page,
                                                                     QueryWrapper<MedicalDrugRule> queryWrapper,
                                                                     String dataSource) {
        return this.baseMapper.selectPageVO(page, queryWrapper, dataSource);
    }

    private Result<?> importDrugRule(MultipartFile file, LoginUser user, String[] mappingFields, String ruleType) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalDrugRuleExportVO> list = new ArrayList<MedicalDrugRuleExportVO>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalDrugRuleExportVO.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalDrugRuleExportVO.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始：" + DateUtils.now());
        /**字典数据 start**/
        Map<String, List<MedicalDictItemVO>> dictAllListMap = medicalDictService.queryByTypes("LIMIT_SCOPE,AGE_RANGE,AGE_UNIT,GB/T2261.1,BM_JZBZ00,YYJB,HIS_YLJGZLKM,YESNO,DRUG_TIME_UNIT,SOLR_DATA_SOURCE,ACTION_LIST,ACTION_TYPE".split(","), MedicalConstant.DICT_KIND_COMMON);
        Map<String, Map<String, String>> dictListMap = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> dictCodeListMap = new HashMap<String, Map<String, String>>();
        dictAllListMap.forEach((key, dictList) -> {
            Map<String, String> dictMap = dictCodeListMap.get(key);
            Map<String, String> dictCodeMap = dictListMap.get(key);
            if (dictMap == null) {
                dictMap = new HashMap<String, String>();
            }
            if (dictCodeMap == null) {
                dictCodeMap = new HashMap<String, String>();
            }
            for (MedicalDictItemVO dictBean : dictList) {
                dictMap.put(dictBean.getValue(), dictBean.getCode());
                dictCodeMap.put(dictBean.getCode(), dictBean.getValue());
            }
            dictListMap.put(key, dictMap);
            dictCodeListMap.put(key, dictCodeMap);
        });
        Map<String, String> limitScopeMap = limitScopeMap();

        JSONArray ageJSONArray = this.ageJSONArray();
        /**字典数据 end**/
        String[] importActionTypeArr = {"0","1","2"};


        List<String> importDrugCodes = new ArrayList<String>();
        for (MedicalDrugRuleExportVO beanVO:list) {
            importDrugCodes.addAll(Arrays.asList(beanVO.getDrugCode().split(",")));
        }
        //查找code来源表
        Set<String> drugCodeSet = new HashSet<String>();
        Set<String> stdAtcCodeSet = new HashSet<String>();
        if (importDrugCodes.size() > 0) {
            List<HashSet<String>> codesSetList = getRuleIdSetList(importDrugCodes,1000);
            if (codesSetList.size() > 0) {
                for (HashSet<String> codesSet : codesSetList) {
                    List<MedicalDrug> drugList = this.medicalDrugService.list(new QueryWrapper<MedicalDrug>().in("CODE", codesSet).eq("STATE", MedicalAuditLogConstants.STATE_YX));
                    for(MedicalDrug bean:drugList){
                        drugCodeSet.add(bean.getCode());
                    }
                    List<MedicalStdAtc> stdAtcList = this.medicalStdAtcService.list(new QueryWrapper<MedicalStdAtc>().in("CODE", codesSet).eq("STATE", MedicalAuditLogConstants.STATE_YX));
                    for(MedicalStdAtc bean:stdAtcList){
                        stdAtcCodeSet.add(bean.getCode());
                    }
                }
            }
        }

        List<MedicalDrugRule> addUpdateList = new ArrayList<MedicalDrugRule>();
        List<String> deleteRuleList = new ArrayList<String>();//删除  主表id
        List<MedicalDrugRuleId> addRuleIdList = new ArrayList<MedicalDrugRuleId>();
        List<String> deleteRuleIdList = new ArrayList<String>();//修改和删除的  关联表记录删除
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalDrugRuleExportVO beanVO = list.get(i);
            if (StringUtils.isBlank(beanVO.getDrugCode())) {
                message += "导入的数据中“YX_药品剂型级别编码”不能为空，如：第" + (i + 2) + "行数据“YX_药品剂型级别编码”为空\n";
                flag = false;
            }
		   /* if (StringUtils.isBlank(beanVO.getLimitScope())) {
		        message += "导入的数据中“限定范围”不能为空，如：第" + (i + 2) + "行数据“限定范围”为空\n";
		    	flag = false;
		    }*/
            /*if (StringUtils.isBlank(beanVO.getActionType())) {
                message += "导入的数据中“不合规行为类型”不能为空，如：第" + (i + 2) + "行数据“不合规行为类型”为空\n";
                flag = false;
            }*/
            if (StringUtils.isBlank(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }

            if (!Arrays.asList(importActionTypeArr).contains(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }

            if(StringUtils.isNotBlank(beanVO.getStartEndTimeStr())){
                if(beanVO.getStartEndTimeStr().split("到").length!=2){
                    message += "导入的数据中“数据时间”格式无法识别，正确格式为：yyyy-MM-ddd到yyyy-MM-dd，如：第" + (i + 2) + "行数据“数据时间”为"+beanVO.getStartEndTimeStr()+"\n";
                    flag = false;
                }
            }

            if (!flag) {
                continue;
            }

            if ("1".equals(beanVO.getImportActionType())) {//新增
                beanVO.setRuleId(IdUtils.uuid());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                if (StringUtils.isBlank(beanVO.getRuleId())) {
                    message += "导入的数据中“id主键”不能为空，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                    continue;
                }
                //判断数据是否存在
                MedicalDrugRule oldBean = this.baseMapper.selectById(beanVO.getRuleId());
                if (oldBean == null) {
                    message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                } else {
                    beanVO.setRuleId(oldBean.getRuleId());
                    beanVO.setCreateTime(oldBean.getCreateTime());
                    beanVO.setCreateUser(oldBean.getCreateUser());
                    beanVO.setCreateUsername(oldBean.getCreateUsername());
                }
            }

            if (!flag) {
                continue;
            }

            if (StringUtils.isNotBlank(beanVO.getLimitScope())) {
                beanVO.setLimitScope(beanVO.getLimitScope().replace("适用症","适应症"));//适用症替换为适应症
            }

            // 检验结果
            if(StringUtils.isNotBlank(beanVO.getTestResultItemType())){
                if("项目".equals(beanVO.getTestResultItemType())){
                    beanVO.setTestResultItemType("ITEM");
                }
                if("项目组".equals(beanVO.getTestResultItemType())){
                    beanVO.setTestResultItemType("GROUP");
                }
            }
            if(StringUtils.isNotBlank(beanVO.getTestResultValueType())){
                if("定量".equals(beanVO.getTestResultValueType())){
                    beanVO.setTestResultValueType("1");
                }
                if("定性".equals(beanVO.getTestResultValueType())){
                    beanVO.setTestResultValueType("2");
                    if (StringUtils.isNotBlank(beanVO.getTestResultValue2())) {
                        beanVO.setTestResultValue(getOtherDictCodes("test_tip", beanVO.getTestResultValue()));
                    }
                }
            }
            if (StringUtils.isNotBlank(beanVO.getTestResultUnit())) {
                beanVO.setTestResultUnit(getOtherDictCodes("testvalueunit", beanVO.getTestResultUnit()));
            }

            MedicalDrugRule bean = beanVO;
            //限定范围值等字典值转换
            bean.setLimitScope(getMedicalDictCodeStr(dictListMap.get("LIMIT_SCOPE"), bean.getLimitScope()));
            bean.setAge(getMedicalDictCodeStr(dictListMap.get("AGE_RANGE"), bean.getAge()));
            bean.setAgeUnit(getMedicalDictCodeStr(dictListMap.get("AGE_UNIT"), bean.getAgeUnit()));
            bean.setSex(getMedicalDictCodeStr(dictListMap.get("GB/T2261.1"), bean.getSex()));
            bean.setJzlx(getMedicalDictCodeStr(dictListMap.get("BM_JZBZ00"), bean.getJzlx()));
            bean.setYyjb(getMedicalDictCodeStr(dictListMap.get("YYJB"), bean.getYyjb()));
            //bean.setOffice(getMedicalDictCodeStr(dictListMap.get("HIS_YLJGZLKM"), bean.getOffice()));
            bean.setTimeUnit(getMedicalDictCodeStr(dictListMap.get("DRUG_TIME_UNIT"), bean.getTimeUnit()));
            bean.setMaxKeepTimeUnit(getMedicalDictCodeStr(dictListMap.get("DRUG_TIME_UNIT"), bean.getMaxKeepTimeUnit()));
            bean.setUnExpense(getMedicalDictCodeStr(dictListMap.get("YESNO"), bean.getUnExpense()));

            if (StringUtils.isNotBlank(bean.getYblx())) {
                bean.setYblx(medicalOtherDictService.getCodeByValue("medinsuranceType", bean.getYblx()));
            }
            bean.setDrugUsage(getOtherDictCodes("usage", bean.getDrugUsage()));
            bean.setOffice(getOtherDictCodes("Department", bean.getOffice()));
            //bean.setActionId(getMedicalDictCodeStr(dictListMap.get("ACTION_LIST"), bean.getActionId()));
            //bean.setActionType(getMedicalDictCodeStr(dictListMap.get("ACTION_TYPE"), bean.getActionType()));
            //if(StringUtils.isBlank(bean.getActionType())){
            bean.setActionType("DRUG");//固定为DRUG
            //}
            this.setActionName(bean);//不合规行为名称

            if(StringUtils.isBlank(beanVO.getStartEndTimeStr())){
                bean.setStartTime(DateUtils.str2Date("2000-01-01",date_sdf));
                bean.setEndTime(DateUtils.str2Date("2099-12-31",date_sdf));
            }else{
                String[] startAndEndTime = beanVO.getStartEndTimeStr().split("到");
                try {
                    bean.setStartTime(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                    bean.setEndTime(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                }catch (Exception e) {
                    message += "导入的数据中“数据时间”格式不正确，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }


            //替换空格换行制表符
            bean.setDrugCode(replaceBlank(bean.getDrugCode()));
            bean.setDrugNames(replaceBlank(bean.getDrugNames()));
            bean.setIndication(replaceBlank(bean.getIndication()));
            bean.setUnIndication(replaceBlank(bean.getUnIndication()));
            bean.setTwoLimitDrug(replaceBlank(bean.getTwoLimitDrug()));
            bean.setTreatProject(replaceBlank(bean.getTreatProject()));
            bean.setTreatDrug(replaceBlank(bean.getTreatDrug()));
            bean.setTwoLimitDrug2(replaceBlank(bean.getTwoLimitDrug2()));
            bean.setUnfitGroupCodes(replaceBlank(bean.getUnfitGroupCodes()));

            bean.setRuleType(ruleType);

            if (!"2".equals(beanVO.getImportActionType())) {//删除不判断
                JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
                String limitScope = "";
                for (Map.Entry<String, String> entry : limitScopeMap.entrySet()) {
                    //选项不为空
                    if (StringUtils.isNotBlank(jsonBean.getString(entry.getValue()))) {
                        limitScope += entry.getKey() + ",";
                    }
                }
                if (StringUtils.isNotBlank(limitScope)) {
                    bean.setLimitScope(limitScope.substring(0, limitScope.length() - 1));
                }
                //判断选择了限定范围类型 具体的类型值是否重复等
                for (String limitCode : bean.getLimitScope().split(",")) {
                    String field = limitScopeMap.get(limitCode);
                    if (StringUtils.isBlank(field)) {
                        continue;
                    }
			    	/*//选项不能为空
			    	if(StringUtils.isNotBlank(field)&&StringUtils.isBlank(jsonBean.getString(field))) {
			    		message += "导入的数据中限定范围包含“"+dictCodeListMap.get("LIMIT_SCOPE").get(limitCode)+"”,“"+dictCodeListMap.get("LIMIT_SCOPE").get(limitCode)+"”不能为空，如：第" + (i + 2) + "行数据\n";
				    	flag = false;
				    	break;
			    	}*/
                    //选项不能重复
                    String[] groupArr = jsonBean.getString(field).split("\\|");//组
                    for (String groupValue : groupArr) {
                        String[] fieldArr = groupValue.split(",");
                        if (!cheakRepeat(fieldArr)) {
                            message += "导入的数据中“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”组之间重复，如：第" + (i + 2) + "行数据\n";
                            flag = false;
                            break;
                        }
                    }
                    Map<String,MedicalDrugRule> map = new HashMap<String,MedicalDrugRule>();

                    //年龄
                    if ("01".equals(limitCode)) {
                        if ("-1".equals(bean.getAge())) {//自定义
                            if (StringUtils.isBlank(bean.getAgeUnit())) {
                                message += "导入的数据中年龄为自定义，“年龄单位”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if (bean.getAgeLow() != null && bean.getAgeLow() != -1 && StringUtils.isBlank(bean.getAgeLowCompare())) {
                                message += "导入的数据中有年龄下限值，“年龄下限比较符”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if (bean.getAgeHigh() != null && bean.getAgeHigh() != -1 && StringUtils.isBlank(bean.getAgeHighCompare())) {
                                message += "导入的数据中有年龄上限值，“年龄上限比较符”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if ("是".equals(bean.getAgeLowCompare())) {
                                bean.setAgeLowCompare("<=");
                            }
                            if ("否".equals(bean.getAgeLowCompare())) {
                                bean.setAgeLowCompare("<");
                            }
                            if ("是".equals(bean.getAgeHighCompare())) {
                                bean.setAgeHighCompare("<=");
                            }
                            if ("否".equals(bean.getAgeHighCompare())) {
                                bean.setAgeHighCompare("<");
                            }
                        }else{
                            int age = Integer.parseInt(bean.getAge());
                            JSONObject ageObj = ageJSONArray.getJSONObject(age-1);
                            if(ageObj!=null){
                                bean.setAgeLow(Integer.parseInt(ageObj.getString("value").replace("[", "").split(",")[0]));
                                bean.setAgeHigh(Integer.parseInt(ageObj.getString("value").replace("]", "").split(",")[1]));
                                bean.setAgeLowCompare(ageObj.getString("compare").replace("[", "").replace("\"", "").split(",")[0]);
                                bean.setAgeHighCompare(ageObj.getString("compare").replace("]", "").replace("\"", "").split(",")[1]);
                                bean.setAgeUnit(ageObj.getString("unit"));
                            }else{
                                message += "导入的数据中年龄在系统没有配置上下限值，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        break;
                    }
                }
            }
            if (!flag) {
                continue;
            }

            //生成新增的addRuleIdList
            if ("1".equals(beanVO.getImportActionType()) || "0".equals(beanVO.getImportActionType())) {//新增、修改
                List<String> drugTypesList = new ArrayList<String>();
                for (String drugCode : bean.getDrugCode().split(",")) {
                    String drugType = "";
                    if(drugCodeSet.contains(drugCode)){
                        drugType = "DRUG";
                    }else if(stdAtcCodeSet.contains(drugCode)){
                        drugType = "ATC";
                    }else{
                        message += "导入的数据中“YX_药品剂型级别编码”在基础数据中不存在，如：第" + (i + 2) + "行数据的编码"+drugCode+"\n";
                        flag = false;
                        break;
                    }
                    drugTypesList.add(drugType);
                    MedicalDrugRuleId drugRuleIdBean = new MedicalDrugRuleId();
                    drugRuleIdBean.setRuleId(bean.getRuleId());
                    drugRuleIdBean.setDrugCode(drugCode);
                    drugRuleIdBean.setDrugType(drugType);
                    addRuleIdList.add(drugRuleIdBean);
                }
                bean.setDrugTypes(StringUtils.join(drugTypesList.toArray(), ","));
                addUpdateList.add(bean);
            }
            if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                deleteRuleIdList.add(beanVO.getRuleId());//修改、删除时  删除关联表
                if ("2".equals(beanVO.getImportActionType())) {
                    deleteRuleList.add(beanVO.getRuleId());//删除时 删除主表
                }
            }
        }

        if (StringUtils.isNotBlank(message)) {
            message += "请核对数据后进行导入。";
            System.out.println(message);
            return Result.error(message);
        } else {
            System.out.println("开始插入时间：" + DateUtils.now());
            //删除关联表
            if (deleteRuleIdList.size() > 0) {
                List<HashSet<String>> ruleIdSetList = getRuleIdSetList(deleteRuleIdList,100);
                if (ruleIdSetList.size() > 0) {
                    for (HashSet<String> idsSet : ruleIdSetList) {
                        this.medicalDrugRuleIdService.remove(new QueryWrapper<MedicalDrugRuleId>().in("RULE_ID", idsSet));
                    }
                }
            }
            //删除主表
            if (deleteRuleList.size() > 0) {
                List<HashSet<String>> ruleIdSetList = getRuleIdSetList(deleteRuleList,100);
                if (ruleIdSetList.size() > 0) {
                    for (HashSet<String> idsSet : ruleIdSetList) {
                        this.baseMapper.delete(new QueryWrapper<MedicalDrugRule>().in("RULE_ID", idsSet));
                    }
                }
            }

            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 100);//直接插入
            }
            if (addRuleIdList.size() > 0) {
                this.medicalDrugRuleIdService.saveBatch(addRuleIdList);
            }
            System.out.println("结束导入时间：" + DateUtils.now());
            message += "导入成功，共导入" + (addUpdateList.size() + deleteRuleList.size()) + "条数据。";
            return Result.ok(message);
        }
    }

    private List<HashSet<String>> getRuleIdSetList(List<String> ruleIdList,int size) {
        List<HashSet<String>> ruleIdSetList = new ArrayList<HashSet<String>>();
        HashSet<String> ruleIdSet = new HashSet<String>();
        for (String ruleId : ruleIdList) {
            if (ruleIdSet.size() >= size) {
                ruleIdSetList.add(ruleIdSet);
                ruleIdSet = new HashSet<String>();
            }
            ruleIdSet.add(ruleId);
        }
        if (ruleIdSet.size() > 0) {
            ruleIdSetList.add(ruleIdSet);
        }
        return ruleIdSetList;
    }

    private String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
            if (StringUtils.isNotBlank(dest) && ",".equals(dest.substring(dest.length() - 1))) {
                dest = dest.substring(0, dest.length() - 1);
            }
        }
        return dest;
    }

    private JSONArray ageJSONArray(){
        String jsonStr = "["+
                "{ value: [0, 28], unit: 'day', compare: ['<=', '<='] },"+
                "{ value: [0, 1], unit: 'year', compare: ['<=', '<='] },"+
                "{ value: [0, 3], unit: 'year', compare: ['<=', '<='] },"+
                "{ value: [3, 6], unit: 'year', compare: ['<', '<='] },"+
                "{ value: [6, 12], unit: 'year', compare: ['<', '<='] },"+
                "{ value: [0, 14], unit: 'year', compare: ['<=', '<='] },"+
                "{ value: [14, 18], unit: 'year', compare: ['<', '<='] },"+
                "{ value: [18, -1], unit: 'year', compare: ['<=', '<'] },"+
                "{ value: [60, -1], unit: 'year', compare: ['<=', '<'] }"+
                "]";
        JSONArray array = JSON.parseArray(jsonStr);
        return array;
    }

    private Result<?> importChargeRule(MultipartFile file, LoginUser user, String[] mappingFields, String ruleType) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalDrugRuleExportVO> list = new ArrayList<MedicalDrugRuleExportVO>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalDrugRuleExportVO.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalDrugRuleExportVO.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始：" + DateUtils.now());
        /**字典数据 start**/
        Map<String, List<MedicalDictItemVO>> dictAllListMap = medicalDictService.queryByTypes("LIMIT_SCOPE,AGE_RANGE,AGE_UNIT,GB/T2261.1,BM_JZBZ00,YYJB,HIS_YLJGZLKM,BM_CFLB00,FREQUENCY_PERIOD,SOLR_DATA_SOURCE,VISIT_PERIOD,YESNO,ACTION_LIST,ACTION_TYPE".split(","), MedicalConstant.DICT_KIND_COMMON);
        Map<String, Map<String, String>> dictListMap = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> dictCodeListMap = new HashMap<String, Map<String, String>>();
        dictAllListMap.forEach((key, dictList) -> {
            Map<String, String> dictMap = dictCodeListMap.get(key);
            Map<String, String> dictCodeMap = dictListMap.get(key);
            if (dictMap == null) {
                dictMap = new HashMap<String, String>();
            }
            if (dictCodeMap == null) {
                dictCodeMap = new HashMap<String, String>();
            }
            for (MedicalDictItemVO dictBean : dictList) {
                dictMap.put(dictBean.getValue(), dictBean.getCode());
                dictCodeMap.put(dictBean.getCode(), dictBean.getValue());
            }
            dictListMap.put(key, dictMap);
            dictCodeListMap.put(key, dictCodeMap);
        });
        Map<String, String> limitScopeMap = limitScopeMap();
        JSONArray ageJSONArray = this.ageJSONArray();
        /**字典数据 end**/
        String[] importActionTypeArr = {"0","1","2"};

        List<MedicalDrugRule> addUpdateList = new ArrayList<MedicalDrugRule>();
        List<String> deleteRuleList = new ArrayList<String>();//删除  主表id
        List<MedicalDrugRuleId> addRuleIdList = new ArrayList<MedicalDrugRuleId>();
        List<String> deleteRuleIdList = new ArrayList<String>();//修改和删除的  关联表记录删除
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalDrugRuleExportVO beanVO = list.get(i);


            if (StringUtils.isBlank(beanVO.getChargeItemCodes())) {
                message += "导入的数据中“收费项目编码”不能为空，如：第" + (i + 2) + "行数据“收费项目编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(beanVO.getLimitScope())) {
                message += "导入的数据中“限定范围”不能为空，如：第" + (i + 2) + "行数据“限定范围”为空\n";
                flag = false;
            }
            /*if (StringUtils.isBlank(beanVO.getActionType())) {
                message += "导入的数据中“不合规行为类型”不能为空，如：第" + (i + 2) + "行数据“不合规行为类型”为空\n";
                flag = false;
            }*/
            if (StringUtils.isBlank(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }
            if (!Arrays.asList(importActionTypeArr).contains(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }

            if (!flag) {
                continue;
            }

            if ("1".equals(beanVO.getImportActionType())) {//新增
                beanVO.setRuleId(IdUtils.uuid());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                if (StringUtils.isBlank(beanVO.getRuleId())) {
                    message += "导入的数据中“id主键”不能为空，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                    continue;
                }
                //判断数据是否存在
                MedicalDrugRule oldBean = this.baseMapper.selectById(beanVO.getRuleId());
                if (oldBean == null) {
                    message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                } else {
                    beanVO.setRuleId(oldBean.getRuleId());
                    beanVO.setCreateTime(oldBean.getCreateTime());
                    beanVO.setCreateUser(oldBean.getCreateUser());
                    beanVO.setCreateUsername(oldBean.getCreateUsername());
                }
            }

            if (!flag) {
                continue;
            }

            if (StringUtils.isNotBlank(beanVO.getPeriod())) {
                beanVO.setPeriod(beanVO.getPeriod().replace("1次就诊", "一次就诊"));//1次就诊替换为一次就诊
            }
            if (StringUtils.isNotBlank(beanVO.getLimitScope())) {
                beanVO.setLimitScope(beanVO.getLimitScope().replace("适用症","适应症"));//适用症替换为适应症
            }

            MedicalDrugRule bean = beanVO;
            //限定范围值等字典值转换
            bean.setLimitScope(getMedicalDictCodeStr(dictListMap.get("LIMIT_SCOPE"), bean.getLimitScope()));
            bean.setAge(getMedicalDictCodeStr(dictListMap.get("AGE_RANGE"), bean.getAge()));
            bean.setAgeUnit(getMedicalDictCodeStr(dictListMap.get("AGE_UNIT"), bean.getAgeUnit()));
            bean.setSex(getMedicalDictCodeStr(dictListMap.get("GB/T2261.1"), bean.getSex()));
            bean.setJzlx(getMedicalDictCodeStr(dictListMap.get("BM_JZBZ00"), bean.getJzlx()));
            //bean.setYblx(getMedicalDictCodeStr(dictListMap.get("WS364.1/CV07.10.003"), bean.getYblx()));
            bean.setYyjb(getMedicalDictCodeStr(dictListMap.get("YYJB"), bean.getYyjb()));
            //bean.setOffice(getMedicalDictCodeStr(dictListMap.get("HIS_YLJGZLKM"), bean.getOffice()));
            bean.setChargeTypes(getMedicalDictCodeStr(dictListMap.get("BM_CFLB00"), bean.getChargeTypes()));
            bean.setPeriod(getMedicalDictCodeStr(dictListMap.get("FREQUENCY_PERIOD"), bean.getPeriod()));
            bean.setTwoPeriod(getMedicalDictCodeStr(dictListMap.get("FREQUENCY_PERIOD"), bean.getTwoPeriod()));
            bean.setRuleSource(getMedicalDictCodeStr(dictListMap.get("SOLR_DATA_SOURCE"), bean.getRuleSource()));
            if (StringUtils.isNotBlank(bean.getYblx())) {
                bean.setYblx(medicalOtherDictService.getCodeByValue("medinsuranceType", bean.getYblx()));
            }
            bean.setPayDurationPeriod(getMedicalDictCodeStr(dictListMap.get("VISIT_PERIOD"), bean.getPayDurationPeriod()));
            bean.setPayDurationUnit(getMedicalDictCodeStr(dictListMap.get("AGE_UNIT"), bean.getPayDurationUnit()));
            bean.setOffice(getOtherDictCodes("Department", bean.getOffice()));
            if (StringUtils.isNotBlank(bean.getFrequency())) {
                if (bean.getFrequency().startsWith(">=") || bean.getFrequency().startsWith("<=")) {
                    bean.setCompare(bean.getFrequency().substring(0, 2));
                    bean.setFrequency(bean.getFrequency().substring(2, bean.getFrequency().length()));
                } else if (bean.getFrequency().startsWith(">") || bean.getFrequency().startsWith("=") || bean.getFrequency().startsWith("<")) {
                    bean.setCompare(bean.getFrequency().substring(0, 1));
                    bean.setFrequency(bean.getFrequency().substring(1, bean.getFrequency().length()));
                }
            }
            if (StringUtils.isNotBlank(bean.getTwoFrequency())) {
                if (bean.getTwoFrequency().startsWith(">=") || bean.getTwoFrequency().startsWith("<=")) {
                    bean.setTwoCompare(bean.getTwoFrequency().substring(0, 2));
                    bean.setTwoFrequency(bean.getTwoFrequency().substring(2, bean.getTwoFrequency().length()));
                } else if (bean.getTwoFrequency().startsWith(">") || bean.getTwoFrequency().startsWith("=") || bean.getTwoFrequency().startsWith("<")) {
                    bean.setTwoCompare(bean.getTwoFrequency().substring(0, 1));
                    bean.setTwoFrequency(bean.getTwoFrequency().substring(1, bean.getTwoFrequency().length()));
                }
            }
            if (StringUtils.isNotBlank(bean.getDiseasegroupFreq())) {
                if ("等于".equals(bean.getDiseasegroupFreq())) {
                    bean.setDiseasegroupFreq("=");
                }
                if ("不等于".equals(bean.getDiseasegroupFreq())) {
                    bean.setDiseasegroupFreq("<>");
                }
            }

            if (!"1".equals(bean.getFrequency())) {
                int num = 0;
                num += bean.getChargeItemCodes().split(",").length;
                if (StringUtils.isNotBlank(bean.getChargeTypes())) {
                    num += bean.getChargeTypes().split(",").length;
                }
                if (num > 1) {
                    message += "导入的数据中多个收费项目、收费分类仅支持数量/频次1为1次，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }
            if (!"1".equals(bean.getTwoFrequency())) {
                int num = 0;
                num += bean.getChargeItemCodes().split(",").length;
                if (StringUtils.isNotBlank(bean.getChargeTypes())) {
                    num += bean.getChargeTypes().split(",").length;
                }
                if (num > 1) {
                    message += "导入的数据中多个收费项目、收费分类仅支持数量/频次2为1次，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }

            //bean.setActionId(getMedicalDictCodeStr(dictListMap.get("ACTION_LIST"), bean.getActionId()));
            //bean.setActionType(getMedicalDictCodeStr(dictListMap.get("ACTION_TYPE"), bean.getActionType()));
            //if(StringUtils.isBlank(bean.getActionType())){
            bean.setActionType("CHARGE");//固定为CHARGE
            //}
            this.setActionName(bean);//不合规行为名称

            if(StringUtils.isBlank(beanVO.getStartEndTimeStr())){
                bean.setStartTime(DateUtils.str2Date("2000-01-01",date_sdf));
                bean.setEndTime(DateUtils.str2Date("2099-12-31",date_sdf));
            }else{
                String[] startAndEndTime = beanVO.getStartEndTimeStr().split("到");
                try {
                    bean.setStartTime(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                    bean.setEndTime(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                }catch (Exception e) {
                    message += "导入的数据中“数据时间”格式不正确，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }

            }

            //替换空格换行制表符
            bean.setChargeItemCodes(replaceBlank(bean.getChargeItemCodes()));
            bean.setChargeItems(replaceBlank(bean.getChargeItems()));
            bean.setChargeTypes(replaceBlank(bean.getChargeTypes()));
            bean.setOrg(replaceBlank(bean.getOrg()));
            bean.setFitGroupCodes(replaceBlank(bean.getFitGroupCodes()));
            bean.setUnfitGroupCodes(replaceBlank(bean.getUnfitGroupCodes()));
            bean.setUnfitGroupCodesDay(replaceBlank(bean.getUnfitGroupCodesDay()));
            bean.setIndication(replaceBlank(bean.getIndication()));
            bean.setUnIndication(replaceBlank(bean.getUnIndication()));
            bean.setUnExpense(getMedicalDictCodeStr(dictListMap.get("YESNO"), bean.getUnExpense()));
            bean.setUnCharge(getMedicalDictCodeStr(dictListMap.get("YESNO"), bean.getUnCharge()));
            bean.setRuleType(ruleType);

            if (!"2".equals(beanVO.getImportActionType())) {//删除不判断
                JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
		    	/*String limitScope = "";
		    	for (Map.Entry<String, String> entry : limitScopeMap.entrySet()) {
		            //选项不为空
			    	if(StringUtils.isNotBlank(jsonBean.getString(entry.getValue()))) {
			    		limitScope +=entry.getKey()+",";
			    	}
		        }
		    	if(StringUtils.isNotBlank(limitScope)) {
		    		bean.setLimitScope(limitScope.substring(0, limitScope.length()-1));
		    	}*/
                //判断选择了限定范围类型 具体的类型值是否重复等
                for (String limitCode : bean.getLimitScope().split(",")) {
                    String field = limitScopeMap.get(limitCode);
                    if (StringUtils.isBlank(field)) {
                        continue;
                    }
                    //选项不能为空
                    if (StringUtils.isNotBlank(field) && StringUtils.isBlank(jsonBean.getString(field))) {
                        message += "导入的数据中限定范围包含“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”,“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”不能为空，如：第" + (i + 2) + "行数据\n";
                        flag = false;
                        break;
                    }
                    //选项不能重复
                    String[] groupArr = jsonBean.getString(field).split("\\|");//组
                    for (String groupValue : groupArr) {
                        String[] fieldArr = groupValue.split(",");
                        if (!cheakRepeat(fieldArr)) {
                            message += "导入的数据中“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”组之间重复，如：第" + (i + 2) + "行数据\n";
                            flag = false;
                            break;
                        }
                    }
                    //年龄
                    if ("01".equals(limitCode)) {
                        if ("-1".equals(bean.getAge())) {//自定义
                            if (StringUtils.isBlank(bean.getAgeUnit())) {
                                message += "导入的数据中年龄为自定义，“年龄单位”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if (bean.getAgeLow() != null && bean.getAgeLow() != -1 && StringUtils.isBlank(bean.getAgeLowCompare())) {
                                message += "导入的数据中有年龄下限值，“年龄下限比较符”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if (bean.getAgeHigh() != null && bean.getAgeHigh() != -1 && StringUtils.isBlank(bean.getAgeHighCompare())) {
                                message += "导入的数据中有年龄上限值，“年龄上限比较符”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if ("是".equals(bean.getAgeLowCompare())) {
                                bean.setAgeLowCompare("<=");
                            }
                            if ("否".equals(bean.getAgeLowCompare())) {
                                bean.setAgeLowCompare("<");
                            }
                            if ("是".equals(bean.getAgeHighCompare())) {
                                bean.setAgeHighCompare("<=");
                            }
                            if ("否".equals(bean.getAgeHighCompare())) {
                                bean.setAgeHighCompare("<");
                            }
                        }else{
                            int age = Integer.parseInt(bean.getAge());
                            JSONObject ageObj = ageJSONArray.getJSONObject(age-1);
                            if(ageObj!=null){
                                bean.setAgeLow(Integer.parseInt(ageObj.getString("value").replace("[", "").split(",")[0]));
                                bean.setAgeHigh(Integer.parseInt(ageObj.getString("value").replace("]", "").split(",")[1]));
                                bean.setAgeLowCompare(ageObj.getString("compare").replace("[", "").replace("\"", "").split(",")[0]);
                                bean.setAgeHighCompare(ageObj.getString("compare").replace("]", "").replace("\"", "").split(",")[1]);
                                bean.setAgeUnit(ageObj.getString("unit"));
                            }else{
                                message += "导入的数据中年龄在系统没有配置上下限值，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        break;
                    }
                }
            }
            if (!flag) {
                continue;
            }

            //生成新增的addRuleIdList
            if ("1".equals(beanVO.getImportActionType()) || "0".equals(beanVO.getImportActionType())) {//新增、修改
                for (String itemCode : bean.getChargeItemCodes().split(",")) {
                    MedicalDrugRuleId drugRuleIdBean = new MedicalDrugRuleId();
                    drugRuleIdBean.setRuleId(bean.getRuleId());
                    drugRuleIdBean.setDrugCode(itemCode);
                    addRuleIdList.add(drugRuleIdBean);
                }
                addUpdateList.add(bean);
            }
            if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                deleteRuleIdList.add(beanVO.getRuleId());//修改、删除时  删除关联表
                if ("2".equals(beanVO.getImportActionType())) {
                    deleteRuleList.add(beanVO.getRuleId());//删除时 删除主表
                }
            }
        }
        if (StringUtils.isNotBlank(message)) {
            message += "请核对数据后进行导入。";
            System.out.println(message);
            return Result.error(message);
        } else {
            System.out.println("开始插入时间：" + DateUtils.now());
            //删除关联表
            if (deleteRuleIdList.size() > 0) {
                List<HashSet<String>> ruleIdSetList = getRuleIdSetList(deleteRuleIdList,100);
                if (ruleIdSetList.size() > 0) {
                    for (HashSet<String> idsSet : ruleIdSetList) {
                        this.medicalDrugRuleIdService.remove(new QueryWrapper<MedicalDrugRuleId>().in("RULE_ID", idsSet));
                    }
                }
            }
            //删除主表
            if (deleteRuleList.size() > 0) {
                List<HashSet<String>> ruleIdSetList = getRuleIdSetList(deleteRuleList,100);
                if (ruleIdSetList.size() > 0) {
                    for (HashSet<String> idsSet : ruleIdSetList) {
                        this.baseMapper.delete(new QueryWrapper<MedicalDrugRule>().in("RULE_ID", idsSet));
                    }
                }
            }

            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 100);//直接插入
            }
            if (addRuleIdList.size() > 0) {
                this.medicalDrugRuleIdService.saveBatch(addRuleIdList);
            }
            System.out.println("结束导入时间：" + DateUtils.now());
            message += "导入成功，共导入" + (addUpdateList.size() + deleteRuleList.size()) + "条数据。";
            return Result.ok(message);
        }
    }

    private Result<?> importTreatRule(MultipartFile file, LoginUser user, String[] mappingFields, String ruleType) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalDrugRuleExportVO> list = new ArrayList<MedicalDrugRuleExportVO>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalDrugRuleExportVO.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalDrugRuleExportVO.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始：" + DateUtils.now());
        /**字典数据 start**/
        Map<String, List<MedicalDictItemVO>> dictAllListMap = medicalDictService.queryByTypes("LIMIT_SCOPE,AGE_RANGE,AGE_UNIT,GB/T2261.1,BM_JZBZ00,YYJB,HIS_YLJGZLKM,BM_CFLB00,FREQUENCY_PERIOD,SOLR_DATA_SOURCE,VISIT_PERIOD,YESNO,ACTION_LIST,ACTION_TYPE".split(","), MedicalConstant.DICT_KIND_COMMON);
        Map<String, Map<String, String>> dictListMap = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> dictCodeListMap = new HashMap<String, Map<String, String>>();
        dictAllListMap.forEach((key, dictList) -> {
            Map<String, String> dictMap = dictCodeListMap.get(key);
            Map<String, String> dictCodeMap = dictListMap.get(key);
            if (dictMap == null) {
                dictMap = new HashMap<String, String>();
            }
            if (dictCodeMap == null) {
                dictCodeMap = new HashMap<String, String>();
            }
            for (MedicalDictItemVO dictBean : dictList) {
                dictMap.put(dictBean.getValue(), dictBean.getCode());
                dictCodeMap.put(dictBean.getCode(), dictBean.getValue());
            }
            dictListMap.put(key, dictMap);
            dictCodeListMap.put(key, dictCodeMap);
        });
        Map<String, String> limitScopeMap = limitScopeMap();
        JSONArray ageJSONArray = this.ageJSONArray();
        /**字典数据 end**/
        String[] importActionTypeArr = {"0","1","2"};

        List<MedicalDrugRule> addUpdateList = new ArrayList<MedicalDrugRule>();
        List<String> deleteRuleList = new ArrayList<String>();//删除  主表id
        List<MedicalDrugRuleId> addRuleIdList = new ArrayList<MedicalDrugRuleId>();
        List<String> deleteRuleIdList = new ArrayList<String>();//修改和删除的  关联表记录删除
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalDrugRuleExportVO beanVO = list.get(i);


            if (StringUtils.isBlank(beanVO.getChargeItemCodes())) {
                message += "导入的数据中“诊疗项目编码”不能为空，如：第" + (i + 2) + "行数据“诊疗项目编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(beanVO.getLimitScope())) {
                message += "导入的数据中“限定范围”不能为空，如：第" + (i + 2) + "行数据“限定范围”为空\n";
                flag = false;
            }
            /*if (StringUtils.isBlank(beanVO.getActionType())) {
                message += "导入的数据中“不合规行为类型”不能为空，如：第" + (i + 2) + "行数据“不合规行为类型”为空\n";
                flag = false;
            }*/
            if (StringUtils.isBlank(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }
            if (!Arrays.asList(importActionTypeArr).contains(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }

            if (!flag) {
                continue;
            }

            if ("1".equals(beanVO.getImportActionType())) {//新增
                beanVO.setRuleId(IdUtils.uuid());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                if (StringUtils.isBlank(beanVO.getRuleId())) {
                    message += "导入的数据中“id主键”不能为空，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                    continue;
                }
                //判断数据是否存在
                MedicalDrugRule oldBean = this.baseMapper.selectById(beanVO.getRuleId());
                if (oldBean == null) {
                    message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                } else {
                    beanVO.setRuleId(oldBean.getRuleId());
                    beanVO.setCreateTime(oldBean.getCreateTime());
                    beanVO.setCreateUser(oldBean.getCreateUser());
                    beanVO.setCreateUsername(oldBean.getCreateUsername());
                }
            }

            if (!flag) {
                continue;
            }

            if (StringUtils.isNotBlank(beanVO.getPeriod())) {
                beanVO.setPeriod(beanVO.getPeriod().replace("1次就诊", "一次就诊"));//1次就诊替换为一次就诊
            }
            if (StringUtils.isNotBlank(beanVO.getLimitScope())) {
                beanVO.setLimitScope(beanVO.getLimitScope().replace("适用症","适应症"));//适用症替换为适应症
            }

            MedicalDrugRule bean = beanVO;
            //限定范围值等字典值转换
            bean.setLimitScope(getMedicalDictCodeStr(dictListMap.get("LIMIT_SCOPE"), bean.getLimitScope()));
            bean.setAge(getMedicalDictCodeStr(dictListMap.get("AGE_RANGE"), bean.getAge()));
            bean.setAgeUnit(getMedicalDictCodeStr(dictListMap.get("AGE_UNIT"), bean.getAgeUnit()));
            bean.setSex(getMedicalDictCodeStr(dictListMap.get("GB/T2261.1"), bean.getSex()));
            bean.setJzlx(getMedicalDictCodeStr(dictListMap.get("BM_JZBZ00"), bean.getJzlx()));
            //bean.setYblx(getMedicalDictCodeStr(dictListMap.get("WS364.1/CV07.10.003"), bean.getYblx()));
            bean.setYyjb(getMedicalDictCodeStr(dictListMap.get("YYJB"), bean.getYyjb()));
            //bean.setOffice(getMedicalDictCodeStr(dictListMap.get("HIS_YLJGZLKM"), bean.getOffice()));
            bean.setChargeTypes(getMedicalDictCodeStr(dictListMap.get("BM_CFLB00"), bean.getChargeTypes()));
            bean.setPeriod(getMedicalDictCodeStr(dictListMap.get("FREQUENCY_PERIOD"), bean.getPeriod()));
            bean.setTwoPeriod(getMedicalDictCodeStr(dictListMap.get("FREQUENCY_PERIOD"), bean.getTwoPeriod()));
            bean.setRuleSource(getMedicalDictCodeStr(dictListMap.get("SOLR_DATA_SOURCE"), bean.getRuleSource()));
            if (StringUtils.isNotBlank(bean.getYblx())) {
                bean.setYblx(medicalOtherDictService.getCodeByValue("medinsuranceType", bean.getYblx()));
            }
            bean.setPayDurationPeriod(getMedicalDictCodeStr(dictListMap.get("VISIT_PERIOD"), bean.getPayDurationPeriod()));
            bean.setPayDurationUnit(getMedicalDictCodeStr(dictListMap.get("AGE_UNIT"), bean.getPayDurationUnit()));
            bean.setOffice(getOtherDictCodes("Department", bean.getOffice()));
            if (StringUtils.isNotBlank(bean.getFrequency())) {
                if (bean.getFrequency().startsWith(">=") || bean.getFrequency().startsWith("<=")) {
                    bean.setCompare(bean.getFrequency().substring(0, 2));
                    bean.setFrequency(bean.getFrequency().substring(2, bean.getFrequency().length()));
                } else if (bean.getFrequency().startsWith(">") || bean.getFrequency().startsWith("=") || bean.getFrequency().startsWith("<")) {
                    bean.setCompare(bean.getFrequency().substring(0, 1));
                    bean.setFrequency(bean.getFrequency().substring(1, bean.getFrequency().length()));
                }
            }
            if (StringUtils.isNotBlank(bean.getTwoFrequency())) {
                if (bean.getTwoFrequency().startsWith(">=") || bean.getTwoFrequency().startsWith("<=")) {
                    bean.setTwoCompare(bean.getTwoFrequency().substring(0, 2));
                    bean.setTwoFrequency(bean.getTwoFrequency().substring(2, bean.getTwoFrequency().length()));
                } else if (bean.getTwoFrequency().startsWith(">") || bean.getTwoFrequency().startsWith("=") || bean.getTwoFrequency().startsWith("<")) {
                    bean.setTwoCompare(bean.getTwoFrequency().substring(0, 1));
                    bean.setTwoFrequency(bean.getTwoFrequency().substring(1, bean.getTwoFrequency().length()));
                }
            }
            if (StringUtils.isNotBlank(bean.getDiseasegroupFreq())) {
                if ("等于".equals(bean.getDiseasegroupFreq())) {
                    bean.setDiseasegroupFreq("=");
                }
                if ("不等于".equals(bean.getDiseasegroupFreq())) {
                    bean.setDiseasegroupFreq("<>");
                }
            }

            if (!"1".equals(bean.getFrequency())) {
                int num = 0;
                num += bean.getChargeItemCodes().split(",").length;
                if (StringUtils.isNotBlank(bean.getChargeTypes())) {
                    num += bean.getChargeTypes().split(",").length;
                }
                if (num > 1) {
                    message += "导入的数据中多个诊疗项目、诊疗分类仅支持数量/频次1为1次，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }
            if (!"1".equals(bean.getTwoFrequency())) {
                int num = 0;
                num += bean.getChargeItemCodes().split(",").length;
                if (StringUtils.isNotBlank(bean.getChargeTypes())) {
                    num += bean.getChargeTypes().split(",").length;
                }
                if (num > 1) {
                    message += "导入的数据中多个诊疗项目、诊疗分类仅支持数量/频次2为1次，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }

            //bean.setActionId(getMedicalDictCodeStr(dictListMap.get("ACTION_LIST"), bean.getActionId()));
            //bean.setActionType(getMedicalDictCodeStr(dictListMap.get("ACTION_TYPE"), bean.getActionType()));
            //if(StringUtils.isBlank(bean.getActionType())){
            bean.setActionType("TREAT");//固定为CHARGE
            //}
            this.setActionName(bean);//不合规行为名称

            if(StringUtils.isBlank(beanVO.getStartEndTimeStr())){
                bean.setStartTime(DateUtils.str2Date("2000-01-01",date_sdf));
                bean.setEndTime(DateUtils.str2Date("2099-12-31",date_sdf));
            }else{
                String[] startAndEndTime = beanVO.getStartEndTimeStr().split("到");
                try {
                    bean.setStartTime(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                    bean.setEndTime(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                }catch (Exception e) {
                    message += "导入的数据中“数据时间”格式不正确，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }

            //替换空格换行制表符
            bean.setChargeItemCodes(replaceBlank(bean.getChargeItemCodes()));
            bean.setChargeItems(replaceBlank(bean.getChargeItems()));
            bean.setChargeTypes(replaceBlank(bean.getChargeTypes()));
            bean.setOrg(replaceBlank(bean.getOrg()));
            bean.setFitGroupCodes(replaceBlank(bean.getFitGroupCodes()));
            bean.setUnfitGroupCodes(replaceBlank(bean.getUnfitGroupCodes()));
            bean.setUnfitGroupCodesDay(replaceBlank(bean.getUnfitGroupCodesDay()));
            bean.setIndication(replaceBlank(bean.getIndication()));
            bean.setUnIndication(replaceBlank(bean.getUnIndication()));
            bean.setUnExpense(getMedicalDictCodeStr(dictListMap.get("YESNO"), bean.getUnExpense()));
            bean.setUnCharge(getMedicalDictCodeStr(dictListMap.get("YESNO"), bean.getUnCharge()));
            bean.setRuleType(ruleType);

            if (!"2".equals(beanVO.getImportActionType())) {//删除不判断
                JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
		    	/*String limitScope = "";
		    	for (Map.Entry<String, String> entry : limitScopeMap.entrySet()) {
		            //选项不为空
			    	if(StringUtils.isNotBlank(jsonBean.getString(entry.getValue()))) {
			    		limitScope +=entry.getKey()+",";
			    	}
		        }
		    	if(StringUtils.isNotBlank(limitScope)) {
		    		bean.setLimitScope(limitScope.substring(0, limitScope.length()-1));
		    	}*/
                //判断选择了限定范围类型 具体的类型值是否重复等
                for (String limitCode : bean.getLimitScope().split(",")) {
                    String field = limitScopeMap.get(limitCode);
                    if (StringUtils.isBlank(field)) {
                        continue;
                    }
                    //选项不能为空
                    if (StringUtils.isNotBlank(field) && StringUtils.isBlank(jsonBean.getString(field))) {
                        message += "导入的数据中限定范围包含“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”,“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”不能为空，如：第" + (i + 2) + "行数据\n";
                        flag = false;
                        break;
                    }
                    //选项不能重复
                    String[] groupArr = jsonBean.getString(field).split("\\|");//组
                    for (String groupValue : groupArr) {
                        String[] fieldArr = groupValue.split(",");
                        if (!cheakRepeat(fieldArr)) {
                            message += "导入的数据中“" + dictCodeListMap.get("LIMIT_SCOPE").get(limitCode) + "”组之间重复，如：第" + (i + 2) + "行数据\n";
                            flag = false;
                            break;
                        }
                    }
                    //年龄
                    if ("01".equals(limitCode)) {
                        if ("-1".equals(bean.getAge())) {//自定义
                            if (StringUtils.isBlank(bean.getAgeUnit())) {
                                message += "导入的数据中年龄为自定义，“年龄单位”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if (bean.getAgeLow() != null && bean.getAgeLow() != -1 && StringUtils.isBlank(bean.getAgeLowCompare())) {
                                message += "导入的数据中有年龄下限值，“年龄下限比较符”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if (bean.getAgeHigh() != null && bean.getAgeHigh() != -1 && StringUtils.isBlank(bean.getAgeHighCompare())) {
                                message += "导入的数据中有年龄上限值，“年龄上限比较符”不能为空，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                            if ("是".equals(bean.getAgeLowCompare())) {
                                bean.setAgeLowCompare("<=");
                            }
                            if ("否".equals(bean.getAgeLowCompare())) {
                                bean.setAgeLowCompare("<");
                            }
                            if ("是".equals(bean.getAgeHighCompare())) {
                                bean.setAgeHighCompare("<=");
                            }
                            if ("否".equals(bean.getAgeHighCompare())) {
                                bean.setAgeHighCompare("<");
                            }
                        }else{
                            int age = Integer.parseInt(bean.getAge());
                            JSONObject ageObj = ageJSONArray.getJSONObject(age-1);
                            if(ageObj!=null){
                                bean.setAgeLow(Integer.parseInt(ageObj.getString("value").replace("[", "").split(",")[0]));
                                bean.setAgeHigh(Integer.parseInt(ageObj.getString("value").replace("]", "").split(",")[1]));
                                bean.setAgeLowCompare(ageObj.getString("compare").replace("[", "").replace("\"", "").split(",")[0]);
                                bean.setAgeHighCompare(ageObj.getString("compare").replace("]", "").replace("\"", "").split(",")[1]);
                                bean.setAgeUnit(ageObj.getString("unit"));
                            }else{
                                message += "导入的数据中年龄在系统没有配置上下限值，如：第" + (i + 2) + "行数据\n";
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        break;
                    }
                }
            }
            if (!flag) {
                continue;
            }

            //生成新增的addRuleIdList
            if ("1".equals(beanVO.getImportActionType()) || "0".equals(beanVO.getImportActionType())) {//新增、修改
                for (String itemCode : bean.getChargeItemCodes().split(",")) {
                    MedicalDrugRuleId drugRuleIdBean = new MedicalDrugRuleId();
                    drugRuleIdBean.setRuleId(bean.getRuleId());
                    drugRuleIdBean.setDrugCode(itemCode);
                    addRuleIdList.add(drugRuleIdBean);
                }
                addUpdateList.add(bean);
            }
            if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                deleteRuleIdList.add(beanVO.getRuleId());//修改、删除时  删除关联表
                if ("2".equals(beanVO.getImportActionType())) {
                    deleteRuleList.add(beanVO.getRuleId());//删除时 删除主表
                }
            }
        }
        if (StringUtils.isNotBlank(message)) {
            message += "请核对数据后进行导入。";
            System.out.println(message);
            return Result.error(message);
        } else {
            System.out.println("开始插入时间：" + DateUtils.now());
            //删除关联表
            if (deleteRuleIdList.size() > 0) {
                List<HashSet<String>> ruleIdSetList = getRuleIdSetList(deleteRuleIdList,100);
                if (ruleIdSetList.size() > 0) {
                    for (HashSet<String> idsSet : ruleIdSetList) {
                        this.medicalDrugRuleIdService.remove(new QueryWrapper<MedicalDrugRuleId>().in("RULE_ID", idsSet));
                    }
                }
            }
            //删除主表
            if (deleteRuleList.size() > 0) {
                List<HashSet<String>> ruleIdSetList = getRuleIdSetList(deleteRuleList,100);
                if (ruleIdSetList.size() > 0) {
                    for (HashSet<String> idsSet : ruleIdSetList) {
                        this.baseMapper.delete(new QueryWrapper<MedicalDrugRule>().in("RULE_ID", idsSet));
                    }
                }
            }

            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 100);//直接插入
            }
            if (addRuleIdList.size() > 0) {
                this.medicalDrugRuleIdService.saveBatch(addRuleIdList);
            }
            System.out.println("结束导入时间：" + DateUtils.now());
            message += "导入成功，共导入" + (addUpdateList.size() + deleteRuleList.size()) + "条数据。";
            return Result.ok(message);
        }
    }

    private Map<String, String> limitScopeMap() {
        Map<String, String> limitScopeMap = new HashMap<String, String>();
        limitScopeMap.put("01", "age");
        limitScopeMap.put("02", "sex");
        limitScopeMap.put("03", "jzlx");
        limitScopeMap.put("04", "yblx");
        limitScopeMap.put("05", "yyjb");
        limitScopeMap.put("06", "office");
        limitScopeMap.put("07", "courseDose");
        limitScopeMap.put("08", "yearDose");
        limitScopeMap.put("09", "treatProject");
        limitScopeMap.put("10", "treatment");
        limitScopeMap.put("12", "twoLimitDrug");
        limitScopeMap.put("13", "indication");
        limitScopeMap.put("14", "treatDrug");
        limitScopeMap.put("16", "outHospPlan");
        limitScopeMap.put("17", "dosageLimit");
        limitScopeMap.put("18", "dosageUnit");
        limitScopeMap.put("19", "takeTimeLimit");
        limitScopeMap.put("20", "timeUnit");
        limitScopeMap.put("21", "maxKeepUseTime");
        limitScopeMap.put("22", "maxKeepTimeUnit");
        limitScopeMap.put("23", "ruleSource");
        limitScopeMap.put("24", "twoLimitDrug2");
        limitScopeMap.put("25", "healthOrgKind");
        limitScopeMap.put("26", "docAdvice");
        limitScopeMap.put("27", "fitGroupCodes");
        limitScopeMap.put("28", "unfitGroupCodes");
        limitScopeMap.put("29", "unfitGroupCodesDay");
        limitScopeMap.put("30", "ruleBasis");
        limitScopeMap.put("31", "unIndication");
        limitScopeMap.put("32", "unExpense");
        limitScopeMap.put("33", "drugUsage");
        limitScopeMap.put("34", "unfitGroupCodes");
        limitScopeMap.put("35", "org");
        limitScopeMap.put("36", "unCharge");
        limitScopeMap.put("37", "payDuration");
        limitScopeMap.put("39", "diseasegroupFreq");
        limitScopeMap.put("40", "testResultItemType");
        return limitScopeMap;
    }

    private boolean cheakRepeat(String[] array) {
        HashSet<String> hashSet = new HashSet<String>();
        for (int i = 0; i < array.length; i++) {
            hashSet.add(array[i]);
        }
        if (hashSet.size() == array.length) {
            return true;
        }
        return false;
    }

    @Override
    public void exportRuleLoseExcel(String ruleType, QueryWrapper<MedicalDrugRule> queryWrapper, OutputStream os, String suffix) throws Exception {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<MedicalDrugRuleVO> list = this.baseMapper.selectListVO(queryWrapper, sysUser.getDataSource());
        exportDrugRuleLose(os, list, suffix,ruleType);//药品合规规则失效明细导出
    }

    private void exportDrugRuleLose(OutputStream os, List<MedicalDrugRuleVO> list, String suffix,String ruleType)
            throws IOException, Exception, WriteException {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalDrugRuleExportVO> exportList = new ArrayList<MedicalDrugRuleExportVO>();
        Set<String> stdAtcSet = new HashSet<String>();//已配置的StdAtc
        Set<String> diseaseGroupSet = new HashSet<String>();//已配置的疾病组
        Set<String> projectGroupSet = new HashSet<String>();//已配置的项目组
        Map<String,Map<String,String>>  stdAtcMap = new HashMap<>();
        Map<String,Map<String,String>>  diseaseGroupMap = new HashMap<>();
        Map<String,Map<String,String>>  projectGroupMap = new HashMap<>();
        //导出list
        List<MedicalDrugRuleExportVO> stdAtcExportList = new ArrayList<>();
        List<MedicalDrugRuleExportVO> projectGroupExportList = new ArrayList<>();
        List<MedicalDrugRuleExportVO> projectItemExportList = new ArrayList<>();
        List<MedicalDrugRuleExportVO> diseaseGroupExportList = new ArrayList<>();
        List<MedicalDrugRuleExportVO> diseaseItemExportList = new ArrayList<>();

        Map<String,String> itemMap = new HashMap<String,String>();
        for (MedicalDrugRuleVO bean : list) {
            //1.判断主药品编码是否有缺失
            List<MedicalDrugRuleId> drugRuleIdList=this.medicalDrugRuleIdService.list(new QueryWrapper<MedicalDrugRuleId>().eq("RULE_ID", bean.getRuleId()));
            if("1".equals(ruleType)){
                for(MedicalDrugRuleId drugRuleIdBean:drugRuleIdList){
                    boolean exsitsFlag = true;
                    if("ATC".equals(drugRuleIdBean.getDrugType())){
                        List<MedicalStdAtc> stdAtcList = this.medicalStdAtcService.list(new QueryWrapper<MedicalStdAtc>().eq("CODE", drugRuleIdBean.getDrugCode()).eq("STATE", MedicalAuditLogConstants.STATE_YX));
                        if(stdAtcList.size()==0){
                            exsitsFlag = false;
                        }else{
                            stdAtcMap.put(drugRuleIdBean.getDrugCode()+"&"+"STD_ATC", new HashMap<String, String>() {{put("code", drugRuleIdBean.getDrugCode());put("name", stdAtcList.get(0).getName());put("table", "STD_ATC");put("id", stdAtcList.get(0).getId());}});
                        }
                    }else if("DRUG".equals(drugRuleIdBean.getDrugType())){
                        List<MedicalDrug> drugList = this.medicalDrugService.list(new QueryWrapper<MedicalDrug>().eq("CODE", drugRuleIdBean.getDrugCode()).eq("STATE", MedicalAuditLogConstants.STATE_YX));
                        if(drugList.size()==0){
                            exsitsFlag = false;
                        }else{
                            stdAtcMap.put(drugRuleIdBean.getDrugCode()+"&"+"STD_DRUG_INFO", new HashMap<String, String>() {{put("code", drugRuleIdBean.getDrugCode());put("name", drugList.get(0).getName());put("table", "STD_DRUG_INFO");put("id", drugList.get(0).getId());}});
                        }
                    }
                    if(exsitsFlag){//存在
                        stdAtcSet.add(drugRuleIdBean.getDrugCode());
                    }else{
                        MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
                        BeanUtils.copyProperties(bean, dataBean);
                        dataBean.setLimitScope("YX_药品剂型级别编码");
                        dataBean.setIndication(drugRuleIdBean.getDrugCode());
                        exportList.add(dataBean);
                    }
                }
            }else if ("2".equals(ruleType)) {
                for(MedicalDrugRuleId drugRuleIdBean:drugRuleIdList){
                    boolean exsitsFlag = true;
                    MedicalTreatProject medicalTreatProject  = medicalTreatProjectService.getBeanByCode(drugRuleIdBean.getDrugCode());
                    if(medicalTreatProject==null){
                        MedicalEquipment medicalEquipment  = medicalEquipmentService.getBeanByCode(drugRuleIdBean.getDrugCode());
                        if(medicalEquipment==null){//不存在
                            MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
                            BeanUtils.copyProperties(bean, dataBean);
                            dataBean.setLimitScope("收费项目编码");
                            dataBean.setIndication(drugRuleIdBean.getDrugCode());
                            exportList.add(dataBean);
                        }else{
                            if(StringUtils.isBlank(itemMap.get(medicalEquipment.getProductcode()+"&"+"STD_MEDICAL_EQUIPMENT"))){
                                itemMap.put(medicalEquipment.getProductcode()+"&"+"STD_MEDICAL_EQUIPMENT",medicalEquipment.getProductcode());
                                MedicalDrugRuleExportVO itemTmp = new MedicalDrugRuleExportVO();
                                itemTmp.setDrugCode(medicalEquipment.getProductcode());
                                itemTmp.setDrugNames(medicalEquipment.getProductname());
                                itemTmp.setMessage("STD_MEDICAL_EQUIPMENT");
                                projectItemExportList.add(itemTmp);
                            }
                        }
                    }else{
                        if(StringUtils.isBlank(itemMap.get(medicalTreatProject.getCode()+"&"+"STD_TREATMENT"))){
                            itemMap.put(medicalTreatProject.getCode()+"&"+"STD_TREATMENT",medicalTreatProject.getCode());
                            MedicalDrugRuleExportVO itemTmp = new MedicalDrugRuleExportVO();
                            itemTmp.setDrugCode(medicalTreatProject.getCode());
                            itemTmp.setDrugNames(medicalTreatProject.getName());
                            itemTmp.setMessage("STD_TREATMENT");
                            projectItemExportList.add(itemTmp);
                        }
                    }
                }
            }

            //判断选择了的限定范围类型  判断缺失
            Map<String, String> limitScopeMap = limitScopeMap();
            Map<String, String> limitScopeFuncMap = limitScopeFuncMap();
            JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
            for (String limitCode : bean.getLimitScope().split(",")) {
                String field = limitScopeMap.get(limitCode);
                if (StringUtils.isBlank(field)) {
                    continue;
                }
                String limitScopeFunc = limitScopeFuncMap.get(field);
                if (StringUtils.isBlank(limitScopeFunc)) {
                    continue;
                }
                //选项是否为空
                if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(jsonBean.getString(field))) {
                    if("testResultItemType".equals(field)&&"ITEM".equals(bean.getTestResultItemType())){
                        MedicalTreatProject medicalTreatProject  = medicalTreatProjectService.getBeanByCode(bean.getTestResultItemCode());
                        if(medicalTreatProject==null){
                            MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
                            BeanUtils.copyProperties(bean, dataBean);
                            dataBean.setLimitScope(limitCode);
                            dataBean.setIndication(bean.getTestResultItemCode());
                            exportList.add(dataBean);
                        }else{
                            if(StringUtils.isBlank(itemMap.get(medicalTreatProject.getCode()+"&"+"STD_TREATMENT"))){
                                itemMap.put(medicalTreatProject.getCode()+"&"+"STD_TREATMENT",medicalTreatProject.getCode());
                                MedicalDrugRuleExportVO itemTmp = new MedicalDrugRuleExportVO();
                                itemTmp.setDrugCode(medicalTreatProject.getCode());
                                itemTmp.setDrugNames(medicalTreatProject.getName());
                                itemTmp.setMessage("STD_TREATMENT");
                                projectItemExportList.add(itemTmp);
                            }
                        }
                    }else {
                        String[] funcAndParams = limitScopeFunc.split(",");
                        if (funcAndParams.length > 0) {
                            String func = funcAndParams[0];
                            Map<String, Object> codeSetMap = new HashMap<>();
                            if ("getLoseGroupNameStr".equals(func)) {
                                String params = funcAndParams[1];
                                codeSetMap = this.getLoseGroupNameStr(params, jsonBean.getString(field));
                                if ("1".equals(params)) {//项目组
                                    projectGroupSet.addAll((Set<String>) codeSetMap.get("existsSet"));
                                    projectGroupMap.putAll((Map<String, Map<String, String>>) codeSetMap.get("existsMap"));
                                } else if ("5".equals(params)) {//疾病组
                                    diseaseGroupSet.addAll((Set<String>) codeSetMap.get("existsSet"));
                                    diseaseGroupMap.putAll((Map<String, Map<String, String>>) codeSetMap.get("existsMap"));
                                }

                            } else if ("getLoseStdAtcStr".equals(func)) {
                                codeSetMap = this.getLoseStdAtcStr(jsonBean.getString(field));
                                if (((Set<String>) codeSetMap.get("existsSet")).size() > 0) {
                                    stdAtcSet.addAll((Set<String>) codeSetMap.get("existsSet"));
                                    stdAtcMap.putAll((Map<String, Map<String, String>>) codeSetMap.get("existsMap"));
                                }
                            }
                            Set<String> lostCodes = (Set<String>) codeSetMap.get("noExistsSet");
                            if (lostCodes.size() > 0) {
                                for (String lostCode : lostCodes) {
                                    MedicalDrugRuleExportVO dataBean = new MedicalDrugRuleExportVO();
                                    BeanUtils.copyProperties(bean, dataBean);
                                    dataBean.setLimitScope(limitCode);
                                    dataBean.setIndication(lostCode);
                                    exportList.add(dataBean);
                                }
                            }
                        }
                    }
                }
            }
        }

        //已配置的STD_ATC
        for (Map<String,String> map : stdAtcMap.values()) {
            MedicalDrugRuleExportVO tmp = new MedicalDrugRuleExportVO();
            tmp.setDrugCode(map.get("code"));
            tmp.setDrugNames(map.get("name"));
            tmp.setMessage(map.get("table"));
            stdAtcExportList.add(tmp);
        }
        //已配置的疾病组
        for (Map<String,String> map : diseaseGroupMap.values()) {
            MedicalDrugRuleExportVO tmp = new MedicalDrugRuleExportVO();
            tmp.setDrugCode(map.get("code"));
            tmp.setDrugNames(map.get("name"));
            tmp.setMessage("STD_DIAGGROUP");
            diseaseGroupExportList.add(tmp);
            //获取疾病组明细
            List<MedicalDiseaseGroupItem> itemList = diseaseGroupItemService.list(new QueryWrapper<MedicalDiseaseGroupItem>().eq("GROUP_ID", map.get("id")));
            for(MedicalDiseaseGroupItem itemBean:itemList){
                if(StringUtils.isBlank(itemMap.get(itemBean.getCode()+"&"+itemBean.getTableType()))){
                    itemMap.put(itemBean.getCode()+"&"+itemBean.getTableType(),itemBean.getCode());
                    MedicalDrugRuleExportVO itemTmp = new MedicalDrugRuleExportVO();
                    itemTmp.setDrugCode(itemBean.getCode());
                    itemTmp.setDrugNames(itemBean.getValue());
                    itemTmp.setMessage(itemBean.getTableType());
                    diseaseItemExportList.add(itemTmp);
                }
            }
        }

        //已配置的项目组
        for (Map<String,String> map : projectGroupMap.values()) {
            MedicalDrugRuleExportVO tmp = new MedicalDrugRuleExportVO();
            tmp.setDrugCode(map.get("code"));
            tmp.setDrugNames(map.get("name"));
            tmp.setMessage("STD_TREATGROUP");
            projectGroupExportList.add(tmp);
            //获取项目组明细
            List<MedicalProjectGroupItem> itemList = projectGroupItemService.list(new QueryWrapper<MedicalProjectGroupItem>().eq("GROUP_ID", map.get("id")));
            for(MedicalProjectGroupItem itemBean:itemList){
                if(StringUtils.isBlank(itemMap.get(itemBean.getCode()+"&"+itemBean.getTableType()))){
                    itemMap.put(itemBean.getCode()+"&"+itemBean.getTableType(),itemBean.getCode());
                    MedicalDrugRuleExportVO itemTmp = new MedicalDrugRuleExportVO();
                    itemTmp.setDrugCode(itemBean.getCode());
                    itemTmp.setDrugNames(itemBean.getValue());
                    itemTmp.setMessage(itemBean.getTableType());
                    projectItemExportList.add(itemTmp);
                }
            }
        }

        String titleStr = "id主键,YX_药品剂型级别编码,YX_药品名称,提示信息,限定范围,缺失编码";
        String fieldStr = "ruleId,drugCode,drugNames,message,limitScope,indication";//导出的字段
        String sheetName = "药品合规规则编码失效明细";
        if ("2".equals(ruleType)) {
            titleStr = "id主键,收费项目编码,收费项目名称,提示信息,限定范围,缺失编码";
            fieldStr = "ruleId,chargeItemCodes,chargeItems,message,limitScope,indication";
            sheetName = "收费合规规则编码失效明细";
        }
        String[] titles = titleStr.split(",");
        String[] fields = fieldStr.split(",");
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, workbook, sheetName);
            ExportXUtils.exportExl(stdAtcExportList, MedicalDrugRuleExportVO.class, new String[] {"ATC编码/药品编码","ATC名称/药品名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, workbook, "已配置的药品");
            ExportXUtils.exportExl(diseaseGroupExportList, MedicalDrugRuleExportVO.class, new String[] {"疾病组编码","疾病组名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, workbook, "已配置的疾病组");
            ExportXUtils.exportExl(projectGroupExportList, MedicalDrugRuleExportVO.class, new String[] {"项目组编码","项目组名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, workbook, "已配置的项目组");
            ExportXUtils.exportExl(diseaseItemExportList, MedicalDrugRuleExportVO.class, new String[] {"疾病编码","疾病名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, workbook, "已配置的疾病");
            ExportXUtils.exportExl(projectItemExportList, MedicalDrugRuleExportVO.class, new String[] {"项目编码","项目名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, workbook, "已配置的项目");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet(sheetName, 0);
            ExportUtils.exportExl(exportList, MedicalDrugRuleExportVO.class, titles, fields, sheet, "");
            WritableSheet sheet2 = wwb.createSheet("已配置的STD_ATC", 1);
            ExportUtils.exportExl(stdAtcExportList, MedicalDrugRuleExportVO.class, new String[] {"ATC编码/药品编码","ATC名称/药品名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, sheet2, "");
            WritableSheet sheet3 = wwb.createSheet("已配置的疾病组", 2);
            ExportUtils.exportExl(diseaseGroupExportList, MedicalDrugRuleExportVO.class, new String[] {"疾病组编码","疾病组名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, sheet3, "");
            WritableSheet sheet4 = wwb.createSheet("已配置的项目组", 3);
            ExportUtils.exportExl(projectGroupExportList, MedicalDrugRuleExportVO.class, new String[] {"项目组编码","项目组名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, sheet4, "");
            WritableSheet sheet5 = wwb.createSheet("已配置的疾病", 4);
            ExportUtils.exportExl(diseaseItemExportList, MedicalDrugRuleExportVO.class, new String[] {"疾病编码","项目组名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, sheet5, "");
            WritableSheet sheet6 = wwb.createSheet("已配置的项目", 5);
            ExportUtils.exportExl(projectItemExportList, MedicalDrugRuleExportVO.class, new String[] {"项目编码","项目名称","所属STD表"}, new String[] {"drugCode","drugNames","message"}, sheet6, "");
            wwb.write();
            wwb.close();
        }

        //1.缺失明细
        //2.ATC
        //3.疾病组
        //4.项目组
        //5.疾病
        //6.项目
    }

    private Map<String, String> limitScopeFuncMap() {
        Map<String, String> limitScopeFuncMap = new HashMap<String, String>();
        limitScopeFuncMap.put("treatProject","getLoseGroupNameStr,1");
        limitScopeFuncMap.put("twoLimitDrug","getLoseStdAtcStr");
        limitScopeFuncMap.put("indication","getLoseGroupNameStr,5");
        limitScopeFuncMap.put("treatDrug","getLoseStdAtcStr");
        limitScopeFuncMap.put("twoLimitDrug2","getLoseStdAtcStr");
        //limitScopeFuncMap.put("healthOrgKind","getLoseOtherDictStr,Medical_Org_type");
        limitScopeFuncMap.put("fitGroupCodes","getLoseGroupNameStr,1");
        limitScopeFuncMap.put("unfitGroupCodes","getLoseGroupNameStr,1");
        limitScopeFuncMap.put("unfitGroupCodesDay","getLoseGroupNameStr,1");
        limitScopeFuncMap.put("unIndication","getLoseGroupNameStr,5");
        limitScopeFuncMap.put("DiseasegroupCodes","getLoseGroupNameStr,5");
        limitScopeFuncMap.put("testResultItemType","getLoseGroupNameStr,1");
        return limitScopeFuncMap;
    }



    private Map<String,Object> getLoseGroupNameStr(String kind, String groupCodes) {
        Set<String> existsSet = new HashSet<>();
        Set<String> noExistsSet = new HashSet<>();
        Map<String,Map<String,String>>  existsMap = new HashMap<>();
        if (StringUtils.isNotBlank(groupCodes)) {
            String[] groupArr = groupCodes.split("\\|");//组
            for (String groupArrStr : groupArr) {
                String[] codeArr = groupArrStr.split(",");
                for (String code : codeArr) {
                    Map<String,String> groupBean = medicalDrugGroupService.getBeanByGroupCode(kind, code);
                    if (groupBean==null) {//不存在
                        noExistsSet.add(code);
                    }else{
                        existsSet.add(code);
                        existsMap.put(code, new HashMap<String, String>() {{put("code", code);put("name", groupBean.get("groupName"));put("id", groupBean.get("groupId"));}});
                    }
                }
            }
        }
        Map<String,Object> data = new HashMap<>();
        data.put("existsSet",existsSet);
        data.put("noExistsSet",noExistsSet);
        data.put("existsMap",existsMap);
        return data;
    }


    private Map<String,Object> getLoseStdAtcStr(String groupCodes) {
        Set<String> existsSet = new HashSet<>();
        Set<String> noExistsSet = new HashSet<>();
        Map<String,Map<String,String>>  existsMap = new HashMap<>();
        if (StringUtils.isNotBlank(groupCodes)) {
            String[] groupArr = groupCodes.split("\\|");//组
            for (String groupArrStr : groupArr) {
                String[] codeArr = groupArrStr.split(",");
                for (String code : codeArr) {
                    MedicalStdAtc medicalStdAtc  = medicalStdAtcService.getBeanByCode(code);
                    if (medicalStdAtc==null) {//不存在
                        noExistsSet.add(code);
                    }else{
                        existsSet.add(code);
                        existsMap.put(code+"&"+"STD_ATC", new HashMap<String, String>() {{put("code", code);put("name", medicalStdAtc.getName());put("table", "STD_ATC");put("id", medicalStdAtc.getId());}});
                    }
                }
            }
        }
        Map<String,Object> data = new HashMap<>();
        data.put("existsSet",existsSet);
        data.put("noExistsSet",noExistsSet);
        data.put("existsMap",existsMap);
        return data;
    }

    private Map<String,Object> getLoseTreatProjectStr(String groupCodes) {
        Set<String> existsSet = new HashSet<>();
        Set<String> noExistsSet = new HashSet<>();
        Map<String,Map<String,String>>  existsMap = new HashMap<>();
        if (StringUtils.isNotBlank(groupCodes)) {
            String[] groupArr = groupCodes.split("\\|");//组
            for (String groupArrStr : groupArr) {
                String[] codeArr = groupArrStr.split(",");
                for (String code : codeArr) {
                    MedicalTreatProject medicalTreatProject  = medicalTreatProjectService.getBeanByCode(code);
                    if (medicalTreatProject==null) {//不存在
                        noExistsSet.add(code);
                    }else{
                        existsSet.add(code);
                        existsMap.put(code+"&"+"STD_TREATMENT", new HashMap<String, String>() {{put("code", code);put("name", medicalTreatProject.getName());put("table", "STD_ATC");put("id", medicalTreatProject.getId());}});
                    }
                }
            }
        }
        Map<String,Object> data = new HashMap<>();
        data.put("existsSet",existsSet);
        data.put("noExistsSet",noExistsSet);
        data.put("existsMap",existsMap);
        return data;
    }

    private Map<String,Object> getLoseEquipmentStr(String groupCodes) {
        Set<String> existsSet = new HashSet<>();
        Set<String> noExistsSet = new HashSet<>();
        Map<String,Map<String,String>>  existsMap = new HashMap<>();
        if (StringUtils.isNotBlank(groupCodes)) {
            String[] groupArr = groupCodes.split("\\|");//组
            for (String groupArrStr : groupArr) {
                String[] codeArr = groupArrStr.split(",");
                for (String code : codeArr) {
                    MedicalEquipment medicalEquipment  = medicalEquipmentService.getBeanByCode(code);
                    if (medicalEquipment==null) {//不存在
                        noExistsSet.add(code);
                    }else{
                        existsSet.add(code);
                        existsMap.put(code+"&"+"STD_MEDICAL_EQUIPMENT", new HashMap<String, String>() {{put("code", code);put("name", medicalEquipment.getProductname());put("table", "STD_ATC");put("id", medicalEquipment.getId());}});
                    }
                }
            }
        }
        Map<String,Object> data = new HashMap<>();
        data.put("existsSet",existsSet);
        data.put("noExistsSet",noExistsSet);
        data.put("existsMap",existsMap);
        return data;
    }
}
