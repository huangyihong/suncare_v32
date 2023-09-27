package com.ai.modules.drg.constants;

import com.ai.modules.ybChargeSearch.constants.RuleRegexConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * @author : zhangly
 * @date : 2023/3/31 14:36
 */
public enum DrgScriptConstants {
    PREPARE("0_prepare", "0.预先准备数据"),
    MDCA("1_MDCA", "1.MDCA组"),
    MDCP("2_MDCP", "2.MDCP组"),
    MDCY("3_MDCY", "3.MDCY组"),
    MDCZ("4_MDCZ", "4.MDCZ组"),
    MDC_OTHER("5_MDC_OTHER", "5.MDC其他组"),
    MDC_REMOVE("5_MDC_REMOVE", "5.MDC删除"),
    ADRG_PREPARE("6.0_ADRG(prepare)", "6.ADRG组（预先准备数据）"),
    ADRG_0SURGERY1DIAG("6.1_ADRG(无手术1诊断)", "6.ADRG组（无手术1诊断）"),
    ADRG_0SURGERY2DIAG("6.2_ADRG(无手术2诊断)", "6.ADRG组（无手术2诊断）"),
    ADRG_1SURGERY1DIAG("7.1_ADRG(1手术1诊断)", "7.ADRG组（1手术1诊断）"),
    ADRG_1SURGERY2DIAG("7.2_ADRG(1手术2诊断)", "7.ADRG组（1手术2诊断）"),
    ADRG_2SURGERY1DIAG("7.3_ADRG(2手术1诊断)", "7.ADRG组（2手术1诊断）"),
    ADRG_3SURGERY1DIAG("7.4_ADRG(3手术1诊断)", "7.ADRG组（3手术1诊断）"),
    ADRG_0ROOM1DIAG("8.1_ADRG(无手术室手术1诊断)", "8.ADRG组（无手术室手术1诊断）"),
    ADRG_0ROOM2DIAG("8.2_ADRG(无手术室手术2诊断)", "8.ADRG组（无手术室手术2诊断）"),
    ADRG_QY("9_ADRG(QY)", "9.ADRG组（歧义）"),
    DRG_PREPARE("10.0_DRG(prepare)", "10.DRG组（预先准备数据）"),
    ADRG_NOT_MAPPING_DRG("10.1_DRG(ADRG未找到所属DRG)", "14.DRG组（ADRG未找到所属DRG）"),
    DRG_NOT_VALID_SEC_DIAG("10.2_DRG(不判断次要诊断)", "14.DRG组（不判断次要诊断）"),
    DRG_NOT_HAVE_SEC_DIAG("10.3_DRG(无次要诊断)", "10.DRG组（无次要诊断）"),
    DRG_VALID_MCC("10.4_DRG(有次要诊断有效MCC)", "11.DRG组（有次要诊断有效MCC）"),
    DRG_INVALID_MCC("10.5_DRG(有次要诊断无效MCC)", "12~13.DRG组（有次要诊断无效MCC）"),
    DRG_NOT_FOUND("11_DRG(未找到DRG分组)", "未找到DRG分组"),
    DRG_NOT_JOINED("12_DRG(未入组的诊断及手术)", "未入组的诊断及手术");

    private String code;
    private String name;

    private DrgScriptConstants(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DrgScriptConstants getByCode(String code){
        if(StringUtils.isBlank(code)) {
            return null;
        }
        for(DrgScriptConstants bean : values()){
            if (bean.getCode().equals(code)){
                return bean;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
