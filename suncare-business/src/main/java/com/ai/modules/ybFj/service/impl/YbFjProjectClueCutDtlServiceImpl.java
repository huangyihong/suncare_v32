package com.ai.modules.ybFj.service.impl;

import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueCutDtl;
import com.ai.modules.ybFj.entity.YbFjProjectClueDtl;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsiteDtl;
import com.ai.modules.ybFj.mapper.YbFjProjectClueCutDtlMapper;
import com.ai.modules.ybFj.service.IYbFjProjectClueCutDtlService;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description: 飞检项目终审确认线索明细
 * @Author: jeecg-boot
 * @Date:   2023-06-13
 * @Version: V1.0
 */
@Service
public class YbFjProjectClueCutDtlServiceImpl extends ServiceImpl<YbFjProjectClueCutDtlMapper, YbFjProjectClueCutDtl> implements IYbFjProjectClueCutDtlService {

    @Override
    public IPage<YbFjProjectClueCutDtl> queryProjectClueDtl(IPage<YbFjProjectClueCutDtl> page, QryProjectClueDtlDto dto) throws Exception {
        return this.page(page, buildQueryWrapper(dto));
    }

    @Override
    public int insertCutClueDtl(SyncClueDto dto) {
        return baseMapper.insertCutClueDtl(dto);
    }

    @Override
    public TaskClueVo queryTaskClueVo(String clueIds) {
        String[] ids = clueIds.split(",");
        QueryWrapper<YbFjProjectClueCutDtl> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", ids);
        return baseMapper.queryTaskClueVo(wrapper);
    }

    private QueryWrapper<YbFjProjectClueCutDtl> buildQueryWrapper(QryProjectClueDtlDto dto) throws Exception {
        if(StringUtils.isBlank(dto.getClueId())) {
            throw new Exception("clueId参数不能为空");
        }
        QueryWrapper<YbFjProjectClueCutDtl> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", dto.getClueId());
        if(StringUtils.isNotBlank(dto.getVisitid())) {
            wrapper.eq("visitid", dto.getVisitid());
        }
        if(StringUtils.isNotBlank(dto.getDoctorname())) {
            wrapper.like("doctorname", dto.getDoctorname());
        }
        if(StringUtils.isNotBlank(dto.getDeptname())) {
            wrapper.like("deptname", dto.getDeptname());
        }
        if(StringUtils.isNotBlank(dto.getVisittype())) {
            wrapper.eq("visittype", dto.getVisittype());
        }
        if(StringUtils.isNotBlank(dto.getClientname())) {
            wrapper.eq("clientname", dto.getClientname());
        }
        if(StringUtils.isNotBlank(dto.getSex())) {
            wrapper.eq("sex", dto.getSex());
        }
        if(StringUtils.isNotBlank(dto.getDis())) {
            wrapper.like("dis", dto.getDis());
        }
        if(StringUtils.isNotBlank(dto.getHisItemname())) {
            wrapper.like("his_itemname", dto.getHisItemname());
        }
        if(StringUtils.isNotBlank(dto.getItemname())) {
            wrapper.like("itemname", dto.getItemname());
        }
        if(StringUtils.isNotBlank(dto.getChargeattri())) {
            wrapper.eq("chargeattri", dto.getChargeattri());
        }
        if(StringUtils.isNotBlank(dto.getVisitdate())) {
            wrapper.eq("visitdate", dto.getVisitdate());
        }
        if(StringUtils.isNotBlank(dto.getLeavedate())) {
            wrapper.eq("leavedate", dto.getLeavedate());
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
