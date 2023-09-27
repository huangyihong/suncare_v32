package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterials;
import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterialsDetail;
import com.ai.modules.ybChargeSearch.vo.YbMeetingMaterialsVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;

/**
 * @Description: 上会材料主表
 * @Author: jeecg-boot
 * @Date:   2022-11-29
 * @Version: V1.0
 */
public interface IYbMeetingMaterialsService extends IService<YbMeetingMaterials> {

    IPage<YbMeetingMaterialsVo> getPage(Page<YbMeetingMaterials> page, YbMeetingMaterialsVo ybMeetingMaterialsVo);

    void saveAll(ArrayList<YbMeetingMaterials> ybMeetingMaterials, ArrayList<YbMeetingMaterialsDetail> ybMeetingMaterialsDetails);

    void removeAll(String id);

    void removeBatchAll(String ids);
}
