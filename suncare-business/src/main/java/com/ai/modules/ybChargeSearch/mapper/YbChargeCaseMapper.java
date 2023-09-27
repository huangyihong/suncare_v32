package com.ai.modules.ybChargeSearch.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ai.modules.ybChargeSearch.entity.YbChargeCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Description: 违规案例库
 * @Author: jeecg-boot
 * @Date:   2023-01-13
 * @Version: V1.0
 */
public interface YbChargeCaseMapper extends BaseMapper<YbChargeCase> {

    List<YbChargeCase> selectByStr(@Param("query") String sql);
}
