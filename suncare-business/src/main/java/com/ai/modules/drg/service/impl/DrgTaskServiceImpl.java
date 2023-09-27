package com.ai.modules.drg.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.mapper.DrgTaskMapper;
import com.ai.modules.drg.service.IDrgHandleService;
import com.ai.modules.drg.service.IDrgTaskService;
import com.ai.modules.drg.vo.DrgTaskVo;
import com.ai.modules.ybFj.service.IYbFjProjectOrgService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: drg任务表
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Service
@DS("greenplum")
public class DrgTaskServiceImpl extends ServiceImpl<DrgTaskMapper, DrgTask> implements IDrgTaskService {
    @Autowired
    @Lazy
    private IDrgHandleService drgHandleService;

    @Override
    public IPage<DrgTaskVo> pageVO(Page<DrgTask> page, Wrapper<DrgTask> wrapper) {
        return this.baseMapper.selectPageVO(page, wrapper);
    }

}
