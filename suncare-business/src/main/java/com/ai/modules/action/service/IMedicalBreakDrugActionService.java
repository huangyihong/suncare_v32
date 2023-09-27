package com.ai.modules.action.service;

import com.ai.modules.action.entity.MedicalBreakDrugAction;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: 不合规结果
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
public interface IMedicalBreakDrugActionService extends IService<MedicalBreakDrugAction> {

    IPage<MedicalBreakDrugAction> pageSolr(Page<MedicalBreakDrugAction> page, MedicalBreakDrugAction medicalBreakDrugAction, HttpServletRequest req) throws Exception;

}
