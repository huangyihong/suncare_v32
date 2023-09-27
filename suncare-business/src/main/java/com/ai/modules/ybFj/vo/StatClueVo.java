package com.ai.modules.ybFj.vo;

import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/3/17 17:02
 */
@Data
public class StatClueVo {
    private StatProjectClueVo submit;
    private StatOnsiteClueVo onsite;

    public StatClueVo(StatProjectClueVo submit, StatOnsiteClueVo onsite) {
        this.submit = submit;
        this.onsite = onsite;
    }
}
