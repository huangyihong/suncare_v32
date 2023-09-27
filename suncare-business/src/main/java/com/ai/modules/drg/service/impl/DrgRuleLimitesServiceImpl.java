package com.ai.modules.drg.service.impl;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.handle.model.DrgRuleModel;
import com.ai.modules.drg.mapper.DrgRuleLimitesMapper;
import com.ai.modules.drg.service.IDrgRuleLimitesService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: drg规则限定条件表
 * @Author: jeecg-boot
 * @Date:   2023-05-08
 * @Version: V1.0
 */
@Service
@DS("greenplum")
public class DrgRuleLimitesServiceImpl extends ServiceImpl<DrgRuleLimitesMapper, DrgRuleLimites> implements IDrgRuleLimitesService {

    @Override
    public List<DrgRuleModel> queryAdrgRule(String versionCode) {
        return baseMapper.queryAdrgRule(versionCode);
    }

    @Override
    public List<DrgRuleModel> queryDrgRule(String versionCode) {
        return baseMapper.queryDrgRule(versionCode);
    }
}
