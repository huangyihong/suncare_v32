package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalEquipInstruction;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 医疗器械说明书
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
public interface IMedicalEquipInstructionService extends IService<MedicalEquipInstruction> {
    void saveMedicalEquipInstruction(MedicalEquipInstruction medicalEquipInstruction, String itemCodes, String itemNames, String tableTypes);

    void updateMedicalEquipInstruction(MedicalEquipInstruction medicalEquipInstruction, String itemCodes, String itemNames, String tableTypes);

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
    public MedicalEquipInstruction getBeanByCode(String code);

    /**
     * 导出excel
     * @param list
     * @param os
     * @return
     */
    public boolean exportExcel(List<MedicalEquipInstruction> list, OutputStream os, String suffix) throws Exception;

    /**
     * 导入
     * @param file
     * @param user
     * @return
     */
    public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;
}
