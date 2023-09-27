package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalDictItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/1/16 15
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalDictItemVO extends MedicalDictItem {
    private String groupCode;
    private String groupKind;
}
