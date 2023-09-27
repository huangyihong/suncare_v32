package com.ai.modules.formal.controller;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.modules.formal.entity.MedicalActionGrp;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.formal.vo.MedicalFormalCaseBusiVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 不合规行为总览
 * @Author: jeecg-boot
 * @Date:   2019-12-02
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合规行为总览")
@RestController
@RequestMapping("/formal/medicalActionResult")
public class MedicalActionResultController extends JeecgController<MedicalFormalCase, IMedicalFormalCaseService> {
	
	@Autowired
    private IMedicalFormalCaseService medicalFormalCaseService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalActionGrp
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "行为总览表-分页列表查询")
	@ApiOperation(value="行为总览-分页列表查询", notes="行为总览-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFormalCaseBusiVO voParams,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<MedicalFormalCaseBusiVO> page = new Page<MedicalFormalCaseBusiVO>(pageNo, pageSize);
		page.setRecords(medicalFormalCaseService.selectCaseBusiVOPage(page, voParams));
		return Result.ok(page);
	}
	
	


}
