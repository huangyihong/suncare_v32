package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalImportTaskService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 不合规行为字典
 * @Author: jeecg-boot
 * @Date:   2021-03-31
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合规行为字典")
@RestController
@RequestMapping("/config/medicalActionDict")
public class MedicalActionDictController extends JeecgController<MedicalActionDict, IMedicalActionDictService> {
	@Autowired
	private IMedicalActionDictService medicalActionDictService;

	@Autowired
	IMedicalImportTaskService importTaskService;


	 /**
	  * 通过codes查询
	  *
	  * @param codes
	  * @return
	  */
	 @AutoLog(value = "医疗项目组-通过code查询")
	 @ApiOperation(value="医疗项目组-通过code查询", notes="医疗项目组-通过code查询")
	 @GetMapping(value = "/queryByCodes")
	 public Result<?> queryByCodes(@RequestParam(name="codes") String codes) {
		 List<String> codeList = Arrays.asList(codes.split(","));
		 List<MedicalActionDict> list = medicalActionDictService.list(new QueryWrapper<MedicalActionDict>().in("ACTION_ID",codeList));
		 return Result.ok(list);
	 }

	 @AutoLog(value = "医疗项目组-获取基础信息列表")
	 @ApiOperation(value="医疗项目组-获取基础信息列表", notes="医疗项目组-获取基础信息列表")
	 @GetMapping(value = "/listBase")
	 public Result<?> listBase() {
		 List<MedicalActionDict> list = medicalActionDictService.list(
		 		new QueryWrapper<MedicalActionDict>().select("ACTION_ID", "ACTION_NAME"));
		 return Result.ok(list);
	 }

