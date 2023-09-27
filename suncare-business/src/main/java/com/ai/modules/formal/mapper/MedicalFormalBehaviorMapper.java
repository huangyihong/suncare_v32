package com.ai.modules.formal.mapper;

import java.util.List;

import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.formal.entity.MedicalFormalBehavior;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 不合规行为配置
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
public interface MedicalFormalBehaviorMapper extends BaseMapper<MedicalFormalBehavior> {
    //行为总览分页查询
    List<MedicalFormalBehavior> selectByBatchCase(@Param("batchId") String batchId,@Param("caseIds") String[] caseId);

    List<MedicalFormalBehaviorVO> selectBehaviorCaseByBatch(@Param("batchId") String batchId);
}
