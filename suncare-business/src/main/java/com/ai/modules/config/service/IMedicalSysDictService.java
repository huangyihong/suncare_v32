package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalSysDict;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
public interface IMedicalSysDictService extends IService<MedicalSysDict> {


    List<MedicalSysDict> queryByType(String type );

    Map<String, List<MedicalSysDict>> queryByTypes(String[] types);

    String queryDictTextByKey(String code, String key);

}
