package com.ai.modules.review.dto;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/3/23 15
 * @Description:
 */
@Data
public class DynamicLinkProp {
    List<Map.Entry<String, Set<String>>> multiLinkMap;
    List<Map.Entry<String, Set<String>>> singleLinkMap;
    Set<String> linkFieldSet;

}
