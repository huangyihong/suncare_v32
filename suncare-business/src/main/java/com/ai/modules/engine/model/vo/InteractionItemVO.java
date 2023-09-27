/**
 * MutextItemVO.java	  V1.0   2020年10月19日 下午3:37:09
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 
 * 功能描述：相互作用的收费项
 *
 * @author  zhangly
 * Date: 2020年10月19日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Data
public class InteractionItemVO {
	//项目编码
	private String itemcode;
	//项目名称
	private String itemname;
	//数量
	private BigDecimal amount;
	//费用
	private BigDecimal fee;
	//可纳入报销费用
	private BigDecimal fundConver;
}
