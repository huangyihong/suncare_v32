package com.ai.modules.drg.handle.model;

import com.ai.modules.drg.entity.DrgRuleLimites;
import lombok.Data;

import java.util.List;

/**
 * @author : zhangly
 * @date : 2023/4/26 16:17
 */
@Data
public class DrgRuleModel {
    /**所属目录*/
    private String catalogType;
    /**目录编码*/
    private String catalogCode;
    /**规则*/
    private List<DrgRuleLimites> ruleLimitesList;
}
