package com.ai.modules.ybChargeSearch.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.ExcelXUtils;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.engine.util.AliyunApiUtil;
import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.service.ISysDatasourceService;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTaskDownload;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskDownloadService;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybChargeSearch.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.websocket.WebSocket;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 收费明细查询任务表
 * @Author: jeecg-boot
 * @Date: 2022-10-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "收费明细查询任务表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeSearchTask")
public class YbChargeSearchTaskController extends JeecgController<YbChargeSearchTask, IYbChargeSearchTaskService> {
    @Autowired
    private IYbChargeSearchTaskService ybChargeSearchTaskService;

    @Autowired
    private IYbChargeSearchTaskDownloadService ybChargeSearchTaskDownloadService;

    @Autowired
    private IMedicalDictService medicalDictService;

    @Autowired
    private ISysDatasourceService sysDatasourceService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${jeecg.sms.defaultCode}")
    private String defaultCode;

    @Autowired
    private WebSocket webSocket;


    @PostConstruct
    public void startRun()  {
        //生成环境  将正在执行或者等待状态设置为 02失败
        if(SpringContextUtils.isProd()){
            QueryWrapper<YbChargeSearchTask> queryWrapper = new QueryWrapper();
            queryWrapper.select("id");
            queryWrapper.ne("status",YbChargeSearchConstant.TASK_SUCCESS);//已完成
            queryWrapper.ne("status",YbChargeSearchConstant.TASK_FAIL);//失败
            List<YbChargeSearchTask> list = ybChargeSearchTaskService.list(queryWrapper);
            if(list.size()>0){
                list.stream().forEach(bean -> {
                    bean.setStatus(YbChargeSearchConstant.TASK_FAIL);
                });
                ybChargeSearchTaskService.updateBatchById(list);
            }

        }
    }

    /**
     * 分页列表查询
     *
     * @param ybChargeSearchTask
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-分页列表查询")
    @ApiOperation(value = "收费明细查询任务表-分页列表查询", notes = "收费明细查询任务表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YbChargeSearchTask ybChargeSearchTask,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<YbChargeSearchTask> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeSearchTask, req.getParameterMap());
        Page<YbChargeSearchTask> page = new Page<YbChargeSearchTask>(pageNo, pageSize);
        IPage<YbChargeSearchTask> pageList = ybChargeSearchTaskService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

//    /**
//     * 数据复制新增
//     *
//     * @param ybChargeSearchTask
//     * @return
//     */
//    @AutoLog(value = "收费明细查询任务表-数据复制新增")
//    @ApiOperation(value = "收费明细查询任务表-数据复制新增", notes = "收费明细查询任务表-数据复制新增")
//    @PostMapping(value = "/copyAdd")
//    public Result<?> copyAdd(@RequestBody YbChargeSearchTask ybChargeSearchTask,HttpServletRequest req) {
//        QueryWrapper<YbChargeSearchTask> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeSearchTask, req.getParameterMap());
//        List<YbChargeSearchTask> list = ybChargeSearchTaskService.list(queryWrapper);
//        list.stream().forEach(t -> {
//            t.setId("");
//            t.setDataSource("shanxi__gp");
//            String outPath = "/home/web/suncare_v4/upload/"+ "excelfiles/" +t.getFileName();
//            t.setFileFullpath(outPath);
//        });
//        ybChargeSearchTaskService.saveBatch(list);
//        return Result.ok("添加成功！");
//    }


