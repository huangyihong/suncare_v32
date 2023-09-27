package com.ai.modules.ybChargeSearch.mapper;

import java.util.List;

import com.ai.modules.ybChargeSearch.vo.YbMeetingMaterialsVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterials;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 上会材料主表
 * @Author: jeecg-boot
 * @Date:   2022-11-29
 * @Version: V1.0
 */
public interface YbMeetingMaterialsMapper extends BaseMapper<YbMeetingMaterials> {

    IPage<YbMeetingMaterialsVo> getPage(Page<YbMeetingMaterials> page,@Param("query") YbMeetingMaterialsVo ybMeetingMaterialsVo);
}
