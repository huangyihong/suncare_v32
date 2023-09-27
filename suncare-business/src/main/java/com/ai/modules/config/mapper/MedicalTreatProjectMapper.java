package com.ai.modules.config.mapper;

import com.ai.modules.config.entity.MedicalTreatProject;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.config.vo.MedicalTreatProjectEquipmentVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗服务项目
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface MedicalTreatProjectMapper extends BaseMapper<MedicalTreatProject> {
	IPage<MedicalTreatProjectEquipmentVO> selectTreatProjectEquipmentPageVO(Page<MedicalTreatProjectEquipmentVO> page, @Param(Constants.WRAPPER) Wrapper<MedicalTreatProjectEquipmentVO> wrapper);
	List<MedicalTreatProjectEquipmentVO> selectTreatProjectEquipmentPageVO(@Param(Constants.WRAPPER) Wrapper<MedicalTreatProjectEquipmentVO> wrapper);

	public List<MedicalCodeNameVO> queryCodeNameIdInCodes(@Param("codes") String[] codes);
}
