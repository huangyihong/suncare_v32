package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.entity.YbChargeitemChecklist;
import com.ai.modules.ybChargeSearch.vo.YbChargeitemChecklistVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 收费明细风控检查内容
 * @Author: jeecg-boot
 * @Date:   2022-11-25
 * @Version: V1.0
 */
public interface IYbChargeitemChecklistService extends IService<YbChargeitemChecklist> {
    List<String> getRoleByUserName(String username);

    List<String> getUserIdByRealName(String realName);

    List<String> getUserRealName();

    List<String> getUserBtnPermission(String username);

    IPage<?> getKeyWordsImportList(YbChargeitemChecklistVo ybChargeitemChecklistVo, Page<YbChargeitemChecklistVo> page) throws Exception;

    IPage<YbChargeitemChecklist> getPage(Page<YbChargeitemChecklist> page, QueryWrapper<YbChargeitemChecklist> queryWrapper);
}
