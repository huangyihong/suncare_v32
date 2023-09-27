package com.ai.modules.review.mapper;


import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.vo.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface DwbMapper {
    IPage<DwbDiagVo> dwbDiagList(Page<DwbDiagVo> page, @Param(Constants.WRAPPER) Wrapper<DwbDiagVo> wrapper);
    IPage<DwbMasterInfoVo> dwbMasterInfoList(Page<DwbMasterInfoVo> page, @Param(Constants.WRAPPER) Wrapper<DwbMasterInfoVo> wrapper);
    IPage<DwbChargeDetailVo> dwbChargeDetailList(Page<DwbChargeDetailVo> page, @Param(Constants.WRAPPER) Wrapper<DwbChargeDetailVo> wrapper);
    IPage<DwbClientVo> dwbClientList(Page<DwbClientVo> page, @Param(Constants.WRAPPER) Wrapper<DwbClientVo> wrapper);
    IPage<DwbOrganizationVo> dwbOrganizationList(Page<DwbOrganizationVo> page, @Param(Constants.WRAPPER) Wrapper<DwbOrganizationVo> wrapper);
    IPage<DwbOrderVo> dwbOrderList(Page<DwbOrderVo> page, @Param(Constants.WRAPPER) Wrapper<DwbOrderVo> wrapper);
    IPage<DwbDoctorVo> dwbDoctorList(Page<DwbDoctorVo> page, @Param(Constants.WRAPPER) Wrapper<DwbDoctorVo> wrapper);
}
