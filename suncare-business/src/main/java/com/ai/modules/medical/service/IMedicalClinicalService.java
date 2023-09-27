package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalClinical;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * @Description: 临床路径主体
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface IMedicalClinicalService extends IService<MedicalClinical> {

    Double getMaxOrderNo();

    int[] importExcel(MultipartFile file) throws Exception;

    void exportExcel(QueryWrapper<MedicalClinical> queryWrapper, OutputStream os) throws Exception;
}
