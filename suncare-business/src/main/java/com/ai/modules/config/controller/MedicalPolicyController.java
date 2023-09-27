package com.ai.modules.config.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExcelTool;
import com.ai.modules.config.entity.MedicalPolicy;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IMedicalPolicyService;
import com.ai.modules.config.vo.MedicalPolicyQuery;
import com.ai.modules.engine.util.SolrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

 /**
 * @Description: 新版本政策法规
 * @Author: jeecg-boot
 * @Date:   2021-08-04
 * @Version: V1.0
 */
@Slf4j
@Api(tags="新版本政策法规")
@RestController
@RequestMapping("/config/medicalPolicy")
public class MedicalPolicyController extends JeecgController<MedicalPolicy, IMedicalPolicyService> {
	@Autowired
	private IMedicalPolicyService medicalPolicyService;
	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalPolicy
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-分页列表查询")
	@ApiOperation(value="新版本政策法规-分页列表查询", notes="新版本政策法规-分页列表查询")
	@RequestMapping(value = "/list", method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> queryPageList(MedicalPolicyQuery medicalPolicy,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
//		QueryWrapper<MedicalPolicy> queryWrapper = QueryGenerator.initQueryWrapper(medicalPolicy, req.getParameterMap());
		Page<MedicalPolicy> page = new Page<MedicalPolicy>(pageNo, pageSize);



		SolrQuery solrQuery = SolrQueryGenerator.initQuery(medicalPolicy, req.getParameterMap());



		  // 设定查询字段
        solrQuery.setStart(((Long) ((page.getCurrent() - 1) * page.getSize())).intValue());
        solrQuery.setRows(((Long) page.getSize()).intValue());

        SolrClient solrClient = SolrUtil.getSolrClient("MEDICAL_POLICY", "default",false);

        try {

	        QueryResponse queryResponse = solrClient.query(solrQuery, METHOD.POST);
	        SolrDocumentList documents = queryResponse.getResults();
	        List<MedicalPolicy> result = new ArrayList<>();

	        Map<String, String> FIELD_MAPPING = SolrUtil.initFieldMap(MedicalPolicyQuery.class);
	        // 获取第一个类型参数的真实类型
	        for (SolrDocument doc : documents) {
	        	MedicalPolicy bean = SolrUtil.solrDocumentToPojo(doc, MedicalPolicy.class, FIELD_MAPPING);
	            result.add(bean);
	        }
	        page.setRecords(result);
	        page.setTotal(documents.getNumFound());
        } catch (Exception e) {
			log.error("",e);
		}
        return Result.ok(page);

		//IPage<MedicalPolicy> pageList = medicalPolicyService.page(page, queryWrapper);
		//return Result.ok(pageList);
	}


	/**
	 * 分页列表查询
	 *
	 * @param medicalPolicy
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-查询文档内容")
	@ApiOperation(value="新版本政策法规-查询文档内容", notes="新版本政策法规-查询文档内容")
	@GetMapping(value = "/queryDocContent")
	public Result<?> queryDocContent(String id ,String searchContent,
								   HttpServletRequest req) {
		try {
			List<Map<String ,Object>> list = medicalPolicyService.queryPolicyDocContent(
					id,searchContent);

			if(list==null) {
				list=new ArrayList<Map<String ,Object>>();
			}

			return Result.ok(list);
		} catch (Exception e) {
			String msg = e.getCause()==null ? e.getMessage() : e.getCause().getMessage();
			return Result.error(msg);
		}

	}


	/**
	 * 添加
	 *
	 * @param medicalPolicy
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-添加")
	@ApiOperation(value="新版本政策法规-添加", notes="新版本政策法规-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalPolicy medicalPolicy) {
		medicalPolicyService.save(medicalPolicy);

		//将记录添加到SOLR

		try {
			medicalPolicyService.saveDocToSolr(medicalPolicy);
		} catch (Exception e) {
			log.error("",e);
			return Result.error("添加失败！");
		}

		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalPolicy
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-编辑")
	@ApiOperation(value="新版本政策法规-编辑", notes="新版本政策法规-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalPolicy medicalPolicy) {
		medicalPolicyService.updateById(medicalPolicy);


		//将记录更新到SOLR

		try {
			MedicalPolicy newDoc = medicalPolicyService.getById(medicalPolicy.getId());
			medicalPolicyService.saveDocToSolr(newDoc);
		} catch (Exception e) {
			log.error("",e);
			return Result.error("添加失败！");
		}
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-通过id删除")
	@ApiOperation(value="新版本政策法规-通过id删除", notes="新版本政策法规-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalPolicyService.removeById(id);

		try {
			medicalPolicyService.deleteDocFromSolr(id);
		} catch (Exception e) {
			log.error("",e);
			return Result.error("删除失败！");
		}

		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-批量删除")
	@ApiOperation(value="新版本政策法规-批量删除", notes="新版本政策法规-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalPolicyService.removeByIds(Arrays.asList(ids.split(",")));

		try {
			String idArray[] = ids.split(",");

			for(String id :idArray) {
				medicalPolicyService.deleteDocFromSolr(id);
			}
		} catch (Exception e) {
			log.error("",e);
			return Result.error("删除失败！");
		}
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "新版本政策法规-通过id查询")
	@ApiOperation(value="新版本政策法规-通过id查询", notes="新版本政策法规-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalPolicy medicalPolicy = medicalPolicyService.getById(id);
		return Result.ok(medicalPolicy);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalPolicy
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalPolicy medicalPolicy) {
      return super.exportXls(request, medicalPolicy, MedicalPolicy.class, "新版本政策法规");
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

			try {
				return importTaskService.saveImportTask("MEDICAL_POLICY","新版本政策法规导入",file,user,
						(f,u)->{
							try {
								return this.medicalPolicyService.importExcel(f,u);
							} catch (Exception e) {
								e.printStackTrace();
								return Result.error(e.getMessage());
							}
						});
			} catch (Exception e) {
				log.error("", e);
				String msg = e.getCause() ==null ? e.getMessage() : e.getCause().getMessage();
				return Result.error("解析文件失败，政策编号，数据状态和更新标志为必填项，其余字段如无数据，请保持空白，不要填入数据或符号。\n" +
						"时间格式为：YYYY-MM-DD\n" +
						"请完全按照导入模板说明录入，详细错误信息：\n" + msg);
			}

		}
		return Result.error("上传文件为空");
  }


	 /**
	  * 直接导出excel
	  *
	  * @param req
	  * @param response
	  * @param medicalPolicy
	  * @throws Exception
	  */
	 @RequestMapping(value = "/exportExcel")
	 public void exportExcel(HttpServletRequest req, HttpServletResponse response, MedicalPolicy medicalPolicy) throws Exception {
		 Result<?> result = new Result<>();
		 String title = req.getParameter("title");
		 if (StringUtils.isBlank(title)) {
			 title = "政策法规_导出";
		 }
		 //response.reset();
		 response.setContentType("application/octet-stream; charset=utf-8");
		 response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
		 try {
			 OutputStream os = response.getOutputStream();
			 // 选中数据
			 String selections = req.getParameter("selections");
			 if (StringUtils.isNotEmpty(selections)) {
				 medicalPolicy.setId(selections);
			 }
			 QueryWrapper<MedicalPolicy> queryWrapper = QueryGenerator.initQueryWrapper(medicalPolicy , req.getParameterMap());
			 List<MedicalPolicy> list = medicalPolicyService.list(queryWrapper);

			 String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
			 medicalPolicyService.exportExcel(list, os, suffix);
		 } catch (Exception e) {
			 throw e;
		 }
	 }


	 /**
	  * 批量导入附件
	  *
	  * @param file
	  * @param response
	  * @return
	  */
	 @RequestMapping(value = "/importFiles", method = RequestMethod.POST)
	 public Result<?> importFiles(@RequestParam("file") MultipartFile file,HttpServletRequest req, HttpServletResponse response) {
		 //导入文件名示例：SD-JN-0001-ZW-【抚州市人民政府办公室关于印发抚州市医疗保险基金市级统收统支实施办法的通知】20201215
		 //此时政策编号为：SD-JN-0001-ZW
		 // 获取文件名
		 String fileName = file.getOriginalFilename();

		 String policyCode = getPolicyCodeFromFileName(fileName);

		 if("".equals(policyCode)) {
			 return Result.error("文件名不符合规范，政策编号取第四个-前面的值！");
		 }

		 try {

			 MedicalPolicy  bean = this.medicalPolicyService.getBeanByPolicyCode(policyCode);
			 if(bean == null){
				 return Result.error("政策法规编码不存在：" + policyCode);
			 }

			 //覆盖原来的文件 Filenames存放原文，TextFilename存放转换后的附件
			 String path = CommonUtil.upload(file,req.getParameter("bizPath"));
			 bean.setFilenames(path);


			 //存放到solr，前台查询的时候主要查SOLR，ORACLE只是一个备份
			 medicalPolicyService.saveDocToSolr(bean);

			 //保存到ORACLE数据库
			 this.medicalPolicyService.updateById(bean);

			 return Result.ok(path);
		 } catch (Exception e) {
			 e.printStackTrace();
			 return Result.error(e.getMessage());
		 }
	 }

	//导入文件名示例：SD-JN-0001-ZW-【抚州市人民政府办公室关于印发抚州市医疗保险基金市级统收统支实施办法的通知】20201215
	 //此时政策编号为：SD-JN-0001-ZW
	 private   String getPolicyCodeFromFileName(String fileName) {
		 //寻找第一个-
		 int index = fileName.indexOf("-");
		 if(index<0) {
			 return "";
		 }

		 //寻找第二个-
		 index = fileName.indexOf("-" ,index+1);
		 if(index<0) {
			 return "";
		 }

		//寻找第三个-
		 index = fileName.indexOf("-" ,index+1);
		 if(index<0) {
			 return "";
		 }
		//寻找第四个-
		 index = fileName.indexOf("-" ,index+1);
		 if(index<0) {
			 return "";
		 }

		 return fileName.substring(0,index);
	 }

}
