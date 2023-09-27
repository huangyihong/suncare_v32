package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalRuleRely;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.OutputStream;

/**
 * @Description: 规则依赖字段表
 * @Author: jeecg-boot
 * @Date:   2022-01-20
 * @Version: V1.0
 */
public interface IMedicalRuleRelyService extends IService<MedicalRuleRely> {

    boolean exportExcel(QueryWrapper<MedicalRuleRely> queryWrapper, OutputStream os, String suffix) throws Exception;
}
