package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.vo.MedicalDruguseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 合理用药配置
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
public interface IMedicalDruguseService extends IService<MedicalDruguse> {

//    void save(MedicalDruguseVO medicalDruguse, List<MedicalRuleConditionSetet> ruleGroups);
    void save(MedicalDruguseVO medicalDruguse, List<MedicalRuleConditionSet> ruleGroups);

    void updateById(MedicalDruguseVO medicalDruguse, List<MedicalRuleConditionSet> ruleGroups);

    boolean delRuleGroup(String ruleId);

    boolean delRuleGroup(List<String> idList);

    int[] importExcel(MultipartFile file) throws Exception;

    void exportExcel(QueryWrapper<MedicalDruguse> queryWrapper, OutputStream os) throws Exception;

    void exportInvalid(QueryWrapper<MedicalDruguse> queryWrapper, OutputStream os) throws Exception;
}
