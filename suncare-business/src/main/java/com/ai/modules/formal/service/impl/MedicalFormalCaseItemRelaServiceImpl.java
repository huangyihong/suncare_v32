package com.ai.modules.formal.service.impl;

import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.formal.mapper.MedicalFormalCaseItemRelaMapper;
import com.ai.modules.formal.service.IMedicalFormalCaseItemRelaService;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 模型关联项目药品或组
 * @Author: jeecg-boot
 * @Date:   2020-07-17
 * @Version: V1.0
 */
@Service
public class MedicalFormalCaseItemRelaServiceImpl extends ServiceImpl<MedicalFormalCaseItemRelaMapper, MedicalFormalCaseItemRela> implements IMedicalFormalCaseItemRelaService {

    @Override
    public List<MedicalFormalCaseItemRelaVO> listVoByBatchId(String batchId) {
        return this.baseMapper.listVoByBatchId(batchId);
    }

    @Override
    public List<MedicalFormalCaseItemRelaVO> listVoByBatchIdAndCaseIds(String batchId, String[] caseIds) {
        return this.baseMapper.listVoByBatchIdAndCaseIds(batchId, caseIds);
    }
}
