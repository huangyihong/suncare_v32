package com.ai.modules.medical.mapper;

import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.vo.MedicalClinicalIOVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 临床路径主体
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface MedicalClinicalMapper extends BaseMapper<MedicalClinical> {
    List<MedicalClinicalIOVO> listWholeInfo(@Param(Constants.WRAPPER) Wrapper<MedicalClinical> wrapper);

    public List<MedicalCodeNameVO> queryIdByCode(@Param("codes") String [] codes);
    public List<MedicalCodeNameVO> queryGroupCodeIdByCodes(@Param("codes") String [] codes, @Param("kind") String kind);
    public List<MedicalCodeNameVO> queryGroupCodeIdInCodes(@Param("codes") String [] codes, @Param("kind") String kind);
    public List<MedicalCodeNameVO> queryItemCodeIdInCodes(@Param("codes") String [] codes, @Param("kind") String kind);
    public List<MedicalCodeNameVO> queryTreatOrEquipmentCodeIdInCodes(@Param("codes") String [] codes);
    public Double queryMaxOrder();
}
