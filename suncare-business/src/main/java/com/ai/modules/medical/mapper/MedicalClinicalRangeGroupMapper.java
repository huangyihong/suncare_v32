package com.ai.modules.medical.mapper;

import java.util.List;

import com.ai.modules.medical.vo.MedicalClinicalRangeGroupVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 临床路径范围组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface MedicalClinicalRangeGroupMapper extends BaseMapper<MedicalClinicalRangeGroup> {

    List<MedicalClinicalRangeGroupVO> listDetail(@Param(Constants.WRAPPER) Wrapper<MedicalClinicalRangeGroup> wrapper,@Param("type") String type);

    /**
     * 包括主体里的信息，用于导出
     * @param wrapper
     * @param type
     * @return
     */
    List<MedicalClinicalRangeGroupVO> listDetailMore(@Param(Constants.WRAPPER) Wrapper<MedicalClinicalRangeGroup> wrapper,@Param("type") String type);
}
