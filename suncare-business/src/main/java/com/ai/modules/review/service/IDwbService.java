package com.ai.modules.review.service;

import com.ai.modules.review.vo.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jxl.write.WritableSheet;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface IDwbService {

    /**
     * 疾病诊断列表
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbDiagVo> dwbDiagList(Page<DwbDiagVo> page, @Param(Constants.WRAPPER) Wrapper<DwbDiagVo> wrapper);

    /**
     * 就诊信息列表
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbMasterInfoVo> dwbMasterInfoList(Page<DwbMasterInfoVo> page, @Param(Constants.WRAPPER) Wrapper<DwbMasterInfoVo> wrapper);
    /**
     * 收费明细列表
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbChargeDetailVo> dwbChargeDetailList(Page<DwbChargeDetailVo> page, @Param(Constants.WRAPPER) Wrapper<DwbChargeDetailVo> wrapper);

    /**
     * 病人信息列表
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbClientVo> dwbClientList(Page<DwbClientVo> page, @Param(Constants.WRAPPER) Wrapper<DwbClientVo> wrapper);

    /**
     * 医院信息列表
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbOrganizationVo> dwbOrganizationList(Page<DwbOrganizationVo> page, @Param(Constants.WRAPPER) Wrapper<DwbOrganizationVo> wrapper);

    /**
     * 医嘱明细
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbOrderVo> dwbOrderList(Page<DwbOrderVo> page, @Param(Constants.WRAPPER) Wrapper<DwbOrderVo> wrapper);

    /**
     * 医生信息
     * @param page
     * @param wrapper
     * @return
     */
    IPage<DwbDoctorVo> dwbDoctorList(Page<DwbDoctorVo> page, @Param(Constants.WRAPPER) Wrapper<DwbDoctorVo> wrapper);

    Map<String, Object> queryDwbSettlementByVisitid(String visitid);

    void exportClientMasterInfo(String visitidParam, WritableSheet sheet)  throws Exception;
}
