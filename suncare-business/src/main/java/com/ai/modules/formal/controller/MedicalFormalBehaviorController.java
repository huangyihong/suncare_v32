package com.ai.modules.formal.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ai.common.utils.IdUtils;
import com.ai.modules.formal.entity.MedicalFormalBehavior;
import com.ai.modules.formal.entity.MedicalFormalCaseBehavior;
import com.ai.modules.formal.service.IMedicalFormalBehaviorService;
import com.ai.modules.formal.service.IMedicalFormalCaseBehaviorService;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 不合规行为配置
 * @Author: jeecg-boot
 * @Date: 2020-02-11
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "不合规行为配置")
@RestController
@RequestMapping("/formal/medicalFormalBehavior")
public class MedicalFormalBehaviorController extends JeecgController<MedicalFormalBehavior, IMedicalFormalBehaviorService> {
    @Autowired
    private IMedicalFormalBehaviorService medicalFormalBehaviorService;

    @Autowired
    private IMedicalFormalCaseBehaviorService medicalFormalCaseBehaviorService;

    /**
     * 分页列表查询
     *
     * @param medicalFormalBehavior
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "不合规行为配置-分页列表查询")
    @ApiOperation(value = "不合规行为配置-分页列表查询", notes = "不合规行为配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalFormalBehavior medicalFormalBehavior,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalFormalBehavior> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalBehavior, req.getParameterMap());
        Page<MedicalFormalBehavior> page = new Page<MedicalFormalBehavior>(pageNo, pageSize);
        IPage<MedicalFormalBehavior> pageList = medicalFormalBehaviorService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @AutoLog(value = "不合规行为配置-按照特定排序显示")
    @ApiOperation(value = "不合规行为配置-按照特定排序显示", notes = "不合规行为配置-按照特定排序显示")
    @GetMapping(value = "/listForCol")
    public Result<?> listForCol(@RequestParam(name = "batchId") String batchId, @RequestParam(name = "type") String type, HttpServletRequest req) {
        List<MedicalFormalBehavior> list = medicalFormalBehaviorService.listByOrder(batchId, type);
        return Result.ok(list);
    }


    /**
     * 添加
     *
     * @param medicalFormalBehavior
     * @return
     */
    @AutoLog(value = "不合规行为配置-添加")
    @ApiOperation(value = "不合规行为配置-添加", notes = "不合规行为配置-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalFormalBehaviorVO medicalFormalBehaviorVO) {
        //medicalFormalBehaviorService.save(medicalFormalBehavior);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        medicalFormalBehaviorVO.setId(IdUtils.uuid());
        medicalFormalBehaviorVO.setCreateTime(new Date());
        medicalFormalBehaviorVO.setCreateUserid(user.getId());
        medicalFormalBehaviorVO.setCreateUsername(user.getRealname());
        //保存不合规行为关联信息

        medicalFormalBehaviorService.saveBehaviorAndCaseBehavior(medicalFormalBehaviorVO);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalFormalBehavior
     * @return
     */
    @AutoLog(value = "不合规行为配置-编辑")
    @ApiOperation(value = "不合规行为配置-编辑", notes = "不合规行为配置-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalFormalBehaviorVO medicalFormalBehaviorVO) {
        //medicalFormalBehaviorService.updateById(medicalFormalBehavior);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        medicalFormalBehaviorVO.setUpdateTime(new Date());
        medicalFormalBehaviorVO.setUpdateUserid(user.getId());
        medicalFormalBehaviorVO.setUpdateUsername(user.getRealname());
        medicalFormalBehaviorService.updateBehaviorAndCaseBehavior(medicalFormalBehaviorVO);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不合规行为配置-通过id删除")
    @ApiOperation(value = "不合规行为配置-通过id删除", notes = "不合规行为配置-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        //medicalFormalBehaviorService.removeById(id);
        medicalFormalBehaviorService.removeBehaviorAndCaseBehaviorById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "不合规行为配置-批量删除")
    @ApiOperation(value = "不合规行为配置-批量删除", notes = "不合规行为配置-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        //this.medicalFormalBehaviorService.removeByIds(Arrays.asList(ids.split(",")));
        this.medicalFormalBehaviorService.removeBehaviorAndCaseBehaviorByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    @AutoLog(value = "不合规行为-通过批次ID和模型ID查询不合规行为")
    @ApiOperation(value = "不合规行为-通过批次ID和模型ID查询不合规行为", notes = "通过批次ID和模型ID查询不合规行为-通过id查询业务模型关联记录")
    @GetMapping(value = "/queryByBatchCase")
    public Result<?> queryByBatchCase(@RequestParam(name = "batchId") String batchId,@RequestParam(name = "caseId") String caseId) {
        List<MedicalFormalBehavior> list = medicalFormalBehaviorService.selectByBatchCase(batchId,caseId.split(","));
        return Result.ok(list);
    }

    /**
     * 通过id查询不合规行为模型关联记录
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不合规行为-通过id查询业务模型关联记录")
    @ApiOperation(value = "不合规行为-通过id查询业务模型关联记录", notes = "不合规行为-通过id查询业务模型关联记录")
    @GetMapping(value = "/queryCaseBehaviorListById")
    public Result<?> queryCaseBehaviorListById(@RequestParam(name = "id", required = true) String id) {
        QueryWrapper<MedicalFormalCaseBehavior> queryWrapper = new QueryWrapper<MedicalFormalCaseBehavior>();
        queryWrapper.eq("BEHAVIOR_ID", id);
        List<MedicalFormalCaseBehavior> list = medicalFormalCaseBehaviorService.list(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 通过batchId查询不合规行为记录
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不合规行为-通过batchId查询不合规行为记录")
    @ApiOperation(value = "不合规行为-通过batchId查询不合规行为记录", notes = "不合规行为-通过batchId查询不合规行为记录")
    @GetMapping(value = "/queryCaseBehaviorListByBatchId")
    public Result<?> queryCaseBehaviorListByBatchId(@RequestParam(name = "batchId", required = true) String batchId) {
        QueryWrapper<MedicalFormalBehavior> queryWrapper = new QueryWrapper<MedicalFormalBehavior>();
        queryWrapper.eq("BATCH_ID", batchId);
        List<MedicalFormalBehavior> list = medicalFormalBehaviorService.list(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 根据历史批次导入
     *
     * @param medicalFormalBehavior
     * @return
     */
    @AutoLog(value = "不合规行为配置- 根据历史批次导入")
    @ApiOperation(value = "不合规行为配置- 根据历史批次导入", notes = "不合规行为配置- 根据历史批次导入")
    @PostMapping(value = "/importByBatchId")
    public Result<?> importByBatchId(@RequestBody JSONObject obj) {
        //medicalFormalBehaviorService.save(medicalFormalBehavior);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		medicalFormalBehaviorVO.setCreateTime(new Date());
//		medicalFormalBehaviorVO.setCreateUserid(user.getId());
//		medicalFormalBehaviorVO.setCreateUsername(user.getRealname());
        //根据历史批次导入复制不合规行为配置
        medicalFormalBehaviorService.importByBatchId(obj, user);
        return Result.ok("添加成功！");
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalFormalBehavior
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalFormalBehavior medicalFormalBehavior) {
        return super.exportXls(request, medicalFormalBehavior, MedicalFormalBehavior.class, "不合规行为配置");
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
        return super.importExcel(request, response, MedicalFormalBehavior.class);
    }

}
