package com.ai.modules.dcmapping.controller;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.dcmapping.entity.DcMappingTask;
import com.ai.modules.dcmapping.service.IDcMappingTaskService;
import com.alibaba.fastjson.JSONObject;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

 /**
 * @Description: 采集映射任务信息表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="采集映射任务信息表")
@RestController
@RequestMapping("/dcmapping/dcMappingTask")
public class DcMappingTaskController extends JeecgController<DcMappingTask, IDcMappingTaskService> {
	@Autowired
	private IDcMappingTaskService dcMappingTaskService;

	@Value("${mappingtask.url:http://localhost:8080}")
	private String mappingtaskUrl;

	/**
	 * 分页列表查询
	 *
	 * @param dcMappingTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "采集映射任务信息表-分页列表查询")
	@ApiOperation(value="采集映射任务信息表-分页列表查询", notes="采集映射任务信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DcMappingTask dcMappingTask,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DcMappingTask> queryWrapper = QueryGenerator.initQueryWrapper(dcMappingTask, req.getParameterMap());
		Page<DcMappingTask> page = new Page<DcMappingTask>(pageNo, pageSize);
		IPage<DcMappingTask> pageList = dcMappingTaskService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param dcMappingTask
	 * @return
	 */
	@AutoLog(value = "采集映射任务信息表-添加")
	@ApiOperation(value="采集映射任务信息表-添加", notes="采集映射任务信息表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DcMappingTask dcMappingTask) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		dcMappingTask.setCreatedBy(user.getUsername());
		dcMappingTask.setCreatedByName(user.getRealname());
		dcMappingTask.setCreatedTime(new Date());
		dcMappingTaskService.save(dcMappingTask);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param dcMappingTask
	 * @return
	 */
	@AutoLog(value = "采集映射任务信息表-编辑")
	@ApiOperation(value="采集映射任务信息表-编辑", notes="采集映射任务信息表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DcMappingTask dcMappingTask) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		dcMappingTask.setUpdatedBy(user.getUsername());
		dcMappingTask.setUpdatedByName(user.getRealname());
		dcMappingTask.setUpdatedTime(new Date());
		dcMappingTaskService.updateById(dcMappingTask);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "采集映射任务信息表-通过id删除")
	@ApiOperation(value="采集映射任务信息表-通过id删除", notes="采集映射任务信息表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		dcMappingTaskService.deleteByIds(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "采集映射任务信息表-批量删除")
	@ApiOperation(value="采集映射任务信息表-批量删除", notes="采集映射任务信息表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		dcMappingTaskService.deleteByIds(ids);
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "采集映射任务信息表-通过id查询")
	@ApiOperation(value="采集映射任务信息表-通过id查询", notes="采集映射任务信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DcMappingTask dcMappingTask = dcMappingTaskService.getById(id);
		return Result.ok(dcMappingTask);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param dcMappingTask
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, DcMappingTask dcMappingTask) {
      return super.exportXls(request, dcMappingTask, DcMappingTask.class, "采集映射任务信息表");
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
      return super.importExcel(request, response, DcMappingTask.class);
  }

	 /**
	  * 重跑前修改状态操作
	  *
	  * @param taskId
	  * @return
	  */
	 @AutoLog(value = "重跑前修改状态操作")
	 @ApiOperation(value="重跑前修改状态操作", notes="重跑前修改状态操作")
	 @GetMapping(value = "/beforeRunTask")
	 public Result<?> beforeRunTask(@RequestParam(name="taskId",required=true) String taskId) throws Exception {
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 String url = mappingtaskUrl;

		 if (StringUtils.isBlank(url)) {
			 return Result.error("未配置映射任务接口地址");
		 }
		 DcMappingTask dcMappingTask = dcMappingTaskService.getById(taskId);
		 if (dcMappingTask == null) {
			 return Result.error("参数异常");
		 }
		 //修改状态任务
		 dcMappingTask.setTaskStatus("4");
		 dcMappingTask.setRemark("等待识别");
		 dcMappingTask.setUpdatedBy(user.getUsername());
		 dcMappingTask.setUpdatedByName(user.getRealname());
		 dcMappingTask.setUpdatedTime(new Date());
		 dcMappingTaskService.updateById(dcMappingTask);
		 return Result.ok();
	 }

	 /**
	  * 映射任务重跑操作
	  *
	  * @param taskId
	  * @return
	  */
	 @AutoLog(value = "映射任务重跑操作")
	 @ApiOperation(value="映射任务重跑操作", notes="映射任务重跑操作")
	 @GetMapping(value = "/runTask")
	 public Result<?> runTask(@RequestParam(name="taskId",required=true) String taskId) throws Exception {
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 String url =mappingtaskUrl;

		 if(StringUtils.isBlank(url)){
			 return Result.error("未配置映射任务接口地址");
		 }

		 Map<String,String> busiParams = new HashMap<String,String>();
		 busiParams.put("taskid", taskId);
		 String responseBody = "";
		 try{
			 responseBody = ApiOauthUtil.doGet(url,"/runtask", busiParams, false);
		 }catch (Exception e){
			 return Result.error("请求映射任务接口失败");
		 }
		 JSONObject jsonObject = JSONObject.parseObject(responseBody);
		 if((boolean)(jsonObject.get("success"))){
			 return Result.ok(jsonObject.get("msg"));
		 }else{
			 return Result.error((String)jsonObject.get("msg"));
		 }
	 }


}