    /**
     * 添加
     *
     * @param ybChargeSearchTask
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-添加")
    @ApiOperation(value = "收费明细查询任务表-添加", notes = "收费明细查询任务表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbChargeSearchTask ybChargeSearchTask) {
        ybChargeSearchTaskService.save(ybChargeSearchTask);
        return Result.ok("添加成功！");
    }

    /**
     * 添加
     *
     * @param addBatchList
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-批量添加")
    @ApiOperation(value = "收费明细查询任务表-批量添加", notes = "收费明细查询任务表-批量添加")
    @PostMapping(value = "/saveBatch")
    public Result<?> saveBatch(@RequestBody List<YbChargeSearchTask> addBatchList) throws Exception{
        ybChargeSearchTaskService.saveBatch(addBatchList);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param ybChargeSearchTask
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-编辑")
    @ApiOperation(value = "收费明细查询任务表-编辑", notes = "收费明细查询任务表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbChargeSearchTask ybChargeSearchTask) {
        ybChargeSearchTaskService.updateById(ybChargeSearchTask);
        //websocket通知状态改变
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cmd", "searchTask");
        jsonObject.put("id", ybChargeSearchTask.getId());
        webSocket.sendAllMessage(JSON.toJSONString(jsonObject));
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-通过id删除")
    @ApiOperation(value = "收费明细查询任务表-通过id删除", notes = "收费明细查询任务表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        try {
            boolean flag = ybChargeSearchTaskService.deleteYbChargeSearchTask(id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-批量删除")
    @ApiOperation(value = "收费明细查询任务表-批量删除", notes = "收费明细查询任务表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.ybChargeSearchTaskService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "收费明细查询任务表-通过id查询")
    @ApiOperation(value = "收费明细查询任务表-通过id查询", notes = "收费明细查询任务表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YbChargeSearchTask ybChargeSearchTask = ybChargeSearchTaskService.getById(id);
        return Result.ok(ybChargeSearchTask);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ybChargeSearchTask
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YbChargeSearchTask ybChargeSearchTask) {
        return super.exportXls(request, ybChargeSearchTask, YbChargeSearchTask.class, "收费明细查询任务表");
    }


    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件对象
            // 判断文件名是否为空
            if (file == null) {
                return Result.error("上传文件为空");
            }
            // 获取文件名
            String name = file.getOriginalFilename();
            // 判断文件大小、即名称
            long size = file.getSize();
            if (name == null || ("").equals(name) && size == 0) {
                return Result.error("上传文件内容为空");
            }
            String[] mappingFields = {"itemname", "itemname1", "itemType", "item1Type", "item1Wgtype", "qtyType", "qtyNum", "isSameDay"};
            List<YbChargeSearchTask> datalist = new ArrayList<>();
            if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
                datalist = ExcelXUtils.readSheet(YbChargeSearchTask.class, mappingFields, 0, 1, file.getInputStream());
            } else {
                datalist = ExcelUtils.readSheet(YbChargeSearchTask.class, mappingFields, 0, 1, file.getInputStream());
            }
            //字典数据
            Map<String, String> item1TypeDict = medicalDictService.queryNameMapByType("ITEM1_TYPE");
            Map<String, String> item1WgtypeDict = medicalDictService.queryNameMapByType("ITEM1_WGTYPE");
            Map<String, String> qtyTypeDict = medicalDictService.queryNameMapByType("QTY_TYPE");

            StringBuffer message = new StringBuffer();
            for (int i = 0; i < datalist.size(); i++) {
                YbChargeSearchTask bean = datalist.get(i);
                if (StringUtils.isBlank(bean.getItemname()) && StringUtils.isNotBlank(bean.getItemname1())) {
                    message.append("第" + (i + 2) + "行查询的收费项目名称关键字为空时，同一天同时收费的项目名称关键字不能存在\n");
                    continue;
                }
                String itemType = "yb";//默认医保
                if (StringUtils.isNotBlank(bean.getItemType())) {
                    if (bean.getItemType().indexOf("医保") != -1) {
                        itemType = "yb";
                    } else if (bean.getItemType().indexOf("医院") != -1 || bean.getItemType().toUpperCase().indexOf("HIS") != -1) {
                        itemType = "his";
                    }
                }
                bean.setItemType(itemType);

                String item1Type = item1TypeDict.get(bean.getItem1Type());
                item1Type = StringUtils.isNotBlank(item1Type) ? item1Type : "oneday";//默认同一天
                bean.setItem1Type(item1Type);

                String item1Wgtype = item1WgtypeDict.get(bean.getItem1Wgtype());
                item1Wgtype = StringUtils.isNotBlank(item1Wgtype) ? item1Wgtype : "haveB";//默认B项目存在违规(重复收费)
                bean.setItem1Wgtype(item1Wgtype);

                String qtyType = qtyTypeDict.get(bean.getQtyType());
                qtyType = StringUtils.isNotBlank(qtyType) ? qtyType : "charge_qty";//默认一天超量
                bean.setQtyType(qtyType);

                String isSameDay = "是".equals(bean.getIsSameDay()) ? "1" : "0";
                bean.setIsSameDay(isSameDay);
            }
            if (message.length() == 0) {
                return Result.ok(datalist);
            } else {
                return Result.error(message.toString());
            }
        }
        return Result.error("上传文件为空");
    }


    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "获取sql语句-通过id查询")
    @ApiOperation(value = "获取sql语句-通过id查询", notes = "获取sql语句-通过id查询")
    @GetMapping(value = "/getSqlByTask")
    public Result<?> getSqlByTask(@RequestParam(name = "id", required = true) String id) throws Exception {
        YbChargeSearchTask bean = ybChargeSearchTaskService.getById(id);
        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(bean.getDataSource());
        if (dbVO == null) {
            Result.error("获取项目地数据源信息失败");
        }
        //获取sql语句对象
        YbChargeQueryDatabase queryDatabase = ybChargeSearchTaskService.getQueryDatabase(dbVO);
        List<YbChargeQuerySql> sqlList = this.ybChargeSearchTaskService.getSqlListByTaskBean(bean, queryDatabase, "");
        return Result.ok(sqlList);
    }

    /**
     * 通过id重跑
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务重跑删除明细数据-通过id重跑")
    @ApiOperation(value = "任务重跑删除明细数据-通过id重跑", notes = "任务重跑删除明细数据-通过id重跑")
    @GetMapping(value = "/deleteYbChargeSearchTaskByRunAgain")
    public Result<?> deleteYbChargeSearchTaskByRunAgain(@RequestParam(name = "id", required = true) String id) throws Exception {
        YbChargeSearchTask bean = ybChargeSearchTaskService.deleteYbChargeSearchTaskByRunAgain(id);
        return Result.ok(bean);
    }

    /**
     * 获取配置信息
     *
     * @return
     */
    @AutoLog(value = "获取配置信息")
    @ApiOperation(value = "获取配置信息", notes = "获取配置信息")
    @GetMapping(value = "/getResultConfigMap")
    public Result<?> getResultConfigMap() throws Exception {
        Map<String, YbChargeSearchConstant.TaskTypeInfo> taskTypeMap = YbChargeSearchConstant.TASK_TYPE_MAP;
        return Result.ok(taskTypeMap);
    }


