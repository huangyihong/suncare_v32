package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueDtl;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 飞检项目线索明细
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
public interface IYbFjProjectClueDtlService extends IService<YbFjProjectClueDtl> {

    IPage<YbFjProjectClueDtl> queryProjectClueDtl(IPage<YbFjProjectClueDtl> page, QryProjectClueDtlDto dto) throws Exception;

    TaskClueVo queryTaskClueVo(String clueIds);
}
