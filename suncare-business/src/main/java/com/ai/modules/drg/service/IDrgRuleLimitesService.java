package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.handle.model.DrgRuleModel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: drg规则限定条件表
 * @Author: jeecg-boot
 * @Date:   2023-05-08
 * @Version: V1.0
 */
public interface IDrgRuleLimitesService extends IService<DrgRuleLimites> {

    List<DrgRuleModel> queryAdrgRule(String versionCode);

    List<DrgRuleModel> queryDrgRule(String versionCode);
}
