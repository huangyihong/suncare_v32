package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * @Auther: zhangpeng
 * @Date: 2021/7/2 11
 * @Description:
 */
public interface IMedicalRuleConfigCommonService {

    void exportExcel(QueryWrapper<MedicalRuleConfig> queryWrapper, OutputStream os) throws Exception;

    Result importExcel(MultipartFile file) throws Exception;

    void exportInvalidExcel(QueryWrapper<MedicalRuleConfig> queryWrapper, OutputStream os) throws Exception;
}
