package com.ai.modules.ybChargeSearch.controller;

import java.util.*;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.bean.BeanUtil;
import com.ai.common.utils.ExcelXUtils;
import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterialsDetail;
import com.ai.modules.ybChargeSearch.vo.YbMeetingMaterialsVo;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import com.ai.modules.ybChargeSearch.entity.YbMeetingMaterials;
import com.ai.modules.ybChargeSearch.service.IYbMeetingMaterialsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 上会材料主表
 * @Author: jeecg-boot
 * @Date:   2022-11-29
 * @Version: V1.0
 */
@Slf4j
@Api(tags="上会材料主表")
@RestController
@RequestMapping("/ybChargeSearch/ybMeetingMaterials")
public class YbMeetingMaterialsController extends JeecgController<YbMeetingMaterials, IYbMeetingMaterialsService> {
	@Autowired
	private IYbMeetingMaterialsService ybMeetingMaterialsService;


	/**
	 * 分页列表查询
	 *
	 * @param ybMeetingMaterialsVo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "上会材料主表-分页列表查询")
	@ApiOperation(value="上会材料主表-分页列表查询", notes="上会材料主表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbMeetingMaterialsVo ybMeetingMaterialsVo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<YbMeetingMaterials> page = new Page<YbMeetingMaterials>(pageNo, pageSize);
		IPage<YbMeetingMaterialsVo> pageList = ybMeetingMaterialsService.getPage(page, ybMeetingMaterialsVo);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybMeetingMaterials
	 * @return
	 */
	@AutoLog(value = "上会材料主表-添加")
	@ApiOperation(value="上会材料主表-添加", notes="上会材料主表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbMeetingMaterials ybMeetingMaterials) {
		ybMeetingMaterialsService.save(ybMeetingMaterials);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param ybMeetingMaterials
	 * @return
	 */
	@AutoLog(value = "上会材料主表-编辑")
	@ApiOperation(value="上会材料主表-编辑", notes="上会材料主表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbMeetingMaterials ybMeetingMaterials) {
		ybMeetingMaterialsService.updateById(ybMeetingMaterials);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "上会材料主表-通过id删除")
	@ApiOperation(value="上会材料主表-通过id删除", notes="上会材料主表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
//		ybMeetingMaterialsService.removeById(id);
		ybMeetingMaterialsService.removeAll(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "上会材料主表-批量删除")
	@ApiOperation(value="上会材料主表-批量删除", notes="上会材料主表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
//		this.ybMeetingMaterialsService.removeByIds(Arrays.asList(ids.split(",")));
		ybMeetingMaterialsService.removeBatchAll(ids);
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "上会材料主表-通过id查询")
	@ApiOperation(value="上会材料主表-通过id查询", notes="上会材料主表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbMeetingMaterials ybMeetingMaterials = ybMeetingMaterialsService.getById(id);
		return Result.ok(ybMeetingMaterials);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybMeetingMaterials
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbMeetingMaterials ybMeetingMaterials) {
      return super.exportXls(request, ybMeetingMaterials, YbMeetingMaterials.class, "上会材料主表");
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
	  LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
	  MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
	  Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
	  //导入校验
	  for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
		  MultipartFile file = entity.getValue();// 获取上传文件对象
		  try {
			  String mappingFieldStr = "orgname,actionname,codename,groupTeam,startdate,timerange,seq,cat,item,wgDesc,pax,fundAmt,penaltyN,penaltyAmt,clauseT,clauseK";//导入的字段
			  String[] mappingFields = mappingFieldStr.split(",");

			  List<YbMeetingMaterialsVo> importList = ExcelXUtils.readSheet(YbMeetingMaterialsVo.class, mappingFields, 0, 4, file.getInputStream(),"内容结束");
			  ArrayList<YbMeetingMaterials> ybMeetingMaterials = new ArrayList<>();
			  ArrayList<YbMeetingMaterialsDetail> ybMeetingMaterialsDetails = new ArrayList<>();
			  //保存
			  importList.stream().forEach(t -> {
				  String id = UUID.randomUUID().toString();
				  YbMeetingMaterials meetingMaterials = BeanUtil.toBean(t, YbMeetingMaterials.class);
				  meetingMaterials.setId(id);
				  meetingMaterials.setCreatedBy(loginUser.getUsername());
				  meetingMaterials.setCreatedByName(loginUser.getRealname());
				  meetingMaterials.setCreatedTime(new Date());
				  ybMeetingMaterials.add(meetingMaterials);
				  YbMeetingMaterialsDetail ybMeetingMaterialsDetail = BeanUtil.toBean(t, YbMeetingMaterialsDetail.class);
				  ybMeetingMaterialsDetail.setMid(id);
				  ybMeetingMaterialsDetails.add(ybMeetingMaterialsDetail);
			  });

			  ybMeetingMaterialsService.saveAll(ybMeetingMaterials,ybMeetingMaterialsDetails);



			  return Result.ok("文件导入成功！数据行数:" + importList.size());
		  } catch (Exception e) {
			  log.error(e.getMessage(),e);
			  return Result.error("文件导入失败:"+e.getMessage());
		  } finally {
			  try {
				  file.getInputStream().close();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
		  }
	  }
	  return Result.ok("文件导入失败！");
  }

}
