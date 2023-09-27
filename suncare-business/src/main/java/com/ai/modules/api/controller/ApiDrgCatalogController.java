package com.ai.modules.api.controller;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.service.IDrgCatalogService;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(tags="drg目录相关")
@Controller
@RequestMapping("/oauth/api/drg")
public class ApiDrgCatalogController {
    @Autowired
    private IDrgCatalogService drgCatalogService;

    @ApiOperation(value = "DRG目录")
    @RequestMapping(value="/drgCatalogByVersion", method = {RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> drgCatalogByVersion(String version) throws Exception {
        DrgCatalog bean = drgCatalogService.findDrgCatalog(version);
        return ApiResponse.ok(bean);
    }

    @ApiOperation(value = "目录")
    @RequestMapping(value="/catalog", method = {RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> catalog(String catalogType, String version) throws Exception {
        DrgCatalog bean = drgCatalogService.findCatalog(catalogType, version);
        return ApiResponse.ok(bean);
    }
}
