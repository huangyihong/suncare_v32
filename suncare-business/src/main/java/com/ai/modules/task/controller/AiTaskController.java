package com.ai.modules.task.controller;

import com.ai.modules.task.entity.AiTask;
import com.ai.modules.task.service.IAiTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.Map;

 /**
 * @Description: AI任务表
 * @Author: jeecg-boot
 * @Date:   2022-02-28
 * @Version: V1.0
 */
@Slf4j
@Api(tags="AI任务表")
@RestController
@RequestMapping("/task/aiTask")
public class AiTaskController extends JeecgController<AiTask, IAiTaskService> {
	@Autowired
	private IAiTaskService aiTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param aiTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "AI任务表-分页列表查询")
	@ApiOperation(value="AI任务表-分页列表查询", notes="AI任务表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(AiTask aiTask,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		QueryWrapper<AiTask> queryWrapper = QueryGenerator.initQueryWrapper(aiTask, req.getParameterMap());
		queryWrapper.eq("DATA_SOURCE",user.getDataSource());
		Page<AiTask> page = new Page<AiTask>(pageNo, pageSize);
		IPage<AiTask> pageList = aiTaskService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param aiTask
	 * @return
	 */
	@AutoLog(value = "AI任务表-添加")
	@ApiOperation(value="AI任务表-添加", notes="AI任务表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody AiTask aiTask) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		aiTask.setDataSource(user.getDataSource());
		aiTaskService.save(aiTask);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param aiTask
	 * @return
	 */
	@AutoLog(value = "AI任务表-编辑")
	@ApiOperation(value="AI任务表-编辑", notes="AI任务表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody AiTask aiTask) {
		aiTaskService.updateById(aiTask);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "AI任务表-通过id删除")
	@ApiOperation(value="AI任务表-通过id删除", notes="AI任务表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		aiTaskService.removeById(id);
		return Result.ok("删除成功!");
	}

	 /**
	  * 通过id修改状态
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "AI任务表-通过id修改状态")
	 @ApiOperation(value="AI任务表-通过id修改状态", notes="AI任务表-通过id修改状态")
	 @RequestMapping(value = "/updateStatus")
	 public Result<?> updateStatus(@RequestParam(name="id",required=true) String id,@RequestParam(name="status",required=true) String status) {
		 AiTask aiTask = aiTaskService.getById(id);
		 if(aiTask==null){
			 return Result.error("参数异常");
		 }
		 aiTask.setStatus(status);
		 aiTaskService.updateById(aiTask);
		 return Result.ok("操作成功!");
	 }

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "AI任务表-批量删除")
	@ApiOperation(value="AI任务表-批量删除", notes="AI任务表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.aiTaskService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "AI任务表-通过id查询")
	@ApiOperation(value="AI任务表-通过id查询", notes="AI任务表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		AiTask aiTask = aiTaskService.getById(id);
		return Result.ok(aiTask);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param aiTask
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, AiTask aiTask) {
      return super.exportXls(request, aiTask, AiTask.class, "AI任务表");
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
      return super.importExcel(request, response, AiTask.class);
  }


 @AutoLog(value = "AI任务保存或者修改")
 @ApiOperation(value = "AI任务保存或者修改", notes = "AI任务保存或者修改")
 @PostMapping(value = "/saveOrUpdateAiTask")
 public Result<?> saveOrUpdateAiTask(@RequestBody AiTask aiTask) {
     LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
     if(StringUtils.isNotBlank(aiTask.getId())){
         aiTaskService.updateById(aiTask);
     }else{
         aiTaskService.save(aiTask);
     }
     Map<String,Object> data = new HashMap<>();
     data.put("task",aiTask);
     return  Result.ok(data);
 }

}
