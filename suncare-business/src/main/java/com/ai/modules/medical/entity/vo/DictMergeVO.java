/**
 * DictMergeVO.java	  V1.0   2021年7月14日 上午9:43:48
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.entity.vo;

import lombok.Data;

@Data
public class DictMergeVO {
	private String itemid;
	private String itemname;
	//类型{case:模型，tmpl:模型模板}
	private String itemtype;
}
