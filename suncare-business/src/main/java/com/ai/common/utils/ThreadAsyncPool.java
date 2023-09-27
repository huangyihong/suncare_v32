package com.ai.common.utils;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskAsyncActionLog;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskAsyncActionLogService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2020/3/30 14
 * @Description:
 */
@Slf4j
@Component
public class ThreadAsyncPool extends ThreadUtils.FixPool {

//    @Resource
//    private WebSocket webSocket;

//    @Autowired
//    private IMedicalDictService medicalDictService;

//    @Autowired
//    private ITaskAsyncActionLogService taskAsyncActionLogService;

//    @Autowired
//    private ITaskProjectBatchService taskProjectBatchService;

    private static String PLATFORM_SOLR = "SOLR";
    private static String TYPE_IMPORT_FIRST = "IMPORT_FIRST";
    private static String TYPE_IMPORT_FIRST_GROUP = "IMPORT_FIRST_GROUP";
    private static String TYPE_IMPORT_SECOND = "IMPORT_SECOND";
    private static String TYPE_IMPORT_SECOND_GROUP = "IMPORT_SECOND_GROUP";
    private static String TYPE_JUDGE = "JUDGE_FIRST";
    private static String TYPE_JUDGE_SECOND= "JUDGE_SECOND";
    private static String TYPE_PUSH_FIRST = "PUSH_FIRST";
    private static String TYPE_PUSH_FIRST_NOT = "PUSH_FIRST_NOT";
    private static String TYPE_PUSH_SECOND = "PUSH_SECOND";
    private static String TYPE_CACHE_GROUP = "TYPE_CACHE_GROUP";
    // 根据不合规行为的违规说明模板更新违规说明
    private static String TYPE_UPDATE_BREAK_STATE = "UPDATE_BREAK_STATE";

    ThreadAsyncPool() {
        super(10);
    }

    /*public void addJudgeImport(int count, Supplier<Result> function){
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_JUDGE);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle(taskProjectBatch.getBatchName());
//        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        this.add(taskAsyncActionLog, function);
    }*/


    public String addActionGroupData(String actionId,String groupFields, String[] param, int count, Function<Consumer<Integer>, Result> function) {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_CACHE_GROUP);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        taskAsyncActionLog.setActionConfig(groupFields);
        String pathParam = "actionId=" + actionId;
        MedicalActionDict medicalActionDict = this.getActionById(actionId);
        taskAsyncActionLog.setActionTitle("不合规行为-" + medicalActionDict.getActionName());
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addActionBreakStateData(String actionId, String BreakStateTempl,  String[] param, int count, Function<Consumer<Integer>, Result> function) {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_UPDATE_BREAK_STATE);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        taskAsyncActionLog.setActionConfig(BreakStateTempl);
        String pathParam = "actionId=" + actionId;
        MedicalActionDict medicalActionDict = this.getActionById(actionId);
        taskAsyncActionLog.setActionTitle("不合规行为-" + medicalActionDict.getActionName());
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addFirstImport(MedicalUnreasonableActionVo actionVo, int count, Function<Consumer<Integer>, Result> function) throws Exception {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
//        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_IMPORT_FIRST);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());

        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addFirstGroupImport(MedicalUnreasonableActionVo actionVo, int count, Function<Consumer<Integer>, Result> function) throws Exception {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionType(TYPE_IMPORT_FIRST_GROUP);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());

        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addSecImport(MedicalUnreasonableActionVo actionVo, int count, Function<Consumer<Integer>, Result> function) throws Exception {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
//        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_IMPORT_SECOND);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());

        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addSecGroupImport(MedicalUnreasonableActionVo actionVo, int count, Function<Consumer<Integer>, Result> function) throws Exception {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
//        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_IMPORT_SECOND_GROUP);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());

        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addJudge(MedicalUnreasonableActionVo actionVo, String[] param, int count, Function<Consumer<Integer>, Result> function) {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_JUDGE);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addSecJudge(MedicalUnreasonableActionVo actionVo, String[] param, int count, Function<Consumer<Integer>, Result> function) {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_JUDGE_SECOND);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());
        taskAsyncActionLog.setActionPathParam(pathParam);
        taskAsyncActionLog.setRecordCount(count);

        return this.add(taskAsyncActionLog, function);
    }

    public String addPush1st(MedicalUnreasonableActionVo actionVo, boolean isPush, String[] param, int count, Function<Consumer<Integer>, Result> function) {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(isPush ? TYPE_PUSH_FIRST : TYPE_PUSH_FIRST_NOT);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        String pathParam = "batchId=" + actionVo.getBatchId();
        if (actionVo.getCaseId() != null) {
            pathParam += "&caseId=" + actionVo.getCaseId();
        }
        if (actionVo.getBusiType() != null) {
            pathParam += "&ruleType=" + actionVo.getBusiType();
        }
        taskAsyncActionLog.setActionPathParam(pathParam);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());
        taskAsyncActionLog.setRecordCount(count);
        return this.add(taskAsyncActionLog, function);
    }

