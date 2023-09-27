package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.entity.YbFjChatOrg;
import com.ai.modules.ybFj.entity.YbFjOrg;
import com.ai.modules.ybFj.vo.YbFjChatOrgVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 飞检项目聊天医疗机构
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
public interface IYbFjChatOrgService extends IService<YbFjChatOrg> {

    void saveChatOrg(String orgId);

    IPage<YbFjChatOrgVo> queryYbFjChatOrgVo(IPage<YbFjChatOrgVo> page, YbFjOrg org);
}
