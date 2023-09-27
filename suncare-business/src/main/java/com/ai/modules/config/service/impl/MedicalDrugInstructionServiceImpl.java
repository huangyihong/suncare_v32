package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrugInstruction;
import com.ai.modules.config.entity.MedicalDrugInstructionItem;
import com.ai.modules.config.mapper.MedicalDrugInstructionItemMapper;
import com.ai.modules.config.mapper.MedicalDrugInstructionMapper;
import com.ai.modules.config.service.IMedicalDrugInstructionItemService;
import com.ai.modules.config.service.IMedicalDrugInstructionService;
import com.ai.modules.config.vo.MedicalDrugInstructionItemVO;
import com.ai.modules.config.vo.MedicalDrugInstructionVO;
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

import javax.transaction.Transactional;
import java.io.File;
import java.io.OutputStream;
import java.util.*;

/**
 * @Description: 药品说明书
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
@Service

public class MedicalDrugInstructionServiceImpl extends ServiceImpl<MedicalDrugInstructionMapper, MedicalDrugInstruction> implements IMedicalDrugInstructionService {

    @Value("${jeecg.path.upload}")
    String UPLOAD_PATH;

    @Autowired
    IMedicalDrugInstructionItemService itemService;

    @Autowired
    MedicalDrugInstructionItemMapper medicalDrugInstructionItemMapper;

    @Override
    @Transactional
    public void saveMedicalDrugInstruction(MedicalDrugInstruction medicalDrugInstruction, String itemCodes, String itemNames, String tableTypes) {
        this.save(medicalDrugInstruction);
        String parentId = medicalDrugInstruction.getId();
        // 插入子项
        if(StringUtils.isNotBlank(itemCodes)){
            String[] codeArray = itemCodes.split(",");
            String[] nameArray = itemNames.split(",");
            String[] tableTypeArray = tableTypes.split(",");
            List<MedicalDrugInstructionItem> itemlist = new ArrayList<>();
            for(int i = 0, len = codeArray.length; i < len; i++){
                MedicalDrugInstructionItem itemBean = new MedicalDrugInstructionItem();
                itemBean.setParentId(parentId);
                itemBean.setItemId(IdUtils.uuid());
                itemBean.setIsOrder((long)i);
                itemBean.setItemCode(codeArray[i]);
                itemBean.setItemValue(nameArray[i]);
                itemBean.setTableType(tableTypeArray[i]);
                itemlist.add(itemBean);
            }
            if(itemlist.size()>0){
                itemService.saveBatch(itemlist);
            }
        }
    }

    @Override
    @Transactional
    public void updateMedicalDrugInstruction(MedicalDrugInstruction medicalDrugInstruction, String itemCodes, String itemNames, String tableTypes) {
        String parentId = medicalDrugInstruction.getId();
        this.updateById(medicalDrugInstruction);
        // 删除子项
        this.medicalDrugInstructionItemMapper.delete(new QueryWrapper<MedicalDrugInstructionItem>()
                .eq("PARENT_ID", parentId));
        // 插入子项
        if(StringUtils.isNotBlank(itemCodes)){
            String[] codeArray = itemCodes.split(",");
            String[] nameArray = itemNames.split(",");
            String[] tableTypeArray = tableTypes.split(",");
            List<MedicalDrugInstructionItem> itemlist = new ArrayList<>();
            for(int i = 0, len = codeArray.length; i < len; i++){
                MedicalDrugInstructionItem itemBean = new MedicalDrugInstructionItem();
                itemBean.setParentId(parentId);
                itemBean.setItemId(IdUtils.uuid());
                itemBean.setIsOrder((long)i);
                itemBean.setItemCode(codeArray[i]);
                itemBean.setItemValue(nameArray[i]);
                itemBean.setTableType(tableTypeArray[i]);
                this.medicalDrugInstructionItemMapper.insert(itemBean);
            }
            if(itemlist.size()>0){
                itemService.saveBatch(itemlist);
            }
        }
    }

    @Override
    @Transactional
    public void deleteById(String id) {

        MedicalDrugInstruction bean = this.getById(id);
        deleteFiles(bean);
        this.removeById(id);
        // 删除子项
        this.medicalDrugInstructionItemMapper.delete(new QueryWrapper<MedicalDrugInstructionItem>()
                .eq("PARENT_ID", id));

    }

    //删除文件
    private void deleteFiles(MedicalDrugInstruction bean) {
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
        //删除文件
        List<MedicalDrugInstruction> list = this.baseMapper.selectList(new QueryWrapper<MedicalDrugInstruction>()
                .in("ID",idList));
        for(MedicalDrugInstruction bean:list){
            deleteFiles(bean);
        }

        this.removeByIds(idList);
        // 删除子项
        this.medicalDrugInstructionItemMapper.delete(new QueryWrapper<MedicalDrugInstructionItem>()
                .in("PARENT_ID",idList));
    }

    @Override
    public boolean isExistName(String code, String id) {
        QueryWrapper<MedicalDrugInstruction> queryWrapper = new QueryWrapper<MedicalDrugInstruction>();
        queryWrapper.eq("CODE", code);
        if(StringUtils.isNotBlank(id)){
            queryWrapper.notIn("ID", id);
        }
        List<MedicalDrugInstruction> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }

    @Override
    public MedicalDrugInstruction getBeanByCode(String code) {
        QueryWrapper<MedicalDrugInstruction> queryWrapper = new QueryWrapper<MedicalDrugInstruction>();
        queryWrapper.eq("CODE", code);

        List<MedicalDrugInstruction> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    private MedicalDrugInstructionItem getItemBeanByCode(String code,String itemCode) {
        QueryWrapper<MedicalDrugInstructionItem> queryWrapper = new QueryWrapper<MedicalDrugInstructionItem>();
        queryWrapper.eq("ITEM_CODE", itemCode);
        queryWrapper.inSql("PARENT_ID",
                "SELECT ID FROM MEDICAL_DRUG_INSTRUCTION where 1=1 and CODE = '" + code+"'");
        List<MedicalDrugInstructionItem> list = this.medicalDrugInstructionItemMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public boolean exportExcel(List<MedicalDrugInstruction> list, OutputStream os, String suffix) throws Exception {
        boolean isSuc = true;

        String titleStr = "说明书编码,说明书修订日期,说明书药品名称,商品名,英文名,汉语拼音,药品成分,性状,规格,适应症," +
                "用法用量,禁忌,不良反应,注意事项,儿童用药,孕妇及哺乳期妇女用药,老年用药,药物相互作用,药物过量," +
                "药理毒理,药代动力学,贮藏,包装,执行标准,批准文号,生产企业名称,生产地址,说明书来源,备注,停用标识(1停用0正常)";
        String[] titles= titleStr.split(",");
        String fieldStr = "code,revisionDate,name,tradeName,ename,cname,ingredient,character,specificaion,indication," +
                "usage,taboo,adverseReaction,attention,childrenDrug,pregnantDrug,olderDrug,drugInteraction,drugOverdose," +
                "toxicology,dynamics,storage,packaging,standard,approveNumber,enterprise,productionAddress,source,remark,isStopUsed";
        String[] fields=fieldStr.split(",");

        String titleStr2 = "说明书编码,说明书药品名称,关联药品编码,关联药品名称,排序号,关联药品所属表";
        String[] titles2= titleStr2.split(",");
        String fieldStr2 = "code,name,itemCode,itemValue,isOrder,tableType";
        String[] fields2=fieldStr2.split(",");
        List<MedicalDrugInstructionItemVO> itemList = getMedicalDrugInstructionItemList(list);
        if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(list, MedicalDrugInstruction.class,titles,fields,workbook,"说明书内容");
            ExportXUtils.exportExl(itemList, MedicalDrugInstructionItemVO.class,titles2,fields2,workbook,"关联药品");
            workbook.write(os);
            workbook.dispose();
        }else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("说明书内容", 0);
            ExportUtils.exportExl(list,MedicalDrugInstruction.class,titles,fields,sheet, "");
            WritableSheet sheet2 = wwb.createSheet("关联药品", 1);
            ExportUtils.exportExl(itemList,MedicalDrugInstructionItemVO.class,titles2,fields2,sheet2, "");
            wwb.write();
            wwb.close();
        }
        return isSuc;
    }

    private List<MedicalDrugInstructionItemVO> getMedicalDrugInstructionItemList(List<MedicalDrugInstruction> list) {
        List<MedicalDrugInstructionItem> itemList = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        Map<String,MedicalDrugInstruction> mapBean = new HashMap<>();
        for(MedicalDrugInstruction bean:list){
            if(idList.size()==1000){
                itemList.addAll(this.medicalDrugInstructionItemMapper.
                        selectList(new QueryWrapper<MedicalDrugInstructionItem>()
                        .in("PARENT_ID",idList).orderByAsc("IS_ORDER")));
                idList = new ArrayList<>();
            }
            idList.add(bean.getId());
            mapBean.put(bean.getId(),bean);
        }
        if(idList.size()>0){
            itemList.addAll(this.medicalDrugInstructionItemMapper.
                    selectList(new QueryWrapper<MedicalDrugInstructionItem>()
                            .in("PARENT_ID",idList).orderByAsc("IS_ORDER")));
        }
        List<MedicalDrugInstructionItemVO> itemVOList = new ArrayList<>();
        for(MedicalDrugInstructionItem itemBean:itemList){
            MedicalDrugInstructionItemVO itemVOBean = new MedicalDrugInstructionItemVO();
            BeanUtils.copyProperties(itemBean,itemVOBean);
            itemVOBean.setCode(mapBean.get(itemBean.getParentId()).getCode());
            itemVOBean.setName(mapBean.get(itemBean.getParentId()).getName());
            itemVOList.add(itemVOBean);
        }
        return itemVOList;
    }

    @Override
    @Transactional
    public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception {
        //sheet0
        String mappingFieldStr = "code,revisionDate,name,tradeName,ename,cname,ingredient,character,specificaion,indication," +
                "usage,taboo,adverseReaction,attention,childrenDrug,pregnantDrug,olderDrug,drugInteraction,drugOverdose," +
                "toxicology,dynamics,storage,packaging,standard,approveNumber,enterprise,productionAddress,source,remark," +
                "isStopUsed,actionType";//导入的字段
        String[] mappingFields=mappingFieldStr.split(",");
        //sheet1
        String mappingFieldStr2 = "code,name,itemCode,itemValue,isOrder,tableType,actionType";//导入的字段
        String[] mappingFields2=mappingFieldStr2.split(",");

        System.out.println("开始导入时间："+DateUtils.now() );
        List<MedicalDrugInstructionVO> list = new ArrayList<MedicalDrugInstructionVO>();
        List<MedicalDrugInstructionItemVO> itemlist = new ArrayList<MedicalDrugInstructionItemVO>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalDrugInstructionVO.class, mappingFields, 0, 1, file.getInputStream());
            itemlist = ExcelXUtils.readSheet(MedicalDrugInstructionItemVO.class, mappingFields2, 1, 1, file.getInputStream());
        }else {
            list = ExcelUtils.readSheet(MedicalDrugInstructionVO.class, mappingFields, 0, 1, file.getInputStream());
            itemlist = ExcelUtils.readSheet(MedicalDrugInstructionItemVO.class, mappingFields2, 1, 1, file.getInputStream());
        }
        if(list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        //sheet0说明书内容读取
        Set<String> codeSet = new HashSet<String>();
        Map<String,String> codeMap = new HashMap<>();
        List<MedicalDrugInstruction> addList = new ArrayList<>();
        List<MedicalDrugInstruction> updateList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();
        System.out.println("校验开始："+DateUtils.now() );
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalDrugInstructionVO bean = list.get(i);
            if (StringUtils.isBlank(bean.getCode())) {
                message += "导入的数据中“说明书编码”不能为空，如：第一个sheet第" + (i + 2) + "行数据“说明书编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getName())) {
                message += "导入的数据中“说明书药品名称”不能为空，如：第一个sheet第" + (i + 2) + "行数据“说明书药品名称”为空\n";
                flag = false;
            }
            /*if (bean.getRevisionDate()==null) {
                message += "导入的数据中“说明书修订日期”不能为空，如：第一个sheet第" + (i + 2) + "行数据“说明书修订日期”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getIngredient())) {
                message += "导入的数据中“药品成分”不能为空，如：第一个sheet第" + (i + 2) + "行数据“药品成分”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getSpecificaion())) {
                message += "导入的数据中“规格”不能为空，如：第一个sheet第" + (i + 2) + "行数据“规格”为空\n";
                flag = false;
            }*/

            if (StringUtils.isBlank(bean.getActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第一个sheet第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }
            if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(bean.getActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第一个sheet第" + (i + 2) + "行数据\n";
                flag = false;
            }

            //判断code在excel中是否重复
            if(codeSet.contains(bean.getCode())){
                message += "导入的数据中“说明书编码”不能重复，如：第一个sheet第" + (i + 2) + "行数据“"+bean.getCode()+"”在excel中重复\n";
                flag = false;
            }
            if(!flag) {
                continue;
            }
            MedicalDrugInstruction oldBean =  getBeanByCode(bean.getCode());
            if("1".equals(bean.getActionType())) {//新增
                if(oldBean!=null){
                    message += "导入的数据中，“说明书编码”在库中已存在，不能新增，如：第一个sheet第" + (i + 2) + "行数据“"+bean.getCode()+"”\n";
                    flag = false;
                }
                if(!flag) {
                    continue;
                }
                bean.setId(IdUtils.uuid());
                bean.setCreateStaff(user.getId());
                bean.setCreateStaffName(user.getRealname());
                bean.setCreateTime(new Date());
                addList.add(bean);
            }else if("0".equals(bean.getActionType())||"2".equals(bean.getActionType())) {//修改、删除
                if(oldBean==null){
                    message += "导入的数据中，“说明书编码”在库中不存在，不能修改删除，如：第一个sheet第" + (i + 2) + "行数据“"+bean.getCode()+"”\n";
                    flag = false;
                }
                if(!flag) {
                    continue;
                }
                bean.setId(oldBean.getId());
                if("0".equals(bean.getActionType())){
                    bean.setUpdateStaff(user.getId());
                    bean.setUpdateStaffName(user.getRealname());
                    bean.setUpdateTime(new Date());
                    updateList.add(bean);
                }else{
                    deleteList.add(bean.getId());
                }
            }
            if(!flag) {
                continue;
            }
            codeSet.add(bean.getCode());
            codeMap.put(bean.getCode(),bean.getId());
        }
        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }
        //sheet1关联药品读取
        Set<String> codeItemSet = new HashSet<String>();
        List<MedicalDrugInstructionItem> addItemList = new ArrayList<>();
        List<MedicalDrugInstructionItem> updateItemList = new ArrayList<>();
        List<String> deleteItemList = new ArrayList<>();
        for (int i = 0; i < itemlist.size(); i++) {
            boolean flag = true;
            MedicalDrugInstructionItemVO itemBean = itemlist.get(i);
            if (StringUtils.isBlank(itemBean.getCode())) {
                message += "导入的数据中“说明书编码”不能为空，如：第二个sheet第" + (i + 2) + "行数据“说明书编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(itemBean.getItemCode())) {
                message += "导入的数据中“关联药品编码”不能为空，如：第二个sheet第" + (i + 2) + "行数据“关联药品编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(itemBean.getItemValue())) {
                message += "导入的数据中“关联药品名称”不能为空，如：第二个sheet第" + (i + 2) + "行数据“关联药品名称”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(itemBean.getTableType())) {
                message += "导入的数据中“子项指向表”不能为空，如：第二个sheet第" + (i + 2) + "行数据“子项指向表”为空\n";
                flag = false;
            }else{
                itemBean.setTableType(itemBean.getTableType().toUpperCase());
            }

            if (StringUtils.isBlank(itemBean.getActionType())) {
                message += "导入的数据中“更新标志”不能为空，如：第二个sheet第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
            }
            if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(itemBean.getActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第二个sheet第" + (i + 2) + "行数据\n";
                flag = false;
            }

            //判断itemCode在excel中是否重复
            if(codeItemSet.contains(itemBean.getCode()+"&&"+itemBean.getItemCode())){
                message += "导入的数据中相同说明书中“关联药品编码”不能重复，如：第二个sheet第" + (i + 2) + "行数据说明书编码为“"+itemBean.getCode()+"关联药品编码为“"+itemBean.getItemCode()+"”在excel中重复\n";
                flag = false;
            }

            if(!flag) {
                continue;
            }

            MedicalDrugInstructionItem oldItemBean = getItemBeanByCode(itemBean.getCode(),itemBean. getItemCode());
            if("1".equals(itemBean.getActionType())) {//新增
                if(oldItemBean!=null){
                    message += "导入的数据中该数据在库中已存在，不能新增，如：第二个sheet第" + (i + 2) + "行数据说明书编码为“"+itemBean.getCode()+"关联药品编码为“"+itemBean.getItemCode()+"”\n";
                    flag = false;
                }
                if(!flag) {
                    continue;
                }
                //设置parentId
                String parentId = codeMap.get(itemBean.getCode());//在第一个页签中获取
                if(StringUtils.isBlank(parentId)){
                    //库中获取
                    MedicalDrugInstruction oldBean =  getBeanByCode(itemBean.getCode());
                    if(oldBean!=null){
                        parentId = oldBean.getId();
                    }
                }
                if(StringUtils.isBlank(parentId)){
                    message += "导入的数据中该数据“说明书编码”在第一个页签或者库中不存在，不能新增，如：第二个sheet第" + (i + 2) + "行数据说明书编码为“"+itemBean.getCode()+"关联药品编码为“"+itemBean.getItemCode()+"”\n";
                    flag = false;
                }
                itemBean.setItemId(IdUtils.uuid());
                itemBean.setParentId(parentId);
                addItemList.add(itemBean);
            }else if("0".equals(itemBean.getActionType())||"2".equals(itemBean.getActionType())) {//修改、删除
                if(oldItemBean==null){
                    message += "导入的数据中该数据在库中不存在，不能修改删除，如：第二个sheet第" + (i + 2) + "行数据说明书编码为“"+itemBean.getCode()+"关联药品编码为“"+itemBean.getItemCode()+"”\n";
                    flag = false;
                }
                if(!flag) {
                    continue;
                }
                itemBean.setItemId(oldItemBean.getItemId());
                itemBean.setParentId(oldItemBean.getParentId());
                if("0".equals(itemBean.getActionType())){
                    updateItemList.add(itemBean);
                }else{
                    deleteItemList.add(itemBean.getItemId());
                }
            }
            if(!flag) {
                continue;
            }

            codeItemSet.add(itemBean.getCode()+"&&"+itemBean.getItemCode());
        }
        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }else{
            System.out.println("开始插入时间："+ DateUtils.now() );
            if(addList.size()>0){
                this.saveBatch(addList);
            }
            if(updateList.size()>0){
                this.updateBatchById(updateList);
            }
            if(deleteList.size()>0){
                this.deleteByIds(deleteList);
            }
            if(addItemList.size()>0){
                this.itemService.saveBatch(addItemList);
            }
            if(updateItemList.size()>0){
                this.itemService.updateBatchById(updateItemList);
            }
            if(deleteItemList.size()>0){
                this.itemService.removeByIds(deleteItemList);
            }
            message += "导入成功，共导入说明书内容"+list.size()+"条数据，关联药品"+itemlist.size()+"条数据。";
            System.out.println("结束导入时间："+DateUtils.now() );
            return Result.ok(message,list.size());
        }
    }


}
