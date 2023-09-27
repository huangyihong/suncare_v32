package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalProjectGroup;
import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IMedicalProjectGroupService;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;

 /**
 * @Description: 医疗项目组
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗项目组")
@RestController
@RequestMapping("/config/medicalProjectGroup")
public class MedicalProjectGroupController extends JeecgController<MedicalProjectGroup, IMedicalProjectGroupService> {
	@Autowired
	private IMedicalProjectGroupService medicalProjectGroupService;

	@Autowired
	IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalProjectGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗项目组-分页列表查询")
	@ApiOperation(value="医疗项目组-分页列表查询", notes="医疗项目组-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalProjectGroup medicalProjectGroup,MedicalProjectGroupItem medicalProjectGroupItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception{
		QueryWrapper<MedicalProjectGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalProjectGroup, req.getParameterMap());
		getQueryWrapper(req,  medicalProjectGroupItem.getCode(), medicalProjectGroupItem.getValue(), queryWrapper);

		Page<MedicalProjectGroup> page = new Page<MedicalProjectGroup>(pageNo, pageSize);
		IPage<MedicalProjectGroup> pageList = medicalProjectGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 private void getQueryWrapper(HttpServletRequest req, String code, String value, QueryWrapper<MedicalProjectGroup> queryWrapper) throws Exception {
		 String inStr = "";
		 if (StringUtils.isNotEmpty(code)) {
			 inStr += "  and CODE like '" + code.replaceAll("\\*","%") + "'";
		 }
		 if (StringUtils.isNotEmpty(value)) {
			 inStr += "  and "+DbDataEncryptUtil.decryptFunc("VALUE")+" like '" + value.replaceAll("\\*","%") + "'";
		 }
		 if (inStr.length() > 0) {
			 queryWrapper.exists("SELECT 1 FROM MEDICAL_PROJECT_GROUP_ITEM a where MEDICAL_PROJECT_GROUP.GROUP_ID=a.GROUP_ID" + inStr);
		 }
		 queryWrapper = MedicalAuditLogConstants.queryTime(queryWrapper,req);
	}

	 /**
	  * 通过code查询
	  *
	  * @param code
	  * @return
	  */
	 @AutoLog(value = "医疗项目组-通过code查询")
	 @ApiOperation(value="医疗项目组-通过code查询", notes="医疗项目组-通过code查询")
	 @GetMapping(value = "/queryByCode")
	 public Result<?> queryByCode(@RequestParam(name="code") String code) {
		 MedicalProjectGroup medicalProjectGroup = medicalProjectGroupService.getOne(new QueryWrapper<MedicalProjectGroup>().eq("GROUP_CODE",code));
		 return Result.ok(medicalProjectGroup);
	 }

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
		 if(codeList.size() == 1){
			 MedicalProjectGroup group = medicalProjectGroupService.getOne(new QueryWrapper<MedicalProjectGroup>().eq("GROUP_CODE",codes));
			 return Result.ok(Collections.singletonList(group));
		 }
		 List<MedicalProjectGroup> list = medicalProjectGroupService.list(new QueryWrapper<MedicalProjectGroup>().in("GROUP_CODE",codeList));
		 return Result.ok(list);
	 }



	 /**
	 * 添加
	 *
	 * @param medicalProjectGroup
	 * @return
	 */
	@AutoLog(value = "医疗项目组-添加")
	@ApiOperation(value="医疗项目组-添加", notes="医疗项目组-添加")
	@PostMapping(value = "/add")
	public Result<?> add(MedicalProjectGroup medicalProjectGroup,String codes, String names, String tableTypes) {
		medicalProjectGroupService.saveGroup(medicalProjectGroup, codes,  names, tableTypes);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalProjectGroup
	 * @return
	 */
	@AutoLog(value = "医疗项目组-编辑")
	@ApiOperation(value="医疗项目组-编辑", notes="医疗项目组-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(MedicalProjectGroup medicalProjectGroup,String codes, String names, String tableTypes) {
		medicalProjectGroupService.updateGroup(medicalProjectGroup, codes,  names, tableTypes);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗项目组-通过id删除")
	@ApiOperation(value="医疗项目组-通过id删除", notes="医疗项目组-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalProjectGroupService.removeGroupById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医疗项目组-批量删除")
	@ApiOperation(value="医疗项目组-批量删除", notes="医疗项目组-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalProjectGroupService.removeGroupByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗项目组-通过id查询")
	@ApiOperation(value="医疗项目组-通过id查询", notes="医疗项目组-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalProjectGroup medicalProjectGroup = medicalProjectGroupService.getById(id);
		return Result.ok(medicalProjectGroup);
	}

	 /**
	       * 导出excel
	   *
	   * @param request
	   * @param medicalProjectGroup
	   */
	  @RequestMapping(value = "/exportXls")
	  public ModelAndView exportXls(HttpServletRequest request, MedicalProjectGroup medicalProjectGroup) {
	      return super.exportXls(request, medicalProjectGroup, MedicalProjectGroup.class, "医疗项目组");
	  }

  /**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			return importTaskService.saveImportTask("MEDICAL_PROJECT_GROUP","项目组导入",file,user,
					(f,u)->{
						try {
							return this.medicalProjectGroupService.importExcel(f,u);
						} catch (Exception e) {
							e.printStackTrace();
							return Result.error(e.getMessage());
						}
					});

		}
		return Result.error("上传文件为空");
	}

	/**
	 * 导出excel
	 *
	 * @param req
	 * @param bean
	 * @throws Exception
	 */
    @AutoLog(value = "线程导出excel")
	@ApiOperation(value="线程导出excel", notes="线程导出excel")
	@RequestMapping(value = "/exportExcelByThread")
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalGroupVO bean, MedicalProjectGroup medicalProjectGroup) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "医疗服务项目组_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		final String tableName = req.getParameter("tableName");
//		int count = medicalProjectGroupService.queryGroupItemCount(bean);

		QueryWrapper<MedicalProjectGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalProjectGroup, req.getParameterMap());
		getQueryWrapper(req, bean.getCode(), bean.getValue(), queryWrapper);
		int count = medicalProjectGroupService.queryGroupItemCount2(queryWrapper);
		ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			Result exportResult = Result.ok();
			try {
//				List<MedicalGroupVO> list = medicalProjectGroupService.queryGroupItem(bean);
				List<MedicalGroupVO> list = medicalProjectGroupService.queryGroupItem2(queryWrapper);
				medicalProjectGroupService.exportExcel(list,os,suffix);
			} catch (Exception e) {
				e.printStackTrace();
				exportResult = Result.error(e.getMessage());
			}
			return exportResult;
		});

	    result.setMessage("等待导出，请在导出记录界面查看进度");
		return result;
	}

    /**
	 * 直接导出excel
	 *
	 * @param req
	 * @param bean
	 * @throws Exception
	 */
    @RequestMapping(value = "/exportExcel")
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalGroupVO bean, MedicalProjectGroup medicalProjectGroup) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "医疗服务项目组_导出";
		}
		//response.reset();
        try {

        	String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
//        	List<MedicalGroupVO> list = medicalProjectGroupService.queryGroupItem(bean);

			// 选中数据
			String selections = req.getParameter("selections");
			if (StringUtils.isNotEmpty(selections)) {
				medicalProjectGroup.setGroupId(selections);
			}

			QueryWrapper<MedicalProjectGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalProjectGroup, req.getParameterMap());
			getQueryWrapper(req, bean.getCode(), bean.getValue(), queryWrapper);
			List<MedicalGroupVO> list = medicalProjectGroupService.queryGroupItem2(queryWrapper);

        	response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename="+new String((title+"."+suffix).getBytes("UTF-8"),"iso-8859-1"));
        	OutputStream os =response.getOutputStream();
        	medicalProjectGroupService.exportExcel(list,os,suffix);
        } catch (Exception e) {
			throw e;
		}
	}

    /**
   	 * 判断code是否重复
   	 * @param request
   	 * @param groupCode
   	 * @param groupId
   	 * @return
   	 */
    @AutoLog(value = "医疗服务项目组信息-判断groupCode是否重复 ")
   	@ApiOperation(value="医疗服务项目组信息-判断groupCode是否重复 ", notes="医疗服务项目组信息-判断groupCode是否重复 ")
   	@GetMapping(value = "/isExistName")
  	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="groupCode",required=true)String groupCode,@RequestParam(name="groupName",required=true)String groupName,String groupId){
   	boolean flag = medicalProjectGroupService.isExistName(groupCode,groupName,groupId);
  		return Result.ok(flag);
  	}

	 /**
	  * 替换项目组
	  * @param request
	  * @param code
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "项目组信息-替换项目组")
	 @ApiOperation(value="项目组信息-替换项目组", notes="项目组信息-替换项目组")
	 @GetMapping(value = "/replaceData")
	 public Result<?> replaceData(HttpServletRequest request,@RequestParam(name="code",required=true)String code,@RequestParam(name="id",required=true)String id)throws Exception{
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 MedicalProjectGroup oldBean = medicalProjectGroupService.getById(id);//需要替换的数据
		 if(oldBean==null){
			 return Result.error("参数异常");
		 }

		 //调用替换逻辑
		/* AbsDictMergeHandleFactory factory = new DrugDictMergeHandleFactory(code,oldBean.getGroupCode());
		 List<DictMergeVO> result  = factory.merge();*/

		 //设置需要替换的失效  删除
		 //medicalProjectGroupService.removeGroupById(id);
		 return Result.ok("替换成功");
	 }
}
