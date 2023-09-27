package com.ai.modules.action.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.action.entity.MedicalBreakBehaviorResult;
import com.ai.modules.action.service.IMedicalBreakBehaviorResultService;
import com.ai.modules.action.vo.MedicalBreakBehaviorResultVO;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.service.IMedicalFormalBehaviorService;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.vo.DwbClientVo;
import com.ai.modules.review.vo.DwbDoctorVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 不合规行为结果
 * @Author: jeecg-boot
 * @Date: 2020-02-14
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "不合规行为结果")
@RestController
@RequestMapping("/action/medicalBreakBehaviorResult")
public class MedicalBreakBehaviorResultController extends JeecgController<MedicalBreakBehaviorResult, IMedicalBreakBehaviorResultService> {
    @Autowired
    private IMedicalBreakBehaviorResultService medicalBreakBehaviorResultService;

    @Autowired
    private IReviewService reviewService;

    @Autowired
    private IMedicalFormalBehaviorService medicalFormalBehaviorService;

    private static Map<String, String> FIELD_MAPPING = SolrUtil.initFieldMap(MedicalBreakBehaviorResult.class);

    /**
     * 分页列表查询
     *
     * @param medicalBreakBehaviorResult
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "不合规行为结果-分页列表查询")
    @ApiOperation(value = "不合规行为结果-分页列表查询", notes = "不合规行为结果-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalBreakBehaviorResultVO medicalBreakBehaviorResult,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        Page<MedicalBreakBehaviorResultVO> page = new Page<>(pageNo, pageSize);
        IPage<MedicalBreakBehaviorResultVO> pageList;
        try {
//			pageList = medicalBreakDrugActionService.pageSolr(page, medicalBreakDrugAction, req);
//			SolrQuery solrQuery = SolrQueryGenerator.initQuery(medicalBreakDrugAction,req.getParameterMap());
//			pageList = SolrQueryGenerator.page(page,solrQuery,EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
            pageList = SolrQueryGenerator.page(page, medicalBreakBehaviorResult,
                    EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT, FIELD_MAPPING, req);
            List<MedicalBreakBehaviorResultVO> list = pageList.getRecords();
            if ("2".equals(medicalBreakBehaviorResult.getTargetType())) {
                List<DwbClientVo> clientList = reviewService.getDwbClientByClientidsBySolr(
                        list.stream().map(MedicalBreakBehaviorResultVO::getTargetId).collect(Collectors.toList()));
                for(MedicalBreakBehaviorResultVO bean: list){
                    Optional optional = clientList.stream().filter(item -> bean.getTargetId().equals(item.getClientid())).findFirst();
                    if(optional.isPresent()){
                        DwbClientVo client = (DwbClientVo) optional.get();
                        bean.setIdNo(client.getIdNo());
                        bean.setInsurancetype(client.getInsurancetype());
                    }
                }
            } else if ("3".equals(medicalBreakBehaviorResult.getTargetType())) {
                List<DwbDoctorVo> doctorList = reviewService.getDwbDoctorByDoctoridsBySolr(
                        list.stream().map(MedicalBreakBehaviorResultVO::getTargetId).collect(Collectors.toList()));
                for(MedicalBreakBehaviorResultVO bean: list){
                    Optional optional = doctorList.stream().filter(item -> bean.getTargetId().equals(item.getDoctorid())).findFirst();
                    if(optional.isPresent()){
                        DwbDoctorVo doctor = (DwbDoctorVo) optional.get();
                        bean.setIdNo(doctor.getIdNo());
                        bean.setOrgid(doctor.getOrgid());
                        bean.setOrgname(doctor.getOrgname());
                    }
                }
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param medicalBreakBehaviorResult
     * @return
     */
    @AutoLog(value = "不合规行为结果-添加")
    @ApiOperation(value = "不合规行为结果-添加", notes = "不合规行为结果-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalBreakBehaviorResult medicalBreakBehaviorResult) {
        medicalBreakBehaviorResultService.save(medicalBreakBehaviorResult);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalBreakBehaviorResult
     * @return
     */
    @AutoLog(value = "不合规行为结果-编辑")
    @ApiOperation(value = "不合规行为结果-编辑", notes = "不合规行为结果-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalBreakBehaviorResult medicalBreakBehaviorResult) {
        medicalBreakBehaviorResultService.updateById(medicalBreakBehaviorResult);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不合规行为结果-通过id删除")
    @ApiOperation(value = "不合规行为结果-通过id删除", notes = "不合规行为结果-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalBreakBehaviorResultService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "不合规行为结果-批量删除")
    @ApiOperation(value = "不合规行为结果-批量删除", notes = "不合规行为结果-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalBreakBehaviorResultService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不合规行为结果-通过id查询")
    @ApiOperation(value = "不合规行为结果-通过id查询", notes = "不合规行为结果-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalBreakBehaviorResult medicalBreakBehaviorResult = medicalBreakBehaviorResultService.getById(id);
        return Result.ok(medicalBreakBehaviorResult);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalBreakBehaviorResult
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalBreakBehaviorResult medicalBreakBehaviorResult) {
        return super.exportXls(request, medicalBreakBehaviorResult, MedicalBreakBehaviorResult.class, "不合规行为结果");
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
        return super.importExcel(request, response, MedicalBreakBehaviorResult.class);
    }
}
