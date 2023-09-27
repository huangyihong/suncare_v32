package com.ai.modules.review.service;

import com.ai.modules.review.dto.DynamicFieldConfig;
import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IMedicalUnreasonableActionService extends IService<MedicalUnreasonableAction> {

    /**
     * 根据批次统计初审、复审等数量
     * @param batchIds
     * @return
     */
    JSONObject facetBatchCount(String batchIds);

    /**
     * 根据条件分组统计不合规行为Action数量
     * @param queryWrapper
     * @return
     */
    List<Map<String, Object>> facetActionData(QueryWrapper<MedicalUnreasonableAction> queryWrapper);

    /**
     * 风控结果表分页查询
     * @param page
     * @param queryWrapper
     * @param joinSql
     * @param whereSql
     * @return
     */
    public IPage<MedicalUnreasonableAction> selectPageVO(
            Page<MedicalUnreasonableAction> page, QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String fields,String orderbySql);

    /**
     * 风控结果表分页查询
     * @param queryWrapper
     * @param joinSql
     * @param whereSql
     * @return
     */
    public int selectCount(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String fields);

    /**
     * 风控结果表分页查询
     * @param page
     * @param queryWrapper
     * @param joinSql
     * @param whereSql
     * @return
     */
    public IPage<Map<String,Object>> selectMapPageVO(
            Page<Map<String,Object>> page, QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String fields,String orderbySql,String linkFields);


    /**
     * 风控结果表查询
     * @param queryWrapper
     * @param joinSql
     * @param whereSql
     * @return
     */
    public List<Map<String,Object>> selectMapVO(
            QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String fields,String orderbySql,String linkFields);


    List<String> getSearchSqls(String dynamicSearch, QueryWrapper<MedicalUnreasonableAction> queryWrapper,String dataSource,Map<String, Set<String>> tabFieldMap) throws ParseException;

    /**
     * 查询风控结果表关联其他表字段内容
     * @param tabFieldMap
     * @param pageList
     * @return
     * @throws Exception
     */
    IPage<Map<String,Object>> pageDynamicResult(Map<String, Set<String>> tabFieldMap,Set<String> resultFieldSet, IPage<Map<String,Object>> pageList) throws Exception;

    // 根据关联信息反查表
    void addFieldFromOther(List<Map<String,Object>> list,
                           DynamicLinkProp dynamicLinkProp) throws Exception;


    /**
     * 根据查询条件统计内容
     * @param queryWrapper
     * @return
     */
    List<Map<String, Object>> facetFields(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String selectFields,String factFields );

    /**
     * 根据查询条件查询分组分页结果
     * @param queryWrapper
     * @return
     */
    IPage<Map<String,Object>> facetFieldsPage(Page<Map<String,Object>> page,QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String selectFields,String factFields,String groupByFields,String orderbySql);

    /**
     * 根据查询条件查询分组结果条数
     * @param queryWrapper
     * @param joinSql
     * @param whereSql
     * @param selectFields
     * @param factFields
     * @param groupByFields
     * @return
     */
    public int facetFieldsCount(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String selectFields,String factFields,String groupByFields);
    /**
     * 根据查询条件不合规行为统计
     * @param queryWrapper
     * @return
     */
    List<Map<String, Object>> facetActionData(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String selectFields );

    /**
     * 字段结果转化
     * @param list
     */
    void resultMapping(List<Map<String, Object>> list,Set<String> resultFieldSet);

    /**
     * 初审
     * @param queryWrapper
     * @param joinSql
     * @param whereSql
     * @param reviewObj
     */
    void updateReviewStatus(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql,String fields, ReviewInfoDTO reviewObj);

    /**
     * 风控结果表导出
     * @param resultList
     * @param fieldConfig
     * @param isStep2
     * @param os
     * @throws Exception
     */
    void dynamicResultExport(List<Map<String,Object>> resultList, DynamicFieldConfig fieldConfig, boolean isStep2, OutputStream  os) throws Exception;

    /**
     * 风控结果表分组导出
     * @param list
     * @param fields
     * @param fieldTitles
     * @param os
     * @throws Exception
     */
    void dynamicGroupExport(List<Map<String, Object>> list, String[] fields, String[] fieldTitles,Map<String, String> linkChild,List<String> groupByList,Map<String,String> fieldMapping, OutputStream os) throws Exception;

    String importReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception;

    String importReviewStatusSec(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception;

    String importGroupReviewStatus(MultipartFile file, MedicalUnreasonableAction searchObj, String dynamicSearch, HttpServletRequest req) throws Exception;

    String importGroupReviewStatusSec(MultipartFile file, MedicalUnreasonableAction searchObj, String dynamicSearch, HttpServletRequest req) throws Exception;


}
