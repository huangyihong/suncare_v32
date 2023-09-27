package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.entity.YbFjDynamicConfig;
import com.ai.modules.ybFj.vo.FjDynamicConfigVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 飞检动态表单
 * @Author: jeecg-boot
 * @Date:   2023-06-07
 * @Version: V1.0
 */
public interface IYbFjDynamicConfigService extends IService<YbFjDynamicConfig> {

    List<FjDynamicConfigVo> queryFjDynamicConfig(String configCategory, String configType);

    List<FjDynamicConfigVo> queryFjDynamicConfigDefault(String configCategory);
}
