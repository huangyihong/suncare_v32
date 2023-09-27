package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterials;
import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterialsDetail;
import com.ai.modules.ybChargeSearch.mapper.YbMeetingMaterialsMapper;
import com.ai.modules.ybChargeSearch.service.IYbMeetingMaterialsDetailService;
import com.ai.modules.ybChargeSearch.service.IYbMeetingMaterialsService;
import com.ai.modules.ybChargeSearch.vo.YbMeetingMaterialsVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 上会材料主表
 * @Author: jeecg-boot
 * @Date:   2022-11-29
 * @Version: V1.0
 */
@Service
public class YbMeetingMaterialsServiceImpl extends ServiceImpl<YbMeetingMaterialsMapper, YbMeetingMaterials> implements IYbMeetingMaterialsService {

    @Autowired
    private YbMeetingMaterialsMapper ybMeetingMaterialsMapper;
    @Autowired
    private IYbMeetingMaterialsDetailService ybMeetingMaterialsDetailService;

    @Override
    public IPage<YbMeetingMaterialsVo> getPage(Page<YbMeetingMaterials> page, YbMeetingMaterialsVo ybMeetingMaterialsVo) {
        IPage<YbMeetingMaterialsVo> result =ybMeetingMaterialsMapper.getPage(page,ybMeetingMaterialsVo);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAll(ArrayList<YbMeetingMaterials> ybMeetingMaterials, ArrayList<YbMeetingMaterialsDetail> ybMeetingMaterialsDetails) {
        saveBatch(ybMeetingMaterials);
        ybMeetingMaterialsDetailService.saveBatch(ybMeetingMaterialsDetails);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeAll(String id) {
        removeById(id);
        LambdaQueryWrapper<YbMeetingMaterialsDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(YbMeetingMaterialsDetail::getMid,id);
        ybMeetingMaterialsDetailService.remove(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBatchAll(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        removeByIds(idList);
        LambdaQueryWrapper<YbMeetingMaterialsDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(YbMeetingMaterialsDetail::getMid,idList);
        ybMeetingMaterialsDetailService.remove(queryWrapper);
    }
}
