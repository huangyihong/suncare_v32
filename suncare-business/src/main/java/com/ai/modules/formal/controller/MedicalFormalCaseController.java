package com.ai.modules.formal.controller;

import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.service.IEngineTrialService;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.formal.vo.MedicalFormalCaseVO;
import com.alibaba.fastjson.JSONObject;
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
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 风控模型正式表
 * @Author: jeecg-boot
 * @Date: 2019-11-26
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "风控模型正式表")
@RestController
@RequestMapping("/formal/medicalFormalCase")
public class MedicalFormalCaseController extends JeecgController<MedicalFormalCase, IMedicalFormalCaseService> {
    @Autowired
    private IMedicalFormalCaseService medicalFormalCaseService;

    @Autowired
    private IEngineTrialService engineTrialService;
    /**
     * 分页列表查询
     *
     * @param medicalFormalCase
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "风控模型正式表-分页列表查询")
    @ApiOperation(value = "风控模型正式表-分页列表查询", notes = "风控模型正式表-分页列表查询")
    @RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
    public Result<?> queryPageList(MedicalFormalCase medicalFormalCase,
                                   String searchCode,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
    	/*String actionGrpId = medicalFormalCase.getActionGrpId();
    	if("all".equals(actionGrpId)||"null".equals(actionGrpId)) {
    		medicalFormalCase.setActionGrpId(null);
		}*/
        QueryWrapper<MedicalFormalCase> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCase, req.getParameterMap());
		/*if("null".equals(actionGrpId)) {
			queryWrapper.isNull("ACTION_GRP_ID");
		}*/

        groupCodeSql(req,searchCode,queryWrapper);

		List<String> caseIdList = new ArrayList<String>();
		String batchId = req.getParameter("batchId");
    	if(StringUtils.isNotBlank(batchId)) {
    		//查询该批次的业务组下的模型
    		caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBatchId(batchId)) ;
    	}
    	String busiId = req.getParameter("busiId");
    	if(StringUtils.isNotBlank(busiId)) {
    		//查询该业务组下的模型
    		caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBusiId(busiId));
    	}
    	String behaviorId = req.getParameter("behaviorId");
    	if(StringUtils.isNotBlank(behaviorId)) {
    		//查询该不合规行为的模型
    		caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBehaviorId(behaviorId));
    	}
    	if(StringUtils.isNotBlank(batchId)||StringUtils.isNotBlank(busiId)||StringUtils.isNotBlank(behaviorId)) {
    		if(caseIdList.size()==0) {
    			caseIdList.add("");
    		}
    		queryWrapper.in("CASE_ID",caseIdList);
    	}
        Page<MedicalFormalCase> page = new Page<MedicalFormalCase>(pageNo, pageSize);
        IPage<MedicalFormalCase> pageList = medicalFormalCaseService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @AutoLog(value = "风控模型正式表-数量查询")
    @ApiOperation(value = "风控模型正式表-数量查询", notes = "风控模型正式表-数量查询")
    @GetMapping(value = "/selectCount")
    public Result<?> selectCount(MedicalFormalCase medicalFormalCase,
                                 String searchCode,
                                         HttpServletRequest req) {
        QueryWrapper<MedicalFormalCase> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCase, req.getParameterMap());

        groupCodeSql(req,searchCode,queryWrapper);

        List<String> caseIdList = new ArrayList<String>();
        String batchId = req.getParameter("batchId");
        if(StringUtils.isNotBlank(batchId)) {
            //查询该批次的业务组下的模型
            caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBatchId(batchId)) ;
        }
        String busiId = req.getParameter("busiId");
        if(StringUtils.isNotBlank(busiId)) {
            //查询该业务组下的模型
            caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBusiId(busiId));
        }
        String behaviorId = req.getParameter("behaviorId");
        if(StringUtils.isNotBlank(behaviorId)) {
            //查询该不合规行为的模型
            caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBehaviorId(behaviorId));
        }
        if(StringUtils.isNotBlank(batchId)||StringUtils.isNotBlank(busiId)||StringUtils.isNotBlank(behaviorId)) {
            if(caseIdList.size()==0) {
                caseIdList.add("");
            }
            queryWrapper.in("CASE_ID",caseIdList);
        }
        int count = medicalFormalCaseService.count(queryWrapper);
        return Result.ok(count);
    }

    /**
     * 业务组关联的所有模型id和名称
     * @param busiId
     * @return
     */
    @AutoLog(value = "风控模型正式-业务组关联的所有模型id和名称")
    @ApiOperation(value = "风控模型正式-业务组关联的所有模型id和名称", notes = "风控模型正式-业务组关联的所有模型id和名称")
    @GetMapping(value = "/querySimpleByBusiId")
    public Result<?> querySimpleByBusiId(@RequestParam(name = "busiId") String busiId) {
        QueryWrapper<MedicalFormalCase> queryWrapper = new QueryWrapper<MedicalFormalCase>()
                .select("CASE_ID","CASE_NAME")
                .inSql("CASE_ID","SELECT CASE_ID FROM MEDICAL_FORMAL_CASE_BUSI " +
                        "WHERE BUSI_ID = '" + busiId + "'");
        return Result.ok(medicalFormalCaseService.list(queryWrapper));
    }

    /**
     *全部list
     *
     * @param medicalFormalCase
     * @param req
     * @return
     */
    @AutoLog(value = "风控模型正式表-列表查询")
    @ApiOperation(value = "风控模型正式表-列表查询", notes = "风控模型正式表-列表查询")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(MedicalFormalCase medicalFormalCase,HttpServletRequest req) {
    	/*String actionGrpId = medicalFormalCase.getActionGrpId();
    	if("all".equals(actionGrpId)||"null".equals(actionGrpId)) {
    		medicalFormalCase.setActionGrpId(null);
		}*/
        QueryWrapper<MedicalFormalCase> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCase, req.getParameterMap());
	/*	if("null".equals(actionGrpId)) {
			queryWrapper.isNull("ACTION_GRP_ID");
		}*/
		List<String> caseIdList = new ArrayList<String>();
		String batchId = req.getParameter("batchId");
    	if(StringUtils.isNotBlank(batchId)) {
    		//查询该批次的业务组下的模型
    		caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBatchId(batchId)) ;
    	}
    	String busiId = req.getParameter("busiId");
    	if(StringUtils.isNotBlank(busiId)) {
    		//查询该业务组下的模型
    		caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBusiId(busiId));
    	}
    	String behaviorId = req.getParameter("behaviorId");
    	if(StringUtils.isNotBlank(behaviorId)) {
    		//查询该不合规行为的模型
    		caseIdList.addAll(medicalFormalCaseService.selectCaseIdByBehaviorId(behaviorId));
    	}
    	if(StringUtils.isNotBlank(batchId)||StringUtils.isNotBlank(busiId)||StringUtils.isNotBlank(behaviorId)) {
    		if(caseIdList.size()==0) {
    			caseIdList.add("");
    		}
    		queryWrapper.in("CASE_ID",caseIdList);
    	}
        return Result.ok(medicalFormalCaseService.list(queryWrapper));
    }

    /**
     * 添加
     *
     * @param medicalFormalCase
     * @return
     */
