package com.ai.modules.ybChargeSearch.controller;

import com.ai.common.utils.ThreadSearchTaskUtils;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import com.ai.modules.ybChargeSearch.vo.OdsCheckorgListVo;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
* @Description: 收费明细查询任务表
* @Author: jeecg-boot
* @Date:   2022-10-09
* @Version: V1.0
*/
@Slf4j
@Api(tags="收费明细查询任务表")
@RestController
@RequestMapping("/apiYbChargeSearch/ybChargeSearchTask")
public class ApiYbChargeSearchTaskController extends JeecgController<YbChargeSearchTask, IYbChargeSearchTaskService> {

    @Autowired
    private IYbChargeSearchTaskService ybChargeSearchTaskService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * 通过bean查询
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "获取医疗机构数据")
    @ApiOperation(value="获取医疗机构数据", notes="获取医疗机构数据")
    @GetMapping(value = "/getOrgList")
    public Result<?> getOrgList(YbChargeSearchTask bean) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(bean.getDataSource());
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        List<Map<String,Object>> orgList = ybChargeSearchTaskService.getOrgList(bean,dbVO);
        return Result.ok(orgList);
    }

    /**
     * 获取医疗机构分页列表
     *
     * @param bean
     * @return
     */
    @ApiOperation(value="获取医疗机构分页列表", notes="获取医疗机构分页列表")
    @GetMapping(value = "/getOrgPageList")
    public Result<?> getOrgPageList(OdsCheckorgListVo bean,
                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = user.getDataSource();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(dataSource);
        Page<OdsCheckorgListVo> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        Page<OdsCheckorgListVo> result = ybChargeSearchTaskService.getOrgPageList(bean,dbVO,page);

        return Result.ok(result);
    }


    /**
     * 通过bean查询
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "获取科室数据")
    @ApiOperation(value="获取科室数据", notes="获取科室数据")
    @GetMapping(value = "/getDeptList")
    public Result<?> getDeptList(YbChargeSearchTask bean) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(bean.getDataSource());
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        List<Map<String,Object>> deptList = ybChargeSearchTaskService.getDeptList(bean,dbVO);
        return Result.ok(deptList);
    }

    /**
     * 通过bean查询
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "获取标签数据")
    @ApiOperation(value="获取标签数据", notes="获取标签数据")
    @GetMapping(value = "/getTagList")
    public Result<?> getTagList(YbChargeSearchTask bean) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(bean.getDataSource());
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        List<Map<String,Object>> orgList = ybChargeSearchTaskService.getTagList(bean,dbVO);
        return Result.ok(orgList);
    }

    /**
     * 通过bean查询
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "获取datamining_org_sum")
    @ApiOperation(value="获取datamining_org_sum", notes="获取datamining_org_sum")
    @GetMapping(value = "/getDataminingOrgSum")
    public Result<?> getDataminingOrgSum(YbChargeSearchTask bean) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(user.getDataSource());
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        List<Map<String,Object>> orgList = ybChargeSearchTaskService.getDataminingOrgSum(bean,dbVO);
        return Result.ok(orgList);
    }


    /**
     * 通过bean查询
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "获取任务类型获取结果数据")
    @ApiOperation(value="获取任务类型获取结果数据", notes="获取任务类型获取结果数据")
    @GetMapping(value = "/genTaskTypeResult")
    public Result<?> genTaskTypeResult(YbChargeSearchTask bean) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(bean.getDataSource());
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        List<Map<String,Object>> orgList = ybChargeSearchTaskService.genTaskTypeResultData(bean,dbVO);
        return Result.ok(orgList);
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-通过id删除")
    @ApiOperation(value="收费明细查询任务表-通过id删除", notes="收费明细查询任务表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        YbChargeSearchTask bean = ApiTokenUtil.getObj("/ybChargeSearch/ybChargeSearchTask/queryById", map, YbChargeSearchTask.class);
        if(bean==null){
            return Result.error("参数异常");
        }
        //删除项目地生成的文件
        String filePath = bean.getFileFullpath();
        if(StringUtils.isNotBlank(filePath)){
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
            }
        }
        ApiResponse apiResponse = ApiTokenUtil.deleteApi("/ybChargeSearch/ybChargeSearchTask/delete", map);
        if(apiResponse.isSuccess()){
            return Result.ok("删除成功!");
        } else {
            return Result.error(apiResponse.getMessage());
        }
    }

    // 查询导出任务线程池
    private static ThreadSearchTaskUtils.FixPool threadPool;

    static {
        threadPool = new ThreadSearchTaskUtils.FixPool(3);
    }

    /**
     * 添加
     *
     * @param ybChargeSearchTask
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-添加")
    @ApiOperation(value="收费明细查询任务表-添加", notes="收费明细查询任务表-添加")
    @PostMapping(value = "/saveTaskBatch")
    public Result<?> saveTaskBatch(@RequestBody YbChargeSearchTask ybChargeSearchTask) {
        SimpleDateFormat dateTimeSdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String taskType = ybChargeSearchTask.getTaskType();
            List<YbChargeSearchTask> dataList = new ArrayList<>();
            Map<String,List<YbChargeSearchTask>> map = new HashMap<>();
            List<YbChargeSearchTask> addBatchList = new ArrayList<>();
            if(YbChargeSearchConstant.DRUG_RULE_STATISTICS.equals(taskType)){
                List<YbChargeDrugRule> drugdata = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeDrugRule.class);
                List<YbChargeSearchTask> data = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeSearchTask.class);
                if(data.size()>0){
                    YbChargeSearchTask bean = data.get(0);
                    bean.setJsonStr(JSON.toJSONString(drugdata));
                    //药品名称
                    List<String> itemnames =drugdata.stream().filter(t->StringUtils.isNotBlank(t.getDrugName())).map(t->t.getDrugName()).collect(Collectors.toList());
                    bean.setItemname(itemnames.stream().map(String::valueOf).collect(Collectors.joining(",")));

                    //拆分医院
                    if(StringUtils.isNotBlank(bean.getOrgs())){
                        String[] orgsArr = bean.getOrgs().split(",");
                        String[] orgidsArr = new String[orgsArr.length];
                        if(StringUtils.isNotBlank(bean.getOrgids())){
                            orgidsArr = bean.getOrgids().split(",");
                        }
                        for(int i=0;i<orgsArr.length;i++){
                            YbChargeSearchTask bean2 = new YbChargeSearchTask();
                            BeanUtils.copyProperties(bean,bean2);
                            bean2.setOrgs(orgsArr[i]);
                            bean2.setOrgids(orgidsArr[i]);
                            addBatchList.add(bean2);
                        }
                    }else{
                        addBatchList.add(bean);
                    }
                }

            }else{
                List<YbChargeSearchTask> data = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeSearchTask.class);

                //解析多选医院
                List<String> itemnames = new ArrayList<>();
                for(YbChargeSearchTask bean:data){
                    if(StringUtils.isNotBlank(bean.getItemname())){
                        itemnames.add(bean.getItemname());
                    }
                    if(StringUtils.isNotBlank(bean.getItemname1())){
                        itemnames.add(bean.getItemname1());
                    }
                    //值处理  兼容旧sql代码
                    if("his".equals(bean.getItemType())){
                        bean.setHisItemName(bean.getItemname());
                        bean.setHisItemName1(bean.getItemname1());
                        bean.setItemname(null);
                        bean.setItemname1(null);
                    }
                    if(bean.getQtyNum()!=null&&bean.getQtyNum()>0){
                        if("charge_qty".equals(bean.getQtyType())){
                            bean.setChargeQty(bean.getQtyNum());
                        }else if("vistid_qty".equals(bean.getQtyType())){
                            bean.setVistidQty(bean.getQtyNum());
                        }else if("inhos_qty".equals(bean.getQtyType())){
                            bean.setInhosQty(bean.getQtyNum());
                        }
                    }



                    //拆分医院
                    if(StringUtils.isNotBlank(bean.getOrgs())){
                        String[] orgsArr = bean.getOrgs().split(",");
                        String[] orgidsArr = new String[orgsArr.length];
                        if(StringUtils.isNotBlank(bean.getOrgids())){
                            orgidsArr = bean.getOrgids().split(",");
                        }
                        for(int i=0;i<orgsArr.length;i++){
                            YbChargeSearchTask bean2 = new YbChargeSearchTask();
                            BeanUtils.copyProperties(bean,bean2);
                            bean2.setOrgs(orgsArr[i]);
                            bean2.setOrgids(orgidsArr[i]);
                            dataList.add(bean2);
                        }
                    }else{
                        dataList.add(bean);
                    }
                }
                for(YbChargeSearchTask bean:dataList){
                    List<YbChargeSearchTask> list = map.computeIfAbsent(bean.getOrgids(), k -> new ArrayList<YbChargeSearchTask>());
                    list.add(bean);
                }

                for (Map.Entry<String,List<YbChargeSearchTask>> entry:map.entrySet()){
                    List<YbChargeSearchTask> list = entry.getValue();
                    //相同机构合并
                    JSONArray jsonArray= JSONArray.parseArray(JSON.toJSONString(list));
                    for(int i=0;i<jsonArray.size();i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        if(json.get("chargedateStartdate")!=null){
                            json.put("chargedateStartdate", dateTimeSdf.format(json.get("chargedateStartdate")));
                        }
                        if(json.get("chargedateEnddate")!=null){
                            json.put("chargedateEnddate", dateTimeSdf.format(json.get("chargedateEnddate")));
                        }
                        if(json.get("leavedate")!=null){
                            json.put("leavedate", dateTimeSdf.format(json.get("leavedate")));
                        }
                        if(json.get("itemChargedate")!=null){
                            json.put("itemChargedate", dateTimeSdf.format(json.get("itemChargedate")));
                        }
                    }

                    YbChargeSearchTask bean = list.get(0);
                    bean.setOrgids(entry.getKey());
                    bean.setJsonStr(jsonArray.toString());
                    bean.setItemname(itemnames.stream().map(String::valueOf).collect(Collectors.joining(",")));
                    addBatchList.add(bean);
                }
            }

            for(YbChargeSearchTask bean:addBatchList){
                bean.setId(UUID.randomUUID().toString());
                bean.setCreateUser(user.getRealname());
                bean.setCreateUserId(user.getId());
                bean.setCreateTime(new Date());
                bean.setUpdateUser(user.getRealname());
                bean.setUpdateUserId(user.getId());
                bean.setUpdateTime(new Date());
                bean.setStatus(YbChargeSearchConstant.TASK_WAIT);
                bean.setTaskType(ybChargeSearchTask.getTaskType());
                String fileName = "收费项目明细";
                if(YbChargeSearchConstant.DRUG_RULE_STATISTICS.equals(taskType)){
                    fileName = "药品收费违规查询";
                }


                String timeStr =  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

                String etlSource = getEtlSourceName(bean);
                String orgname = StringUtils.isBlank(bean.getOrgs())?"所有医院":bean.getOrgs();
                bean.setFileName(orgname+"-"+etlSource+fileName+"-"+timeStr+".xlsx");

                String bizPath = "excelfiles";

                String relativePath = bizPath + File.separator + bean.getFileName();
                String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();
                bean.setFilePath(relativePath);
                bean.setFileFullpath(outPath);
            }


            DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(addBatchList.get(0).getDataSource());
            if(dbVO==null){
                Result.error("获取项目地数据源信息失败");
            }

            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeSearchTask/saveBatch", addBatchList);

           for(YbChargeSearchTask bean1:addBatchList){
                //开启线程 查询hive和导出excel
                threadPool.add(new Runnable() {
                    @Override
                    public void run() {
                        runCommonTask(bean1,dbVO);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok("添加成功！");
    }



    /**
     * 添加
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-添加")
    @ApiOperation(value="收费明细查询任务表-添加", notes="收费明细查询任务表-添加")
    @PostMapping(value = "/saveTaskByType")
    public Result<?> saveTaskByType(@RequestBody YbChargeSearchTask bean) {
        SimpleDateFormat dateTimeSdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfYyyyMMddhhmmss = new SimpleDateFormat("yyyyMMddhhmmss");
        try {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

            bean.setId(UUID.randomUUID().toString());
            bean.setCreateUser(user.getRealname());
            bean.setCreateUserId(user.getId());
            bean.setCreateTime(new Date());
            bean.setUpdateUser(user.getRealname());
            bean.setUpdateUserId(user.getId());
            bean.setUpdateTime(new Date());
            bean.setStatus(YbChargeSearchConstant.TASK_WAIT);


            String taskType = bean.getTaskType();
            if(StringUtils.isBlank(taskType)){
                return Result.error("操作失败：taskType参数错误");
            }

            String fileName = "";
            if(YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType)!=null){
                fileName = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getFileName();
            }

            List<YbChargeSearchTask> addBatchList = new ArrayList<>();
            String timeStr =  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String bizPath = "excelfiles";
            String etlSource = getEtlSourceName(bean);
            if(YbChargeSearchConstant.YEAR_STATISTICS.equals(taskType)
                    ||YbChargeSearchConstant.YEAR_USERATE_STATISTICS.equals(taskType)
                    ||YbChargeSearchConstant.YEAR_SURGERY_STATISTICS.equals(taskType)
                    ||YbChargeSearchConstant.YEAR_ORG_ONLINE_PATIENT_COUNT.equals(taskType)
                    ||YbChargeSearchConstant.FRAUD_PROJECT.equals(taskType)
                    ||YbChargeSearchConstant.FRAUD_HOSPITAL.equals(taskType)
                    ||YbChargeSearchConstant.FRAUD_PATIENT.equals(taskType)){
                //多个医院不拆分,多个医院用|分割
                // 文件名称  一个医院查询  文件名加上医院名称
                String orgname = "";
                if(StringUtils.isNotBlank(bean.getOrgs())){
                    String[] orgs_arr = bean.getOrgs().split(",");
                    if(orgs_arr.length==1){
                        orgname = bean.getOrgs()+"-";
                    }
                    bean.setOrgs(bean.getOrgs().replaceAll(",", "|"));
                    if(StringUtils.isNotBlank(bean.getOrgids())){
                        bean.setOrgids(bean.getOrgids().replaceAll(",", "|"));
                    }
                }else{
                    orgname = "所有医院-";
                }
                bean.setFileName(orgname+etlSource+fileName+"-"+timeStr+".xlsx");


                String relativePath = bizPath + File.separator + bean.getFileName();
                String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();
                bean.setFilePath(relativePath);
                bean.setFileFullpath(outPath);

                addBatchList.add(bean);
            }else{
                //解析医院名称,为多条数据
                if(StringUtils.isNotBlank(bean.getOrgs())) {
                    String[] orgsArr = bean.getOrgs().split(",");
                    String[] orgidsArr = new String[orgsArr.length];
                    if(StringUtils.isNotBlank(bean.getOrgids())){
                        orgidsArr = bean.getOrgids().split(",");
                    }
                    for(int i=0;i<orgsArr.length;i++){
                        YbChargeSearchTask bean2 = new YbChargeSearchTask();
                        BeanUtils.copyProperties(bean,bean2);
                        bean2.setId(UUID.randomUUID().toString());
                        bean2.setOrgs(orgsArr[i]);
                        bean2.setOrgids(orgidsArr[i]);

                        String orgname = StringUtils.isBlank(bean2.getOrgs())?"所有医院":bean2.getOrgs();
                        bean2.setFileName(orgname+"-"+etlSource+fileName+"-"+timeStr+".xlsx");

                        String relativePath = bizPath + File.separator + bean2.getFileName();
                        String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();
                        bean2.setFilePath(relativePath);
                        bean2.setFileFullpath(outPath);

                        addBatchList.add(bean2);
                    }
                }else{
                    String orgname = StringUtils.isBlank(bean.getOrgs())?"所有医院":bean.getOrgs();
                    bean.setFileName(orgname+"-"+etlSource+fileName+"-"+sdfYyyyMMddhhmmss.format(new Date())+".xlsx");

                    String relativePath = bizPath + File.separator + bean.getFileName();
                    String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();
                    bean.setFilePath(relativePath);
                    bean.setFileFullpath(outPath);

                    addBatchList.add(bean);
                }
            }

            DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(addBatchList.get(0).getDataSource());
            if(dbVO==null){
                Result.error("获取项目地数据源信息失败");
            }

            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeSearchTask/saveBatch", addBatchList);

            for(YbChargeSearchTask bean1:addBatchList){
                //开启线程 查询hive和导出excel
                threadPool.add(new Runnable() {
                    @Override
                    public void run() {
                        runCommonTask(bean1,dbVO);

                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok("添加成功！");
    }

    private String getEtlSourceName(YbChargeSearchTask bean) {
        String etlSource="";
        if(StringUtils.isNotBlank(bean.getEtlSource())){
            etlSource = "yb".equals(bean.getEtlSource())?"医保-":"his".equals(bean.getEtlSource())?"HIS-":bean.getEtlSource();
        }
        String dataStaticsLevel = StringUtils.isNotBlank(bean.getDataStaticsLevel())?bean.getDataStaticsLevel()+"-":"";
        return etlSource+dataStaticsLevel;
    }

    private void runCommonTask(YbChargeSearchTask bean1,DatasourceAndDatabaseVO dbVO) {
        try {
            bean1.setStatus(YbChargeSearchConstant.TASK_RUNING);
//            ybChargeSearchTaskService.updateById(bean1);
            ApiTokenUtil.putBodyApi("/ybChargeSearch/ybChargeSearchTask/edit", bean1);
            ybChargeSearchTaskService.run(bean1,dbVO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过id重跑
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务重跑-通过id重跑")
    @ApiOperation(value="任务重跑-通过id重跑", notes="任务重跑-通过id重跑")
    @GetMapping(value = "/runAgain")
    public Result<?> runAgain(@RequestParam(name="id",required=true) String id) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            YbChargeSearchTask bean = ApiTokenUtil.getObj("/ybChargeSearch/ybChargeSearchTask/deleteYbChargeSearchTaskByRunAgain", map, YbChargeSearchTask.class);

            bean.setUpdateUser(user.getRealname());
            bean.setUpdateUserId(user.getId());
            bean.setUpdateTime(new Date());
            String bizPath = "excelfiles";
            String relativePath = bizPath + File.separator + bean.getFileName();
            String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();
            bean.setFilePath(relativePath);
            bean.setFileFullpath(outPath);

            DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(bean.getDataSource());
            if(dbVO==null){
                Result.error("获取项目地数据源信息失败");
            }

            //开启线程 查询hive和导出excel
            threadPool.add(new Runnable() {
                @Override
                public void run() {
                    runCommonTask(bean,dbVO);

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok("操作成功！");
    }


    /**
     * 下载文件是否存在
     *
     * @param fileFullpath
     * @return
     */
    @AutoLog(value = "下载文件是否存在")
    @ApiOperation(value="下载文件是否存在", notes="下载文件是否存在")
    @GetMapping(value = "/fileExists")
    public Result<?> fileExists(@RequestParam(name="fileFullpath",required=true) String fileFullpath) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        try {
            File outFile = new File(fileFullpath);
            if (!outFile.exists()) {
                return Result.error("文件不存在");
            }
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @AutoLog(value = "下载文件")
    @ApiOperation(value="下载文件", notes="下载文件")
    @GetMapping(value = "/download")
    public void download(@RequestParam(name="fileFullpath",required=true) String fileFullpath,
                         @RequestParam(name="fileName",required=true) String fileName,
                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filePath = fileFullpath;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String filename = new String(fileFullpath.getBytes("UTF-8"),"iso-8859-1");
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;fileName=" + filename);
            inputStream = new BufferedInputStream(new FileInputStream(new File(filePath)));
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        } catch (Exception e) {
            log.info("文件下载失败" + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }



    /**
     * 清缓存
     *
     * @return
     */
    @AutoLog(value = "项目地和数据库配置信息清缓存")
    @ApiOperation(value="项目地和数据库配置信息清缓存", notes="项目地和数据库配置信息清缓存")
    @GetMapping(value = "/clearCacheDatasourceAndDatabase")
    public Result<?> clearCacheDatasourceAndDatabase() throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = user.getDataSource();
        ybChargeSearchTaskService.clearCacheDatasourceAndDatabase(dataSource);
        return Result.ok();
    }

    /**
     * 获取项目地和数据库配置信息
     *
     * @return
     */
    @AutoLog(value = "获取项目地和数据库配置信息")
    @ApiOperation(value="获取项目地和数据库配置信息", notes="获取项目地和数据库配置信息")
    @GetMapping(value = "/getDatasourceAndDatabase")
    public Result<?> getDatasourceAndDatabase(String dataSource) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(StringUtils.isBlank(dataSource)){
            dataSource = user.getDataSource();
        }
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(dataSource);
        return Result.ok(dbVO);
    }


    /**
     * 分页列表查询
     *
     * @param bean
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "查询任务结果-分页列表查询")
    @ApiOperation(value="查询任务结果-分页列表查询", notes="查询任务结果-分页列表查询")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(YbChargeSearchTask bean,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {

        Map<String, String> map = new HashMap<>();
        map.put("dataSource", bean.getDataSource());
        DatasourceAndDatabaseVO dbVO = ApiTokenUtil.getObj("/apiYbChargeSearch/ybChargeSearchTask/getDatasourceAndDatabase", map, DatasourceAndDatabaseVO.class);
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        if(StringUtils.isNotBlank(bean.getOrgs())){
            bean.setOrgs(bean.getOrgs().replaceAll(",", "|"));
            if(StringUtils.isNotBlank(bean.getOrgids())){
                bean.setOrgids(bean.getOrgids().replaceAll(",", "|"));
            }
        }
        Page<Map<String,Object>> page = new Page<>(pageNo, pageSize);
        try{
            IPage<Map<String,Object>> pageList =ybChargeSearchTaskService.querySearchResult(bean,dbVO,page);
            return Result.ok(pageList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }

    }

    /**
     * 创表
     *
     * @return
     */
    @AutoLog(value = "年度统计创建表")
    @ApiOperation(value="年度统计创建表", notes="年度统计创建表")
    @GetMapping(value = "/createYearSql")
    public Result<?> createYearSql() throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(user.getDataSource());
        if(dbVO==null){
            Result.error("获取项目地数据源信息失败");
        }
        //开启线程 查询hive和导出excel
        threadPool.add(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                ybChargeSearchTaskService.createYearSql(dbVO);
            }
        });

        return Result.ok();
    }

}
