package com.ai.modules.config.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.config.entity.MedicalOrgan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 医疗机构
 * @Author: jeecg-boot
 * @Date:   2019-12-31
 * @Version: V1.0
 */
public interface MedicalOrganMapper extends BaseMapper<MedicalOrgan> {
    IPage<MedicalOrgan> listByMasterInfoJoin(Page<MedicalOrgan> page, @Param(Constants.WRAPPER) Wrapper<MedicalOrgan> wrapper, @Param("dataSource") String dataSource);

    List<MedicalCodeNameVO> listMasterInfoJoinSelectMaps(@Param(Constants.WRAPPER) Wrapper<MedicalOrgan> queryWrapper, @Param("dataSource") String dataSource);
}
