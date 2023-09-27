package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.vo.YbChargeDrugRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: 药品规则库
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
public interface IYbChargeDrugRuleService extends IService<YbChargeDrugRule> {

    IPage<?> drugRuleImportList(YbChargeDrugRuleVo ybChargeDrugRuleVo, Page<YbChargeDrugRuleVo> page, HttpServletRequest req) throws Exception;
}
