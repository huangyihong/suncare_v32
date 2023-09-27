package com.ai.modules.medical.mapper;

import java.util.List;

import com.ai.modules.medical.entity.dto.MedicalDruguseIO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 合理用药配置
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
public interface MedicalDruguseMapper extends BaseMapper<MedicalDruguse> {

    List<MedicalDruguseIO> listIO(@Param(Constants.WRAPPER) QueryWrapper<MedicalDruguse> queryWrapper);

}
