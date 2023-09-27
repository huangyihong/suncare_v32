package com.ai.modules.ybFj.mapper;

import com.ai.modules.ybFj.entity.YbFjChatOrg;
import com.ai.modules.ybFj.vo.YbFjChatOrgVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 飞检项目聊天医疗机构
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
public interface YbFjChatOrgMapper extends BaseMapper<YbFjChatOrg> {

    IPage<YbFjChatOrgVo> queryYbFjChatOrgVo(IPage<YbFjChatOrgVo> page, @Param(Constants.WRAPPER) Wrapper<YbFjChatOrgVo> wrapper);
}
