package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.entity.MedicalDrugGroup;
import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.service.IMedicalDrugGroupService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.ai.modules.engine.util.HttpSimulator;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.solr.SolrDataSourceProperty;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;

 /**
 * @Description: 药品组
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品组")
@RestController
@RequestMapping("/config/medicalDrugGroup")
public class MedicalDrugGroupController extends JeecgController<MedicalDrugGroup, IMedicalDrugGroupService> {
	@Autowired
	private IMedicalDrugGroupService medicalDrugGroupService;

	@Autowired
	IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品组-分页列表查询")
	@ApiOperation(value="药品组-分页列表查询", notes="药品组-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugGroup medicalDrugGroup,MedicalDrugGroupItem medicalDrugGroupItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception{
		QueryWrapper<MedicalDrugGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugGroup, req.getParameterMap());
		getQueryWrapper(req, medicalDrugGroupItem.getCode(), medicalDrugGroupItem.getValue(), queryWrapper);
		Page<MedicalDrugGroup> page = new Page<MedicalDrugGroup>(pageNo, pageSize);
		IPage<MedicalDrugGroup> pageList = medicalDrugGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 private void getQueryWrapper(HttpServletRequest req, String code, String value, QueryWrapper<MedicalDrugGroup> queryWrapper) throws Exception {
		 String inStr = "";
		 if (StringUtils.isNotEmpty(code)) {
			 inStr += "  and CODE like '" + code.replaceAll("\\*","%") + "'";
		 }
		 if (StringUtils.isNotEmpty(value)) {
			 inStr += "  and "+DbDataEncryptUtil.decryptFunc("VALUE")+" like '" + value.replaceAll("\\*","%") + "'";
		 }
		 if (inStr.length() > 0) {
			 queryWrapper.exists("SELECT 1 FROM MEDICAL_DRUG_GROUP_ITEM a where MEDICAL_DRUG_GROUP.GROUP_ID=a.GROUP_ID" + inStr);
		 }
		 queryWrapper = MedicalAuditLogConstants.queryTime(queryWrapper,req);
	 }

	 /**
	  * 通过code查询
	  *
	  * @param code
	  * @return
	  */
	 @AutoLog(value = "药品组-通过code查询")
	 @ApiOperation(value="药品组-通过code查询", notes="药品组-通过code查询")
	 @GetMapping(value = "/queryByCode")
	 public Result<?> queryByCode(@RequestParam(name="code") String code) {
		 MedicalDrugGroup medicalDrugGroup = medicalDrugGroupService.getOne(new QueryWrapper<MedicalDrugGroup>().eq("GROUP_CODE",code));
		 return Result.ok(medicalDrugGroup);
	 }

	 /**
	  * 通过codes查询
	  *
	  * @param codes
	  * @return
	  */
	 @AutoLog(value = "药品组-通过code查询")
	 @ApiOperation(value="药品组-通过code查询", notes="药品组-通过code查询")
	 @GetMapping(value = "/queryByCodes")
	 public Result<?> queryByCodes(@RequestParam(name="codes") String codes) {
	 	 List<String> codeList = Arrays.asList(codes.split(","));
	 	 List<MedicalDrugGroup> list = medicalDrugGroupService.list(new QueryWrapper<MedicalDrugGroup>().in("GROUP_CODE",codeList));
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param medicalDrugGroup
	 * @return
	 */
	@AutoLog(value = "药品组-添加")
	@ApiOperation(value="药品组-添加", notes="药品组-添加")
	@PostMapping(value = "/add")
	public Result<?> add(MedicalDrugGroup medicalDrugGroup,String codes, String names, String tableTypes) {
		medicalDrugGroupService.saveGroup(medicalDrugGroup, codes,  names, tableTypes);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDrugGroup
	 * @return
	 */
	@AutoLog(value = "药品组-编辑")
	@ApiOperation(value="药品组-编辑", notes="药品组-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(MedicalDrugGroup medicalDrugGroup,String codes, String names, String tableTypes) {
		medicalDrugGroupService.updateGroup(medicalDrugGroup, codes,  names, tableTypes);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品组-通过id删除")
	@ApiOperation(value="药品组-通过id删除", notes="药品组-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDrugGroupService.removeGroupById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "药品组-批量删除")
	@ApiOperation(value="药品组-批量删除", notes="药品组-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDrugGroupService.removeGroupByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品组-通过id查询")
	@ApiOperation(value="药品组-通过id查询", notes="药品组-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugGroup medicalDrugGroup = medicalDrugGroupService.getById(id);
		return Result.ok(medicalDrugGroup);
	}

	 /**
	       * 导出excel
	   *
	   * @param request
	   * @param medicalDrugGroup
	   */
	  @RequestMapping(value = "/exportXls")
	  public ModelAndView exportXls(HttpServletRequest request, MedicalDrugGroup medicalDrugGroup) {
	      return super.exportXls(request, medicalDrugGroup, MedicalDrugGroup.class, "药品组");
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
			return importTaskService.saveImportTask("MEDICAL_DRUG_GROUP","药品组导入",file,user,
					(f,u)->{
						try {
							return this.medicalDrugGroupService.importExcel(f,u);
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
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalGroupVO bean,MedicalDrugGroup medicalDrugGroup) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "药品组_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		final String tableName = req.getParameter("tableName");
//		int count = medicalDrugGroupService.queryGroupItemCount(bean);

		QueryWrapper<MedicalDrugGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugGroup, req.getParameterMap());
		getQueryWrapper(req, bean.getCode(), bean.getValue(), queryWrapper);
		int count = medicalDrugGroupService.queryGroupItemCount2(queryWrapper);
		ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			Result exportResult = Result.ok();
			try {
//				List<MedicalGroupVO> list = medicalDrugGroupService.queryGroupItem(bean);
				List<MedicalGroupVO> list = medicalDrugGroupService.queryGroupItem2(queryWrapper);
				medicalDrugGroupService.exportExcel(list,os,suffix);
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
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalGroupVO bean,MedicalDrugGroup medicalDrugGroup) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "药品组_导出";
		}
		//response.reset();
        try {
        	String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
//        	List<MedicalGroupVO> list = medicalDrugGroupService.queryGroupItem(bean);
			// 选中数据
			String selections = req.getParameter("selections");
			if (StringUtils.isNotEmpty(selections)) {
				medicalDrugGroup.setGroupId(selections);
			}
			QueryWrapper<MedicalDrugGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugGroup, req.getParameterMap());
			getQueryWrapper(req, bean.getCode(), bean.getValue(), queryWrapper);
			List<MedicalGroupVO> list = medicalDrugGroupService.queryGroupItem2(queryWrapper);

        	response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename="+new String((title+"."+suffix).getBytes("UTF-8"),"iso-8859-1"));
        	OutputStream os =response.getOutputStream();

        	medicalDrugGroupService.exportExcel(list,os,suffix);
        } catch (Exception e) {
			throw e;
		}
	}

    /**
	  * 通过分类查询
	  *
	  * @param kinds
	  * @return
	  */
	 @AutoLog(value = "药品合规规则分组-通过kinds查询分组字典")
	 @ApiOperation(value = "药品合规规则分组-通过kinds查询分组字典", notes = "药品合规规则分组-通过kinds查询分组字典")
	 @GetMapping(value = "/queryGroupByKinds")
	 public Result<?> queryGroupByKinds(@RequestParam(name = "kinds", required = true) String kinds) {
		 Map<String, List<MedicalDict>> map = medicalDrugGroupService.queryGroupByKinds(kinds.trim().split(","));
		 return Result.ok(map);
	 }

	/**
	 * 判断code是否重复
	 * @param request
	 * @param groupCode
	 * @param groupId
	 * @return
	 */
   	@AutoLog(value = "药品组信息-判断groupCode是否重复 ")
	@ApiOperation(value="药品组信息-判断groupCode是否重复 ", notes="药品组信息-判断groupCode是否重复 ")
	@GetMapping(value = "/isExistName")
   	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="groupCode",required=true)String groupCode,@RequestParam(name="groupName",required=true)String groupName,String groupId){
    	boolean flag = medicalDrugGroupService.isExistName(groupCode,groupName,groupId);
   		return Result.ok(flag);
   	}

   	/**
	 * 同步solr
	 *
	 * @param tableName
	 * @return
	 */
	@AutoLog(value = "药品组/疾病组/项目组-同步solr")
	@ApiOperation(value="药品组/疾病组/项目组-同步solr", notes="药品组/疾病组/项目组-同步solr")
	@GetMapping(value = "/dataimportSolr")
	public Result<?> dataimportSolr(@RequestParam(name="tableName",required=true)String tableName) {
		try {
			String uuid=IdUtils.uuid();
			//String cmd="curl -v --data \"_="+uuid+"&clean=true&command=full-import\"  http://10.63.82.188:8983/solr/"+tableName+"/dataimport";
			//Runtime.getRuntime().exec(cmd);
		    //Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
			Map<String, String> params = new HashMap<String, String>();
			params.put("_", uuid);
			params.put("clean", "true");
			params.put("command", "full-import");
			params.put("tableName", tableName);

			Map<String, SolrDataSourceProperty> datasource = SolrUtil.dynamicSolrProperties.getDatasource();
			if(datasource == null|| datasource.size() == 0){
                return Result.error("系统未配置solr地址");
            }

			List<String> dataSourceList = ApiTokenUtil.getDataSources();

			//如果当前后台进程没有负责的数据源，则忽略
			if (dataSourceList.size() == 0) {
				return Result.error("当前进程没有关联的solr地址");
			}


			//如果当前是中心节点，则利用SOLR自带的同步插件同步
			if(ApiTokenUtil.IS_CENTER) {
				//遍历所有数据源
				 for (String key : datasource.keySet()) {
				 	//忽略默认的和不在当前负责的数据源节点
	                if("default".equalsIgnoreCase(key)  || !dataSourceList.contains(key)){
	                	continue;
	                }

	                //调用SOLR自带的插件进行同步
                	try{
                    	HttpSimulator.callApi(SolrUtil.getSolrUrl(datasource.get(key)), tableName+"/dataimport", params, "post");
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
			}
			else {
				//当前非中心节点，则使用下载文件再同步的方式进行SOLR同步
				String fileName = tableName +"_"+IdUtils.uuid()+ ".json";
				File file =new File(fileName);
				FileOutputStream outStream = new FileOutputStream(file);

				ApiOauthUtil.writeResultToStream("/oauth/api/queryStdDataForJson", params, outStream);
				outStream.close();

				if(!file.exists() || file.length()<10) {
					return Result.error("同步失败，没有查询到相关数据，忽略同步！");
				}


				 for (String key : datasource.keySet()) {
				 	//忽略默认的和不在当前负责的数据源节点
	                if("default".equalsIgnoreCase(key)  || !dataSourceList.contains(key)){
	                	continue;
	                }

	                if(!file.exists() || file.length()<10) {
	                	break;
	                }

	                //调用SOLR自带的插件进行同步
                	try{
                		String solrUrl = SolrUtil.getSolrUrl(datasource.get(key))+"/"+ tableName+"/update";
                		//先删除原先数据

            			SolrUtil.deleteSolrDataByPostTool(solrUrl, "*:*");


                		//再导入新的文件
                		SolrUtil.importJsonToSolr(file.getAbsolutePath(), solrUrl,false,true);
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }

			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		return Result.ok("同步成功!");
	}

	 /**
	  * 替换药品组
	  * @param request
	  * @param code
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "药品组信息-替换药品组")
	 @ApiOperation(value="药品组信息-替换药品组", notes="药品组信息-替换药品组")
	 @GetMapping(value = "/replaceData")
	 public Result<?> replaceData(HttpServletRequest request,@RequestParam(name="code",required=true)String code,@RequestParam(name="id",required=true)String id)throws Exception{
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 MedicalDrugGroup oldBean = medicalDrugGroupService.getById(id);//需要替换的数据
		 if(oldBean==null){
			 return Result.error("参数异常");
		 }

		 //调用替换逻辑
		/* AbsDictMergeHandleFactory factory = new DrugDictMergeHandleFactory(code,oldBean.getGroupCode());
		 List<DictMergeVO> result  = factory.merge();*/

		 //设置需要替换的失效  删除
		 //medicalDrugGroupService.removeGroupById(id);
		 return Result.ok("替换成功");
	 }

}
