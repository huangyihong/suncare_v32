package com.ai.modules.drg.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class SrcYbClientVo {
    private String clientid;

    private String clientname;

    private String sex;

    private String birthday;

    private String idNo;

    private String insurancetype;

    private String clientphone;

    private String address;
}
