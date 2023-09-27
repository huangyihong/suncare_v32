package com.ai.modules.config.mapper;

import java.util.List;

import com.ai.modules.config.vo.MedicalColConfigVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.config.entity.MedicalColConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 表字段配置
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
public interface MedicalColConfigMapper extends BaseMapper<MedicalColConfig> {

    List<MedicalColConfigVO> getRuleSelectCol(@Param("tableName") String tableName);

}
