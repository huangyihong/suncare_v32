package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalPolicyBasis;
import com.ai.modules.config.mapper.MedicalPolicyBasisMapper;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.config.service.IMedicalPolicyBasisService;
import com.ai.modules.config.vo.MedicalPolicyBasisImport;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 政策法规
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
@Service
public class MedicalPolicyBasisServiceImpl extends ServiceImpl<MedicalPolicyBasisMapper, MedicalPolicyBasis> implements IMedicalPolicyBasisService {

    @Value("${jeecg.path.upload}")
    String UPLOAD_PATH;

    @Autowired
    private IMedicalOtherDictService medicalOtherDictService;

    @Override
    public boolean isExistName(String name, String id) {
        QueryWrapper<MedicalPolicyBasis> queryWrapper = new QueryWrapper<MedicalPolicyBasis>();
        queryWrapper.eq("NAME", name);
        if(StringUtils.isNotBlank(id)){
            queryWrapper.notIn("ID", id);
        }
        List<MedicalPolicyBasis> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        MedicalPolicyBasis bean = this.getById(id);
        deleteFiles(bean);
        this.removeById(id);
    }

    //删除文件
    private void deleteFiles(MedicalPolicyBasis bean) {
        if(StringUtils.isNotBlank(bean.getFilenames())){
            String[] filePaths = bean.getFilenames().split(",");
            for(String path: filePaths){
                File file = new File(UPLOAD_PATH+ File.separator +path);
                if(file.exists()) {
                    file.delete();
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteByIds(List<String> idList) {
        List<HashSet<String>> idSetList = getIdSetList(idList,1000);
        if (idSetList.size() > 0) {
            for (HashSet<String> idsSet : idSetList) {
                //删除文件
                List<MedicalPolicyBasis> list = this.baseMapper.selectList(new QueryWrapper<MedicalPolicyBasis>()
                        .in("ID",idsSet));
                for(MedicalPolicyBasis bean:list){
                    deleteFiles(bean);
                }
                this.removeByIds(idsSet);
            }
        }
    }

    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception {
        String mappingFieldStr = "id,policyTypeName,effectLevelName,name,policyNumber,issuingOffice,applyArea,applyPeople,startEndDateStr,remark,importActionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importExcel(file, user,mappingFields);
    }

    @Override
    public boolean exportExcel(List<MedicalPolicyBasis> list, OutputStream os, String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,政策类型,效力级别,名称,政策文号,发文机关,适用地区,适用人群,适用时间,备注"
                + ",创建人,创建时间,修改人,修改时间";
        String[] titles = titleStr.split(",");
        String fieldStr = "id,policyTypeName,effectLevelName,name,policyNumber,issuingOffice,applyArea,applyPeople,startEndDateStr,remark"
                + ",createUsername,createTime,updateUsername,updateTime";//导出的字段
        String[] fields = fieldStr.split(",");
        List<MedicalPolicyBasisImport> exportList = new ArrayList<MedicalPolicyBasisImport>();
        for (MedicalPolicyBasis bean : list) {
            MedicalPolicyBasisImport dataBean = new MedicalPolicyBasisImport();
            BeanUtils.copyProperties(bean, dataBean);
            //数据时间
            dataBean.setStartEndDateStr(DateUtils.date2Str(bean.getStartdate(),date_sdf)+"到"+DateUtils.date2Str(bean.getEnddate(),date_sdf));
            exportList.add(dataBean);
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, MedicalPolicyBasisImport.class, titles, fields, workbook, "政策法规");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("政策法规", 0);
            ExportUtils.exportExl(exportList, MedicalPolicyBasisImport.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间："+DateUtils.now() );
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalPolicyBasisImport> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalPolicyBasisImport.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalPolicyBasisImport.class, mappingFields, 0, 1, file.getInputStream());
        }
        if(list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始："+DateUtils.now() );
        String[] importActionTypeArr = {"0","1","2"};
        //字典值检验
        List<MedicalPolicyBasis> addUpdateList = new ArrayList<MedicalPolicyBasis>();
        List<String> deleteList = new ArrayList<String>();//删除id
        Set<String> nameSet = new HashSet<String>();
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalPolicyBasisImport beanVO = list.get(i);
            if (StringUtils.isBlank(beanVO.getName())) {
                message += "导入的数据中“名称”不能为空，如：第" + (i + 2) + "行数据“名称”为空\n";
                flag = false;
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
            if(StringUtils.isNotBlank(beanVO.getPolicyTypeName())){
                beanVO.setPolicyTypeCode(medicalOtherDictService.getCodeByValue("rule_sourcetype", beanVO.getPolicyTypeName()));
            }
            if(StringUtils.isNotBlank(beanVO.getApplyArea())){
                beanVO.setApplyAreaId(medicalOtherDictService.getCodeByValue("region", beanVO.getApplyArea()));
            }

            if(!flag) {
                continue;
            }

            if ("1".equals(beanVO.getImportActionType())) {//新增
                beanVO.setId(IdUtils.uuid());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                if (StringUtils.isBlank(beanVO.getId())) {
                    message += "导入的数据中“id主键”不能为空，如：第" + (i + 2) + "行数据，无主键id无法修改或删除\n";
                    flag = false;
                }
                //判断数据是否存在
                MedicalPolicyBasis oldBean = this.getById(beanVO.getId());
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
            MedicalPolicyBasis bean = beanVO;
            if(StringUtils.isBlank(beanVO.getStartEndDateStr())){
                bean.setStartdate(DateUtils.str2Date("2000-01-01",date_sdf));
                bean.setEnddate(DateUtils.str2Date("2099-12-31",date_sdf));
            }else{
                String[] startAndEndTime = beanVO.getStartEndDateStr().split("到");
                try {
                    bean.setStartdate(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                    bean.setEnddate(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                }catch (Exception e) {
                    message += "导入的数据中“适用时间”格式不正确，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }
            if(this.isExistName(bean.getName(),bean.getId())){
                message += "导入的数据中“名称”重复，如：第" + (i + 2) + "行数据在系统中已存在\n";
                flag = false;
            }
            if (!flag) {
                continue;
            }
            //生成新增的addUpdateList
            if ("1".equals(beanVO.getImportActionType()) || "0".equals(beanVO.getImportActionType())) {//新增、修改
                addUpdateList.add(bean);
            }else{
                deleteList.add(beanVO.getId());//删除时
            }
            nameSet.add(bean.getName());
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
                        this.baseMapper.delete(new QueryWrapper<MedicalPolicyBasis>().in("ID", idsSet));
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
    public MedicalPolicyBasis getBeanByName(String name) {
        QueryWrapper<MedicalPolicyBasis> queryWrapper = new QueryWrapper<MedicalPolicyBasis>();
        queryWrapper.eq("NAME", name);

        List<MedicalPolicyBasis> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
    }
}
