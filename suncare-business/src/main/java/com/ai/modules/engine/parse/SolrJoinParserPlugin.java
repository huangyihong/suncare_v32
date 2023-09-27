/**
 * SolrJoinParser.java	  V1.0   2021年4月12日 上午11:37:19
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;

public class SolrJoinParserPlugin {
	private String fromIndex;
	private String from;
	private String to;
	private String method = "crossCollection";
	private boolean routed = false;
	//排除
	private static final Set<String> excludeSet = new HashSet<String>();
	static {
		excludeSet.add(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
		excludeSet.add(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
		excludeSet.add(EngineUtil.MEDICAL_TRAIL_ACTION);
		excludeSet.add(EngineUtil.MEDICAL_TRAIL_DRUG_ACTION);
	}
	
	public SolrJoinParserPlugin(String fromIndex, String from, String to) {
		this.fromIndex = fromIndex;
		this.from = from;
		this.to = to;
		if("VISITID".equals(from) && !excludeSet.contains(fromIndex)) {
			this.routed = true;
		}
	}
	
	public SolrJoinParserPlugin(String fromIndex, String from, String to, boolean routed) {
		this(fromIndex, from, to);
		this.routed = routed;
	}
	
	public static SolrJoinParserPlugin build(EngineMapping mapping) {
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(mapping.getFromIndex(), mapping.getFrom(), mapping.getTo());
		return plugin;
	}
	
	public String parse() {
		boolean cluster = SolrUtil.isCluster();
		if(cluster) {
			//集群模式
			String where = "{!join method=%s routed=%s fromIndex=%s from=%s to=%s}";
			where = String.format(where, method, routed, fromIndex, from, to);
			if(!routed) {
				where = StringUtils.replace(where, " routed=false", "");
			}
			return where;
		} else {
			//单节点模式
			String where = "{!join fromIndex=%s from=%s to=%s}";
			where = String.format(where, fromIndex, from, to);
			return where;
		}
	}
	
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
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public boolean isRouted() {
		return routed;
	}
	public void setRouted(boolean routed) {
		this.routed = routed;
	}
	
	public static void main(String[] args) throws Exception {
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_ORDER", "VISITID", "VISITID");
		//plugin.setRouted(true);
		System.out.println(plugin.parse());
	}
}
