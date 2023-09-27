package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.config.mapper.StdHoslevelFundpaypropMapper;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.config.service.IStdHoslevelFundpaypropService;
import com.ai.modules.config.vo.StdHoslevelFundpaypropImport;
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
 * @Description: 各地不同物价级别报销比例
 * @Author: jeecg-boot
 * @Date:   2020-11-16
 * @Version: V1.0
 */
@Service
public class StdHoslevelFundpaypropServiceImpl extends ServiceImpl<StdHoslevelFundpaypropMapper, StdHoslevelFundpayprop> implements IStdHoslevelFundpaypropService {
    @Autowired
    private IMedicalOtherDictService medicalOtherDictService;


    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "id,project,hosplevel,hosplevelName,fundpayprop,startEndDateStr,importActionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
		return importExcel(file, user,mappingFields);
    }

    @Override
    public boolean exportExcel(List<StdHoslevelFundpayprop> list, OutputStream os,String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,项目地名称,物价级别编码,物价级别名称,报销比例(%),适用时间"
                + ",创建人,创建时间,修改人,修改时间";
        String[] titles = titleStr.split(",");
        String fieldStr = "id,project,hosplevel,hosplevelName,fundpayprop,startEndDateStr"
                + ",createUsername,createTime,updateUsername,updateTime";//导出的字段
        String[] fields = fieldStr.split(",");
        List<StdHoslevelFundpaypropImport> exportList = new ArrayList<StdHoslevelFundpaypropImport>();
        for (StdHoslevelFundpayprop bean : list) {
            StdHoslevelFundpaypropImport dataBean = new StdHoslevelFundpaypropImport();
            BeanUtils.copyProperties(bean, dataBean);
            //数据时间
            dataBean.setStartEndDateStr(DateUtils.date2Str(bean.getStartdate(),date_sdf)+"到"+DateUtils.date2Str(bean.getEnddate(),date_sdf));
            exportList.add(dataBean);
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, StdHoslevelFundpaypropImport.class, titles, fields, workbook, "各地不同物价级别报销比例");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("各地不同物价级别报销比例", 0);
            ExportUtils.exportExl(exportList, StdHoslevelFundpaypropImport.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间："+DateUtils.now() );
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<StdHoslevelFundpaypropImport> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(StdHoslevelFundpaypropImport.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(StdHoslevelFundpaypropImport.class, mappingFields, 0, 1, file.getInputStream());
        }
        if(list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始："+DateUtils.now() );
        String[] importActionTypeArr = {"0","1","2"};
        //字典值检验
        Map<String, String> dictMap = medicalOtherDictService.queryMapByType("price_level");
        List<StdHoslevelFundpayprop> addUpdateList = new ArrayList<StdHoslevelFundpayprop>();
        List<String> deleteList = new ArrayList<String>();//删除id
        Set<String> existSet = new HashSet<String>();
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            StdHoslevelFundpaypropImport beanVO = list.get(i);
            if (StringUtils.isBlank(beanVO.getProject())) {
                message += "导入的数据中“项目地名称”不能为空，如：第" + (i + 2) + "行数据“项目地名称”为空\n";
                flag = false;
            }
            if (beanVO.getFundpayprop()<=0||beanVO.getFundpayprop()>100) {
                message += "导入的数据中“报销比例”不能小于0或者超过100，如：第" + (i + 2) + "行数据“报销比例”为小于0或者超过100\n";
                flag = false;
            }
            if (StringUtils.isNotBlank(beanVO.getHosplevel())&&StringUtils.isBlank(dictMap.get(beanVO.getHosplevel()))) {
                message += "导入的数据中“物价级别编码”在系统其他字典中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }else{
                beanVO.setHosplevelName(dictMap.get(beanVO.getHosplevel()));
            }
            //判断该项目地物价级别时间范围的报销比例在excel中是否重复
            if(existSet.contains(beanVO.getProject()+"&"+beanVO.getHosplevel()+"&"+beanVO.getStartEndDateStr())){
                message += "导入的数据中该项目地物价级别时间范围的报销比例在excel已存在，如：第" + (i + 2) + "行数据在excel中重复\n";
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

            if(!flag) {
                continue;
            }

            if ("1".equals(beanVO.getImportActionType())) {//新增
                beanVO.setId(IdUtils.uuid());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                if (StringUtils.isBlank(beanVO.getId())) {
                    message += "导入的数据中“id主键”不能为空，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                    flag = false;
                    continue;
                }
                //判断数据是否存在
                StdHoslevelFundpayprop oldBean = this.baseMapper.selectById(beanVO.getId());
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
            StdHoslevelFundpayprop bean = beanVO;
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
            if("1".equals(beanVO.getImportActionType())||"0".equals(beanVO.getImportActionType())){
                boolean flag1 = this.isExist(bean);
                if(flag1){
                    message += "导入的数据中该项目地物价级别时间范围的报销比例数据在库中已存在，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }else{
                    existSet.add(beanVO.getProject()+"&"+beanVO.getHosplevel()+"&"+beanVO.getStartEndDateStr());
                }
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
                        this.baseMapper.delete(new QueryWrapper<StdHoslevelFundpayprop>().in("ID", idsSet));
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

    // 判断同一个项目地同一个级别同一个时间范围数据是否已存在
    @Override
    public boolean isExist(StdHoslevelFundpayprop bean) {
        QueryWrapper<StdHoslevelFundpayprop> queryWrapper = new QueryWrapper<StdHoslevelFundpayprop>();
        queryWrapper.eq("PROJECT", bean.getProject());
        queryWrapper.eq("HOSPLEVEL", bean.getHosplevel());
        queryWrapper.eq("STARTDATE", bean.getStartdate());
        queryWrapper.eq("ENDDATE", bean.getEnddate());
        if(StringUtils.isNotBlank(bean.getId())){
            queryWrapper.ne("ID",bean.getId());
        }
        List<StdHoslevelFundpayprop> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }
}
