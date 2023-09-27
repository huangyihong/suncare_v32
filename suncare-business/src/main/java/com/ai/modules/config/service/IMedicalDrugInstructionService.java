package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDrugInstruction;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 药品说明书
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
public interface IMedicalDrugInstructionService extends IService<MedicalDrugInstruction> {

    void saveMedicalDrugInstruction(MedicalDrugInstruction medicalDrugInstruction, String itemCodes, String itemNames, String tableTypes);

    void updateMedicalDrugInstruction(MedicalDrugInstruction medicalDrugInstruction, String itemCodes, String itemNames, String tableTypes);

    void deleteById(String id);

    void deleteByIds(List<String> idList);

    /**
     * 编码是否重复
     * @param code
     * @param id
     * @return
     */
    boolean isExistName(String code, String id);

    /**
     * 根据编码获取bean对象
     * @param code
     * @return
     */
    public MedicalDrugInstruction getBeanByCode(String code);

    /**
     * 导出excel
     * @param list
     * @param os
     * @return
     */
    public boolean exportExcel(List<MedicalDrugInstruction> list, OutputStream os, String suffix) throws Exception;

    /**
     * 导入
     * @param file
     * @param user
     * @return
     */
    public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;
}
