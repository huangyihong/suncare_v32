package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description: 临床路径准入条件组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface IMedicalClinicalAccessGroupService extends IService<MedicalClinicalAccessGroup> {

    int importExcel(MultipartFile file) throws Exception;
}
