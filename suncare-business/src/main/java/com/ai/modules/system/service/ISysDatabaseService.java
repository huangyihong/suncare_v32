package com.ai.modules.system.service;

import org.jeecg.common.api.vo.Result;
import com.ai.modules.system.entity.SysDatabase;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 数据源配置
 * @Author: jeecg-boot
 * @Date:   2022-11-22
 * @Version: V1.0
 */
public interface ISysDatabaseService extends IService<SysDatabase> {

    Result<?> testDbConnection(SysDatabase sysDatabase);

    SysDatabase getByDbname(String dbname);
}
