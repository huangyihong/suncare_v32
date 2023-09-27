package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.vo.MedicalDrugRuleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.OutputStream;

/**
 * @Description: 药品合规规则
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
public interface IMedicalDrugRuleService extends IService<MedicalDrugRule> {

    void saveMedicalDrugRule(MedicalDrugRule bean);
    void updateMedicalDrugRule(MedicalDrugRule bean);
    void exportExcel(String ruleType,QueryWrapper<MedicalDrugRule> queryWrapper, OutputStream os, String suffix)  throws Exception;

	Result<?> importExcel(MultipartFile file, MultipartHttpServletRequest multipartRequest, LoginUser user) throws Exception;

    IPage<MedicalDrugRuleVO> pageVO(Page<MedicalDrugRuleVO> page, QueryWrapper<MedicalDrugRule> queryWrapper, String dataSource);

    void exportRuleLoseExcel(String ruleType,QueryWrapper<MedicalDrugRule> queryWrapper, OutputStream os, String suffix)  throws Exception;
}
