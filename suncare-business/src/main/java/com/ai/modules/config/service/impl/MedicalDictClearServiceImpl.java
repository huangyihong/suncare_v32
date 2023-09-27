package com.ai.modules.config.service.impl;

import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.mapper.MedicalDictMapper;
import com.ai.modules.config.service.IMedicalDictClearService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Service
@Slf4j
public class MedicalDictClearServiceImpl extends ServiceImpl<MedicalDictMapper, MedicalDict> implements IMedicalDictClearService {


    @Override
    @CacheEvict(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE,key = "#code+':'+#key+':'+#kind")
    public void clearCache(String code, String key, String kind){
        log.info("清除缓存：" + code + " -- " + key + " --- " + kind);
    }

    @Override
    @CacheEvict(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE,key = "#code+'::'+#text+':'+#kind")
    public void clearTextCache(String code, String text, String kind) {
        log.info("清除缓存：" + code + " -- " + text + " --- " + kind);

    }

    @Override
    @CacheEvict(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE,key = "#code+'::'+#kind")
    public void clearCache(String code, String kind){
        log.info("清除缓存：" + code + " -- " + kind);
    }

    @Override
    @CacheEvict(value = CacheConstant.REMOTE_OTHER_DICT_CACHE, allEntries=true)
    public void clearRemoteOdictCache(String code){
        log.info("清除远程其他字典缓存：" + code);
    }


    @Override
    @CacheEvict(value = CacheConstant.REMOTE_MEDICAL_DICT_CACHE, allEntries=true)
    public void clearRemoteMdictCache(String code, String kind){
        log.info("清除远程医疗字典缓存：" + code + " -- " + kind);
    }

}
