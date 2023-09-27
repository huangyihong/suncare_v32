package com.ai.modules.ybFj.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.entity.YbFjChatOrg;
import com.ai.modules.ybFj.entity.YbFjOrg;
import com.ai.modules.ybFj.mapper.YbFjChatOrgMapper;
import com.ai.modules.ybFj.service.IYbFjChatOrgService;
import com.ai.modules.ybFj.vo.YbFjChatOrgVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 飞检项目聊天医疗机构
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Service
public class YbFjChatOrgServiceImpl extends ServiceImpl<YbFjChatOrgMapper, YbFjChatOrg> implements IYbFjChatOrgService {


    @Override
    public void saveChatOrg(String orgId) {
        YbFjChatOrg chatOrg = new YbFjChatOrg();
        chatOrg.setOrgId(orgId);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        chatOrg.setCreateTime(DateUtils.getDate());
        chatOrg.setCreateUser(user.getUsername());
        chatOrg.setCreateUsername(user.getRealname());
        chatOrg.setTopTime(DateUtils.getDate());
        this.saveOrUpdate(chatOrg);
    }

    @Override
    public IPage<YbFjChatOrgVo> queryYbFjChatOrgVo(IPage<YbFjChatOrgVo> page, YbFjOrg org) {
        QueryWrapper<YbFjChatOrgVo> wrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(org.getOrgName())) {
            wrapper.like("y.org_name", org.getOrgName());
        }
        return baseMapper.queryYbFjChatOrgVo(page, wrapper);
    }
}
