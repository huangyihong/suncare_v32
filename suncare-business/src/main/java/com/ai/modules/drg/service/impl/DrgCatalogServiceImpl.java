package com.ai.modules.drg.service.impl;

import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgCatalogDetail;
import com.ai.modules.drg.mapper.DrgCatalogMapper;
import com.ai.modules.drg.service.IDrgCatalogDetailService;
import com.ai.modules.drg.service.IDrgCatalogService;
import com.ai.modules.drg.vo.DrgCatalogVo;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: DRG分组目录版本表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
@Service
@DS("greenplum")
public class DrgCatalogServiceImpl extends ServiceImpl<DrgCatalogMapper, DrgCatalog> implements IDrgCatalogService {

    @Autowired
    private IDrgCatalogDetailService drgCatalogDetailService;

    @Override
    public IPage<DrgCatalogVo> pageVO(Page<DrgCatalog> page, Wrapper<DrgCatalog> wrapper) {
        return this.baseMapper.selectPageVO(page, wrapper);
    }

    @Override
    @Transactional
    public Result<?> delete(String id) {
        //判断是否在其他基础信息版本被引用
        DrgCatalog bean = this.getById(id);
        String catalogType = bean.getCatalogType();
        String field = getFieldByCatalogType(catalogType);
        if(StringUtils.isNotBlank(field)){
            int count = this.count(new QueryWrapper<DrgCatalog>().eq(field,bean.getVersionCode()));
            if(count>0){
                return Result.error("您所选择的版本和其他基础信息版本存在关联关系,不允许删除!");
            }
        }
        this.removeById(id);
        //删除版本目录详情
        drgCatalogDetailService.remove(new QueryWrapper<DrgCatalogDetail>().eq("CATALOG_ID",id));
        return Result.ok("删除成功!");
    }

    @Override
    @Transactional
    public Result<?> deleteBatch(String ids) {
        List<DrgCatalog> list = (List<DrgCatalog>)this.listByIds(Arrays.asList(ids.split(",")));
        if(list.size()==0){
            return Result.error("参数异常!");
        }
        String catalogType = list.get(0).getCatalogType();
        String field = getFieldByCatalogType(catalogType);
        if(StringUtils.isNotBlank(field)){
            Set<String> versionCodeList = list.stream().map(t->t.getVersionCode()).collect(Collectors.toSet());
            int count = this.count(new QueryWrapper<DrgCatalog>().in(field,versionCodeList));
            if(count>0){
                return Result.error("您所选择的版本和其他基础信息版本存在关联关系,不允许删除!");
            }
        }
        this.removeByIds(Arrays.asList(ids.split(",")));
        //删除版本目录详情
        drgCatalogDetailService.remove(new QueryWrapper<DrgCatalogDetail>().in("CATALOG_ID",Arrays.asList(ids.split(","))));
        return Result.ok("批量删除成功！");
    }

    private String getFieldByCatalogType(String catalogType) {
        String field = "";
        switch (catalogType) {
            case DrgCatalogConstants.MDC_V: {
                field = "MDC_CATALOG_V";
                break;
            }
            case DrgCatalogConstants.ADRG_V: {
                field = "ADRG_CATALOG_V";
                break;
            }
            case DrgCatalogConstants.MDC_INFO_V: {
                field = "mdc_Info_V";
                break;
            }
            case DrgCatalogConstants.ADRG_LIST_V: {
                field = "adrg_List_V";
                break;
            }
            case DrgCatalogConstants.MCC_INFO_V: {
                field = "mcc_Info_V";
                break;
            }
            case DrgCatalogConstants.CC_INFO_V: {
                field = "cc_Info_V";
                break;
            }
            case DrgCatalogConstants.EXCLUDE_INFO_V: {
                field = "exclude_Info_V";
                break;
            }
            case DrgCatalogConstants.SURGERY_INFO_V: {
                field = "surgery_Info_V";
                break;
            }
            case DrgCatalogConstants.DRG_V: {
                break;
            }
        }
        return field;
    }

    @Override
    public DrgCatalog findDrgCatalog(String version) {
        QueryWrapper<DrgCatalog> wrapper = new QueryWrapper<>();
        wrapper.eq("catalog_type", DrgCatalogConstants.DRG_V);
        wrapper.eq("version_code", version);
        return this.getOne(wrapper);
    }

    @Override
    public DrgCatalog findCatalog(String catalogType, String version) {
        QueryWrapper<DrgCatalog> wrapper = new QueryWrapper<>();
        wrapper.eq("catalog_type", catalogType);
        wrapper.eq("version_code", version);
        return this.getOne(wrapper);
    }
}
