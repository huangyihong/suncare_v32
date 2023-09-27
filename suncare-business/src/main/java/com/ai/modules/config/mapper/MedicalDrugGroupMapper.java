package com.ai.modules.config.mapper;

import com.ai.modules.config.entity.MedicalDrugGroup;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 药品组
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
public interface MedicalDrugGroupMapper extends BaseMapper<MedicalDrugGroup> {

	List<MedicalGroupVO> queryGroupItem(MedicalGroupVO bean);

	int queryGroupItemCount(MedicalGroupVO bean);

    List<MedicalGroupVO> queryGroupItemByGroupCodes(@Param("groupCodes") String[] groupCodes);

	List<MedicalGroupVO> queryGroupItem2(@Param(Constants.WRAPPER) Wrapper<MedicalDrugGroup> wrapper);
	int queryGroupItemCount2(@Param(Constants.WRAPPER) Wrapper<MedicalDrugGroup> wrapper);
}
