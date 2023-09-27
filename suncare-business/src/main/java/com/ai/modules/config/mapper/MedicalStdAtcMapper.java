package com.ai.modules.config.mapper;

import com.ai.modules.config.entity.MedicalStdAtc;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: ATC药品级别信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface MedicalStdAtcMapper extends BaseMapper<MedicalStdAtc> {

    public List<MedicalCodeNameVO> queryCodeNameIdInCodes(@Param("codes") String[] codes);


}
