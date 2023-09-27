package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbChargeCase;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 违规案例库
 * @Author: jeecg-boot
 * @Date:   2023-01-13
 * @Version: V1.0
 */
public interface IYbChargeCaseService extends IService<YbChargeCase> {


    List<YbChargeCase> selectByStr(String sql);
}
