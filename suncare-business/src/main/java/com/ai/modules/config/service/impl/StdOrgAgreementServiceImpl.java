package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.StdOrgAgreement;
import com.ai.modules.config.mapper.StdOrgAgreementMapper;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.config.service.IStdOrgAgreementService;
import com.ai.modules.config.vo.StdOrgAgreementImport;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 医疗机构医保协议相关参数
 * @Author: jeecg-boot
 * @Date:   2020-12-03
 * @Version: V1.0
 */
@Service
public class StdOrgAgreementServiceImpl extends ServiceImpl<StdOrgAgreementMapper, StdOrgAgreement> implements IStdOrgAgreementService {
    @Autowired
    private IMedicalOtherDictService medicalOtherDictService;

    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "orgid,orgname,surancetypecode,surancetypename,pertimeZydays,drgsettleRatio,zypertimeAmt,zyDayavgAmt,medFeeratio,outConvertInRatio,fundpayRatio,startEndDateStr,policybasis,importActionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importExcel(file, user,mappingFields);
    }

    @Override
    public boolean exportExcel(List<StdOrgAgreement> list, OutputStream os, String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "医疗机构编码,医疗机构名称,医疗保险类别代码,医疗保险类别名称,均次住院天数,单病种结算率(%),均次住院费用,床日费用,药费占住院费用比例(%),百人门诊住院率(%),实际报销比例(%),适用时间,政策依据"
                + ",创建人,创建时间,修改人,修改时间";
        String[] titles = titleStr.split(",");
        String fieldStr = "orgid,orgname,surancetypecode,surancetypename,pertimeZydays,drgsettleRatio,zypertimeAmt,zyDayavgAmt,medFeeratio,outConvertInRatio,fundpayRatio,startEndDateStr,policybasis"
                + ",createUsername,createTime,updateUsername,updateTime";//导出的字段
        String[] fields = fieldStr.split(",");
        List<StdOrgAgreementImport> exportList = new ArrayList<StdOrgAgreementImport>();
        for (StdOrgAgreement bean : list) {
            StdOrgAgreementImport dataBean = new StdOrgAgreementImport();
            BeanUtils.copyProperties(bean, dataBean);
            //数据时间
            dataBean.setStartEndDateStr(DateUtils.date2Str(bean.getStartdate(),date_sdf)+"到"+DateUtils.date2Str(bean.getEnddate(),date_sdf));
            exportList.add(dataBean);
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, StdOrgAgreementImport.class, titles, fields, workbook, "医疗机构医保协议相关参数");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("医疗机构医保协议相关参数", 0);
            ExportUtils.exportExl(exportList, StdOrgAgreementImport.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间："+DateUtils.now() );
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<StdOrgAgreementImport> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(StdOrgAgreementImport.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(StdOrgAgreementImport.class, mappingFields, 0, 1, file.getInputStream());
        }
        if(list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始："+DateUtils.now() );
        String[] importActionTypeArr = {"0","1","2"};
        //字典值检验
        Map<String, String> dictMap = medicalOtherDictService.queryMapByType("medinsuranceType");
        List<StdOrgAgreement> addUpdateList = new ArrayList<StdOrgAgreement>();
        List<String> deleteList = new ArrayList<String>();//删除id
        Set<String> codeSet = new HashSet<String>();
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            StdOrgAgreementImport beanVO = list.get(i);
            if (StringUtils.isBlank(beanVO.getOrgid())) {
                message += "导入的数据中“医疗机构编码”不能为空，如：第" + (i + 2) + "行数据“医疗机构编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(beanVO.getOrgname())) {
                message += "导入的数据中“医疗机构名称”不能为空，如：第" + (i + 2) + "行数据“医疗机构名称”为空\n";
                flag = false;
            }
            if (StringUtils.isNotBlank(beanVO.getSurancetypecode())&&StringUtils.isBlank(dictMap.get(beanVO.getSurancetypecode()))) {
                message += "导入的数据中“医疗保险类别代码”在系统其他字典中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }else{
                beanVO.setSurancetypename(dictMap.get(beanVO.getSurancetypecode()));
            }
            if (StringUtils.isBlank(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }

            if (!Arrays.asList(importActionTypeArr).contains(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }

            if(StringUtils.isNotBlank(beanVO.getStartEndDateStr())){
                if(beanVO.getStartEndDateStr().split("到").length!=2){
                    message += "导入的数据中“数据时间”格式无法识别，正确格式为：yyyy-MM-ddd到yyyy-MM-dd，如：第" + (i + 2) + "行数据“数据时间”为"+beanVO.getStartEndDateStr()+"\n";
                    flag = false;
                }
            }
            //判断医疗机构编码+医疗保险类别代码+适用时间在excel中是否重复
            if(codeSet.contains(beanVO.getOrgid()+"&"+beanVO.getSurancetypecode()+"&"+beanVO.getStartEndDateStr())){
                message += "导入的数据中“医疗机构编码+医疗保险类别代码+适用时间”不能重复，如：第" + (i + 2) + "行数据在excel中重复\n";
                flag = false;
            }

            if(StringUtils.isBlank(beanVO.getStartEndDateStr())){
                beanVO.setStartdate(DateUtils.str2Date("2000-01-01",date_sdf));
                beanVO.setEnddate(DateUtils.str2Date("2099-12-31",date_sdf));
            }else{
                String[] startAndEndTime = beanVO.getStartEndDateStr().split("到");
                try {
                    beanVO.setStartdate(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                    beanVO.setEnddate(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                }catch (Exception e) {
                    message += "导入的数据中“适用时间”格式不正确，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }

            if(!flag) {
                continue;
            }



            if ("1".equals(beanVO.getImportActionType())) {//新增
                if(isExist(beanVO.getOrgid(),null,beanVO.getSurancetypecode(),beanVO.getStartdate(),beanVO.getEnddate())){
                    message += "导入的数据中，新增数据中包含库中已存在的“医疗机构编码+医疗保险类别代码+适用时间”记录，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
                if(!flag) {
                    continue;
                }
                beanVO.setId(IdUtils.uuid());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                //判断数据是否存在
                StdOrgAgreement oldBean = this.selectByOrgid(beanVO.getOrgid(),beanVO.getSurancetypecode(),beanVO.getStartdate(),beanVO.getEnddate());
                if (oldBean == null) {
                    message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                } else {
                    beanVO.setId(oldBean.getId());
                    beanVO.setCreateTime(oldBean.getCreateTime());
                    beanVO.setCreateUser(oldBean.getCreateUser());
                    beanVO.setCreateUsername(oldBean.getCreateUsername());
                }
            }

            if (!flag) {
                continue;
            }
            StdOrgAgreement bean = beanVO;
            if (!flag) {
                continue;
            }
            //生成新增的addUpdateList
            if ("1".equals(beanVO.getImportActionType()) || "0".equals(beanVO.getImportActionType())) {//新增、修改
                addUpdateList.add(bean);
            }else{
                deleteList.add(beanVO.getId());//删除时
            }
            codeSet.add(beanVO.getOrgid()+"&"+beanVO.getSurancetypecode()+"&"+beanVO.getStartEndDateStr());
        }
        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }else{
            System.out.println("开始插入时间："+ DateUtils.now() );//删除表
            //删除表
            if (deleteList.size() > 0) {
                List<HashSet<String>> idSetList = getIdSetList(deleteList,1000);
                if (idSetList.size() > 0) {
                    for (HashSet<String> idsSet : idSetList) {
                        this.baseMapper.delete(new QueryWrapper<StdOrgAgreement>().in("ID", idsSet));
                    }
                }
            }
            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 1000);//直接插入
            }
            System.out.println("结束导入时间："+DateUtils.now() );
            message += "导入成功，共导入"+list.size()+"条数据。";
            return Result.ok(message,list.size());
        }
    }

    private List<HashSet<String>> getIdSetList(List<String> idList, int size) {
        List<HashSet<String>> idSetList = new ArrayList<HashSet<String>>();
        HashSet<String> idSet = new HashSet<String>();
        for (String id : idList) {
            if (idSet.size() >= size) {
                idSetList.add(idSet);
                idSet = new HashSet<String>();
            }
            idSet.add(id);
        }
        if (idSet.size() > 0) {
            idSetList.add(idSet);
        }
        return idSetList;
    }

    @Override
    public boolean isExist(String orgid,String id,String surancetypecode,Date startdate,Date enddate) {
        QueryWrapper<StdOrgAgreement> queryWrapper = new QueryWrapper<StdOrgAgreement>();
        queryWrapper.eq("ORGID", orgid);
        if(StringUtils.isNotBlank(id)){
            queryWrapper.ne("ID",id);
        }
        queryWrapper.eq("SURANCETYPECODE", surancetypecode);
        queryWrapper.eq("STARTDATE", startdate);
        queryWrapper.eq("ENDDATE", enddate);
        List<StdOrgAgreement> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }


    private StdOrgAgreement selectByOrgid(String orgid,String surancetypecode,Date startdate,Date enddate){
        QueryWrapper<StdOrgAgreement> queryWrapper = new QueryWrapper<StdOrgAgreement>();
        queryWrapper.eq("ORGID", orgid);
        if(StringUtils.isNotBlank(surancetypecode)){
            queryWrapper.eq("SURANCETYPECODE", surancetypecode);
        }else{
            queryWrapper.isNull("SURANCETYPECODE");
        }
        queryWrapper.eq("STARTDATE", startdate);
        queryWrapper.eq("ENDDATE", enddate);
        List<StdOrgAgreement> list = this.baseMapper.selectList(queryWrapper);
        if(list.size()>0){
           return list.get(0);
        }
        return null;
    }
}
