package com.ai.modules.drg.service.impl;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.ExcelXUtils;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgCatalogDetail;
import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.mapper.DrgCatalogDetailMapper;
import com.ai.modules.drg.mapper.DrgCatalogMapper;
import com.ai.modules.drg.service.IDrgCatalogDetailService;
import com.ai.modules.drg.service.IDrgRuleLimitesService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: DRG分组目录数据详细表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
@Service
@DS("greenplum")
public class DrgCatalogDetailServiceImpl extends ServiceImpl<DrgCatalogDetailMapper, DrgCatalogDetail> implements IDrgCatalogDetailService {

    @Autowired
    private DrgCatalogMapper catalogMapper;

    @Autowired
    private IDrgRuleLimitesService drgRuleLimitesService;

    @Override
    public Result<?> delete(String id) {
        //判断是否在其他基础信息目录被引用
        DrgCatalogDetail bean = this.getById(id);
        String catalogType = bean.getCatalogType();
        String field = getFieldByCatalogType(catalogType);
        if(StringUtils.isNotBlank(field)){
            QueryWrapper<DrgCatalogDetail> queryWrapper = getDrgCatalogDetailQueryWrapper(bean, catalogType);
            int count = this.count(queryWrapper);
            if(count>0){
                return Result.error("您所选择的目录和其他基础信息目录存在关联关系,不允许删除!");
            }
        }
        this.removeById(id);
        return Result.ok("删除成功!");
    }

    @Override
    public Result<?> deleteBatch(String ids) {
        List<DrgCatalogDetail> list = (List<DrgCatalogDetail>)this.listByIds(Arrays.asList(ids.split(",")));
        if(list.size()==0){
            return Result.error("参数异常!");
        }
        String catalogType = list.get(0).getCatalogType();
        String field = getFieldByCatalogType(catalogType);
        if(StringUtils.isNotBlank(field)){
            for(DrgCatalogDetail bean:list){
                QueryWrapper<DrgCatalogDetail> queryWrapper = getDrgCatalogDetailQueryWrapper(bean, catalogType);
                int count = this.count(queryWrapper);
                if(count>0){
                    return Result.error("您所选择的目录和其他基础信息目录存在关联关系,不允许删除!");
                }
            }

        }
        this.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功!");
    }

    @Override
    public Result<?> deleteByCatalogId(String catalogId) {
        List<DrgCatalogDetail> list =this.list(new QueryWrapper<DrgCatalogDetail>().eq("CATALOG_ID",catalogId));
        if(list.size()==0){
            return Result.error("目录下无数据!");
        }
        String catalogType = list.get(0).getCatalogType();
        String field = getFieldByCatalogType(catalogType);
        if(StringUtils.isNotBlank(field)){
            for(DrgCatalogDetail bean:list){
                QueryWrapper<DrgCatalogDetail> queryWrapper = getDrgCatalogDetailQueryWrapper(bean, catalogType);
                int count = this.count(queryWrapper);
                if(count>0){
                    return Result.error("您所选择的目录和其他基础信息目录存在关联关系,不允许删除!");
                }
            }

        }
        this.remove(new QueryWrapper<DrgCatalogDetail>().eq("CATALOG_ID",catalogId));
        return Result.ok("一键删除成功!");
    }

