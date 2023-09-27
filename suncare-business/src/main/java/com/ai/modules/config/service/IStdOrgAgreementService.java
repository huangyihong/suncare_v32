package com.ai.modules.config.service;

import com.ai.modules.config.entity.StdOrgAgreement;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * @Description: 医疗机构医保协议相关参数
 * @Author: jeecg-boot
 * @Date:   2020-12-03
 * @Version: V1.0
 */
public interface IStdOrgAgreementService extends IService<StdOrgAgreement> {
    Result<?> importExcel(MultipartFile file,  LoginUser user) throws Exception;

    boolean exportExcel(List<StdOrgAgreement> list, OutputStream os, String suffix) throws Exception;

    boolean isExist(String orgid, String id, String surancetypecode, Date startdate, Date enddate);
}
