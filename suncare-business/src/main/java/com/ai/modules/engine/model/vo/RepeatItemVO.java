/**
 * RepeatItemVO.java	  V1.0   2021年6月11日 下午2:19:48
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import lombok.Data;

@Data
public class RepeatItemVO extends ChargedayVO {
	//重复用药白名单药品编码
	private String mainCode;
	//重复用药白名单药品名称
	private String mainName;
}