/*    @AutoLog(value = "风控模型正式表-添加")
    @ApiOperation(value = "风控模型正式表-添加", notes = "风控模型正式表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalFormalCase medicalFormalCase) {
        medicalFormalCaseService.save(medicalFormalCase);
        return Result.ok("添加成功！");
    }

    *//**
     * 编辑
     *
     * @param medicalFormalCase
     * @return
     *//*
    @AutoLog(value = "风控模型正式表-编辑")
    @ApiOperation(value = "风控模型正式表-编辑", notes = "风控模型正式表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalFormalCase medicalFormalCase) {
        medicalFormalCaseService.updateById(medicalFormalCase);
        return Result.ok("编辑成功!");
    }*/

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "风控模型正式表-通过id删除")
    @ApiOperation(value = "风控模型正式表-通过id删除", notes = "风控模型正式表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalFormalCaseService.removeFormalCaseById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "风控模型正式表-批量删除")
    @ApiOperation(value = "风控模型正式表-批量删除", notes = "风控模型正式表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalFormalCaseService.removeFormalCaseByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "风控模型正式表-通过id查询")
    @ApiOperation(value = "风控模型正式表-通过id查询", notes = "风控模型正式表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalFormalCase medicalFormalCase = medicalFormalCaseService.getById(id);
        return Result.ok(medicalFormalCase);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalFormalCase
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalFormalCase medicalFormalCase) {
        return super.exportXls(request, medicalFormalCase, MedicalFormalCase.class, "风控模型正式表");
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
        return super.importExcel(request, response, MedicalFormalCase.class);
    }

    /**
     * 添加模型探查信息
     *
     * @param medicalFormalCaseVO
     * @return
     */
    @AutoLog(value = "流程图-保存模型归纳信息")
    @ApiOperation(value = "流程图-保存模型归纳信息", notes = "流程图-保存模型归纳信息")
    @PostMapping(value = "/saveFormalCase")
    public Result<?> saveFormalCase(@RequestBody MedicalFormalCaseVO medicalFormalCaseVO) {
//        List<MedicalFormalFlowRule> ruleList = JSONArray.parseArray(medicalFormalCaseVO.getRules(), MedicalFormalFlowRule.class);
//        List<MedicalFormalFlowRuleGrade> gradeList = JSONArray.parseArray(medicalFormalCaseVO.getGrades(), MedicalFormalFlowRuleGrade.class);

        if (StringUtils.isEmpty(medicalFormalCaseVO.getCaseId())) {
            Date nowTime = new Date();
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            medicalFormalCaseVO.setCaseId(IdUtils.uuid());
            medicalFormalCaseVO.setCaseVersion(1.0f);
            medicalFormalCaseVO.setCreateTime(nowTime);
            medicalFormalCaseVO.setCreateUserid(user.getId());
            medicalFormalCaseVO.setCreateUsername(user.getRealname());
            medicalFormalCaseService.addFormalCase(medicalFormalCaseVO);
            return Result.ok(medicalFormalCaseVO);
        } else {
            try {
                medicalFormalCaseService.updateFormalCase(medicalFormalCaseVO);
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }
        return Result.ok(medicalFormalCaseVO);
    }


    @AutoLog(value = "风控模型正式表-复制新增")
    @ApiOperation(value = "风控模型正式表-复制新增", notes = "风控模型正式表-复制新增")
    @PostMapping(value = "/copyAdd")
    public Result<?> copyAdd(@RequestParam(name = "ids", required = true) String ids) {
        medicalFormalCaseService.copyAdd(ids.split(","));
        return Result.ok("复制新增成功！");
    }

    /**
     * 提交流程图信息
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "流程图-提交模型信息")
    @ApiOperation(value = "流程图-提交模型信息", notes = "流程图-提交模型信息")
    @GetMapping(value = "/setFormalCaseSubmit")
    public Result<?> setFormalCaseSubmit(@RequestParam(name = "ids") String ids) {
        medicalFormalCaseService.submitFormalCase(Arrays.asList(ids.split(",")));
        return Result.ok("提交成功！");
    }

    /**
     * 通过id查询所有模型信息
     *
     * @param id
     * @return
     */
    @AutoLog(value = "流程图-通过id查询所有模型信息")
    @ApiOperation(value = "流程图-通过id查询所有模型信息", notes = "流程图-通过id查询所有模型信息")
    @GetMapping(value = "/getFormalCaseById")
    public Result<?> getFormalCaseById(@RequestParam(name = "id") String id, HttpServletRequest req) {
        JSONObject jsonObject = medicalFormalCaseService.getFormalCaseById(id);
        if(jsonObject != null && StringUtils.isNotEmpty(req.getParameter("copyCreate"))){
            jsonObject.put("caseCode", UUIDGenerator.getShortCode());
        }
        return Result.ok(jsonObject);
    }

    @AutoLog(value = "流程图-批量导入转换模型")
    @ApiOperation(value = "流程图-批量导入转换模型", notes = "流程图-批量导入转换模型")
    @PostMapping(value = "/importCaseExcel")
    public Result<?> importCaseExcel(@RequestParam("file") MultipartFile file, HttpServletResponse response) {

        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                long beginTime = System.currentTimeMillis();
                int num = this.medicalFormalCaseService.importExcel(file);
                long endTime = System.currentTimeMillis();
                log.info("[" + name + "]导入时间：" + (endTime - beginTime) / 1000 + "秒");
                return Result.ok("操作成功，转换 " + num + " 条记录");

            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }

    @AutoLog(value = "流程图-导出模型信息")
    @ApiOperation(value = "流程图-导出模型信息", notes = "流程图-导出模型信息")
    @RequestMapping(value = "/exportCaseInfo")
    public Result<?> caseInfoExport(MedicalFormalCase medicalFormalCase,
                                    String searchCode,
                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                    HttpServletRequest req,
                                    HttpServletResponse response) throws Exception {
        // 选中数据
        String selections = req.getParameter("selections");
        if (StringUtils.isNotEmpty(selections)) {
            medicalFormalCase.setCaseId(selections);
        }
        QueryWrapper<MedicalFormalCase> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCase, req.getParameterMap());

        groupCodeSql(req,searchCode,queryWrapper);

        String title = "模型信息";
        long count = medicalFormalCaseService.count(queryWrapper);
        if(count == 0){
            throw new Exception("没有可以导出的记录");
        }

        if(count > 10000){
            ThreadUtils.EXPORT_POOL.add(title + "_导出", "xlsx", (int) count, (os) -> {
                try {
                    this.medicalFormalCaseService.exportCaseInfo(queryWrapper, os);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error(e.getMessage());
                }
                return Result.ok();
            });
            return Result.ok("等待导出");
        } else {
            //response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "导出" + System.currentTimeMillis() + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            OutputStream os = response.getOutputStream();

            this.medicalFormalCaseService.exportCaseInfo(queryWrapper, os);
            return null;

        }

    }

    @AutoLog(value = "流程图-批量导入更新模型信息")
    @ApiOperation(value = "流程图-批量导入更新模型信息", notes = "流程图-批量导入更新模型信息")
    @PostMapping(value = "/importCaseInfo")
    public Result<?> importCaseInfo(@RequestParam("file") MultipartFile file, HttpServletResponse response) {

        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                long beginTime = System.currentTimeMillis();
                int num = this.medicalFormalCaseService.importCaseInfo(file);
                long endTime = System.currentTimeMillis();
                log.info("[" + name + "]导入时间：" + (endTime - beginTime) / 1000 + "秒");
                return Result.ok("操作成功，更新 " + num + " 条记录");

            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }

    private void groupCodeSql(HttpServletRequest req, String searchCode, QueryWrapper<MedicalFormalCase> queryWrapper) {
        if(StringUtils.isNotBlank(searchCode)) {
            String finalSearchCode = searchCode.substring(1, searchCode.length() - 1);
            queryWrapper.and(wrapper ->
                    wrapper.like("RULE_SOURCE", finalSearchCode)
                            .or().like("CASE_NAME", finalSearchCode)
                            .or().eq("CASE_ID", finalSearchCode)
                            .or().inSql("ACTION_TYPE", "SELECT CODE FROM MEDICAL_DICT_ITEM t JOIN MEDICAL_DICT t1 ON t1.GROUP_CODE = 'ACTION_TYPE' AND t1.GROUP_ID = t.GROUP_ID WHERE t.VALUE LIKE '%" + finalSearchCode +"%'")
                            .or().exists("SELECT 1 FROM MEDICAL_ACTION_DICT t WHERE MEDICAL_FORMAL_CASE.ACTION_ID=t.ACTION_ID and "+DbDataEncryptUtil.decryptFunc("ACTION_NAME")+" LIKE '%" + finalSearchCode + "%'")
                            .or().inSql("CASE_STATUS", "SELECT CODE FROM MEDICAL_DICT_ITEM t JOIN MEDICAL_DICT t1 ON t1.GROUP_CODE = 'SWITCH_STATUS' AND t1.GROUP_ID = t.GROUP_ID WHERE t.VALUE LIKE '%" + finalSearchCode +"%'")
            );
        }
        String diseaseGroupCode = req.getParameter("diseaseGroupCode");//疾病组编码
        if (org.apache.commons.lang3.StringUtils.isNotBlank(diseaseGroupCode)) {
            queryWrapper.exists("select 1 from MEDICAL_FORMAL_FLOW_RULE a where  MEDICAL_FORMAL_CASE.CASE_ID=a.CASE_ID and a.COL_NAME='DISEASECODEGROUP' and COMPARE_VALUE is not null\n" +
                    "and "+DbDataEncryptUtil.decryptFunc("COMPARE_VALUE")+"='" + diseaseGroupCode + "'");
        }
        String drugGroupCode = req.getParameter("drugGroupCode");//药品组编码
        if (org.apache.commons.lang3.StringUtils.isNotBlank(drugGroupCode)) {
            queryWrapper.exists("select 1 from MEDICAL_FORMAL_FLOW_RULE a where  MEDICAL_FORMAL_CASE.CASE_ID=a.CASE_ID and a.COL_NAME='DRUGCODEGROUP' and COMPARE_VALUE is not null\n" +
                    "and "+DbDataEncryptUtil.decryptFunc("COMPARE_VALUE")+"='" + drugGroupCode + "'");
        }
        String projectGroupCode = req.getParameter("projectGroupCode");//项目组编码
        if (org.apache.commons.lang3.StringUtils.isNotBlank(projectGroupCode)) {
            queryWrapper.exists("select 1 from MEDICAL_FORMAL_FLOW_RULE a where  MEDICAL_FORMAL_CASE.CASE_ID=a.CASE_ID and a.COL_NAME='TREATCODEGROUP' and COMPARE_VALUE is not null\n" +
                    "and "+DbDataEncryptUtil.decryptFunc("COMPARE_VALUE")+"='" + projectGroupCode + "'");
        }
    }

}