	 @AutoLog(value = "医疗项目组-通过ACTION_ID查询")
	 @ApiOperation(value="医疗项目组-通过ACTION_ID查询", notes="医疗项目组-通过ACTION_ID查询")
	 @GetMapping(value = "/queryByActionId")
	 public Result<?> queryByActionId(@RequestParam(name="actionId") String actionId) {
		 MedicalActionDict medicalActionDict = medicalActionDictService.getOne(new QueryWrapper<MedicalActionDict>().eq("ACTION_ID",actionId));
		 return Result.ok(medicalActionDict);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param medicalActionDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "不合规行为字典-分页列表查询")
	@ApiOperation(value="不合规行为字典-分页列表查询", notes="不合规行为字典-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalActionDict medicalActionDict,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalActionDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalActionDict, req.getParameterMap());
		queryWrapper.ne("STATUS","-1");
		Page<MedicalActionDict> page = new Page<MedicalActionDict>(pageNo, pageSize);
		IPage<MedicalActionDict> pageList = medicalActionDictService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalActionDict
	 * @return
	 */
	@AutoLog(value = "不合规行为字典-添加")
	@ApiOperation(value="不合规行为字典-添加", notes="不合规行为字典-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalActionDict medicalActionDict) {
		boolean flag = this.medicalActionDictService.isExistName(medicalActionDict);
		if(flag){
			return Result.error("新增失败，该不合规行为名称已存在！");
		}
		flag = this.medicalActionDictService.isExistCode(medicalActionDict);
		if(flag){
			return Result.error("新增失败，该不合规行为编码已存在！");
		}
		medicalActionDict.setStatus("1");
		medicalActionDictService.save(medicalActionDict);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalActionDict
	 * @return
	 */
	@AutoLog(value = "不合规行为字典-编辑")
	@ApiOperation(value="不合规行为字典-编辑", notes="不合规行为字典-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalActionDict medicalActionDict) {
		boolean flag = this.medicalActionDictService.isExistName(medicalActionDict);
		if(flag){
			return Result.error("修改失败，该不合规行为名称已存在！");
		}
		medicalActionDictService.updateById(medicalActionDict);
		//修改名称 -> 合规规则和模型中用到的名称同步
		medicalActionDictService.updateMedicalActionDict(medicalActionDict);

		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合规行为字典-通过id删除")
	@ApiOperation(value="不合规行为字典-通过id删除", notes="不合规行为字典-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		//medicalActionDictService.removeById(id);
		MedicalActionDict medicalActionDict = medicalActionDictService.getById(id);
		medicalActionDict.setStatus("-1");
		medicalActionDictService.updateById(medicalActionDict);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不合规行为字典-批量删除")
	@ApiOperation(value="不合规行为字典-批量删除", notes="不合规行为字典-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		//this.medicalActionDictService.removeByIds(Arrays.asList(ids.split(",")));
		List<MedicalActionDict> list = (List<MedicalActionDict>) this.medicalActionDictService.listByIds(Arrays.asList(ids.split(",")));
		for(MedicalActionDict medicalActionDict:list){
			medicalActionDict.setStatus("-1");
		}
		medicalActionDictService.saveOrUpdateBatch(list);
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合规行为字典-通过id查询")
	@ApiOperation(value="不合规行为字典-通过id查询", notes="不合规行为字典-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalActionDict medicalActionDict = medicalActionDictService.getById(id);
		return Result.ok(medicalActionDict);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalActionDict
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalActionDict medicalActionDict) {
      return super.exportXls(request, medicalActionDict, MedicalActionDict.class, "不合规行为字典");
  }


 /**
  * 生成不合规行为编码
  * @return
  */
 @AutoLog(value = "不合规行为字典-生成不合规行为编码")
 @ApiOperation(value="不合规行为字典-生成不合规行为编码", notes="不合规行为字典-生成不合规行为编码")
 @GetMapping(value = "/getMaxCode")
 public Result<?> getMaxCode() throws Exception {
	 int codeMax = medicalActionDictService.getMaxCode();
	 return Result.ok("bhgxw-"+String.format("%04d", codeMax+1));
 }

 /**
  * 通过id和status启用禁用
  *
  * @param id
  * @return
  */
 @AutoLog(value = "不合规行为字典-通过id和status启用禁用")
 @ApiOperation(value="不合规行为字典-通过id和status启用禁用", notes="不合规行为字典-通过id和status启用禁用")
 @GetMapping(value = "/updateStatus")
 public Result<?> updateStatus(@RequestParam(name="id",required=true) String id,@RequestParam(name="status",required=true) String status) {
	 MedicalActionDict medicalActionDict = medicalActionDictService.getById(id);
	 medicalActionDict.setStatus(status);
	 medicalActionDictService.updateById(medicalActionDict);
	 return Result.ok("操作成功!");
 }
 /**
  * 通过excel导入数据
  *
  * @param request
  * @param response
  * @return
  */
 @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
 public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception{
	 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
	 MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
	 Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
	 for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
		 MultipartFile file = entity.getValue();// 获取上传文件对象
		 // 判断文件名是否为空
		 if (file == null) {
			 return Result.error("上传文件为空");
		 }
		 // 获取文件名
		 String name = file.getOriginalFilename();
		 // 判断文件大小、即名称
		 long size = file.getSize();
		 if (name == null || ("").equals(name) && size == 0) {
			 return Result.error("上传文件内容为空");
		 }
		 return importTaskService.saveImportTask("MEDICAL_ACTION_DICT","不合规行为字典导入",file,user,
				 (f,u)->{
					 try {
						 return this.medicalActionDictService.importExcel(f,u);
					 } catch (Exception e) {
						 e.printStackTrace();
						 return Result.error(e.getMessage());
					 }
				 });
	 }
	 return Result.error("上传文件为空");
 }

 /**
  * 直接导出excel
  *
  * @param req
  * @param response
  * @param medicalActionDict
  * @throws Exception
  */
 @RequestMapping(value = "/exportExcel")
 public void exportExcel(HttpServletRequest req, HttpServletResponse response, MedicalActionDict medicalActionDict) throws Exception {
	 Result<?> result = new Result<>();
	 String title = req.getParameter("title");
	 if (StringUtils.isBlank(title)) {
		 title = "不合规行为_导出";
	 }
	 //response.reset();
	 response.setContentType("application/octet-stream; charset=utf-8");
	 response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
	 try {
		 OutputStream os = response.getOutputStream();
		 // 选中数据
		 String selections = req.getParameter("selections");
		 if (StringUtils.isNotEmpty(selections)) {
			 medicalActionDict.setId(selections);
		 }
		 QueryWrapper<MedicalActionDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalActionDict, req.getParameterMap());
		 queryWrapper.ne("STATUS","-1");
		 List<MedicalActionDict> list = medicalActionDictService.list(queryWrapper);
		 String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		 medicalActionDictService.exportExcel(list, os, suffix);
	 } catch (Exception e) {
		 throw e;
	 }
 }

}
