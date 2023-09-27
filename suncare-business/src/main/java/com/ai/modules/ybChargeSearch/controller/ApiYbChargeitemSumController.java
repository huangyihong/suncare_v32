package com.ai.modules.ybChargeSearch.controller;

import com.ai.modules.ybChargeSearch.service.IYbChargeitemSumHandler;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : zhangly
 * @date : 2023/2/28 15:23
 */
@Slf4j
@RestController
@RequestMapping("/apiDc/ybChargeitemSum")
public class ApiYbChargeitemSumController {

    @Autowired
    private IYbChargeitemSumHandler handler;

    @GetMapping(value = "/compute")
    public Result<?> compute(String datasource) throws Exception{
        handler.computeYbChargeitemSum(datasource);
        return Result.ok();
    }
}
