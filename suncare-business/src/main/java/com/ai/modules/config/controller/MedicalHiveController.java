package com.ai.modules.config.controller;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.StdToHiveConfig;
import com.ai.modules.config.service.IStdToHiveConfigService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "同步Hive")
@RestController
@RequestMapping("/config/medicalHive")
public class MedicalHiveController {

    @Autowired
    private GbdpWebUrlProperties gbdpWebUrlProperties;

    @Autowired
    private IStdToHiveConfigService stdToHiveConfigService;

    /**
     * 基础数据同步Hive操作
     *
     * @param tableName
     * @return
     */
    @AutoLog(value = "基础数据同步hive操作")
    @ApiOperation(value="基础数据同步hive操作", notes="基础数据同步hive操作")
    @GetMapping(value = "/updateStdToHiveConfig")
    public Result<?> updateStdToHiveConfig(@RequestParam(name="tableName",required=true) String tableName) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = user.getDataSource();
        String url ="";
        String url_gp = "";
        Map<String,String> gbdpwebUrlMap = gbdpWebUrlProperties.getUrl();
        if(gbdpwebUrlMap!=null){
            if(gbdpwebUrlMap.get(dataSource)!=null){
                url = gbdpwebUrlMap.get(dataSource);
            }else{
                url = gbdpwebUrlMap.get("default");
            }
            if(gbdpwebUrlMap.get(dataSource)!=null){
                url_gp = gbdpwebUrlMap.get(dataSource+"_gp");
            }else{
                url_gp = gbdpwebUrlMap.get("default_gp");
            }
        }
        if(StringUtils.isBlank(url)){
            return Result.error("未配置同步Hive接口地址");
        }
        if(StringUtils.isBlank(url_gp)){
            return Result.error("未配置同步greenplum接口地址");
        }
        String msg = "";
        String responseBody = "";
        JSONObject jsonObject = new JSONObject();
        //同步hive
        /*Map<String,String> busiParamsHive = new HashMap<String,String>();
        busiParamsHive.put("stableName", tableName);

        try{
            responseBody = ApiOauthUtil.doPost(url,"/task/dataCheck/updateStdToHiveConfig", busiParamsHive, false);
        }catch (Exception e){
            msg += "请求同步Hive接口失败;";
        }

        jsonObject = JSONObject.parseObject(responseBody);
        if(StringUtils.isBlank(msg)&&!(boolean)(jsonObject.get("success"))){
            msg += (String)jsonObject.get("msg");
        }*/

        //通过源表表名转化为目标表表名
        Map<String,String> map = new HashMap<String,String>();
        map.put("stableName", tableName);
        StdToHiveConfig bean = ApiTokenUtil.getObj("/config/medicalHive/queryStdToHiveConfig", map, StdToHiveConfig.class);
        if(bean!=null){
            tableName = bean.getTtableName();
        }

        //同步gp
        Map<String,String> busiParamsGp = new HashMap<String,String>();
        busiParamsGp.put("tableName", tableName);
        responseBody = "";
        try{
            responseBody = ApiOauthUtil.doGet(url_gp,"/bpapp/sync/dic/inform", busiParamsGp, false);
        }catch (Exception e){
            msg += "请求同步greenplum接口失败;";
        }
        jsonObject = JSONObject.parseObject(responseBody);
        if(StringUtils.isBlank(msg)&&200!=jsonObject.getIntValue("code")){
            msg +=(String)jsonObject.get("msg");
        }
        if(StringUtils.isNotBlank(msg)){
            return Result.error(msg);
        }else{
            return Result.ok("操作成功!");
        }
    }

    /**
     * 通过STABLE_NAME查询
     *
     * @param stableName
     * @return
     */
    @AutoLog(value = "基础数据配置表-通过stableName查询")
    @ApiOperation(value="基础数据配置表-通过stableName查询", notes="基础数据配置表-通过stableName查询")
    @GetMapping(value = "/queryStdToHiveConfig")
    public Result<?> queryStdToHiveConfig(@RequestParam(name="stableName",required=true) String stableName) {
        StdToHiveConfig bean = stdToHiveConfigService.getOne(new QueryWrapper<StdToHiveConfig>().eq("STABLE_NAME", stableName.toUpperCase()));
        return Result.ok(bean);
    }

    /**
     * 基础数据配置表
     * @return
     */
    @AutoLog(value = "基础数据配置表")
    @ApiOperation(value="基础数据配置表", notes="基础数据配置表")
    @GetMapping(value = "/queryStdToHiveConfigAll")
    public Result<?> queryStdToHiveConfigAll() {
        List<StdToHiveConfig> list = stdToHiveConfigService.list(new QueryWrapper<StdToHiveConfig>().like("TTABLE_NAME","STD_"));
        return Result.ok(list);
    }


    /**
     * 基础数据全部表同步
     *
     * @return
     */
    @AutoLog(value = "基础数据全部表同步")
    @ApiOperation(value="基础数据全部表同步", notes="基础数据全部表同步")
    @GetMapping(value = "/updateStdToHiveConfigAll")
    public Result<?> updateStdToHiveConfigAll() throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = user.getDataSource();
        String url_gp = "";
        Map<String,String> gbdpwebUrlMap = gbdpWebUrlProperties.getUrl();
        if(gbdpwebUrlMap!=null){
            if(gbdpwebUrlMap.get(dataSource)!=null){
                url_gp = gbdpwebUrlMap.get(dataSource+"_gp");
            }else{
                url_gp = gbdpwebUrlMap.get("default_gp");
            }
        }
        if(StringUtils.isBlank(url_gp)){
            return Result.error("未配置同步greenplum接口地址");
        }


        //获取全部表配置信息
        List<StdToHiveConfig> configList = ApiTokenUtil.getArray("/config/medicalHive/queryStdToHiveConfigAll", new HashMap<String,String>(), StdToHiveConfig.class);
        if(configList==null||configList.size()==0){
            return Result.error("获取配置表信息失败");
        }
        List<String> tableNameList = configList.stream().map(t->t.getTtableName()).collect(Collectors.toList());
        String tableName = StringUtils.join(tableNameList,",");

        //同步gp
        Map<String,String> busiParamsGp = new HashMap<String,String>();
        busiParamsGp.put("tableName", tableName);
        String responseBody = "";
        try{
            responseBody = ApiOauthUtil.doGet(url_gp,"/bpapp/sync/dic/inform", busiParamsGp, false);
        }catch (Exception e){
            return Result.error("请求同步greenplum接口失败");
        }
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        if(200==jsonObject.getIntValue("code")){
            return Result.ok("操作成功!");
        }else{
            return Result.error((String)jsonObject.get("msg"));
        }
    }


}
