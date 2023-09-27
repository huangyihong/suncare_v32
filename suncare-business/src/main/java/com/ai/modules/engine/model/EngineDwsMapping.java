/**
 * EngineDwsMapping.java	  V1.0   2020年5月26日 下午5:34:16
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

/**
 * 
 * 功能描述：dws与dwb直接关联关系
 *
 * @author  zhangly
 * Date: 2020年5月26日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineDwsMapping {
	private String fromIndex;
	private String from;
	//dws表id生成模板
	private String toTemplate;
	/*//日数据
	private String to_did;
	//周数据
	private String to_wid;
	//月数据
	private String to_mid;
	//季数据
	private String to_qid;
	//年数据
	private String to_yid;*/
	
	public EngineDwsMapping(String fromIndex, String from, String toTemplate) {
		this.fromIndex = fromIndex;
		this.from = from;
		this.toTemplate = toTemplate;
	}
	
	/*public EngineDwsMapping build() {
		this.to_did = "TO_".concat(this.fromIndex).concat("_DID");
		this.to_wid = "TO_".concat(this.fromIndex).concat("_WID");
		this.to_mid = "TO_".concat(this.fromIndex).concat("_MID");
		this.to_qid = "TO_".concat(this.fromIndex).concat("_QID");
		this.to_yid = "TO_".concat(this.fromIndex).concat("_YID");
		return this;
	}*/
	
	public String getFromIndex() {
		return fromIndex;
	}
	public void setFromIndex(String fromIndex) {
		this.fromIndex = fromIndex;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}

	public String getToTemplate() {
		return toTemplate;
	}

	public void setToTemplate(String toTemplate) {
		this.toTemplate = toTemplate;
	}
}
