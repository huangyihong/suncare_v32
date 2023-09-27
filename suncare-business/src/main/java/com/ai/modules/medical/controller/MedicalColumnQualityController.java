package com.ai.modules.medical.controller;

import com.ai.modules.medical.entity.MedicalColumnQuality;
import com.ai.modules.medical.service.IMedicalColumnQualityService;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.service.ITaskProjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 规则依赖字段质量表
 * @Author: jeecg-boot
 * @Date:   2021-03-16
 * @Version: V1.0
 */
@Slf4j
@Api(tags="规则依赖字段质量表")
@RestController
@RequestMapping("/medical/medicalColumnQuality")
public class MedicalColumnQualityController extends JeecgController<MedicalColumnQuality, IMedicalColumnQualityService> {
    @Autowired
    private IMedicalColumnQualityService medicalColumnQualityService;
    @Autowired
    private ITaskProjectService taskProjectService;

    /**
     * 分页列表查询
     *
     * @param medicalColumnQuality
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-分页列表查询")
    @ApiOperation(value="规则依赖字段质量表-分页列表查询", notes="规则依赖字段质量表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalColumnQuality medicalColumnQuality,String tableColumn,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalColumnQuality> queryWrapper = getMedicalColumnQualityQueryWrapper(medicalColumnQuality, req);
        if(StringUtils.isNotBlank(tableColumn)){
            if("1".equals(req.getParameter("hasResult"))){
//                queryWrapper.inSql("COLUMN_ID"," select column_id from (select column_id,table_name||'.'||column_name as table_column from medical_column_quality )t  \n" +
                queryWrapper.inSql("COLUMN_ID"," select column_id from (select column_id,concat(table_name,'.',column_name) as table_column from medical_column_quality )t  \n" +
                        " where  t.table_column in ('"+tableColumn.replace(",","','")+"')");
            }else{
//                queryWrapper.notInSql("COLUMN_ID"," select column_id from (select column_id,table_name||'.'||column_name as table_column from medical_column_quality )t  \n" +
                queryWrapper.notInSql("COLUMN_ID"," select column_id from (select column_id,concat(table_name,'.',column_name) as table_column from medical_column_quality )t  \n" +
                        " where  t.table_column in ('"+tableColumn.replace(",","','")+"')");
            }
        }
        Page<MedicalColumnQuality> page = new Page<MedicalColumnQuality>(pageNo, pageSize);
        IPage<MedicalColumnQuality> pageList = medicalColumnQualityService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 列表查询
     *
     * @param medicalColumnQuality
     * @param req
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-记录查询")
    @ApiOperation(value="规则依赖字段质量表-记录查询", notes="规则依赖字段质量表-记录查询")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(MedicalColumnQuality medicalColumnQuality,String tableColumn, HttpServletRequest req) {
        QueryWrapper<MedicalColumnQuality> queryWrapper = getMedicalColumnQualityQueryWrapper(medicalColumnQuality, req);
        if(StringUtils.isNotBlank(tableColumn)){
            if("1".equals(req.getParameter("hasResult"))){
//                queryWrapper.inSql("COLUMN_ID"," select column_id from (select column_id,table_name||'.'||column_name as table_column from medical_column_quality )t  \n" +
                queryWrapper.inSql("COLUMN_ID"," select column_id from (select column_id,concat(table_name,'.',column_name) as table_column from medical_column_quality )t  \n" +
                        " where  t.table_column in ('"+tableColumn.replace(",","','")+"')");
            }else{
//                queryWrapper.notInSql("COLUMN_ID"," select column_id from (select column_id,table_name||'.'||column_name as table_column from medical_column_quality )t  \n" +
                queryWrapper.notInSql("COLUMN_ID"," select column_id from (select column_id,concat(table_name,'.',column_name) as table_column from medical_column_quality )t  \n" +
                        " where  t.table_column in ('"+tableColumn.replace(",","','")+"')");
            }
        }
        List<MedicalColumnQuality> list = medicalColumnQualityService.list(queryWrapper);
        return Result.ok(list);
    }

    private QueryWrapper<MedicalColumnQuality> getMedicalColumnQualityQueryWrapper(MedicalColumnQuality medicalColumnQuality, HttpServletRequest req) {
        if(StringUtils.isNotBlank(medicalColumnQuality.getTableName())){
            medicalColumnQuality.setTableName(medicalColumnQuality.getTableName().toUpperCase());
        }
        if(StringUtils.isNotBlank(medicalColumnQuality.getColumnName())){
            medicalColumnQuality.setColumnName(medicalColumnQuality.getColumnName().toUpperCase());
        }
        QueryWrapper<MedicalColumnQuality> queryWrapper = QueryGenerator.initQueryWrapper(medicalColumnQuality, req.getParameterMap());
        //不合规行为 查询
        String actionName = req.getParameter("actionName");
        if(StringUtils.isNotBlank(actionName)){
            String[] actionNamesArr = actionName.split(",");
            queryWrapper.and(j -> {
                for(int k=0;k<actionNamesArr.length;k++){
                    if(k==0){
                        j = j.like("ACTION_NAMES",actionNamesArr[k]);
                    }else{
                        j = j.or().like("ACTION_NAMES",actionNamesArr[k]);
                    }
                }
                return j;
            });
        }
        return queryWrapper;
    }

    /**
     * 添加
     *
     * @param medicalColumnQuality
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-添加")
    @ApiOperation(value="规则依赖字段质量表-添加", notes="规则依赖字段质量表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalColumnQuality medicalColumnQuality) {
        medicalColumnQualityService.save(medicalColumnQuality);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalColumnQuality
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-编辑")
    @ApiOperation(value="规则依赖字段质量表-编辑", notes="规则依赖字段质量表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalColumnQuality medicalColumnQuality) {
        medicalColumnQualityService.updateById(medicalColumnQuality);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-通过id删除")
    @ApiOperation(value="规则依赖字段质量表-通过id删除", notes="规则依赖字段质量表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        medicalColumnQualityService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-批量删除")
    @ApiOperation(value="规则依赖字段质量表-批量删除", notes="规则依赖字段质量表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        this.medicalColumnQualityService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-通过id查询")
    @ApiOperation(value="规则依赖字段质量表-通过id查询", notes="规则依赖字段质量表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        MedicalColumnQuality medicalColumnQuality = medicalColumnQualityService.getById(id);
        return Result.ok(medicalColumnQuality);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalColumnQuality
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalColumnQuality medicalColumnQuality) {
        return super.exportXls(request, medicalColumnQuality, MedicalColumnQuality.class, "规则依赖字段质量表");
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
        return super.importExcel(request, response, MedicalColumnQuality.class);
    }

    /**
     * 项目列表
     * @return
     */
    @AutoLog(value = "项目列表")
    @ApiOperation(value = "项目列表", notes = "项目列表")
    @GetMapping(value = "/queryTaskProject")
    public Result<?> queryTaskProject() {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<TaskProject> queryWrapper = new QueryWrapper<TaskProject>();
        queryWrapper.eq("DATA_SOURCE",user.getDataSource());
        queryWrapper.orderByDesc("CREATE_TIME");
        List<TaskProject> list = taskProjectService.list(queryWrapper);
        return Result.ok(list);
    }

}
