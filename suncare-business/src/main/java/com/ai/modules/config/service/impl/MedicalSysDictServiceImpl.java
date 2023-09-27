package com.ai.modules.config.service.impl;

import com.ai.modules.config.entity.MedicalSysDict;
import com.ai.modules.config.mapper.MedicalSysDictMapper;
import com.ai.modules.config.service.IMedicalSysDictService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.DictModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
@Service
@Slf4j
public class MedicalSysDictServiceImpl extends ServiceImpl<MedicalSysDictMapper, MedicalSysDict> implements IMedicalSysDictService {


    @Override
    public List<MedicalSysDict> queryByType(String type) {
        return this.baseMapper.selectList(new QueryWrapper<MedicalSysDict>().eq("DICT_TYPE",type).orderByAsc("IS_ORDER"));
    }

    @Override
    public Map<String, List<MedicalSysDict>> queryByTypes(String[] types) {
        Map<String, List<MedicalSysDict>> map = new HashMap<>();
        List<MedicalSysDict> dataList = this.baseMapper.selectList(new QueryWrapper<MedicalSysDict>().in("DICT_TYPE",types).orderByAsc("IS_ORDER", "DICT_TYPE"));
        for(String type:types){
            map.put(type, new ArrayList<>());
        }
        for(MedicalSysDict dict: dataList){
            map.get(dict.getDictType()).add(dict);
        }
        return map;
    }

    @Override
    @Cacheable(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE,key = "#code+':'+#key")
    public String queryDictTextByKey(String code, String key) {
//        log.info("数据库查询医疗字典" +code +"-" +key);
        return this.baseMapper.queryDictTextByKey(code, key);
    }
}
