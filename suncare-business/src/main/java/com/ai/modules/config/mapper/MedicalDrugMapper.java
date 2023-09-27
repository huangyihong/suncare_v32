package com.ai.modules.config.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.config.vo.MedicalCodeNameVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.config.entity.MedicalDrug;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 药品信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface MedicalDrugMapper extends BaseMapper<MedicalDrug> {

    public List<MedicalCodeNameVO> queryCodeNameIdInCodes(@Param("codes") String[] codes);
}
