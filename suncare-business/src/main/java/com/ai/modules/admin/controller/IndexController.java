package com.ai.modules.admin.controller;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@Api(tags="后台首页")
@RequestMapping("/admin")
public class IndexController {

    /**
     * freemaker方式 【页面路径： src/main/resources/templates】
     *
     * @param modelAndView
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView index(ModelAndView modelAndView) {
        log.info("跳转到首页");
        modelAndView.setViewName("index");
        return modelAndView;
    }
    @RequestMapping("/index2")
    public String index2(HttpServletRequest request) {
        log.info("跳转到首页");
        return "index";
    }
    @ApiOperation("测试")
    @GetMapping("/test")
    public Result<JSONObject> test(){
        Result<JSONObject> result = new Result<JSONObject>();

        result.setSuccess(true);
        result.setMessage("操作成功");
        return result;
    }

}
