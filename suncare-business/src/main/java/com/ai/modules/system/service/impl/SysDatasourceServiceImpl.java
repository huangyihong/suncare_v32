package com.ai.modules.system.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.service.IMedicalDictClearService;
import com.ai.modules.config.service.IMedicalDictItemService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.system.entity.SysQuickMenu;
import com.ai.modules.system.vo.RoleVo;
import com.ai.modules.system.vo.SysDatasourceVo;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.mapper.SysDatasourceMapper;
import com.ai.modules.system.service.ISysDatasourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 项目地配置
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Service
public class SysDatasourceServiceImpl extends ServiceImpl<SysDatasourceMapper, SysDatasource> implements ISysDatasourceService {
    @Autowired
    IMedicalDictClearService medicalDictClearService;

    @Autowired
    IMedicalDictItemService medicalDictItemService;

    @Autowired
    IMedicalDictService medicalDictService;

    @Autowired
    private SysDatasourceMapper sysDatasourceMapper;

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void addByTransactional(SysDatasource sysDatasource) throws Exception {
        LambdaQueryWrapper<SysDatasource> sysDatasourceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysDatasourceLambdaQueryWrapper.eq(SysDatasource::getCode,sysDatasource.getCode());
        List<SysDatasource> datasourceList = list(sysDatasourceLambdaQueryWrapper);
        if(datasourceList.size()>0){
            throw new Exception("项目编码已存在!");
        }


        //更新字典
        LambdaQueryWrapper<MedicalDict> medicalDictLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictLambdaQueryWrapper.eq(MedicalDict::getGroupCode,"SOLR_DATA_SOURCE");
        List<MedicalDict> list = medicalDictService.list(medicalDictLambdaQueryWrapper);

        if(list.size()>0){
            MedicalDict medicalDict = list.get(0);
            String groupId = medicalDict.getGroupId();
            String groupCode = medicalDict.getGroupCode();
            String groupKind = medicalDict.getKind();

            int dicItems = medicalDictItemService.count(((QueryWrapper) Wrappers.query().eq("group_id", groupId)));

            MedicalDictItem dictItem = new MedicalDictItem();
            dictItem.setGroupId(groupId);
            dictItem.setItemId(IdUtils.uuid());
            dictItem.setIsOrder((long) dicItems+1);
            dictItem.setCode(sysDatasource.getCode());
            dictItem.setValue(sysDatasource.getName());
            dictItem.setKind(medicalDict.getKind());

            medicalDictItemService.save(dictItem);

            // 清除旧的子项缓存
            medicalDictClearService.clearCache(groupCode, dictItem.getCode(), groupKind);
            medicalDictClearService.clearTextCache(groupCode, dictItem.getValue(), groupKind);
            medicalDictClearService.clearCache(groupCode, groupKind);

            //保存项目地配置
            save(sysDatasource);
        }else{
            throw new Exception("请先配置医疗字典:SOLR_DATA_SOURCE!");
        }



    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void updateByTransactional(SysDatasource sysDatasource) {
        SysDatasource ds = getById(sysDatasource.getId());


        //字典组id
        LambdaQueryWrapper<MedicalDict> medicalDictLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictLambdaQueryWrapper.eq(MedicalDict::getGroupCode,"SOLR_DATA_SOURCE");
        MedicalDict medicalDict = medicalDictService.getOne(medicalDictLambdaQueryWrapper);
        String groupId = medicalDict.getGroupId();


        //更新字典
        String code = ds.getCode();
        LambdaQueryWrapper<MedicalDictItem> medicalDictItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictItemLambdaQueryWrapper.eq(MedicalDictItem::getCode,code);
        medicalDictItemLambdaQueryWrapper.eq(MedicalDictItem::getGroupId,groupId);
        MedicalDictItem medicalDictItem = medicalDictItemService.getOne(medicalDictItemLambdaQueryWrapper);
        medicalDictItem.setCode(sysDatasource.getCode());
        medicalDictItem.setValue(sysDatasource.getName());
        medicalDictItemService.updateById(medicalDictItem);

        //更新项目地配置
        updateById(sysDatasource);

    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void removeByTransactional(String id) {
        SysDatasource ds = getById(id);

        //字典组id
        LambdaQueryWrapper<MedicalDict> medicalDictLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictLambdaQueryWrapper.eq(MedicalDict::getGroupCode,"SOLR_DATA_SOURCE");
        MedicalDict medicalDict = medicalDictService.getOne(medicalDictLambdaQueryWrapper);
        String groupId = medicalDict.getGroupId();

        //删除字典
        String code = ds.getCode();
        LambdaQueryWrapper<MedicalDictItem> medicalDictItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictItemLambdaQueryWrapper.eq(MedicalDictItem::getCode,code);
        medicalDictItemLambdaQueryWrapper.eq(MedicalDictItem::getGroupId,groupId);
        medicalDictItemService.remove(medicalDictItemLambdaQueryWrapper);

        //删除项目地配置
        removeById(id);


    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void removeByTransactionals(String ids) {

        List<String> strings = Arrays.asList(ids.split(","));

        //字典组id
        LambdaQueryWrapper<MedicalDict> medicalDictLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictLambdaQueryWrapper.eq(MedicalDict::getGroupCode,"SOLR_DATA_SOURCE");
        MedicalDict medicalDict = medicalDictService.getOne(medicalDictLambdaQueryWrapper);
        String groupId = medicalDict.getGroupId();

        //批量删除字典
        ArrayList<String> codes = new ArrayList<>();
        for(String id:strings){
            SysDatasource ds = baseMapper.selectById(id);
            codes.add(ds.getCode());
        }
        LambdaQueryWrapper<MedicalDictItem> medicalDictItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        medicalDictItemLambdaQueryWrapper.eq(MedicalDictItem::getGroupId,groupId);
        medicalDictItemLambdaQueryWrapper.in(MedicalDictItem::getCode,codes);
        medicalDictItemService.remove(medicalDictItemLambdaQueryWrapper);
        //批量删除项目地配置
        removeByIds(strings);
    }

    @Override
    public SysDatasource getByCode(String code) {
        SysDatasource bean = this.baseMapper.selectOne(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getCode,code));
        return bean;
    }

    @Override
    public IPage<RoleVo> getRoleList(Page<RoleVo> page, SysDatasourceVo sysDatasource) {
        IPage<RoleVo> result = sysDatasourceMapper.getRoleList(page,sysDatasource);
        return result;
    }

    @Override
    public void addRoleBatch(String code, String ids) {
        sysDatasourceMapper.addRoleBatch(code,ids);
    }

    @Override
    public void delRoleBatch(String code, String ids) {
        sysDatasourceMapper.delRoleBatch(code,ids);
    }

    @Override
    public IPage<SysDatasource> getPage(Page<SysDatasource> page, QueryWrapper<SysDatasource> queryWrapper) {
        IPage<SysDatasource> pageList = sysDatasourceMapper.getPage(page, queryWrapper);
        return pageList;
    }
}