    private QueryWrapper<DrgCatalogDetail> getDrgCatalogDetailQueryWrapper(DrgCatalogDetail bean, String catalogType) {
        //获取版本信息
        DrgCatalog catalogBean = this.catalogMapper.selectById(bean.getCatalogId());
        QueryWrapper<DrgCatalogDetail> queryWrapper=new QueryWrapper<DrgCatalogDetail>();
        switch (catalogType) {
            case DrgCatalogConstants.MDC_V: {
                queryWrapper.and(qr->qr.eq("MDC_CATALOG_CODE",bean.getCode()).
                        or().
                        eq("DIAG_GROUP_CODE1",bean.getCode()).
                        or().
                        eq("DIAG_GROUP_CODE2",bean.getCode()));
                queryWrapper.inSql("CATALOG_ID","select id from drg_catalog t where t.mdc_catalog_v='"+catalogBean.getVersionCode()+"'");
                break;
            }
            case DrgCatalogConstants.ADRG_V: {
                queryWrapper.eq("ADRG_CATALOG_CODE",bean.getCode());
                queryWrapper.inSql("CATALOG_ID","select id from drg_catalog t where t.adrg_catalog_v='"+catalogBean.getVersionCode()+"'");
                break;
            }
            case DrgCatalogConstants.CONDITION_V: {
                queryWrapper.eq("CONDITION_NAME",bean.getName());
                break;
            }
        }
        return queryWrapper;
    }

