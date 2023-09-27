package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
public interface IMedicalRuleConfigDruguseService extends IMedicalRuleConfigCommonService {

    String RULE_TYPE = "DRUGUSE";

    String RULE_LIMIT_DICT = "RULE_LIMIT_DRUGUSE";

}
