package com.ai.modules.task.controller;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.task.entity.TaskAsyncActionLog;
import com.ai.modules.task.service.ITaskAsyncActionLogService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
* @Description: 异步操作日志
* @Author: jeecg-boot
* @Date:   2020-12-07
* @Version: V1.0
*/
@Slf4j
@Api(tags="异步操作日志")
@RestController
@RequestMapping("/apiTask/taskAsyncActionLog")
public class ApiTaskAsyncActionLogController extends JeecgController<TaskAsyncActionLog, ITaskAsyncActionLogService> {


   /**
    * 分页列表查询
    *
    * @param taskAsyncActionLog
    * @param pageNo
    * @param pageSize
    * @param req
    * @return
    */
   @AutoLog(value = "异步操作日志-分页列表查询")
   @ApiOperation(value="异步操作日志-分页列表查询", notes="异步操作日志-分页列表查询")
   @GetMapping(value = "/list")
   public Result<?> queryPageList(TaskAsyncActionLog taskAsyncActionLog,
                                  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                  HttpServletRequest req) throws Exception {
       Map<String, String> map = new HashMap<>();
       for(Map.Entry<String, String[]> entry: req.getParameterMap().entrySet()){
           String[] values = entry.getValue();
           if(values != null && values.length > 0){
               map.put(entry.getKey(), entry.getValue()[0]);
           }
       }

       IPage<TaskAsyncActionLog> pageList = ApiTokenUtil.Page("/task/taskAsyncActionLog/list", map, TaskAsyncActionLog.class);

       Map<String, List<TaskAsyncActionLog>> collectionMap = new HashMap<>();
       for(TaskAsyncActionLog bean: pageList.getRecords()){
           if(bean.getLeftCount() != null){
               continue;
           }

           if(MedicalConstant.RUN_STATE_RUNNING.equals(bean.getStatus()) && bean.getActionParam() != null){
               List<TaskAsyncActionLog> beans = collectionMap.computeIfAbsent(bean.getActionObject(), k -> new ArrayList<>());
               beans.add(bean);
           } else if(MedicalConstant.RUN_STATE_NORMAL.equals(bean.getStatus())){
               bean.setLeftCount(0);
           } else {
               bean.setLeftCount(bean.getRecordCount());

           }

       }

       for(Map.Entry<String, List<TaskAsyncActionLog>> entry: collectionMap.entrySet()){
           JSONObject facetJson = new JSONObject();
           for(TaskAsyncActionLog bean: entry.getValue()){
               if(StringUtils.isBlank(bean.getActionParam())){
                   continue;
               }
               List<String> param = JSONArray.parseArray(bean.getActionParam()).toJavaList(String.class);
               String q = param.stream().map(r1 -> r1.startsWith("{!")
                       ?"_query_:\"" + r1.replaceAll("\"", "\\\"") + "\""
                       :r1).collect(Collectors.joining(" AND "));
               JSONObject json = new JSONObject();
               json.put("query", q);
               facetJson.put(bean.getLogId(), json);
           }
           if(facetJson.size() == 0){
               continue;
           }
           JSONObject resultJson = SolrUtil.jsonFacet(entry.getKey(),new String[0], facetJson.toJSONString());
           for(TaskAsyncActionLog bean: entry.getValue()){
               bean.setLeftCount(resultJson.getJSONObject(bean.getLogId()).getIntValue("count"));
           }
       }



       return Result.ok(pageList);
   }

}
