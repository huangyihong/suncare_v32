package com.ai.modules.config.controller;

import java.util.List;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.service.ICommonAuditService;
import com.ai.modules.config.service.IMedicalAuditLogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 基础数据维护操作日志
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags="基础数据维护操作日志")
@RestController
@RequestMapping("/config/medicalAuditLog")
public class MedicalAuditLogController extends JeecgController<MedicalAuditLog, IMedicalAuditLogService> {
	@Autowired
	private IMedicalAuditLogService service;

	 @Autowired
	 @Qualifier("diseaseDiagAuditService")
	 private ICommonAuditService diseaseDiagAuditService;

	 @Autowired
	 @Qualifier("chineseDrugAuditService")
	 private ICommonAuditService chineseDrugAuditService;

	 @Autowired
	 @Qualifier("otherDictAuditService")
	 private ICommonAuditService otherDictAuditService;

	 @Autowired
	 @Qualifier("operationAuditService")
	 private ICommonAuditService operationAuditService;

	 @Autowired
	 @Qualifier("officeAuditService")
	 private ICommonAuditService officeAuditService;

	 @Autowired
	 @Qualifier("treatProjectAuditService")
	 private ICommonAuditService treatProjectAuditService;

	 @Autowired
	 @Qualifier("drugAuditService")
	 private ICommonAuditService drugAuditService;

	 @Autowired
	 @Qualifier("organAuditService")
	 private ICommonAuditService organAuditService;

	 @Autowired
	 @Qualifier("componentAuditService")
	 private ICommonAuditService componentAuditService;

	 @Autowired
	 @Qualifier("pathologyAuditService")
	 private ICommonAuditService pathologyAuditService;

	 @Autowired
	 @Qualifier("drugPropertyAuditService")
	 private ICommonAuditService drugPropertyAuditService;

	@Autowired
	@Qualifier("equipmentAuditService")
	private ICommonAuditService equipmentAuditService;

	 @Autowired
	 @Qualifier("stdAtcAuditService")
	 private ICommonAuditService stdAtcAuditService;

	@AutoLog(value = "审核信息查看")
	@ApiOperation(value="基审核信息查看", notes="审核信息查看")
	@GetMapping(value = "/showMedicalAuditLog")
	public Result<?> showMedicalAuditLog(@RequestParam(name="id",required=true) String id,@RequestParam(name="tableName",required=true) String tableName) {
    	List<MedicalAuditLog> logList = service.getMedicalAuditLogListByKey(id,tableName,null);
    	return Result.ok(logList);
    }

    @AutoLog(value = "审核操作")
	@ApiOperation(value="审核操作", notes="审核操作")
    @PutMapping(value = "/saveAuditMedicalAuditLog")
    @RequiresPermissions("basicDataManage:audit")
	public Result<?> saveAuditMedicalAuditLog(@RequestBody MedicalAuditLog bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			ICommonAuditService auditService = this.getAuditService(bean.getTableName());
			auditService.saveAuditMedicalAuditLog(bean);
			return Result.ok("审核操作成功");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}



    @AutoLog(value = "撤销保存")
   	@ApiOperation(value="撤销保存", notes="撤销保存")
    @PutMapping(value = "/saveUndoMedicalAuditLog")
    @RequiresPermissions("basicDataManage:undo")
   	public Result<?> saveUndoMedicalAuditLog(@RequestBody MedicalAuditLog bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			ICommonAuditService auditService = this.getAuditService(bean.getTableName());
			auditService.saveUndoMedicalAuditLog(bean);
			return Result.ok("撤销操作成功");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
    }

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "基础数据维护操作日志-通过id查询")
	@ApiOperation(value="基础数据维护操作日志-通过id查询", notes="基础数据维护操作日志-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalAuditLog medicalAuditLog = service.getById(id);
		return Result.ok(medicalAuditLog);
	}

	private ICommonAuditService getAuditService(String tableName){
		if("MEDICAL_DISEASE_DIAG".equals(tableName)){//ICD国际疾病
			return diseaseDiagAuditService;
		}
		if("MEDICAL_CHINESE_DRUG".equals(tableName)){//中草药
			return chineseDrugAuditService;
		}
		if("MEDICAL_OTHER_DICT".equals(tableName)){//其他字典
			return otherDictAuditService;
		}
		if("MEDICAL_OPERATION".equals(tableName)){//手术信息
			return operationAuditService;
		}
		if("MEDICAL_OFFICE".equals(tableName)){//科室信息
			return officeAuditService;
		}
		if("MEDICAL_TREAT_PROJECT".equals(tableName)){//医疗服务项目
			return treatProjectAuditService;
		}
		if("MEDICAL_DRUG".equals(tableName)){//药品信息
			return drugAuditService;
		}
		if("MEDICAL_ORGAN".equals(tableName)){//医疗机构信息
			return organAuditService;
		}
		if("MEDICAL_COMPONENT".equals(tableName)){//成分表
			return componentAuditService;
		}
		if("MEDICAL_PATHOLOGY".equals(tableName)){//形态学编码表
			return pathologyAuditService;
		}
		if("MEDICAL_DRUG_PROPERTY".equals(tableName)){//药品属性表
			return drugPropertyAuditService;
		}
		if("MEDICAL_EQUIPMENT".equals(tableName)){//医疗器械信息表
			return equipmentAuditService;
		}
		if("MEDICAL_STD_ATC".equals(tableName)){//ATC药品级别信息表
			return stdAtcAuditService;
		}
		return null;
	}


}
