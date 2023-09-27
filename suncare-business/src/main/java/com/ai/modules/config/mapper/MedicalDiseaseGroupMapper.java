package com.ai.modules.config.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import com.ai.modules.config.entity.MedicalDiseaseGroup;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 疾病组
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface MedicalDiseaseGroupMapper extends BaseMapper<MedicalDiseaseGroup> {

	List<MedicalGroupVO> queryGroupItem(MedicalGroupVO bean);

	int queryGroupItemCount(MedicalGroupVO bean);

	List<MedicalGroupVO> queryGroupItemByGroupCodes(@Param("groupCodes") String[] groupCodes);

	List<MedicalGroupVO> queryGroupItem2(@Param(Constants.WRAPPER) Wrapper<MedicalDiseaseGroup> wrapper);
	int queryGroupItemCount2(@Param(Constants.WRAPPER) Wrapper<MedicalDiseaseGroup> wrapper);
}
