package com.ai.modules.ybChargeSearch.mapper;

import com.ai.modules.ybChargeSearch.entity.YbChargeitemChecklist;
import com.ai.modules.ybChargeSearch.vo.YbChargeitemChecklistVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 收费明细风控检查内容
 * @Author: jeecg-boot
 * @Date:   2022-11-25
 * @Version: V1.0
 */
public interface YbChargeitemChecklistMapper extends BaseMapper<YbChargeitemChecklist> {
    @Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
    List<String> getRoleByUserName(@Param("username") String username);
    @Select("select id from sys_user where realname=#{realName}")
    List<String> getUserIdByRealName(String realName);

    @Select("select realname from sys_user ")
    List<String> getUserRealName();

    List<String> getUserBtnPermission(@Param("username") String username);

    IPage<YbChargeitemChecklist> selectPageVO(Page<YbChargeitemChecklist> page,@Param(Constants.WRAPPER) QueryWrapper<YbChargeitemChecklist> queryWrapper);
}
