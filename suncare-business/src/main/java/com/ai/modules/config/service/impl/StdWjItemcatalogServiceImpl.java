package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.StdWjItemcatalog;
import com.ai.modules.config.mapper.StdWjItemcatalogMapper;
import com.ai.modules.config.service.IStdWjItemcatalogService;
import com.ai.modules.config.vo.StdWjItemcatalogImport;
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

/**
 * @Description: 医疗服务项目物价目录
 * @Author: jeecg-boot
 * @Date:   2021-04-08
 * @Version: V1.0
 */
@Service
public class StdWjItemcatalogServiceImpl extends ServiceImpl<StdWjItemcatalogMapper, StdWjItemcatalog> implements IStdWjItemcatalogService {

    @Override
    public boolean isExistName(String code, String id) {
        QueryWrapper<StdWjItemcatalog> queryWrapper = new QueryWrapper<StdWjItemcatalog>();
        queryWrapper.eq("ITEMCODE", code);
        if(StringUtils.isNotBlank(id)){
            queryWrapper.notIn("ID", id);
        }
        List<StdWjItemcatalog> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }

    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "id,itemcode,itemname,chargeclassIdSrc,chargeclassSrc,itemcodeSrc,itemnameSrc,itemContent,exceptContent,chargeunit," +
                "itempriceProvince,itemprice1,itemprice2,itemprice3,itempricecounty1,itempricecounty2,itempricecounty3,itemnote,specificaion,pack,manufactorCode,manufactor,parentcode,parentname," +
                "projectAreaId,projectArea,owntype,owntypeName,startEndDateStr,fileName,updateReason,importActionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importExcel(file, user,mappingFields);
    }

    @Override
    public boolean exportExcel(List<StdWjItemcatalog> list, OutputStream os, String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "id主键,亚信项目编码,亚信项目名称,财务分类编码(原始),财务分类名称(原始),目录原始项目编码,目录原始项目名称,项目内涵,除外内容,计价单位," +
                "省级价格(元),市一级价格(元),市二级价格(元),市三级价格(元),县一级价格(元),县二级价格(元),县三级价格(元),项目说明,项目规格,项目包装,项目生产企业编码,项目生产企业名称,项目父级编码,项目父级名称," +
                "项目地id,项目地名称,适用的所有制形式,适用的所有制形式名称,有效起始日期,政策文件名称," +
                "创建人,创建时间,修改人,修改时间,修改原因";//导出的字段
        String[] titles = titleStr.split(",");
        String fieldStr = "id,itemcode,itemname,chargeclassIdSrc,chargeclassSrc,itemcodeSrc,itemnameSrc,itemContent,exceptContent,chargeunit," +
                "itempriceProvince,itemprice1,itemprice2,itemprice3,itempricecounty1,itempricecounty2,itempricecounty3,itemnote,specificaion,pack,manufactorCode,manufactor,parentcode,parentname," +
                "projectAreaId,projectArea,owntype,owntypeName,startEndDateStr,fileName," +
                "createStaffName,createTime,updateStaffName,updateTime,updateReason";//导出的字段
        String[] fields = fieldStr.split(",");
        List<StdWjItemcatalogImport> exportList = new ArrayList<StdWjItemcatalogImport>();
        for (StdWjItemcatalog bean : list) {
            StdWjItemcatalogImport dataBean = new StdWjItemcatalogImport();
            BeanUtils.copyProperties(bean, dataBean);
            //数据时间
            dataBean.setStartEndDateStr(DateUtils.date2Str(bean.getStartdate(),date_sdf)+"到"+DateUtils.date2Str(bean.getEnddate(),date_sdf));
            exportList.add(dataBean);
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(exportList, StdWjItemcatalogImport.class, titles, fields, workbook, "医疗服务项目物价目录");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("医疗服务项目物价目录", 0);
            ExportUtils.exportExl(exportList, StdWjItemcatalogImport.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<StdWjItemcatalogImport> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(StdWjItemcatalogImport.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(StdWjItemcatalogImport.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始：" + DateUtils.now());
        String[] importActionTypeArr = {"0","1","2"};
        //字典值检验
        List<StdWjItemcatalog> addUpdateList = new ArrayList<StdWjItemcatalog>();
        List<String> deleteList = new ArrayList<String>();//删除id
//        Set<String> codeSet = new HashSet<String>();
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            StdWjItemcatalogImport beanVO = list.get(i);
            /*if (StringUtils.isBlank(beanVO.getItemcode())) {
                message += "导入的数据中“亚信项目编码”不能为空，如：第" + (i + 2) + "行数据“亚信项目编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(beanVO.getItemname())) {
                message += "导入的数据中“亚信项目名称”不能为空，如：第" + (i + 2) + "行数据“亚信项目名称”为空\n";
                flag = false;
            }*/
            if (StringUtils.isBlank(beanVO.getItemnameSrc())) {
                message += "导入的数据中“目录原始项目名称”不能为空，如：第" + (i + 2) + "行数据“目录原始项目名称”为空\n";
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
                    message += "导入的数据中“有效起始日期”格式无法识别，正确格式为：yyyy-MM-ddd到yyyy-MM-dd，如：第" + (i + 2) + "行数据“数据时间”为"+beanVO.getStartEndDateStr()+"\n";
                    flag = false;
                }
            }
            /*//判断医疗机构编码+医疗保险类别代码+适用时间在excel中是否重复
            if(codeSet.contains(beanVO.getItemcode())){
                message += "导入的数据中“亚信项目编码”不能重复，如：第" + (i + 2) + "行数据在excel中重复\n";
                flag = false;
            }*/

            if(StringUtils.isBlank(beanVO.getStartEndDateStr())){
                beanVO.setStartdate(DateUtils.str2Date("2000-01-01",date_sdf));
                beanVO.setEnddate(DateUtils.str2Date("2099-12-31",date_sdf));
            }else{
                String[] startAndEndTime = beanVO.getStartEndDateStr().split("到");
                try {
                    beanVO.setStartdate(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                    beanVO.setEnddate(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                }catch (Exception e) {
                    message += "导入的数据中“有效起始日期”格式不正确，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }

            if(!flag) {
                continue;
            }

            if(StringUtils.isNotBlank(beanVO.getOwntype())){
                beanVO.setOwntype(beanVO.getOwntype().replace("|",","));
            }
            if(StringUtils.isNotBlank(beanVO.getOwntypeName())){
                beanVO.setOwntypeName(beanVO.getOwntypeName().replace("|",","));
            }
            if ("1".equals(beanVO.getImportActionType())) {//新增
                /*if(this.isExistName(beanVO.getItemcode(),null)){
                    message += "导入的数据中，新增数据中包含库中已存在的“亚信项目编码”记录，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
                if(!flag) {
                    continue;
                }*/
                beanVO.setId(IdUtils.uuid());
                beanVO.setCreateStaff(user.getId());
                beanVO.setCreateStaffName(user.getRealname());
                beanVO.setCreateTime(new Date());
            } else if ("0".equals(beanVO.getImportActionType()) || "2".equals(beanVO.getImportActionType())) {//修改、删除
                if(StringUtils.isBlank(beanVO.getId())){
                    message += "导入的数据中，修改或删除的数据“id主键”不能为空，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }else{
                    //判断数据是否存在
                    StdWjItemcatalog oldBean = this.baseMapper.selectById(beanVO.getId());
                    if (oldBean == null) {
                        message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据，无法修改或删除\n";
                        flag = false;
                    } else {
                        beanVO.setId(oldBean.getId());
                        beanVO.setCreateTime(oldBean.getCreateTime());
                        beanVO.setCreateStaff(oldBean.getCreateStaff());
                        beanVO.setCreateStaffName(oldBean.getCreateStaffName());
                        beanVO.setUpdateStaff(user.getId());
                        beanVO.setUpdateStaffName(user.getRealname());
                        beanVO.setUpdateTime(new Date());
                    }
                }
            }

            if (!flag) {
                continue;
            }
            StdWjItemcatalog bean = beanVO;
            if (!flag) {
                continue;
            }
            //生成新增的addUpdateList
            if ("1".equals(beanVO.getImportActionType()) || "0".equals(beanVO.getImportActionType())) {//新增、修改
                addUpdateList.add(bean);
            }else{
                deleteList.add(beanVO.getId());//删除时
            }
//            codeSet.add(beanVO.getItemcode());
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
                        this.baseMapper.delete(new QueryWrapper<StdWjItemcatalog>().in("ID", idsSet));
                    }
                }
            }
            //批量新增修改
            if (addUpdateList.size() > 0) {
                this.saveOrUpdateBatch(addUpdateList, 1000);//直接插入
            }
            System.out.println("结束导入时间："+DateUtils.now() );
            message += "导入成功，共导入"+list.size()+"条数据。";
            return Result.ok(message, list.size());
        }
    }

    private StdWjItemcatalog selectByItemcode(String itemcode){
        QueryWrapper<StdWjItemcatalog> queryWrapper = new QueryWrapper<StdWjItemcatalog>();
        queryWrapper.eq("ITEMCODE", itemcode);
        List<StdWjItemcatalog> list = this.baseMapper.selectList(queryWrapper);
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