    private String getFieldByCatalogType(String catalogType) {
        String field = "";
        switch (catalogType) {
            case DrgCatalogConstants.MDC_V: {
                field = "MDC_CATALOG_CODE";
                break;
            }
            case DrgCatalogConstants.ADRG_V: {
                field = "ADRG_CATALOG_CODE";
                break;
            }
            case DrgCatalogConstants.CONDITION_V: {
                field = "CONDITION_NAME";
                break;
            }
        }
        return field;
    }

    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user,String catalogType,String catalogId,Map<String, Map<String,String>> dictMap)throws Exception {
        DrgCatalogConstants.CatalogTypeInfo typeInfo = DrgCatalogConstants.CATALOG_TYPE_MAP.get(catalogType);
        if(typeInfo==null){
            return Result.error("参数异常！");
        }
        String[] mappingFields = Arrays.copyOf(typeInfo.getFieldArr(), typeInfo.getFieldArr().length - 1);
        List<DrgCatalogDetail> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(DrgCatalogDetail.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(DrgCatalogDetail.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }


        //根据id获取
        List<String> idList = new ArrayList<>();
        if(DrgCatalogConstants.DRG_V.equals(catalogType)){
          idList = list.stream().filter(t->StringUtils.isNotBlank(t.getId())).map(t->t.getId()).collect(Collectors.toList());
        }

        QueryWrapper<DrgCatalogDetail> queryWrapper=new QueryWrapper<DrgCatalogDetail>();
        queryWrapper.eq("CATALOG_TYPE",catalogType);
        if(StringUtils.isNotBlank(catalogId)){
            queryWrapper.eq("CATALOG_ID",catalogId);
        }
        if(idList.size()>0){
            queryWrapper.in("ID",idList);
        }
        queryWrapper.select("ID", "CODE", "NAME", "MDC_CATALOG_CODE");
        List<DrgCatalogDetail> oldList = this.list(queryWrapper);

        Map<String,DrgCatalogDetail> codeBeanMap = new HashMap<String,DrgCatalogDetail>();
        oldList.stream().forEach(t->{
            if(catalogType.equals(DrgCatalogConstants.DRG_V)) {
                codeBeanMap.put(t.getId(),t);
            }else if(catalogType.equals(DrgCatalogConstants.MDC_INFO_V)) {
                codeBeanMap.put(t.getCode() + "&" + t.getMdcCatalogCode(), t);
            }else if(catalogType.equals(DrgCatalogConstants.CONDITION_V)) {
                codeBeanMap.put(t.getName(), t);
            }else{
                codeBeanMap.put(t.getCode(),t);
            }
        });
        Set<String> codeSet = new HashSet<String>();//编码在Excel中是否重复
        String message = "";
        //获取版本信息
        DrgCatalog catalogBean = this.catalogMapper.selectById(catalogId);
        //获取mdcCatalogCode列表
        Set<String> mdcCatalogCodeSet = new HashSet<>();
        if(DrgCatalogConstants.ADRG_V.equals(catalogType)||DrgCatalogConstants.MDC_INFO_V.equals(catalogType)||DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)||DrgCatalogConstants.DRG_V.equals(catalogType)){
            List<DrgCatalog> catalogList = this.catalogMapper.selectList(new QueryWrapper<DrgCatalog>().eq("VERSION_CODE",catalogBean.getMdcCatalogV()).eq("CATALOG_TYPE",DrgCatalogConstants.MDC_V));
            if(catalogList.size()==1){
                List<DrgCatalogDetail> detailList = this.list(new QueryWrapper<DrgCatalogDetail>().eq("CATALOG_ID",catalogList.get(0).getId()).eq("CATALOG_TYPE",DrgCatalogConstants.MDC_V));
                detailList.stream().forEach(t->{
                    mdcCatalogCodeSet.add(t.getCode());
                });
            }
        }
        //获取adrgCatalogCode列表
        Set<String> adrgCatalogCodeSet = new HashSet<>();
        if(DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)||DrgCatalogConstants.DRG_V.equals(catalogType)){
            List<DrgCatalog> catalogList = this.catalogMapper.selectList(new QueryWrapper<DrgCatalog>().select("ID").eq("VERSION_CODE",catalogBean.getAdrgCatalogV()).eq("CATALOG_TYPE",DrgCatalogConstants.ADRG_V));
            if(catalogList.size()==1){
                List<DrgCatalogDetail> detailList = this.list(new QueryWrapper<DrgCatalogDetail>().select("CODE").eq("CATALOG_ID",catalogList.get(0).getId()).eq("CATALOG_TYPE",DrgCatalogConstants.ADRG_V));
                detailList.stream().forEach(t->{
                    adrgCatalogCodeSet.add(t.getCode());
                });
            }
        }
        //获取conditionName列表
        //Set<String> conditionNameSet = new HashSet<>();
        Map<String,String> conditionNameCode = new HashMap<>();
        if(DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)){
            List<DrgCatalogDetail> detailList = this.list(new QueryWrapper<DrgCatalogDetail>().select("CODE", "NAME").eq("CATALOG_TYPE",DrgCatalogConstants.CONDITION_V));
            detailList.stream().forEach(t->{
                conditionNameCode.put(t.getName(),t.getCode());
            });

        }

        for(int i = 0; i < list.size(); i++){
            DrgCatalogDetail bean = list.get(i);
            String codeKey = bean.getCode();
            if(catalogType.equals(DrgCatalogConstants.DRG_V)) {
                codeKey = bean.getId();
            }else if(catalogType.equals(DrgCatalogConstants.MDC_INFO_V)){
                codeKey = bean.getCode()+"&"+bean.getMdcCatalogCode();
            }else if(catalogType.equals(DrgCatalogConstants.CONDITION_V)) {
                codeKey = bean.getName();
            }
            boolean flag = true;
            //非空校验
            if(!DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)){
                if (StringUtils.isBlank(bean.getCode())) {
                    message += "导入的数据中“"+typeInfo.getTitleArr()[0]+"”不能为空，如：第" + (i + 2) + "行数据“"+typeInfo.getTitleArr()[0]+"”为空\n";
                    flag = false;
                }
                if (StringUtils.isBlank(bean.getName())) {
                    message += "导入的数据中“"+typeInfo.getTitleArr()[1]+"”不能为空，如：第" + (i + 2) + "行数据“"+typeInfo.getTitleArr()[1]+"”为空\n";
                    flag = false;
                }
            }
            if(DrgCatalogConstants.ADRG_V.equals(catalogType)
                    ||DrgCatalogConstants.MDC_INFO_V.equals(catalogType)
                    ||DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)
                    ||DrgCatalogConstants.DRG_V.equals(catalogType)){
                if (StringUtils.isBlank(bean.getMdcCatalogCode())) {
                    message += "导入的数据中“MDC目录编码”不能为空，如：第" + (i + 2) + "行数据“MDC目录编码”为空\n";
                    flag = false;
                }
            }
            if(DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)||DrgCatalogConstants.DRG_V.equals(catalogType)){
                if (StringUtils.isBlank(bean.getAdrgCatalogCode())) {
                    message += "导入的数据中“ADRG目录编码”不能为空，如：第" + (i + 2) + "行数据“ADRG目录编码”为空\n";
                    flag = false;
                }
            }
            if(!flag) {
                continue;
            }


            if(DrgCatalogConstants.DRG_V.equals(catalogType)){ //判断id是否存在
                if(StringUtils.isNotBlank(bean.getId())&&codeBeanMap.get(bean.getId())==null){
                    message += "导入的数据中“"+typeInfo.getSheefName()+"id主键”在系统不存在，如：第" + (i + 2) + "行数据\n";
                    flag = false;
                }
            }else if(!DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)&&codeBeanMap.get(codeKey)!=null){//判断code是否存在
                //存在修改
                bean.setId(codeBeanMap.get(codeKey).getId());
            }
            //判断在excel中是否重复
            if(!DrgCatalogConstants.ADRG_LIST_V.equals(catalogType)&&StringUtils.isNotBlank(codeKey)&&codeSet.contains(codeKey)){
                if(catalogType.equals(DrgCatalogConstants.DRG_V)) {
                    message += "导入的数据中“id主键”不能重复，如：第" + (i + 2) + "行数据在excel中重复\n";
                }else if(catalogType.equals(DrgCatalogConstants.CONDITION_V)){
                    message += "导入的数据中“分组条件名称”不能重复，如：第" + (i + 2) + "行数据在excel中重复\n";
                }else if(catalogType.equals(DrgCatalogConstants.MDC_INFO_V)){
                    message += "导入的数据中“相同MDC目录"+bean.getMdcCatalogCode()+"下编码”不能重复，如：第" + (i + 2) + "行数据在excel中重复\n";
                }else{
                    message += "导入的数据中“"+typeInfo.getSheefName()+"编码”不能重复，如：第" + (i + 2) + "行数据在excel中重复\n";
                }
                flag = false;
            }

            //校验mdcCatalogCode是否存在
            if(StringUtils.isNotBlank(bean.getMdcCatalogCode())&&!mdcCatalogCodeSet.contains(bean.getMdcCatalogCode())){
                message += "导入的数据中“MDC目录编码”在MDC目录库中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }
            if(StringUtils.isNotBlank(bean.getDiagGroupCode1())&&!mdcCatalogCodeSet.contains(bean.getDiagGroupCode1())){
                message += "导入的数据中“关联诊断组1”在MDC目录库中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }
            if(StringUtils.isNotBlank(bean.getDiagGroupCode2())&&!mdcCatalogCodeSet.contains(bean.getDiagGroupCode2())){
                message += "导入的数据中“关联诊断组2”在MDC目录库中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }
            //校验adrgCatalogCode是否存在
            if(StringUtils.isNotBlank(bean.getAdrgCatalogCode())&&!adrgCatalogCodeSet.contains(bean.getAdrgCatalogCode())){
                message += "导入的数据中“ADRG目录编码”在ADRG目录库中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }
            //校验conditionName是否存在
            if(StringUtils.isNotBlank(bean.getConditionName())&&conditionNameCode.get(bean.getConditionName())==null){
                message += "导入的数据中“分组条件”在分组条件信息库中不存在，如：第" + (i + 2) + "行数据\n";
                flag = false;
            }else if(StringUtils.isNotBlank(bean.getConditionName())){
                bean.setConditionCode(conditionNameCode.get(bean.getConditionName()));
            }
            if(!flag) {
                continue;
            }

            //字典值转换
            Map<String, String> yesnoDict = dictMap.get("YESNO");
            Map<String, String> havingornoDict = dictMap.get("HAVINGORNO");
            bean.setValidSecondDiag(StringUtils.isNotBlank(yesnoDict.get(bean.getValidSecondDiag()))?yesnoDict.get(bean.getValidSecondDiag()):bean.getValidSecondDiag());
            bean.setValidMcc(StringUtils.isNotBlank(yesnoDict.get(bean.getValidMcc()))?yesnoDict.get(bean.getValidMcc()):bean.getValidMcc());
            bean.setValidCc(StringUtils.isNotBlank(yesnoDict.get(bean.getValidCc()))?yesnoDict.get(bean.getValidCc()):bean.getValidCc());
            bean.setValidSurgery1(StringUtils.isNotBlank(yesnoDict.get(bean.getValidSurgery1()))?yesnoDict.get(bean.getValidSurgery1()):bean.getValidSurgery1());
            bean.setValidSurgery2(StringUtils.isNotBlank(yesnoDict.get(bean.getValidSurgery2()))?yesnoDict.get(bean.getValidSurgery2()):bean.getValidSurgery2());
            bean.setValidSurgery3(StringUtils.isNotBlank(yesnoDict.get(bean.getValidSurgery3()))?yesnoDict.get(bean.getValidSurgery3()):bean.getValidSurgery3());
            bean.setHasCondition(StringUtils.isNotBlank(havingornoDict.get(bean.getHasCondition()))?havingornoDict.get(bean.getHasCondition()):bean.getHasCondition());

            bean.setCatalogType(catalogType);
            if(StringUtils.isNotBlank(catalogId)){
                bean.setCatalogId(catalogId);
            }
            bean.setExamineStatus("0");
            codeSet.add(codeKey);
        }
        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }else{
            this.saveOrUpdateBatch(list);
            message += "导入成功，共导入"+list.size()+"条数据。";
            return Result.ok(message);
        }
    }

    @Override
    @Transactional
    public void saveBean(DrgCatalogDetail drgCatalogDetail) {
        if(drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.ADRG_V)||drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.DRG_V)){
            this.deleteDrgRuleLimites(drgCatalogDetail);//先删除
            if("1".equals(drgCatalogDetail.getHasCondition())&&StringUtils.isNotBlank(drgCatalogDetail.getDrgRuleLimites())){
                List<DrgRuleLimites> ruleLimites = JSONObject.parseArray(drgCatalogDetail.getDrgRuleLimites(), DrgRuleLimites.class);
                drgRuleLimitesService.saveBatch(ruleLimites);
            }
        }
        this.save(drgCatalogDetail);
    }

    @Override
    @Transactional
    public void updateBean(DrgCatalogDetail drgCatalogDetail) {
        if(drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.ADRG_V)||drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.DRG_V)){
            this.deleteDrgRuleLimites(drgCatalogDetail);//先删除
            if("1".equals(drgCatalogDetail.getHasCondition())&&StringUtils.isNotBlank(drgCatalogDetail.getDrgRuleLimites())){
                List<DrgRuleLimites> ruleLimites = JSONObject.parseArray(drgCatalogDetail.getDrgRuleLimites(), DrgRuleLimites.class);
                drgRuleLimitesService.saveBatch(ruleLimites);
            }
        }
        this.updateById(drgCatalogDetail);
    }

    private void deleteDrgRuleLimites(DrgCatalogDetail drgCatalogDetail){
        //获取版本信息
        DrgCatalog catalogBean = this.catalogMapper.selectById(drgCatalogDetail.getCatalogId());
        String versionCode = catalogBean.getVersionCode();
        //没有分组条件  删除库中分组数据
        QueryWrapper<DrgRuleLimites> queryWrapper = new QueryWrapper<DrgRuleLimites>();
        queryWrapper.eq("CATALOG_TYPE",drgCatalogDetail.getCatalogType());
        queryWrapper.eq("CATALOG_CODE",drgCatalogDetail.getCode());
        queryWrapper.eq("VERSION_CODE",versionCode);
        drgRuleLimitesService.remove(queryWrapper);
    }
}
