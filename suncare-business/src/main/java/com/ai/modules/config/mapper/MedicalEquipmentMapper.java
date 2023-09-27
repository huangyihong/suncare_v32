package com.ai.modules.config.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.config.vo.MedicalCodeNameVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.config.entity.MedicalEquipment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 医疗器械信息表
 * @Author: jeecg-boot
 * @Date:   2020-05-09
 * @Version: V1.0
 */
public interface MedicalEquipmentMapper extends BaseMapper<MedicalEquipment> {
    public List<MedicalCodeNameVO> queryCodeNameIdInCodes(@Param("codes") String[] codes);
}
