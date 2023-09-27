package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.config.mapper.MedicalYbDrugMapper;
import com.ai.modules.config.service.IMedicalYbDrugService;
import com.ai.modules.config.vo.MedicalYbDrugVO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 重复用药
 * @Author: jeecg-boot
 * @Date:   2021-06-08
 * @Version: V1.0
 */
@Service
public class MedicalYbDrugServiceImpl extends ServiceImpl<MedicalYbDrugMapper, MedicalYbDrug> implements IMedicalYbDrugService {

    @Override
    @Transactional
    public void updateGroup(MedicalYbDrug medicalYbDrug, String codes, String names, String tableTypes, String dosageCodes,String dosages, LoginUser user) throws Exception {
        // 删除子项
       int count =  this.baseMapper.delete(new QueryWrapper<MedicalYbDrug>()
                .eq("PARENT_CODE", medicalYbDrug.getParentCode()));
        // 插入子项
        Set<String> codeSet = new HashSet<String>();
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            String[] tableTypeArray = tableTypes.split(",");
            String[] dosageCodeArray = dosageCodes.split(",");
            String[] dosageArray = dosages.split(",");
            MedicalYbDrug bean = new MedicalYbDrug();
            bean.setParentCode(medicalYbDrug.getParentCode());
            bean.setParentName(medicalYbDrug.getParentName());
            bean.setRemark(medicalYbDrug.getRemark());
            bean.setRuleGrade(medicalYbDrug.getRuleGrade());
            bean.setRuleGradeRemark(medicalYbDrug.getRuleGradeRemark());
            bean.setCreateStaff(user.getId());
            bean.setCreateStaffName(user.getRealname());
            bean.setCreateTime(new Date());
            bean.setUpdateStaff(user.getId());
            bean.setUpdateStaffName(user.getRealname());
            bean.setUpdateTime(new Date());
            for(int i = 0, len = codeArray.length; i < len; i++){
                bean.setId(IdUtils.uuid());
                bean.setIsOrder((long)i);
                bean.setCode(codeArray[i]);
                bean.setName(nameArray[i]);
                bean.setTableType(tableTypeArray[i]);
                bean.setDosageCode(dosageCodeArray[i]);
                bean.setDosage(dosageArray[i]);
                //判断code是否重复
                if(codeSet.contains(bean.getParentCode()+"&&"+bean.getCode())){
                    throw new Exception("相同的父级编码中的“药品编码”不能重复，如：药品编码为“"+bean.getCode()+"”重复");
                }
                codeSet.add(bean.getParentCode()+"&&"+bean.getCode()+"&&"+bean.getDosageCode());
                this.baseMapper.insert(bean);
            }
        }
    }

    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "parentCode,parentName,code,name,dosageCode,dosage,tableType,isOrder,ruleGrade,ruleGradeRemark,actionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importExcel(file, user,mappingFields);
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalYbDrugVO> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalYbDrugVO.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalYbDrugVO.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        Set<String> codeSet = new HashSet<String>();
        List<MedicalYbDrug> addUpdateList = new ArrayList<MedicalYbDrug>();
        List<String> deleteList = new ArrayList<String>();
        System.out.println("校验开始："+DateUtils.now() );
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalYbDrugVO bean = list.get(i);
            if (StringUtils.isBlank(bean.getParentCode())) {
                message += "导入的数据中“父级编码”不能为空，如：第" + (i + 2) + "行数据“父级编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getParentName())) {
                message += "导入的数据中“父级名称”不能为空，如：第" + (i + 2) + "行数据“父级名称”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getCode())) {
                message += "导入的数据中“药品编码”不能为空，如：第" + (i + 2) + "行数据“药品编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getName())) {
                message += "导入的数据中“药品名称”不能为空，如：第" + (i + 2) + "行数据“药品名称”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getTableType())) {
                message += "导入的数据中“所属类别”不能为空，如：第" + (i + 2) + "行数据“所属类别”为空\n";
                flag = false;
            }else{
                bean.setTableType(bean.getTableType().toUpperCase());
            }
            if (StringUtils.isBlank(bean.getActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }
            if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(bean.getActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }
            //判断code在excel中是否重复
            if(codeSet.contains(bean.getParentCode()+"&&"+bean.getCode())){
                message += "导入的数据中相同的父级编码中的“药品编码”不能重复，如：第" + (i + 2) + "行数据父级编码为“"+bean.getParentCode()+"药品编码为“"+bean.getCode()+"”在excel中重复\n";
                flag = false;
            }
            if("1".equals(bean.getActionType())) {//新增
                if(this.selectByParentCodeAndCode(bean.getParentCode(),bean.getCode())!=null) {
                    message += "导入的数据中相同的父级编码中“药品编码”在库中已存在，无法新增，如：第" + (i + 2) + "行数据父级编码为“"+bean.getParentCode()+"药品编码为“"+bean.getCode()+"”\n";
                    flag = false;
                }else{
                    bean.setId(IdUtils.uuid());
                    bean.setCreateStaff(user.getId());
                    bean.setCreateStaffName(user.getRealname());
                    bean.setCreateTime(new Date());
                }
                addUpdateList.add(bean);
            }else if("0".equals(bean.getActionType())) {//修改
                MedicalYbDrug oldBean = this.selectByParentCodeAndCode(bean.getParentCode(),bean.getCode());
                if(oldBean==null) {
                    message += "导入的数据中该数据在库中不存在，无法修改，如：第" + (i + 2) + "行数据父级编码为“"+bean.getParentCode()+"药品编码为“"+bean.getCode()+"”\n";
                    flag = false;
                }else{
                   bean.setId(oldBean.getId());
                   bean.setCreateStaff(bean.getCreateStaff());
                   bean.setCreateStaffName(bean.getCreateStaffName());
                   bean.setCreateTime(bean.getCreateTime());
                }
                addUpdateList.add(bean);
            }else if("2".equals(bean.getActionType())) {//删除
                MedicalYbDrug oldBean = this.selectByParentCodeAndCode(bean.getParentCode(),bean.getCode());
                if(oldBean==null) {
                    message += "导入的数据中该数据在库中不存在，无法删除，如：第" + (i + 2) + "行数据父级编码为“"+bean.getParentCode()+"药品编码为“"+bean.getCode()+"”\n";
                    flag = false;
                }else{
                    deleteList.add(oldBean.getId());
                }

            }
            if(!flag) {
                continue;
            }
            codeSet.add(bean.getParentCode()+"&&"+bean.getCode());

        }
        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }else{
            System.out.println("开始插入时间："+DateUtils.now() );
            //删除表
            if (deleteList.size() > 0) {
                List<HashSet<String>> idSetList = getIdSetList(deleteList,1000);
                if (idSetList.size() > 0) {
                    for (HashSet<String> idsSet : idSetList) {
                        this.baseMapper.delete(new QueryWrapper<MedicalYbDrug>().in("ID", idsSet));
                    }
                }
            }
            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 1000);//直接插入
            }
            message += "导入成功，共导入"+list.size()+"条数据。";
            System.out.println("结束导入时间："+DateUtils.now() );
            return Result.ok(message,list.size());
        }
    }

    @Override
    public boolean exportExcel(List<MedicalYbDrug> list, OutputStream os, String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "父级编码,父级名称,药品编码,药品名称,剂型代码,剂型名称,所属类别,顺序号,规则级别,级别备注";
        String[] titles= titleStr.split(",");
        String fieldStr = "parentCode,parentName,code,name,dosageCode,dosage,tableType,isOrder,ruleGrade,ruleGradeRemark";
        String[] fields = fieldStr.split(",");
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(list, MedicalYbDrug.class, titles, fields, workbook, "重复用药");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("重复用药", 0);
            ExportUtils.exportExl(list, MedicalYbDrug.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }


    private MedicalYbDrug selectByParentCodeAndCode(String parentCode,String code){
        QueryWrapper<MedicalYbDrug> queryWrapper = new QueryWrapper<MedicalYbDrug>();
        queryWrapper.eq("PARENT_CODE", parentCode);
        queryWrapper.eq("CODE", code);
        List<MedicalYbDrug> list = this.baseMapper.selectList(queryWrapper);
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


}
