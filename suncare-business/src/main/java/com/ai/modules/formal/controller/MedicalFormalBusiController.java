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
import com.ai.modules.formal.entity.MedicalFormalBusi;
import com.ai.modules.formal.entity.MedicalFormalCaseBusi;
import com.ai.modules.formal.service.IMedicalFormalBusiService;
import com.ai.modules.formal.service.IMedicalFormalCaseBusiService;
import com.ai.modules.formal.vo.MedicalFormalBusiVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 业务组表
 * @Author: jeecg-boot
 * @Date:   2019-11-28
 * @Version: V1.0
 */
@Slf4j
@Api(tags="业务组表")
@RestController
@RequestMapping("/formal/medicalFormalBusi")
public class MedicalFormalBusiController extends JeecgController<MedicalFormalBusi, IMedicalFormalBusiService> {
	@Autowired
	private IMedicalFormalBusiService medicalFormalBusiService;

	@Autowired
	private IMedicalFormalCaseBusiService medicalFormalCaseBusiService;


	/**
	 * 分页列表查询
	 *
	 * @param medicalFormalBusi
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "业务组表-分页列表查询")
	@ApiOperation(value="业务组表-分页列表查询", notes="业务组表-分页列表查询")
	@RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> queryPageList(MedicalFormalBusi medicalFormalBusi,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFormalBusi> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalBusi, req.getParameterMap());
		Page<MedicalFormalBusi> page = new Page<MedicalFormalBusi>(pageNo, pageSize);
		IPage<MedicalFormalBusi> pageList = medicalFormalBusiService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalFormalBusi
	 * @return
	 */
	@AutoLog(value = "业务组表-添加")
	@ApiOperation(value="业务组表-添加", notes="业务组表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalFormalBusiVO medicalFormalBusiVO) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		medicalFormalBusiVO.setBusiId(IdUtils.uuid());
		medicalFormalBusiVO.setCreateTime(new Date());
		medicalFormalBusiVO.setCreateUserid(user.getId());
		medicalFormalBusiVO.setCreateUsername(user.getRealname());
		//保存业务模型关联信息

		medicalFormalBusiService.saveBusiAndCaseBusi(medicalFormalBusiVO);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalFormalBusi
	 * @return
	 */
	@AutoLog(value = "业务组表-编辑")
	@ApiOperation(value="业务组表-编辑", notes="业务组表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalFormalBusiVO medicalFormalBusiVO) {
		medicalFormalBusiService.updateBusiAndCaseBusi(medicalFormalBusiVO);
		//medicalFormalBusiService.updateById(medicalFormalBusi);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组表-通过id删除")
	@ApiOperation(value="业务组表-通过id删除", notes="业务组表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		//medicalFormalBusiService.removeById(id);
		medicalFormalBusiService.removeBusiAndCaseBusiById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "业务组表-批量删除")
	@ApiOperation(value="业务组表-批量删除", notes="业务组表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		//this.medicalFormalBusiService.removeByIds(Arrays.asList(ids.split(",")));
		this.medicalFormalBusiService.removeBusiAndCaseBusiByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组表-通过id查询")
	@ApiOperation(value="业务组表-通过id查询", notes="业务组表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFormalBusi medicalFormalBusi = medicalFormalBusiService.getById(id);
		return Result.ok(medicalFormalBusi);
	}

	/**
	 * 通过id查询业务模型关联记录
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组表-通过id查询业务模型关联记录")
	@ApiOperation(value="业务组表-通过id查询业务模型关联记录", notes="业务组表-通过id查询业务模型关联记录")
	@GetMapping(value = "/queryCaseBusiListById")
	public Result<?> queryCaseBusiListById(@RequestParam(name="id",required=true) String id) {
		QueryWrapper<MedicalFormalCaseBusi> queryWrapper = new QueryWrapper<MedicalFormalCaseBusi>();
		queryWrapper.eq("BUSI_ID", id);
		List<MedicalFormalCaseBusi> list = medicalFormalCaseBusiService.list(queryWrapper);
		return Result.ok(list);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFormalBusi
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFormalBusi medicalFormalBusi) {
      return super.exportXls(request, medicalFormalBusi, MedicalFormalBusi.class, "业务组表");
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
      return super.importExcel(request, response, MedicalFormalBusi.class);
  }

}
