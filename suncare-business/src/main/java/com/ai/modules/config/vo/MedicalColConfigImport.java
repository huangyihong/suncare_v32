package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalColConfig;
import lombok.Data;

/**
 * @Auther: zhangpeng
 * @Date: 2019/12/4 09
 * @Description:
 */
@Data
public class MedicalColConfigImport extends MedicalColConfig {
    private String isWhereColStr;
    private String whereInputTypeStr;
    private String colOrderStr;
    private String isDisplayColStr;
}
