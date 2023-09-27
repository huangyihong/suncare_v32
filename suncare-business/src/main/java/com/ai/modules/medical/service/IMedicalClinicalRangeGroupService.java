package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.vo.MedicalClinicalRangeGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 临床路径范围组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface IMedicalClinicalRangeGroupService extends IService<MedicalClinicalRangeGroup> {

    List<MedicalClinicalRangeGroupVO> listDetail(QueryWrapper<MedicalClinicalRangeGroup> queryWrapper, String type);
}
