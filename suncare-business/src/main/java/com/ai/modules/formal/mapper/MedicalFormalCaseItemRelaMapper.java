package com.ai.modules.formal.mapper;

import java.util.List;

import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 模型关联项目药品或组
 * @Author: jeecg-boot
 * @Date:   2020-07-17
 * @Version: V1.0
 */
public interface MedicalFormalCaseItemRelaMapper extends BaseMapper<MedicalFormalCaseItemRela> {

    List<MedicalFormalCaseItemRelaVO> listVoByBatchId(@Param("batchId") String batchId);
    List<MedicalFormalCaseItemRelaVO> listVoByBatchIdAndCaseIds(@Param("batchId") String batchId,@Param("caseIds") String[] caseIds);

}
