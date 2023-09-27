package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.constant.CacheConstant;
import org.springframework.cache.annotation.CacheEvict;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
public interface IMedicalDictClearService extends IService<MedicalDict> {
    void clearCache(String code, String key, String kind);
    void clearTextCache(String code, String text, String kind);

    void clearCache(String code, String kind);

    @CacheEvict(value = CacheConstant.REMOTE_OTHER_DICT_CACHE, allEntries=true)
    void clearRemoteOdictCache(String code);

    @CacheEvict(value = CacheConstant.REMOTE_MEDICAL_DICT_CACHE, allEntries=true)
    void clearRemoteMdictCache(String code, String kind);
}
