package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalYbDrug;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 重复用药
 * @Author: jeecg-boot
 * @Date:   2021-06-08
 * @Version: V1.0
 */
public interface IMedicalYbDrugService extends IService<MedicalYbDrug> {

    void updateGroup(MedicalYbDrug medicalYbDrug, String codes, String names, String tableTypes, String dosageCodes,String dosages, LoginUser user) throws Exception;

    Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

    boolean exportExcel(List<MedicalYbDrug> list, OutputStream os, String suffix) throws Exception;
}
