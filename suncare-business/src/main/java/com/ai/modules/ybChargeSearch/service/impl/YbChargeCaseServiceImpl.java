package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.modules.ybChargeSearch.entity.YbChargeCase;
import com.ai.modules.ybChargeSearch.mapper.YbChargeCaseMapper;
import com.ai.modules.ybChargeSearch.service.IYbChargeCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 违规案例库
 * @Author: jeecg-boot
 * @Date:   2023-01-13
 * @Version: V1.0
 */
@Service
public class YbChargeCaseServiceImpl extends ServiceImpl<YbChargeCaseMapper, YbChargeCase> implements IYbChargeCaseService {

    @Autowired
    private YbChargeCaseMapper ybChargeCaseMapper;

    @Override
    public List<YbChargeCase> selectByStr(String sql) {
        List<YbChargeCase> list= ybChargeCaseMapper.selectByStr(sql);
        return list;
    }
}
