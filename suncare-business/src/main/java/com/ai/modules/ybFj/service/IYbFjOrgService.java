package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.YbFjOrgDto;
import com.ai.modules.ybFj.entity.YbFjOrg;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 医疗机构信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
public interface IYbFjOrgService extends IService<YbFjOrg> {

    void saveOrg(YbFjOrgDto dto) throws Exception;

    void updateOrg(YbFjOrgDto dto) throws Exception;

    /**
     *
     * 功能描述：审核
     * @author zhangly
     * @date 2023-03-06 15:52:02
     *
     * @param ids
     * @param auditState 审核状态
     * @param auditOpinion 审核意见
     *
     * @return void
     *
     */
    void audit(String ids, String auditState, String auditOpinion);

    void dataImportGp() throws Exception;

    YbFjOrg findOrg(String orgId);

    YbFjOrg findOrgByUser(String userId);

    IPage<OrgUserVo> getOrgUser(Page<OrgUserVo> page, OrgUserVo orgUserVo);

    IPage<OrgUserVo> getUserOrgList(Page<OrgUserVo> page, OrgUserVo orgUserVo);
}
