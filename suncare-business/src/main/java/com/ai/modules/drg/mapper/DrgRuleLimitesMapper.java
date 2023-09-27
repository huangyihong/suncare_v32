package com.ai.modules.drg.mapper;

import java.util.List;

import com.ai.modules.drg.handle.model.DrgRuleModel;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.drg.entity.DrgRuleLimites;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: drg规则限定条件表
 * @Author: jeecg-boot
 * @Date:   2023-05-08
 * @Version: V1.0
 */
public interface DrgRuleLimitesMapper extends BaseMapper<DrgRuleLimites> {

    List<DrgRuleModel> queryAdrgRule(@Param("versionCode") String versionCode);

    List<DrgRuleModel> queryDrgRule(@Param("versionCode") String versionCode);
}
