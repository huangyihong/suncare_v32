package com.ai.modules.drg.controller;

import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.*;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.service.IDrgHandleService;
import com.ai.modules.drg.service.IDrgTaskService;
import com.ai.modules.drg.service.IMedicalVisitDrgService;
import com.ai.modules.drg.service.IVisitDrgService;
import com.ai.modules.drg.vo.*;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: drg任务表
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Slf4j
@Api(tags="drg任务表")
@RestController
@RequestMapping("/drg/drgTask")
public class DrgTaskController extends JeecgController<DrgTask, IDrgTaskService> {
	@Autowired
	private IDrgTaskService drgTaskService;

	@Autowired
	private IVisitDrgService visitDrgService;

	@Autowired
	private IYbChargeSearchTaskService ybChargeSearchTaskService;

	@Autowired
	private IDrgHandleService drgHandleService;

	@Autowired
	private IMedicalVisitDrgService medicalVisitDrgService;

	/**
	 * 分页列表查询
	 *
	 * @param drgTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "drg任务表-分页列表查询")
	@ApiOperation(value="drg任务表-分页列表查询", notes="drg任务表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DrgTask drgTask,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DrgTask> queryWrapper = QueryGenerator.initQueryWrapper(drgTask, req.getParameterMap());
		Page<DrgTask> page = new Page<DrgTask>(pageNo, pageSize);
		IPage<DrgTaskVo> pageList = drgTaskService.pageVO(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 分页列表查询
	 *
	 * @param bean
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "drg任务表-分页列表查询")
	@ApiOperation(value="drg任务表-分页列表查询", notes="drg任务表-分页列表查询")
	@GetMapping(value = "/visitDrgList")
	public Result<?> visitDrgList(VisitDrgVo bean,
								  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								  HttpServletRequest req) throws Exception{
		QueryWrapper<VisitDrgVo> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
		Page<VisitDrgVo> page = new Page<VisitDrgVo>(pageNo, pageSize);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String project = this.getProject(dbVO);

		String schema = getSchema(dbVO);
		IPage<VisitDrgVo> pageList = visitDrgService.visitDrgListPage(page, queryWrapper,project,schema,req.getParameter("batchId"));
		return Result.ok(pageList);
	}


	/**
	 * 分页列表查询
	 *
	 * @param bean
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "drg任务表-分页列表查询")
	@ApiOperation(value="drg任务表-分页列表查询", notes="drg任务表-分页列表查询")
	@GetMapping(value = "/visitNoDrgList")
	public Result<?> visitNoDrgList(VisitDrgVo bean,
								  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								  HttpServletRequest req) throws Exception{
		QueryWrapper<VisitDrgVo> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
		Page<VisitDrgVo> page = new Page<VisitDrgVo>(pageNo, pageSize);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String project = this.getProject(dbVO);

		String schema = getSchema(dbVO);

		//获取当前任务信息
		DrgTask drgTask = drgTaskService.getById(req.getParameter("taskId"));
		String orgIds = drgTask.getOrgids();
		if(StrUtil.isNotEmpty(orgIds)){
			orgIds = orgIds.replace("|",",");
			orgIds = orgIds.replace(",","','");
			orgIds = "'"+orgIds+"'";
		}
		drgTask.setOrgids(orgIds);
		IPage<VisitDrgVo> pageList = visitDrgService.visitNoDrgListPage(page, queryWrapper,project,schema,req.getParameter("batchId"),drgTask);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param drgTask
	 * @return
	 */
	@AutoLog(value = "drg任务表-添加")
	@ApiOperation(value="drg任务表-添加", notes="drg任务表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DrgTaskVo drgTask) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//批次号
		String batchId =  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String taskId = IdUtils.uuid();
		drgTask.setId(taskId);
		drgTask.setBatchId(batchId);
		drgTask.setDataSource(user.getDataSource());
		drgTask.setStatus(DrgCatalogConstants.TASK_INIT);
		this.drgTaskService.save(drgTask);
		try{
			if("1".equals(drgTask.getIsRun())){
				drgHandleService.execute(taskId);
			}
		}catch (Exception e){
			return Result.error("任务运行失败:"+e.getMessage());
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param drgTask
	 * @return
	 */
	@AutoLog(value = "drg任务表-编辑")
	@ApiOperation(value="drg任务表-编辑", notes="drg任务表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DrgTaskVo drgTask) {
		String taskId =drgTask.getId();
		drgTaskService.updateById(drgTask);
		try{
			if("1".equals(drgTask.getIsRun())){
				drgHandleService.execute(taskId);
			}
		}catch (Exception e){
			return Result.error("任务重跑失败:"+e.getMessage());
		}
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "drg任务表-通过id删除")
	@ApiOperation(value="drg任务表-通过id删除", notes="drg任务表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		DrgTask drgTask = drgTaskService.getById(id);
		String batchId = drgTask.getBatchId();
		drgTaskService.removeById(id);
		//刪除dp中改批次的结果数据
		visitDrgService.deleteVisitDrgByBatchId(batchId);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "drg任务表-批量删除")
	@ApiOperation(value="drg任务表-批量删除", notes="drg任务表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.drgTaskService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "drg任务表-通过id查询")
	@ApiOperation(value="drg任务表-通过id查询", notes="drg任务表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DrgTask drgTask = drgTaskService.getById(id);
		return Result.ok(drgTask);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param drgTask
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, DrgTask drgTask) {
      return super.exportXls(request, drgTask, DrgTask.class, "drg任务表");
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
      return super.importExcel(request, response, DrgTask.class);
  }


    private  static String[]  visitDrgTitleArr = new String[]{"就诊ID", "医保住院登记号","医疗机构名称","患者姓名","医保个人编号","性别","出生时间","参保类型","入院日期","出院日期",
		  	"主诊断编码","主诊断名称","主要手术和操作编码","主要手术和操作名称","DRG目录编码","DRG目录名称","ADRG目录编码","ADRG目录名称","MDC目录编码","MDC目录名称"};
	private  static String[]  visitDrgFieldArr = new String[]{"visitid", "case_id","orgname","clientname","insurancecard_no","sex","birthday","insurancetype","admitdate","leavedate",
			"drg_diag_code","drg_diag_name","drg_surgery_code","drg_surgery_name","drg","drg_name","adrg","adrg_name","mdc","mdc_name"};
	private  static String[]  visitNoDrgTitleArr = new String[]{"就诊ID", "医保住院登记号","医疗机构名称","患者姓名","医保个人编号","性别","出生时间","参保类型","入院日期","出院日期",
			"主诊断编码","主诊断名称","主要手术和操作编码","主要手术和操作名称","ADRG目录编码","ADRG目录名称","MDC目录编码","MDC目录名称"};
	private  static String[]  visitNoDrgFieldArr =  new String[]{"visitid", "case_id","orgname","clientname","insurancecard_no","sex","birthday","insurancetype","admitdate","leavedate",
			"drg_diag_code","drg_diag_name","drg_surgery_code","drg_surgery_name","adrg","adrg_name","mdc","mdc_name"};

	@AutoLog(value = "drg入组病历-直接导出")
	@ApiOperation(value="drg入组病历-直接导出", notes="drg入组病历-直接导出")
	@GetMapping(value = "/exportVisitDrgList")
	public void exportVisitDrgList(VisitDrgVo bean,
								   HttpServletRequest req,
								   HttpServletResponse response) throws Exception {
		QueryWrapper<VisitDrgVo> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String project = this.getProject(dbVO);

		String schema = getSchema(dbVO);
		String title = "drg入组病历" + System.currentTimeMillis();
		String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		//response.reset();
		response.setContentType("application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "." + suffix).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

		OutputStream os = response.getOutputStream();
		String[]  titleArr = visitDrgTitleArr;
		String[]  fieldArr = Arrays.stream(visitDrgFieldArr).map(t-> StringCamelUtils.underline2Camel(t)).collect(Collectors.toList()).toArray(new String[visitDrgFieldArr.length]);
		List<VisitDrgVo> dataList =visitDrgService.visitDrgList(queryWrapper,project,schema,req.getParameter("batchId"));
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		ExportXUtils.exportExl(dataList, VisitDrgVo.class,titleArr,fieldArr,workbook,"数据");
		workbook.write(os);
		workbook.dispose();


	}

	@AutoLog(value = "drg入组病历-线程导出")
	@ApiOperation(value="drg入组病历-线程导出", notes="drg入组病历-线程导出")
	@GetMapping(value = "/exportVisitDrgListByThread")
	public Result<?> exportVisitDrgListByThread(VisitDrgVo bean,
								   HttpServletRequest req,
								   HttpServletResponse response) throws Exception {
		Result<?> result = new Result<>();
		QueryWrapper<VisitDrgVo> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String project = this.getProject(dbVO);

		String schema = getSchema(dbVO);
		String title = "drg入组病历" + System.currentTimeMillis();
		String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls

		String[]  titleArr = visitDrgTitleArr;
		String[]  fieldArr =  visitDrgFieldArr;

		int count= Integer.parseInt(req.getParameter("count"));
		String batchId = req.getParameter("batchId");
		ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			Result exportResult = Result.ok();
			try {
				//创建表格
				SXSSFWorkbook xssfWorkbook = new SXSSFWorkbook(5000);
				SXSSFSheet sheet = (SXSSFSheet) xssfWorkbook.createSheet("数据");
				CellStyle colTitleStyle = createColTitleCellStyle(xssfWorkbook);//title样式
				// 创建第一页的第一行，索引从0开始
				final int[] rowNum = {0};
				Row row0 = sheet.createRow(rowNum[0]++);
				//表头数据,循环将表头数据填充到第1行
				for (int i = 0; i < titleArr.length; i++) {
					Cell c1 = row0.createCell(i);
					c1.setCellStyle(colTitleStyle);
					c1.setCellValue(titleArr[i]);
				}

				visitDrgService.streamQueryVisitDrgList(queryWrapper,project,schema,batchId,new ResultHandler<Map<String,Object>>() {
					@SneakyThrows
					@Override
					public void handleResult(ResultContext<? extends Map<String,Object>> resultContext) {
						if(rowNum[0]>1000000){
							return;
						}
						Map<String,Object> bean = resultContext.getResultObject();
						//写入单元格
						Row temp = sheet.createRow((rowNum[0]++));
						for (int j = 0; j < fieldArr.length; j++) {
							Cell c = temp.createCell(j);
							c.setCellValue((String)bean.get(fieldArr[j]));
						}
					}
				});
				xssfWorkbook.write(os);
				xssfWorkbook.dispose();
			} catch (Exception e) {
				e.printStackTrace();
				exportResult = Result.error(e.getMessage());
			}
			return exportResult;
		});

		result.setMessage("等待导出，请在导出记录界面查看进度");
		return result;

	}

	@AutoLog(value = "drg未入组病历-直接导出")
	@ApiOperation(value="drg未入组病历-直接导出", notes="drg未入组病历-直接导出")
	@GetMapping(value = "/exportVisitNoDrgList")
	public void exportVisitNoDrgList(VisitDrgVo bean,
									 HttpServletRequest req,
									 HttpServletResponse response) throws Exception {
		QueryWrapper<VisitDrgVo> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String project = this.getProject(dbVO);

		String schema = getSchema(dbVO);
		//获取当前任务信息
		DrgTask drgTask = drgTaskService.getById(req.getParameter("taskId"));
		if(drgTask==null){
			throw new Exception("参数异常");
		}
		String orgIds = drgTask.getOrgids();
		if(StrUtil.isNotEmpty(orgIds)){
			orgIds = orgIds.replace("|",",");
			orgIds = orgIds.replace(",","','");
			orgIds = "'"+orgIds+"'";
		}
		drgTask.setOrgids(orgIds);

		String title = "drg未入组病历" + System.currentTimeMillis();
		String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		//response.reset();
		response.setContentType("application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "." + suffix).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

		OutputStream os = response.getOutputStream();

		String[]  titleArr = visitNoDrgTitleArr;
		String[]  fieldArr = Arrays.stream(visitNoDrgFieldArr).map(t-> StringCamelUtils.underline2Camel(t)).collect(Collectors.toList()).toArray(new String[visitNoDrgFieldArr.length]);

		List<VisitDrgVo> dataList =visitDrgService.visitNoDrgList(queryWrapper,project,schema,req.getParameter("batchId"),drgTask);
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		ExportXUtils.exportExl(dataList, VisitDrgVo.class,titleArr,fieldArr,workbook,"数据");
		workbook.write(os);
		workbook.dispose();
	}



	@AutoLog(value = "drg未入组病历-线程导出")
	@ApiOperation(value="drg未入组病历-线程导出", notes="drg未入组病历-线程导出")
	@GetMapping(value = "/exportVisitNoDrgListByThread")
	public Result<?> exportVisitNoDrgListByThread(VisitDrgVo bean,
								   HttpServletRequest req,
								   HttpServletResponse response) throws Exception {
		Result<?> result = new Result<>();
		QueryWrapper<VisitDrgVo> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String project = this.getProject(dbVO);

		String schema = getSchema(dbVO);
		//获取当前任务信息
		DrgTask drgTask = drgTaskService.getById(req.getParameter("taskId"));
		if(drgTask==null){
			result.setMessage("参数异常");
			result.setSuccess(false);
			return result;
		}
		String orgIds = drgTask.getOrgids();
		if(StrUtil.isNotEmpty(orgIds)){
			orgIds = orgIds.replace("|",",");
			orgIds = orgIds.replace(",","','");
			orgIds = "'"+orgIds+"'";
		}
		drgTask.setOrgids(orgIds);

		String title = "drg未入组病历" + System.currentTimeMillis();
		String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls

		String[]  titleArr = visitNoDrgTitleArr;
		String[]  fieldArr =  visitNoDrgFieldArr;

		int count= Integer.parseInt(req.getParameter("count"));
		ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			Result exportResult = Result.ok();
			try {
				//创建表格
				SXSSFWorkbook xssfWorkbook = new SXSSFWorkbook(5000);
				SXSSFSheet sheet = (SXSSFSheet) xssfWorkbook.createSheet("数据");
				CellStyle colTitleStyle = createColTitleCellStyle(xssfWorkbook);//title样式
				// 创建第一页的第一行，索引从0开始
				final int[] rowNum = {0};
				Row row0 = sheet.createRow(rowNum[0]++);
				//表头数据,循环将表头数据填充到第1行
				for (int i = 0; i < titleArr.length; i++) {
					Cell c1 = row0.createCell(i);
					c1.setCellStyle(colTitleStyle);
					c1.setCellValue(titleArr[i]);
				}

				visitDrgService.streamQueryVisitNoDrgList(queryWrapper,project,schema,drgTask.getBatchId(),drgTask,new ResultHandler<Map<String,Object>>() {
					@SneakyThrows
					@Override
					public void handleResult(ResultContext<? extends Map<String,Object>> resultContext) {
						if(rowNum[0]>1000000){
							return;
						}
						Map<String,Object> bean = resultContext.getResultObject();
						//写入单元格
						Row temp = sheet.createRow((rowNum[0]++));
						for (int j = 0; j < fieldArr.length; j++) {
							Cell c = temp.createCell(j);
							c.setCellValue((String)bean.get(fieldArr[j]));
						}
					}
				});
				xssfWorkbook.write(os);
				xssfWorkbook.dispose();
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
	 * 通过clientid查询
	 *
	 * @param clientid
	 * @return
	 */
	@AutoLog(value = "患者信息-通过clientId查询")
	@ApiOperation(value="患者信息-通过clientId查询", notes="患者信息-通过clientId查询")
	@GetMapping(value = "/queryClientById")
	public Result<?> queryClientById(@RequestParam(name="clientid",required=true) String clientid) throws Exception{
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String schema = getSchema(dbVO);
		SrcYbClientVo bean = visitDrgService.getSrcYbClientById(schema,clientid);
		return Result.ok(bean);
	}

	/**
	 * 通过clientid查询
	 *
	 * @param clientid
	 * @return
	 */
	@AutoLog(value = "结算信息-通过clientId查询")
	@ApiOperation(value="结算信息-通过clientId查询", notes="结算信息-通过clientId查询")
	@GetMapping(value = "/getSrcYbSettlementById")
	public Result<?> getSrcYbSettlementById(@RequestParam(name="clientid",required=true) String clientid,
											@RequestParam(name="visitid",required=true) String visitid) throws Exception{
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		DatasourceAndDatabaseVO dbVO = this.getDatasourceAndDatabaseVO(dataSource);
		String schema = getSchema(dbVO);
		SrcYbSettlementVo bean = visitDrgService.getSrcYbSettlementById(schema,clientid,visitid);
		return Result.ok(bean);
	}


	/**
	 * 通过visitDrgId查询
	 *
	 * @param visitDrgId
	 * @return
	 */
	@AutoLog(value = "DRG分组逻辑-通过visitDrgId查询")
	@ApiOperation(value="DRG分组逻辑-通过visitDrgId查询", notes="DRG分组逻辑-通过visitDrgId查询")
	@GetMapping(value = "/queryDrgTargetDtl")
	public Result<?> queryDrgTargetDtl(@RequestParam(name="visitDrgId",required=true) String visitDrgId) throws Exception {
		DrgTargetDtlVo bean = medicalVisitDrgService.query(visitDrgId);
		return Result.ok(bean);
	}

	private DatasourceAndDatabaseVO getDatasourceAndDatabaseVO(String dataSource) throws Exception {
		DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(dataSource);
		if (dbVO == null) {
			throw new Exception("获取项目地数据源信息失败");
		}
		if (dbVO.getSysDatasource() == null) {
			throw new Exception("获取项目地信息失败");
		}
		if (dbVO.getSysDatabase() == null) {
			throw new Exception("获取项目地关联数据源信息失败");
		}
		return dbVO;
	}

	private String getProject(DatasourceAndDatabaseVO dbVO) {
		String project = dbVO.getSysDatasource().getDataProject();
		//project = StringUtils.replace(project, "__gp", "");
		return project;
	}

	private String getSchema(DatasourceAndDatabaseVO dbVO) {
		String schema = this.getParameterFromUrl(dbVO.getSysDatabase().getUrl(), "currentSchema");
		if (StringUtils.isBlank(schema)) {
			schema = dbVO.getSysDatabase().getDbname();
		}
		return schema;
	}

	//获取url中的参数
	private String getParameterFromUrl(String url, String key) {
		HashMap<String, String> urlMap = new HashMap<String, String>();
		String queryString = StringUtils.substringAfter(url, "?");
		for (String param : queryString.split("&")) {
			urlMap.put(StringUtils.substringBefore(param, "="), StringUtils.substringAfter(param, "="));
		}
		return urlMap.get(key);
	}

	private CellStyle createColTitleCellStyle(Workbook workbook) {
		CellStyle headstyle = workbook.createCellStyle();
		// 设置字体
		Font headfont = workbook.createFont();
		// 字体大小
		headfont.setFontHeightInPoints((short) 10);
		// 加粗
		headfont.setBold(true);
		headstyle.setFont(headfont);
		headstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headstyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		return headstyle;
	}



}
