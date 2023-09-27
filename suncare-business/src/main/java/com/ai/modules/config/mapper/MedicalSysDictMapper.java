package com.ai.modules.config.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ai.modules.config.entity.MedicalSysDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.common.system.vo.DictModel;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
public interface MedicalSysDictMapper extends BaseMapper<MedicalSysDict> {
    String queryDictTextByKey(@Param("code") String code,@Param("key") String key);

}
