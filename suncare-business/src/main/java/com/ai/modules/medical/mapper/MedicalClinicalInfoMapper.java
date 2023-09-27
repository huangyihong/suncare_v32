package com.ai.modules.medical.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 临床路径资料信息
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface MedicalClinicalInfoMapper extends BaseMapper<MedicalClinicalInfo> {

    MedicalClinicalInfo getByCode(@Param("code") String code);
}