    public String addPush2ed(MedicalUnreasonableActionVo actionVo, String[] param, int count, Function<Consumer<Integer>, Result> function) {
        TaskAsyncActionLog taskAsyncActionLog = new TaskAsyncActionLog();
        taskAsyncActionLog.setActionPlatform(PLATFORM_SOLR);
        taskAsyncActionLog.setActionParam(JSONArray.toJSONString(param));
        taskAsyncActionLog.setActionType(TYPE_PUSH_SECOND);
        taskAsyncActionLog.setActionObject(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        String pathParam = "batchId=" + actionVo.getBatchId();
        taskAsyncActionLog.setActionPathParam(pathParam);
        TaskProjectBatch taskProjectBatch = this.getBatchById(actionVo.getBatchId());
        taskAsyncActionLog.setActionTitle("批次-" + taskProjectBatch.getBatchName());
        taskAsyncActionLog.setRecordCount(count);
        return this.add(taskAsyncActionLog, function);
    }

    public String add(TaskAsyncActionLog taskAsyncActionLog, Function<Consumer<Integer>, Result> function) {

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = user.getId();

        taskAsyncActionLog.setDataSource(user.getDataSource());
        taskAsyncActionLog.setLogId(IdUtils.uuid());
        taskAsyncActionLog.setStatus(MedicalConstant.RUN_STATE_WAIT);
        this.saveLog(taskAsyncActionLog);

        String ds = SolrUtil.getLoginUserDatasource();
        executor.submit(new EngineFunctionRunnable(ds, user.getToken(), () -> {
            Result result = null;
            try {
                taskAsyncActionLog.setStatus(MedicalConstant.RUN_STATE_RUNNING);
                taskAsyncActionLog.setStartTime(new Date());
                int total = taskAsyncActionLog.getRecordCount() == -1? 999999999: taskAsyncActionLog.getRecordCount();
                taskAsyncActionLog.setLeftCount(total);
                this.updateLog(taskAsyncActionLog);

                Consumer<Integer> updateProcess = (addCount) -> {
                    taskAsyncActionLog.setLeftCount(taskAsyncActionLog.getLeftCount() - addCount);
                    this.updateLog(taskAsyncActionLog);
                };
                result = function.apply(updateProcess);
                if (!result.isSuccess()) {
                    throw new Exception(result.getMessage());
                }
                if (result.getResult() != null && result.getResult() instanceof Integer) {
                    taskAsyncActionLog.setRecordCount((Integer) result.getResult());
                }
                taskAsyncActionLog.setMsg(String.valueOf(result.getResult()));
                taskAsyncActionLog.setStatus(MedicalConstant.RUN_STATE_NORMAL);
            } catch (Exception e) {
                log.error("", e);
                taskAsyncActionLog.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
                String msg = e.getMessage();
                if (msg.length() > 1000) {
                    msg = msg.substring(0, 1000);
                }
                taskAsyncActionLog.setMsg(msg);
            } finally {
                taskAsyncActionLog.setEndTime(new Date());
                this.updateLog(taskAsyncActionLog);
                String actionTypeText = this.queryMedicalDictTextByKey("ASYNC_ACTION_TYPE", taskAsyncActionLog.getActionType());
/*
                sysBaseAPI.sendSysAnnouncement("admin", taskAsyncActionLog.getCreateUser()
                        , "异步操作通知", actionTypeText + "完成");*/
                JSONObject obj = new JSONObject();
                obj.put("cmd", "async");
                obj.put("userId", taskAsyncActionLog.getCreateUser());
                obj.put("msgId", taskAsyncActionLog.getLogId());
                obj.put("msgTxt", taskAsyncActionLog.getActionTitle() + "\n" +
                        actionTypeText
                        + (MedicalConstant.RUN_STATE_ABNORMAL.equals(taskAsyncActionLog.getStatus()) ? "异常" : "完成"));
                this.sendWebsocketMsg(userId, obj.toJSONString());

            }
//            return result;
        }));
        return taskAsyncActionLog.getLogId();
    }


    private void saveLog(TaskAsyncActionLog taskAsyncActionLog) {
        ApiTokenUtil.postBodyApi("/task/taskAsyncActionLog/add", taskAsyncActionLog);
    }

    private void updateLog(TaskAsyncActionLog taskAsyncActionLog) {
        ApiTokenUtil.putBodyApi("/task/taskAsyncActionLog/edit", taskAsyncActionLog);
    }

    private String queryMedicalDictTextByKey(String type, String key) {
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        map.put("code", key);
        return ApiTokenUtil.getApi("/config/medicalDict/common/queryValByTypeCode", map).getResult().toString();
    }

    private TaskProjectBatch getBatchById(String batchId) {
        Map<String, String> map = new HashMap<>();
        map.put("id", batchId);
        return ApiTokenUtil.getObj("/task/taskProjectBatch/queryById", map, TaskProjectBatch.class);
    }

    private TaskProject getProjectById(String projectId) {
        Map<String, String> map = new HashMap<>();
        map.put("id", projectId);
        return ApiTokenUtil.getObj("/task/taskProject/queryById", map, TaskProject.class);
    }

    private MedicalActionDict getActionById(String actionId) {
        Map<String, String> map = new HashMap<>();
        map.put("actionId", actionId);
        return ApiTokenUtil.getObj("/config/medicalActionDict/queryByActionId", map, MedicalActionDict.class);
    }

    private void sendWebsocketMsg(String userId, String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("msg", msg);
        ApiTokenUtil.postApi("/api/websocket/sendUser", map);
    }

}
