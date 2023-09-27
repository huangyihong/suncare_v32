package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.StringCamelUtils;
import com.ai.common.utils.StringUtil;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.entity.YbFjDynamicConfig;
import com.ai.modules.ybFj.mapper.YbFjDynamicConfigMapper;
import com.ai.modules.ybFj.service.IYbFjDynamicConfigService;
import com.ai.modules.ybFj.vo.FjDynamicConfigVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 飞检动态表单
 * @Author: jeecg-boot
 * @Date:   2023-06-07
 * @Version: V1.0
 */
@Service
public class YbFjDynamicConfigServiceImpl extends ServiceImpl<YbFjDynamicConfigMapper, YbFjDynamicConfig> implements IYbFjDynamicConfigService {

    @Override
    public List<FjDynamicConfigVo> queryFjDynamicConfig(String configCategory, String configType) {
        QueryWrapper<YbFjDynamicConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_category", configCategory);
        wrapper.eq("config_type", configType);
        wrapper.eq("display_flag", DcFjConstants.STATE_YES);
        wrapper.orderByAsc("col_seq");
        List<YbFjDynamicConfig> list = this.list(wrapper);
        if(list!=null && list.size()>0) {
            List<FjDynamicConfigVo> result = new ArrayList<>();
            for(YbFjDynamicConfig config : list) {
                FjDynamicConfigVo vo = new FjDynamicConfigVo();
                BeanUtil.copyProperties(config, vo, CopyOptions.create().ignoreNullValue());
                String colName = vo.getColName().toLowerCase();
                //转为驼峰风格
                colName = StringCamelUtils.underline2Camel(colName, true);
                vo.setColName(colName);
                result.add(vo);
            }
            return result;
        }
        return null;
    }

    @Override
    public List<FjDynamicConfigVo> queryFjDynamicConfigDefault(String configCategory) {
        return queryFjDynamicConfig(configCategory, "default");
    }
}
