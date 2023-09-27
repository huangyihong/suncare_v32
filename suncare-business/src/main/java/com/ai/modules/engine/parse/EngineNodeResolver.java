/**
 * EngineUtil.java	  V1.0   2019年11月28日 下午3:34:22
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.parse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.beans.BeanUtils;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.formal.vo.CaseNode;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class EngineNodeResolver {
	private static EngineNodeResolver instance = new EngineNodeResolver();
	
	private IApiCaseService caseSV = SpringContextUtils.getApplicationContext().getBean(IApiCaseService.class);
	
	private EngineNodeResolver() {
		
	}
	
	public static EngineNodeResolver getInstance() {
		return instance;
	}

	/**
	 * 
	 * 功能描述：解析节点流程图json字符串（仅有一条流程节点路径）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年8月25日 下午5:36:32</p>
	 *
	 * @param flowJson
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
    public List<EngineNode> parseFlow(String flowJson, List<EngineNodeRule> ruleList) {
        List<EngineNode> result = new ArrayList<EngineNode>();
        Map<String, EngineNode> nodeMap = new HashMap<String, EngineNode>();
        Map<String, CaseNode> caseNodeMap = this.parseCaseNodeMap(flowJson);
        //规则按节点分组
        Map<String, List<EngineNodeRule>> nodeRuleMap = ruleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getNodeCode));        
        for (Map.Entry<String, CaseNode> entry : caseNodeMap.entrySet()) {
            EngineNode node = new EngineNode();
            CaseNode caseNode = entry.getValue();
            node.setNodeCode(caseNode.getKey());
            JSONObject json = caseNode.getData();
            String type = json.getString("type");
            node.setNodeType(type);
            node.setNodeName(json.getString("text"));
            if (caseNode.getParent() != null) {
                node.setPrevNodeCode(caseNode.getParent().getKey());
            }
            if(json.getString("param")!=null) {
            	node.setParamCode(json.getString("param"));
            }
            node.setPrevNodeCondition(caseNode.isFromYes() ? "YES" : "NO");
            nodeMap.put(node.getNodeCode(), node);
            //解析节点的查询条件            
            this.parseEngineNodeRule(node, nodeRuleMap);
            result.add(node);
        }
        //再次遍历节点，设置它的父级节点条件
        for (EngineNode node : result) {
            EngineNode parent = nodeMap.get(node.getPrevNodeCode());
            if(parent!=null) {
            	parent.setCondition(node.getPrevNodeCondition());
            }            
        }
        return result;
    }
    
    /**
	 * 
	 * 功能描述：解析节点流程图json字符串（遍历出每个节点的流程路径）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年8月25日 下午5:36:32</p>
	 *
	 * @param flowJson 流程图json字符串
	 * @param ruleList 节点查询条件
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
    public List<List<EngineNode>> parseEveryNodeFlow(String flowJson, List<EngineNodeRule> ruleList) {
    	Map<String, EngineNode> nodeMap = new HashMap<String, EngineNode>();
        Map<String, CaseNode> caseNodeMap = this.parseCaseNodeMap(flowJson);
        //规则按节点分组
        Map<String, List<EngineNodeRule>> nodeRuleMap = ruleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getNodeCode));
        Set<String> excludSet = new HashSet<String>();
        excludSet.add("start");
        excludSet.add("end");
        excludSet.add("blank");
        for (Map.Entry<String, CaseNode> entry : caseNodeMap.entrySet()) {
        	CaseNode caseNode = entry.getValue();
        	JSONObject json = caseNode.getData();
            String type = json.getString("type");
            if(excludSet.contains(type)) {
            	continue;
            }
            EngineNode node = new EngineNode();            
            node.setNodeCode(caseNode.getKey());            
            node.setNodeType(type);
            node.setNodeName(json.getString("text"));
            if (caseNode.getParent() != null) {
                node.setPrevNodeCode(caseNode.getParent().getKey());
            }
            if(json.getString("param")!=null) {
            	node.setParamCode(json.getString("param"));
            }
            node.setPrevNodeCondition(caseNode.isFromYes() ? "YES" : "NO");
            nodeMap.put(node.getNodeCode(), node);
            //解析节点的查询条件           
            this.parseEngineNodeRule(node, nodeRuleMap);
        }
        List<List<EngineNode>> result = new ArrayList<List<EngineNode>>();
        for (Map.Entry<String, EngineNode> entry : nodeMap.entrySet()) {
        	List<EngineNode> flow = new ArrayList<EngineNode>();
        	flow = this.recursionParseEngineNode(entry.getValue(), nodeMap, flow);
        	result.add(flow);
        }
        for(List<EngineNode> nodeList : result) {
        	nodeMap.clear();
        	nodeList.forEach(node -> {
                nodeMap.put(node.getNodeCode(), node);
        	});
        	//再次遍历节点，设置它的父级节点条件
        	nodeList.forEach(node -> {
                EngineNode parent = nodeMap.get(node.getPrevNodeCode());
                if(parent!=null) {
                	parent.setCondition(node.getPrevNodeCondition());
                }            
            });
        }
        return result;
    }
    
    /**
	 * 
	 * 功能描述：遍历出每个节点的流程路径
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年8月25日 下午5:36:32</p>
	 *
	 * @param flowJson 流程图json字符串
	 * @param ruleList 节点查询条件
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
    public List<List<EngineNode>> parseEveryNodeFlow(List<EngineNode> nodeList, List<EngineNodeRule> ruleList) {
    	Set<String> excludSet = new HashSet<String>();
        excludSet.add("start");
        excludSet.add("end");
        excludSet.add("blank");
    	Map<String, EngineNode> nodeMap = new HashMap<String, EngineNode>();
    	//规则按节点分组
        Map<String, List<EngineNodeRule>> nodeRuleMap = ruleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getNodeCode));
    	nodeList.forEach(node -> {
    		if(!excludSet.contains(node.getNodeType())) {
    			nodeMap.put(node.getNodeCode(), node);
        		//解析节点的查询条件           
                this.parseEngineNodeRule(node, nodeRuleMap);
            }    		
        });
        List<List<EngineNode>> result = new ArrayList<List<EngineNode>>();
        for (Map.Entry<String, EngineNode> entry : nodeMap.entrySet()) {
        	List<EngineNode> flow = new ArrayList<EngineNode>();
        	flow = this.recursionParseEngineNode(entry.getValue(), nodeMap, flow);
        	result.add(flow);
        }
        for(List<EngineNode> flow : result) {
        	nodeMap.clear();
        	flow.forEach(node -> {
                nodeMap.put(node.getNodeCode(), node);
        	});
        	//再次遍历节点，设置它的父级节点条件
        	flow.forEach(node -> {
                EngineNode parent = nodeMap.get(node.getPrevNodeCode());
                if(parent!=null) {
                	parent.setCondition(node.getPrevNodeCondition());
                }            
            });
        }
        return result;
    }
    
    /**
     * 
     * 功能描述：解析模型的节点流程
     *
     * @author  zhangly
     * <p>创建日期 ：2021年8月26日 上午11:18:34</p>
     *
     * @param nodeList
     * @param ruleList
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public List<List<EngineNode>> parseMultFlow(List<EngineNode> nodeList, List<EngineNodeRule> ruleList) {
    	Map<String, EngineNode> nodeMap = new HashMap<String, EngineNode>();
        //规则按节点分组
        Map<String, List<EngineNodeRule>> nodeRuleMap = ruleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getNodeCode));
        nodeList.forEach(node -> {
            if (!nodeMap.containsKey(node.getNodeCode())) {
                nodeMap.put(node.getNodeCode(), node);
            }
            this.parseEngineNodeRule(node, nodeRuleMap);
        });
        //再次遍历节点，设置它的父级节点以及流程
        List<List<EngineNode>> result = new ArrayList<List<EngineNode>>();
        List<EngineNode> temp = null;
        for (EngineNode node : nodeList) {
            if ("end".equalsIgnoreCase(node.getNodeType())) {
                temp = new ArrayList<>();
            }
            EngineNode parent = nodeMap.get(node.getPrevNodeCode());
            if(parent!=null) {
            	parent.setCondition(node.getPrevNodeCondition());
            }
            temp.add(node);
            if ("start".equalsIgnoreCase(node.getNodeType())) {
                //按序号排序
                List<EngineNode> flow = temp.stream().sorted(Comparator.comparing(EngineNode::getOrderNo)).collect(Collectors.toList());
                result.add(flow);
            }
        }
        return result;
    }
    
    /**
     * 
     * 功能描述：解析某个节点的查询条件
     *
     * @author  zhangly
     * <p>创建日期 ：2021年8月26日 上午11:20:06</p>
     *
     * @param node
     * @param nodeRuleMap
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private void parseEngineNodeRule(EngineNode node, Map<String, List<EngineNodeRule>> nodeRuleMap) {
    	List<EngineNodeRule> tempRuleList = nodeRuleMap.get(node.getNodeCode());
        if(node.getNodeType().contains("_v")) {
        	//模板节点
        	tempRuleList = caseSV.queryMedicalFormalFlowRuleByTmpl(node.getParamCode(), node.getNodeCode());
        }
        if (tempRuleList == null) {
            return;
        }
        //规则按组号分组
        Map<Integer, List<EngineNodeRule>> grpRuleMap = tempRuleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getGroupNo));
        grpRuleMap = grpRuleMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        if (grpRuleMap != null) {
            //按组号排序
            List<Map.Entry<Integer, List<EngineNodeRule>>> grpRuleList = new ArrayList<>(grpRuleMap.entrySet());
            grpRuleList.sort(Comparator.comparing(Map.Entry::getKey));
            int index = 0;
            List<EngineNodeRuleGrp> wheres = new ArrayList<EngineNodeRuleGrp>();
            for (Map.Entry<Integer, List<EngineNodeRule>> entry : grpRuleList) {
                List<EngineNodeRule> tempList = entry.getValue();
                //按组内规则排序
                tempList.sort(Comparator.comparing(EngineNodeRule::getOrderNo));
                EngineNodeRuleGrp grp = new EngineNodeRuleGrp();
                if (index == 0) {
                    //组间第一组的逻辑运算符设置为null
                    grp.setLogic(null);
                } else {
                    grp.setLogic(tempList.get(0).getLogic());
                }
                grp.setRuleList(tempList);
                //组内第一个条件的逻辑运算符设置为null
                tempList.get(0).setLogic(null);
                wheres.add(grp);
                index++;
            }
            node.setWheres(wheres);
        }
    }
    
    /**
     * 
     * 功能描述：递归查找某节点的整个流程
     *
     * @author  zhangly
     * <p>创建日期 ：2021年8月25日 下午5:52:14</p>
     *
     * @param node
     * @param nodeMap
     * @param flow
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private List<EngineNode> recursionParseEngineNode(EngineNode node, Map<String, EngineNode> nodeMap, List<EngineNode> flow) {
    	if(flow==null) {
    		flow = new ArrayList<EngineNode>();
    	}
    	if("diam".equals(node.getNodeType())) {
    		//条件节点，可能发生是、否两个分支，克隆对象存储集合中
    		EngineNode copy = new EngineNode();
    		BeanUtils.copyProperties(node, copy);
    		copy.setWheres(node.getWheres());
    		flow.add(copy);
    	} else {
    		flow.add(node);
    	}
    	if(StringUtils.isNotBlank(node.getPrevNodeCode())) {
    		EngineNode parent = nodeMap.get(node.getPrevNodeCode());
    		if(parent!=null) {
    			recursionParseEngineNode(parent, nodeMap, flow);
    		}
    	}
    	return flow;
    }
    
    private Map<String, CaseNode> parseCaseNodeMap(String flowJson) {
    	JSONObject jsonObject = (JSONObject) JSONObject.parse(flowJson);
        JSONArray nodeArray = jsonObject.getJSONArray("nodeDataArray");
        JSONArray linkArray = jsonObject.getJSONArray("linkDataArray");
        Map<String, CaseNode> caseNodeMap = new HashMap<>();
        for (int i = 0, len = nodeArray.size(); i < len; i++) {
            JSONObject json = nodeArray.getJSONObject(i);
            String key = json.getString("key");
            CaseNode node = new CaseNode();
            node.setKey(key);
            node.setData(json);
            caseNodeMap.put(key, node);
        }
        for (int i = 0, len = linkArray.size(); i < len; i++) {
            JSONObject json = linkArray.getJSONObject(i);
            String from = json.getString("from");
            String to = json.getString("to");
            CaseNode fromNode = caseNodeMap.get(from);
            CaseNode toNode = caseNodeMap.get(to);
            toNode.setParent(fromNode);
            fromNode.addChild(toNode);
            // 节点条件为否
            if (StringUtils.isNotEmpty(json.getString("visible"))
                    && "否".equals(json.get("text"))) {
                toNode.setFromYes(false);
            }
        }
        return caseNodeMap;
    }
}
