/**
 * EngineLimitScopeEnum.java	  V1.0   2020年7月22日 下午5:44:01
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model;

public enum EngineLimitScopeEnum {
	CODE_01("01", "年龄", "违反限定年龄"),
	CODE_02("02", "性别", "违反限定性别"),
	CODE_03("03", "就医方式", "违反限定就诊类型"),
	CODE_04("04", "参保类别", "违反限定参保类型"),
	CODE_05("05", "医院级别", "违反限定医院级别"),
	CODE_06("06", "科室", "违反限定科室"),
	//CODE_07("07", "疗程用药剂量"),
	//CODE_08("08", "年度用药剂量"),
	CODE_09("09", "治疗项目", "无必须治疗项目"),
	CODE_10("10", "治疗方式", "违反限定治疗方式"),
	CODE_11("11", "重复用药", "违反限定重复用药"),
	CODE_12("12", "二线用药", "违反限定二线用药"),
	CODE_13("13", "适应症", "违反限定适应症"),
	CODE_14("14", "治疗用药", "无必须治疗用药"),
	/*CODE_16("16", "门诊统筹"),
	CODE_17("17", "用药量限制"),
	CODE_18("18", "用药量单位"),
	CODE_19("19", "用药时限"),
	CODE_20("20", "时间单位"),
	CODE_21("21", "最大持续使用时间"),
	CODE_22("22", "最大持续时间单位"),
	CODE_23("23", "规则来源"),*/
	CODE_24("24", "合用不予支付药品", "合用不予支付药品"),
	CODE_25("25", "卫生机构类别", "违反限定卫生机构类别"),
	//CODE_26("26", "医嘱"),
	CODE_27("27", "合规项目组", "相关项目缺失"),
	CODE_28("28", "互斥项目组", "重复治疗"),
	CODE_29("29", "一日互斥项目组", "一日内重复治疗"),
	//CODE_30("30", "政策依据"),
	CODE_31("31", "禁忌症", "违反限定禁忌症"),
	CODE_32("32", "不能报销", "医保基金不予支付"),
	CODE_33("33", "给药途径", "违反限定给药途径"),
	CODE_34("34", "相互作用", "合用存在相互作用"),
	CODE_35("35", "医疗机构", "违反限定医疗机构"),
	CODE_36("36", "不能收费", "非收费项目"),
	CODE_37("37", "支付时长", "超支付时长"),
	CODE_38("38", "数量/频次", "超数量/频次"),
	CODE_39("39", "频率疾病组", "违反限定频率疾病组"),
	CODE_40("40", "检验结果", "检验结果不符合要求");

	private String code;
	private String name;
	private String tips;

	private EngineLimitScopeEnum(String code, String name, String tips) {
        this.code = code;
        this.name = name;
        this.tips = tips;
    }

	public static EngineLimitScopeEnum enumValueOf(String code) {
		EngineLimitScopeEnum[] values = EngineLimitScopeEnum.values();
		EngineLimitScopeEnum v = null;
        for (int i = 0; i < values.length; i++) {
            if (code.equals(values[i].getCode())) {
                v = values[i];
                break;
            }
        }
        return v;
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

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}
}
