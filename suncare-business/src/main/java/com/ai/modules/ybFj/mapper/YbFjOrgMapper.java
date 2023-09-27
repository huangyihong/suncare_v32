package com.ai.modules.ybFj.mapper;

import java.util.List;

import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.ybFj.entity.YbFjOrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 医疗机构信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
public interface YbFjOrgMapper extends BaseMapper<YbFjOrg> {

    IPage<OrgUserVo> getOrgUser(Page<OrgUserVo> page, @Param("query") OrgUserVo orgUserVo);

    IPage<OrgUserVo> getUserOrgList(Page<OrgUserVo> page, @Param("query") OrgUserVo orgUserVo);
}
