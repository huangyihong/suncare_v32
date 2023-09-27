package com.ai.modules.ybFj.mapper;

import java.util.List;

import com.ai.modules.ybFj.dto.SyncClueDto;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsiteDtl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 飞检项目现场核查线索明细
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
public interface YbFjProjectClueOnsiteDtlMapper extends BaseMapper<YbFjProjectClueOnsiteDtl> {

    int insertOnsiteClueDtl(SyncClueDto dto);
}