    /**
     * 通过任务id保存下载记录
     *
     * @param id
     * @return
     */
    @AutoLog(value = "保存下载记录")
    @ApiOperation(value = "保存下载记录", notes = "保存下载记录")
    @GetMapping(value = "/saveDownloadData")
    public Result<?> saveDownloadData(@RequestParam(name = "id", required = true) String id) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbChargeSearchTask bean = ybChargeSearchTaskService.getById(id);
        //保存下载日志记录
        YbChargeSearchTaskDownload downloadBean = new YbChargeSearchTaskDownload();
        BeanUtils.copyProperties(bean, downloadBean);
        downloadBean.setId(UUID.randomUUID().toString());
        downloadBean.setTaskId(bean.getId());
        downloadBean.setCreateUser(user.getRealname());
        downloadBean.setCreateUserId(user.getId());
        downloadBean.setCreateTime(new Date());
        ybChargeSearchTaskDownloadService.save(downloadBean);
        return Result.ok(downloadBean);
    }

    //发送验证码
    @AutoLog(value = "发送验证码")
    @ApiOperation(value = "发送验证码", notes = "发送验证码")
    @PostMapping(value = "/sendSmsDonload")
    public Result<?> sendSmsDonload(@RequestParam(name = "phone", required = true) String phone) throws Exception {
        String phoneNo = "sendSmsDonload:" + phone;

        // 判断发送间隔
        JSONObject json = (JSONObject) redisUtil.get(phoneNo);
        if (json != null) {
            long sendTime = json.getLong("sendTime");
            long expireTime = (System.currentTimeMillis() - sendTime) / 1000;
            if (expireTime < 60) {
                return Result.error("发送间隔时间太短");
            }
        }
        // 判断24小时内发送次数
        String sendCountKey = "sendSmsDonload:count:" + phone;
        Integer sendCount = (Integer) redisUtil.get(sendCountKey);
        if (sendCount != null && sendCount == 8) {
            return Result.error("今日发送次数达到上限");
        }

        String smsTemplateId = "SMS_214820624";
        String captcha = RandomUtil.randomNumbers(4);
        String templateParam = "{\"code\":\"%s\"}";
        templateParam = String.format(templateParam, captcha);
        log.info(phone + "验证码：" + captcha);
        SendSmsResponse smsResponse = AliyunApiUtil.sendSms(phone, smsTemplateId, templateParam);
//		SendSmsResponse smsResponse = new SendSmsResponse();
//		smsResponse.setCode("OK");
        if ("OK".equals(smsResponse.getCode())) {
            json = new JSONObject();
            json.put("captcha", captcha);
            json.put("sendTime", System.currentTimeMillis());
            json.put("validCount", 0);
            //短信发送成功，验证码写入redis中3分钟内有效
            redisUtil.set(phoneNo, json, 180);
            if (sendCount == null) {
                redisUtil.set(sendCountKey, 1, 60 * 60 * 24);
            } else {
                redisUtil.set(sendCountKey, sendCount + 1, redisUtil.getExpire(sendCountKey));
            }
        } else {
            return Result.error(smsResponse.getMessage());
        }

        phone = phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);

        return Result.ok("已发送验证码至：" + phone);
    }

    //短信验证码校验
    @AutoLog(value = "短信验证码校验")
    @ApiOperation(value = "短信验证码校验", notes = "短信验证码校验")
    @PostMapping(value = "/checkSms")
    public Result<?> checkSms(@RequestParam(name = "phone", required = true) String phone, @RequestParam(name = "captcha", required = true) String captcha) {
        Result<JSONObject> result = new Result<>();
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //获取字典中的验证码
        String dictSmsCode = medicalDictService.queryDictKeyByText("K", "K2");
        if (StringUtils.isNotBlank(dictSmsCode)) {
            dictSmsCode = StringUtils.reverse(dictSmsCode).toLowerCase();
        }

        if (!defaultCode.equals(captcha) && !dictSmsCode.equals(captcha)) {
            String phoneNo = "sendSmsDonload:" + phone;
            JSONObject json = (JSONObject) redisUtil.get(phoneNo);
            if (json == null) {
                result.error500("验证码失效，请重新发送");
                return result;
            }
            int validCount = json.getIntValue("validCount");
            if (validCount == 4) {
                result.error500("验证次数达到上限，请重新发送");
                return result;
            }
            String checkCode = json.getString("captcha");
            if (!checkCode.equals(captcha)) {
                // 增加已验证次数
                json.put("validCount", validCount + 1);
                redisUtil.set(phoneNo, json);

                result.error500("验证码错误，请重新输入");
                return result;
            }
            // 重置发送次数
            String sendCountKey = "sendSmsDonload:count:" + phone;
            redisUtil.del(sendCountKey);
        }
        //成功 24小时内免校验
        String checkSmsDonload = "checkSmsDonload:" + user.getUsername();
        redisUtil.set(checkSmsDonload, 1, 60 * 60 * 24);
        return result;
    }

    /**
     * 24小时内短信校验是否通过
     *
     * @return
     */
    @AutoLog(value = "24小时内短信校验是否通过")
    @ApiOperation(value = "24小时内短信校验是否通过", notes = "24小时内短信校验是否通过")
    @GetMapping(value = "/checkSmsDonload")
    public Result<?> checkSmsDonload() throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String checkSmsDonload = "checkSmsDonload:" + user.getUsername();
        Integer checkCount = (Integer) redisUtil.get(checkSmsDonload);
        if (checkCount != null) {
            return Result.error("未进行短信验证码校验");
        }
        return Result.ok();
    }


    /**
     * 使用情况统计
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "使用情况统计", notes = "使用情况统计")
    @GetMapping(value = "/getSearchTaskCount")
    public Result<?> getSearchTaskCount(YbChargeSearchTaskCountVo chargeSearchTaskCountVo,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) {

        Page<YbChargeSearchTask> page = new Page<YbChargeSearchTask>(pageNo, pageSize);
        IPage<Map<String, Object>> result = ybChargeSearchTaskService.getSearchTaskCount(chargeSearchTaskCountVo, page);
        return Result.ok(result);
    }


    /**
     * 获取动态项目地字段
     */
    @ApiOperation(value = "获取动态项目地字段", notes = "获取动态项目地字段")
    @GetMapping(value = "/getDataSourceFields")
    public Result<?> getDataSourceFields() {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String systemCode = loginUser.getSystemCode();
        List<SysDatasource> list = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).select(SysDatasource::getCode, SysDatasource::getName));
        return Result.ok(list);
    }


    /**
     * 导出使用统计情况excel
     */
    @RequestMapping(value = "/exportCountXls")
    public void exportXls2(HttpServletResponse response, YbChargeSearchTaskCountVo chargeSearchTaskCountVo) throws Exception {
        OutputStream os = response.getOutputStream();
        String title = "使用统计情况" + System.currentTimeMillis();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

        //动态表头
        StringBuffer strFields = new StringBuffer();
        //动态字段
        StringBuffer fields = new StringBuffer();
        strFields.append("周开始时间");
        strFields.append(",");
        fields.append("weekStart");
        fields.append(",");

        strFields.append("周结束时间");
        strFields.append(",");
        fields.append("weekEnd");
        fields.append(",");

        strFields.append("周数");
        strFields.append(",");
        fields.append("weekNum");
        fields.append(",");

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String systemCode = loginUser.getSystemCode();
        List<SysDatasource> titleList = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).select(SysDatasource::getCode, SysDatasource::getName));

        for (int i = 0; i < titleList.size(); i++) {
            SysDatasource datasource = titleList.get(i);
            String name = datasource.getName();
            String code = datasource.getCode();
            strFields.append(name);
            fields.append(code);

            strFields.append(",");
            fields.append(",");


        }

        strFields.append("合计");
        fields.append("total");


        String[] titleArr = strFields.toString().split(",");
        String[] fieldArr = fields.toString().split(",");

        //导出数据
        List<Map<String, Object>> vueList = ybChargeSearchTaskService.getSearchTaskCountList(chargeSearchTaskCountVo);

        // 生成一个表格
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("使用统计情况");

        int startHang = 0;

        // 设置标题样式
        CellStyle titleStyle = initTitleStyle(workbook);

        Row rowTitle = sheet.createRow(startHang);
        rowTitle.setHeight((short) 500);
        for (int i = 0, len = titleArr.length; i < len; i++) {
            String t = titleArr[i];
            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(t);
            cell.setCellStyle(titleStyle);
            sheet.setColumnWidth(i, 15 * 256);
        }

        startHang++;

        if (vueList.size() > 0) {
            int celNum = 0;
            for (Map<String, Object> map : vueList) {
                Row row = sheet.createRow(startHang++);
                for (String field : fieldArr) {
                    Cell cell = row.createCell(celNum++);
                    Object o = map.get(field);
                    if (ObjectUtil.isEmpty(o) || String.valueOf(o).equals("0") || String.valueOf(o).equals("0.0") || String.valueOf(o).equals("0.00")) {
                        o = "";
                    }
                    cell.setCellValue(String.valueOf(o));

                }
                celNum = 0;
            }
        }


        workbook.write(os);
        workbook.dispose();


    }


    public static CellStyle initTitleStyle(SXSSFWorkbook workbook) {
        //设置标题字体;
        Font font = workbook.createFont();
        // 设置字体大小
        font.setFontHeightInPoints((short) 11);
        // 字体加粗
        font.setBold(true);
        // 设置字体名字
        font.setFontName("Courier New");
        // 设置样式
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(font);
        // 设置单元格居中对齐
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        // 设置单元格居中对齐
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 创建单元格内容不显示自动换行
        titleStyle.setWrapText(false);
        titleStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        //solid 填充  foreground  前景色
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        titleStyle.setBorderBottom(BorderStyle.THIN);
        titleStyle.setBottomBorderColor(IndexedColors.BLACK.index);
        titleStyle.setBorderLeft(BorderStyle.THIN);
        titleStyle.setLeftBorderColor(IndexedColors.BLACK.index);
        titleStyle.setBorderRight(BorderStyle.THIN);
        titleStyle.setRightBorderColor(IndexedColors.BLACK.index);
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setTopBorderColor(IndexedColors.BLACK.index);

        return titleStyle;
    }


    /**
     * 功能点使用统计
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "功能点使用统计", notes = "功能点使用统计")
    @GetMapping(value = "/getSearchTaskFunCount")
    public Result<?> getSearchTaskFunCount(YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           HttpServletRequest req) {

        Page<YbChargeSearchTask> page = new Page<YbChargeSearchTask>(pageNo, pageSize);
        IPage<Map<String, Object>> result = ybChargeSearchTaskService.getSearchTaskFunCount(chargeSearchTaskFunCountVo, page);
        return Result.ok(result);
    }


    /**
     * 导出功能点使用统计excel
     */
    @RequestMapping(value = "/exportFunCountXls")
    public void exportFunCountXls(HttpServletResponse response, YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo) throws Exception {
        OutputStream os = response.getOutputStream();
        String title = "功能点使用统计" + System.currentTimeMillis();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

        //表头
        StringBuffer strFields = new StringBuffer();
        //字段
        StringBuffer fields = new StringBuffer();
        strFields.append("功能大类");
        strFields.append(",");
        fields.append("bigTitle");
        fields.append(",");

        strFields.append("功能小类");
        strFields.append(",");
        fields.append("smallTitle");
        fields.append(",");

        strFields.append("数量");
        strFields.append(",");
        fields.append("num");
        fields.append(",");


        String[] titleArr = strFields.toString().split(",");
        String[] fieldArr = fields.toString().split(",");

        //导出数据
        List<Map<String, Object>> vueList = ybChargeSearchTaskService.getSearchTaskFunCountList(chargeSearchTaskFunCountVo);

        // 生成一个表格
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("功能点使用统计");

        int startHang = 0;

        // 设置标题样式
        CellStyle titleStyle = initTitleStyle(workbook);

        Row rowTitle = sheet.createRow(startHang);
        rowTitle.setHeight((short) 500);
        //填充表头
        for (int i = 0, len = titleArr.length; i < len; i++) {
            String t = titleArr[i];
            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(t);
            cell.setCellStyle(titleStyle);
            sheet.setColumnWidth(i, 15 * 256);
        }

        startHang++;

        //填充值
        if (vueList.size() > 0) {
            int celNum = 0;
            for (Map<String, Object> map : vueList) {
                Row row = sheet.createRow(startHang++);
                for (String field : fieldArr) {
                    Cell cell = row.createCell(celNum++);
                    Object o = map.get(field);
                    if (ObjectUtil.isEmpty(o)) {
                        o = "";
                    }
                    cell.setCellValue(String.valueOf(o));

                }
                celNum = 0;
            }
        }


        workbook.write(os);
        workbook.dispose();


    }


    /**
     * 首页使用情况统计
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "首页使用情况统计", notes = "首页使用情况统计")
    @GetMapping(value = "/getIndexUseCountList")
    public Result<?> getIndexUseCountList(YbChargeSearchTaskCountVo chargeSearchTaskCountVo, HttpServletRequest req) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //系统编码
        String systemCode = loginUser.getSystemCode();
        //统计数据
        List<Map<String, Object>> result = ybChargeSearchTaskService.getSearchTaskCountList(chargeSearchTaskCountVo);
        //项目地列表
        List<SysDatasource> list = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).or().eq(SysDatasource::getSystemCode, ""));
        //周数统计列表
        ArrayList<Map<String, Object>> result1 = new ArrayList<>();
        result.stream().forEach(t -> {
            HashMap<String, Object> map1 = new HashMap<>();
            String weekStart = String.valueOf(t.get("weekStart"));
            if(!weekStart.equalsIgnoreCase("合计")){
                String year = "";
                if (StrUtil.isNotEmpty(weekStart)) {
                    year = weekStart.split("-")[0];
                }

                map1.put("x", year + "-" + t.get("weekNum"));
                map1.put("y", t.get("total"));
                result1.add(map1);
            }

        });

        //项目地数据排行统计列表
        ArrayList<Map<String, Object>> result2 = new ArrayList<>();
        //项目地数据排行统计
        HashMap<String, Integer> map = new HashMap<>();
        for(SysDatasource datasource:list){
            String code = datasource.getCode();
            String name = datasource.getName();
            List<Map<String, Object>> collect1 = result.stream().filter(t -> !t.get("weekStart").equals("合计")).collect(Collectors.toList());
            IntSummaryStatistics collect = collect1.stream().collect(Collectors.summarizingInt(b -> Integer.parseInt(b.get(code).toString())));
            int sum = (int)collect.getSum();
            map.put(name,sum);
        }
        map.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue() - o1.getValue())
                .forEach(e -> {
                    Map<String, Object> map2 = new LinkedHashMap<>();
                    map2.put("name", e.getKey());
                    map2.put("total", e.getValue());
                    result2.add(map2);
                });


        HashMap<String, List> result3 = new HashMap<>();
        if(result1.size()>12){
            result3.put("barData", result1.subList(result1.size()-12,result1.size()));
        }else{
            result3.put("barData", result1);
        }

        result3.put("rankList", result2);

        return Result.ok(result3);
    }


}
