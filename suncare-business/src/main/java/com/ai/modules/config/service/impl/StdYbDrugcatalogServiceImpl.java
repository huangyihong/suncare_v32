package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.StdYbDrugcatalog;
import com.ai.modules.config.mapper.StdYbDrugcatalogMapper;
import com.ai.modules.config.service.IStdYbDrugcatalogService;
import com.ai.modules.config.vo.StdYbDrugcatalogImport;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 药品医保目录
 * @Author: jeecg-boot
 * @Date:   2021-04-13
 * @Version: V1.0
 */
@Service
public class StdYbDrugcatalogServiceImpl extends ServiceImpl<StdYbDrugcatalogMapper, StdYbDrugcatalog> implements IStdYbDrugcatalogService {
    @Override
    public boolean isExistName(String code,String fileName, String id) {
        QueryWrapper<StdYbDrugcatalog> queryWrapper = new QueryWrapper<StdYbDrugcatalog>();
        queryWrapper.eq("DRUGCODE_YBREGISTER", code);
        if(StringUtils.isNotBlank(fileName)){
            queryWrapper.eq("FILE_NAME", fileName);
        }else{
            queryWrapper.isNull("FILE_NAME");
        }
        if(StringUtils.isNotBlank(id)){
            queryWrapper.ne("ID", id);
        }
        if(this.baseMapper.selectCount(queryWrapper)>0){
            return true;
        }
        return false;
    }

    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "id,drugcodeYbregister,drugnameYbregister,drugcode869,drugname869,drugcode,drugname,chargeclassId,chargeclass,drugcodeSrc,drugnameSrc,dosageNameSrc," +
                "specificaion,packageNum,preparationUnit,packageUnit,packMaterial," +
                "manufactorCode,manufactor,parentcode,parentname,chargeattriSrc,chargeattricode,chargeattriname,itemnote," +
                "projectAreaId,projectArea,owntype,owntypeName,startEndDateStr,fileName,updateReason,importActionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importExcel(file, user,mappingFields);
    }

    @Override
    public boolean exportExcel(List<StdYbDrugcatalog> list, OutputStream os, String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,医保药品代码,注册药品名称,药监药品本位码,药监药品名称,亚信药品编码,亚信药品名称,亚信药品收费类别编码,亚信药品收费类别名称,医保目录编码,医保药品名称(原始),剂型名称(原始)," +
                "规格,最小包装数量,最小制剂单位,最小包装单位,包装材质," +
                "药品生产企业编码,药品生产企业名称,药品父级编码,药品父级名称,收费项目等级名称(原始),收费项目等级编码(映射后),收费项目等级名称(映射后),备注," +
                "适用地id,适用地名称,适用的所有制形式,适用的所有制形式名称,有效起始日期,医保目录版本," +
                "创建人,创建时间,修改人,修改时间,修改原因";//导出的字段
        String[] titles = titleStr.split(",");
        String fieldStr = "id,drugcodeYbregister,drugnameYbregister,drugcode869,drugname869,drugcode,drugname,chargeclassId,chargeclass,drugcodeSrc,drugnameSrc,dosageNameSrc," +
                "specificaion,packageNum,preparationUnit,packageUnit,packMaterial," +
                "manufactorCode,manufactor,parentcode,parentname,chargeattriSrc,chargeattricode,chargeattriname,itemnote," +
                "projectAreaId,projectArea,owntype,owntypeName,startEndDateStr,fileName," +
                "createStaffName,createTime,updateStaffName,updateTime,updateReason";//导出的字段
        String[] fields = fieldStr.split(",");
        List<StdYbDrugcatalogImport> exportList = new ArrayList<StdYbDrugcatalogImport>();
        for (StdYbDrugcatalog bean : list) {
            StdYbDrugcatalogImport dataBean = new StdYbDrugcatalogImport();
            BeanUtils.copyProperties(bean, dataBean);
            //数据时间
            dataBean.setStartEndDateStr(DateUtils.date2Str(bean.getStartdate(),date_sdf)+"到"+DateUtils.date2Str(bean.getEnddate(),date_sdf));
            exportList.add(dataBean);
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, StdYbDrugcatalogImport.class, titles, fields, workbook, "药品物价目录");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("药品物价目录", 0);
            ExportUtils.exportExl(exportList, StdYbDrugcatalogImport.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<StdYbDrugcatalogImport> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(StdYbDrugcatalogImport.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(StdYbDrugcatalogImport.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始：" + DateUtils.now());
        String[] importActionTypeArr = {"0","1","2"};
        //字典值检验
        List<StdYbDrugcatalog> addUpdateList = new ArrayList<StdYbDrugcatalog>();
        List<String> deleteList = new ArrayList<String>();//删除id
        Set<String> codeFilenameSet = new HashSet<String>();
        List<String> codesAdd = new ArrayList<>();//新增编码
        List<String> codesUpdate = new ArrayList<>();//修改编码
        List<String> codesDelete = new ArrayList<>();//删除编码
        Map<String, StdYbDrugcatalog> addMap = new HashMap<>();
        Map<String,StdYbDrugcatalog> updateMap = new HashMap<>();
        Map<String,StdYbDrugcatalog> deleteMap = new HashMap<>();
        List<String> errorMsg =  new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            StdYbDrugcatalogImport beanVO = list.get(i);
            if (StringUtils.isBlank(beanVO.getDrugcodeYbregister())) {
                errorMsg.add("导入的数据中“医保药品代码”不能为空，如：第" + (i + 2) + "行数据“医保药品代码”为空");
            }
//            if (StringUtils.isBlank(beanVO.getDrugnameSrc())) {
//                errorMsg.add("导入的数据中“医保药品名称(原始)”不能为空，如：第" + (i + 2) + "行数据“医保药品名称(原始)”为空");
//            }
            if (StringUtils.isBlank(beanVO.getImportActionType())) {
                errorMsg.add("导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空");
            }

            if (!Arrays.asList(importActionTypeArr).contains(beanVO.getImportActionType())) {
                errorMsg.add("导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据");
            }

            if(StringUtils.isNotBlank(beanVO.getStartEndDateStr())){
                if(beanVO.getStartEndDateStr().split("到").length!=2){
                    errorMsg.add("导入的数据中“有效起始日期”格式无法识别，正确格式为：yyyy-MM-ddd到yyyy-MM-dd，如：第" + (i + 2) + "行数据“数据时间”为"+beanVO.getStartEndDateStr());
                }
            }
            //判断医保药品代码+医保目录版本在excel中是否重复
            if(codeFilenameSet.contains(beanVO.getDrugcodeYbregister()+'&'+beanVO.getFileName())){
                errorMsg.add("导入的数据中“医保药品代码+医保目录版本”不能重复，如：第" + (i + 2) + "行数据在excel中重复");
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
                    errorMsg.add("导入的数据中“有效起始日期”格式不正确，如：第" + (i + 2) + "行数据");
                }
            }

            if(StringUtils.isNotBlank(beanVO.getOwntype())){
                beanVO.setOwntype(beanVO.getOwntype().replace("|",","));
            }
            if(StringUtils.isNotBlank(beanVO.getOwntypeName())){
                beanVO.setOwntypeName(beanVO.getOwntypeName().replace("|",","));
            }

            if(StringUtils.isBlank(beanVO.getDrugcodeYbregister())){
                continue;
            }

            if("1".equals(beanVO.getImportActionType())) {//新增
//                if(this.isExistName(beanVO.getDrugcodeYbregister(),beanVO.getFileName(),null)){
//                    errorMsg.add("导入的数据中，新增数据中包含库中已存在的“(医保药品代码+医保目录版本)”记录，如：第" + (i + 2) + "行数据");
//                    continue;
//                }
                codesAdd.add(beanVO.getDrugcodeYbregister()+'&'+beanVO.getFileName());
                addMap.put(beanVO.getDrugcodeYbregister()+'&'+beanVO.getFileName(),beanVO);
            }else if("0".equals(beanVO.getImportActionType())) {//修改
                if(StringUtils.isBlank(beanVO.getId())){
                    errorMsg.add("导入的数据中，修改数据“id主键”不能为空，如：第" + (i + 2) + "行数据");
                    continue;
                }
//                if(this.isExistName(beanVO.getDrugcodeYbregister(),beanVO.getFileName(),beanVO.getId())){
//                    errorMsg.add("导入的数据中，修改数据中包含库中已存在的“(医保药品代码+医保目录版本)”记录，如：第" + (i + 2) + "行数据");
//                    continue;
//                }
                codesUpdate.add(beanVO.getId());
                updateMap.put(beanVO.getId(),beanVO);
            }else if("2".equals(beanVO.getImportActionType())) {//删除
                if(StringUtils.isBlank(beanVO.getId())){
                    errorMsg.add("导入的数据中，删除数据“id主键”不能为空，如：第" + (i + 2) + "行数据");
                    continue;
                }
                codesDelete.add(beanVO.getId());
                deleteMap.put(beanVO.getId(),beanVO);
            }
            codeFilenameSet.add(beanVO.getDrugcodeYbregister()+'&'+beanVO.getFileName());
        }

        if(codesAdd.size()>0){
            if(errorMsg.size()==0){
                addMap.forEach((k, bean) -> {
                    bean.setId(IdUtils.uuid());
                    bean.setCreateStaff(user.getId());
                    bean.setCreateStaffName(user.getRealname());
                    bean.setCreateTime(new Date());
                    addUpdateList.add(bean);
                });
            }
        }
        if(codesUpdate.size()>0){
            //不存在的记录
            List<StdYbDrugcatalog> existList = getBeanById(codesUpdate,null);
            List<String> existCode = existList.stream().map(StdYbDrugcatalog::getId).collect(Collectors.toList());
            List<String> notExistCode = codesUpdate.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
            if(notExistCode.size()>0){
                errorMsg.add("导入的数据中，修改数据中包含系统中不存在的ID数据，如：[" +
                        StringUtils.join(notExistCode, ",") + "]");
            }
            if(errorMsg.size()==0) {
                existList.forEach(oldBean -> {
                    StdYbDrugcatalog bean = updateMap.get(oldBean.getId());
                    bean.setId(oldBean.getId());
                    bean.setCreateTime(oldBean.getCreateTime());
                    bean.setCreateStaff(oldBean.getCreateStaff());
                    bean.setCreateStaffName(oldBean.getCreateStaffName());
                    bean.setUpdateStaff(user.getId());
                    bean.setUpdateStaffName(user.getRealname());
                    bean.setUpdateTime(new Date());
                    addUpdateList.add(bean);
                });
            }
        }
        if(addUpdateList.size()>0&&this.isBatchExistName(addUpdateList)){
            errorMsg.add("导入的数据中，新增修改数据中包含库中已存在的“(医保药品代码+医保目录版本)”记录,“(医保药品代码+医保目录版本)”需唯一");
        }
        if(codesDelete.size()>0){
            //不存在的记录
            List<StdYbDrugcatalog> existList = getBeanById(codesDelete,"ID".split(","));
            List<String> existCode = existList.stream().map(StdYbDrugcatalog::getId).collect(Collectors.toList());
            List<String> notExistCode = codesDelete.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
            if(notExistCode.size()>0){
                errorMsg.add("导入的数据中，删除数据中包含系统中不存在的ID数据，如：[" +
                        StringUtils.join(notExistCode, ",") + "]");
            }
            if(errorMsg.size()==0) {
                deleteList = codesDelete.stream().collect(Collectors.toList());
            }
        }
        if(errorMsg.size()>0){
            message = StringUtils.join(errorMsg, "\n");
        }
        if(StringUtils.isNotBlank(message)){
            message +="\n请核对数据后进行批量导入。";
            return Result.error(message);
        }else{
            System.out.println("开始插入时间："+ DateUtils.now() );//删除表
            //删除表
            if (deleteList.size() > 0) {
                List<HashSet<String>> idSetList = getIdSetList(deleteList,1000);
                if (idSetList.size() > 0) {
                    for (HashSet<String> idsSet : idSetList) {
                        this.baseMapper.delete(new QueryWrapper<StdYbDrugcatalog>().in("ID", idsSet));
                    }
                }
            }
            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 1000);//直接插入
            }
            System.out.println("结束导入时间："+ DateUtils.now() );
            message += "导入成功，共导入"+list.size()+"条数据。";
            return Result.ok(message,list.size());
        }
    }

    private List<StdYbDrugcatalog> getBeanByCode(List<String> codes,String[] fileds){
        List<StdYbDrugcatalog> alllist = new ArrayList<>();
        List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(codes,1000);
        for(Set<String> strList:setList){
            QueryWrapper<StdYbDrugcatalog> queryWrapper = new QueryWrapper<StdYbDrugcatalog>();
            queryWrapper.in("DRUGCODE_YBREGISTER",strList);
            if(fileds!=null&&fileds.length>0){
                queryWrapper.select(fileds);
            }
            alllist.addAll(this.baseMapper.selectList(queryWrapper));
        }
        return  alllist;
    }

    private List<StdYbDrugcatalog> getBeanById(List<String> ids,String[] fileds){
        List<StdYbDrugcatalog> alllist = new ArrayList<>();
        List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(ids,1000);
        for(Set<String> strList:setList){
            QueryWrapper<StdYbDrugcatalog> queryWrapper = new QueryWrapper<StdYbDrugcatalog>();
            queryWrapper.in("ID",strList);
            if(fileds!=null&&fileds.length>0){
                queryWrapper.select(fileds);
            }
            alllist.addAll(this.baseMapper.selectList(queryWrapper));
        }
        return  alllist;
    }

    private StdYbDrugcatalog selectByDrugcode(String Drugcode){
        QueryWrapper<StdYbDrugcatalog> queryWrapper = new QueryWrapper<StdYbDrugcatalog>();
        queryWrapper.eq("Drugcode", Drugcode);
        List<StdYbDrugcatalog> list = this.baseMapper.selectList(queryWrapper);
        if(list.size()>0){
            return list.get(0);
        }
        return null;
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


    private boolean isBatchExistName(List<StdYbDrugcatalog> list) {
        boolean flag = false;
        List<List<StdYbDrugcatalog>> allList = this.getSetList(list,500);
        for(List<StdYbDrugcatalog> batchList:allList){
            QueryWrapper<StdYbDrugcatalog> queryWrapper = new QueryWrapper<StdYbDrugcatalog>();
            for(StdYbDrugcatalog bean:batchList){
                queryWrapper.or(wrapper ->{
                    wrapper.eq("DRUGCODE_YBREGISTER", bean.getDrugcodeYbregister());
                    if(StringUtils.isNotBlank(bean.getFileName())){
                        wrapper.eq("FILE_NAME",bean.getFileName());
                    }else{
                        wrapper.isNull("FILE_NAME");
                    }

                    if(StringUtils.isNotBlank(bean.getId())){
                        wrapper.ne("ID",bean.getId());
                    }
                    return wrapper;
                });
            }
            if(this.baseMapper.selectCount(queryWrapper)>0){
                flag = true;
                break;
            }
        }
       return flag;
    }

   private List<List<StdYbDrugcatalog>> getSetList(List<StdYbDrugcatalog> list, int size) {
        List<List<StdYbDrugcatalog>> allList = new ArrayList<List<StdYbDrugcatalog>>();
        List<StdYbDrugcatalog> batchList = new ArrayList<StdYbDrugcatalog>();
        if(list.size()<size){
            allList.add(list);
            return allList;
        }
        for (StdYbDrugcatalog bean : list) {
            if (batchList.size() >= size) {
                allList.add(batchList);
                batchList = new ArrayList<StdYbDrugcatalog>();
            }
            batchList.add(bean);
        }
        if (batchList.size() > 0) {
            allList.add(batchList);
        }
        return allList;
    }
}
