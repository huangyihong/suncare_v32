package com.ai.modules.formal.service;

import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 模型关联项目药品或组
 * @Author: jeecg-boot
 * @Date:   2020-07-17
 * @Version: V1.0
 */
public interface IMedicalFormalCaseItemRelaService extends IService<MedicalFormalCaseItemRela> {

    List<MedicalFormalCaseItemRelaVO> listVoByBatchId(String batchId);
    List<MedicalFormalCaseItemRelaVO> listVoByBatchIdAndCaseIds(String batchId, String[] caseIds);
}
