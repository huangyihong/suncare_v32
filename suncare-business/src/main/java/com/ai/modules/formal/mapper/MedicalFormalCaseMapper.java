package com.ai.modules.formal.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.vo.MedicalFormalCaseBusiVO;
import com.ai.modules.formal.vo.MedicalFormalCaseVO;
import com.ai.modules.formal.vo.QueryMedicalFormalCaseVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Description: 风控模型正式表
 * @Author: jeecg-boot
 * @Date:   2019-11-26
 * @Version: V1.0
 */
public interface MedicalFormalCaseMapper extends BaseMapper<MedicalFormalCase> {

    //行为总览分页查询
    List<MedicalFormalCaseBusiVO> selectCaseBusiVOPage(IPage page,@Param("voParams") MedicalFormalCaseBusiVO voParams);

    //根据批次获取该批次下的业务组模型
    List<String> selectCaseIdByBatchId(String batchId);

    //根据业务组Id获取业务组下的模型
  	List<String> selectCaseIdByBusiId(String busiId);

    //根据不合规行为Id获取不合规行为下的模型
  	List<String> selectCaseIdByBehaviorId(String behaviorId);

  	List<QueryMedicalFormalCaseVO> referMedicalFormalCase();
}
