package com.ai.modules.config.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sun.mail.imap.protocol.ID;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.config.entity.DwbMasterInfoOrg;
import com.ai.modules.config.service.IDwbMasterInfoOrgService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 就诊记录中的医疗机构
 * @Author: jeecg-boot
 * @Date: 2020-11-30
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "就诊记录中的医疗机构")
@RestController
@RequestMapping("/config/dwbMasterInfoOrg")
public class DwbMasterInfoOrgController extends JeecgController<DwbMasterInfoOrg, IDwbMasterInfoOrgService> {
    @Autowired
    private IDwbMasterInfoOrgService dwbMasterInfoOrgService;

    @AutoLog(value = "就诊记录中的医疗机构-同步数据")
    @ApiOperation(value = "就诊记录中的医疗机构-同步数据", notes = "就诊记录中的医疗机构-同步数据")
    @PostMapping(value = "/sync")
    public Result<?> sync(@RequestParam(name = "dataSource") String dataSource, @RequestParam(name = "data") String data, HttpServletRequest req) {

        List<DwbMasterInfoOrg> list = JSONObject.parseArray(data, DwbMasterInfoOrg.class);
        list.forEach(bean -> {
            bean.setId(IdUtils.uuid());
            bean.setDataSource(dataSource);
        });
        dwbMasterInfoOrgService.remove(new QueryWrapper<DwbMasterInfoOrg>().eq("DATA_SOURCE", dataSource));
        dwbMasterInfoOrgService.saveBatch(list);
        return Result.ok();
    }

    @AutoLog(value = "就诊记录中的医疗机构-手动同步数据")
    @ApiOperation(value = "就诊记录中的医疗机构-手动同步数据", notes = "就诊记录中的医疗机构-手动同步数据")
    @GetMapping(value = "/manualSync")
    public Result<?> manualSync(String dataSource) {

        if(StringUtils.isNotBlank(dataSource)){
            this.syncData(dataSource);
        } else {
            this.syncData();
        }

        return Result.ok();
    }

    @Scheduled(cron = "0 0 2 * * ?")//凌晨两点执行
    protected void syncData() {
        for(String dataSource: ApiTokenUtil.getNodeDataSources()){

            String ds = dataSource.trim();
            if(StringUtils.isBlank(ds)) {
                continue;
            }
            this.syncData(ds);
        }
    }

    private void syncData(String dataSource){
        JSONObject facetChild = new JSONObject();
        facetChild.put("ORGNAME", "max(ORGNAME)");
        JSONObject termFacet = new JSONObject();
        termFacet.put("type", "terms");
        termFacet.put("field", "ORGID");
        termFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        termFacet.put("overrequest", Integer.MAX_VALUE);
        termFacet.put("facet", facetChild);

        List<DwbMasterInfoOrg> list = new ArrayList<>();
        ThreadUtils.setDatasource(dataSource);
        try {
            SolrUtil.jsonFacet(EngineUtil.DWB_MASTER_INFO, new String[0], termFacet.toJSONString(), (json) -> {
                String code = json.getString("val");
                String name = json.getString("ORGNAME");
                DwbMasterInfoOrg bean = new DwbMasterInfoOrg();
//                bean.setId(IdUtils.uuid());
                bean.setCode(code);
                bean.setName(name);
//                bean.setDataSource(dataSource);
                list.add(bean);
            });
        } catch (Exception e){
            log.info(dataSource + "同步MASTER机构信息-获取数据失败:" +  dataSource );
        }
        ThreadUtils.removeDatasource();
        if(list.size() > 0){
            Map<String, String> busiParams = new HashMap<>();
            busiParams.put("dataSource", dataSource);
            busiParams.put("data", JSONObject.toJSONString(list));

            String text;
            try {
                text = ApiTokenUtil.doPost(ApiTokenUtil.API_URL, "/config/dwbMasterInfoOrg/sync", busiParams);
                ApiResponse<?> apiResponse = JSON.parseObject(text, ApiResponse.class);
                if(apiResponse.isSuccess()){
                    log.info(dataSource + "同步MASTER机构信息-成功：" + list.size() );
                } else {
                    log.info(dataSource + "同步MASTER机构信息-失败：" + apiResponse.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info(dataSource + "同步MASTER机构信息-失败：" + e.getMessage());
            }
        }
    }

    /**
     * 分页列表查询
     *
     * @param dwbMasterInfoOrg
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "就诊记录中的医疗机构-分页列表查询")
    @ApiOperation(value = "就诊记录中的医疗机构-分页列表查询", notes = "就诊记录中的医疗机构-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(DwbMasterInfoOrg dwbMasterInfoOrg,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<DwbMasterInfoOrg> queryWrapper = QueryGenerator.initQueryWrapper(dwbMasterInfoOrg, req.getParameterMap());
        Page<DwbMasterInfoOrg> page = new Page<DwbMasterInfoOrg>(pageNo, pageSize);
        IPage<DwbMasterInfoOrg> pageList = dwbMasterInfoOrgService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param dwbMasterInfoOrg
     * @return
     */
    @AutoLog(value = "就诊记录中的医疗机构-添加")
    @ApiOperation(value = "就诊记录中的医疗机构-添加", notes = "就诊记录中的医疗机构-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody DwbMasterInfoOrg dwbMasterInfoOrg) {
        dwbMasterInfoOrgService.save(dwbMasterInfoOrg);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param dwbMasterInfoOrg
     * @return
     */
    @AutoLog(value = "就诊记录中的医疗机构-编辑")
    @ApiOperation(value = "就诊记录中的医疗机构-编辑", notes = "就诊记录中的医疗机构-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody DwbMasterInfoOrg dwbMasterInfoOrg) {
        dwbMasterInfoOrgService.updateById(dwbMasterInfoOrg);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "就诊记录中的医疗机构-通过id删除")
    @ApiOperation(value = "就诊记录中的医疗机构-通过id删除", notes = "就诊记录中的医疗机构-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        dwbMasterInfoOrgService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "就诊记录中的医疗机构-批量删除")
    @ApiOperation(value = "就诊记录中的医疗机构-批量删除", notes = "就诊记录中的医疗机构-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.dwbMasterInfoOrgService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "就诊记录中的医疗机构-通过id查询")
    @ApiOperation(value = "就诊记录中的医疗机构-通过id查询", notes = "就诊记录中的医疗机构-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        DwbMasterInfoOrg dwbMasterInfoOrg = dwbMasterInfoOrgService.getById(id);
        return Result.ok(dwbMasterInfoOrg);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param dwbMasterInfoOrg
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DwbMasterInfoOrg dwbMasterInfoOrg) {
        return super.exportXls(request, dwbMasterInfoOrg, DwbMasterInfoOrg.class, "就诊记录中的医疗机构");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DwbMasterInfoOrg.class);
    }

}
