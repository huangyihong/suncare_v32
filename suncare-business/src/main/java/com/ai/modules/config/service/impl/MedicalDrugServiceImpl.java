package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.mapper.MedicalDrugMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalDrugService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 药品信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Service
public class MedicalDrugServiceImpl extends ServiceImpl<MedicalDrugMapper, MedicalDrug> implements IMedicalDrugService {
	@Autowired
	IMedicalAuditLogService logService;

	private static final String TABLE_NAME="MEDICAL_DRUG";//表名

	@Override
	public QueryWrapper<MedicalDrug> getQueryWrapper(MedicalDrug medicalDrug,HttpServletRequest request) throws Exception {
		medicalDrug.setState("");
		medicalDrug.setAuditResult("");
		medicalDrug.setActionType("");
		QueryWrapper<MedicalDrug> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrug, request.getParameterMap());
		String state = request.getParameter("state");
		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		String auditResult = request.getParameter("auditResult");
		if(StringUtils.isNotBlank(auditResult)){
			queryWrapper.in("AUDIT_RESULT", Arrays.asList(auditResult.split(",")));
		}
		String actionType = request.getParameter("actionType");
		if(StringUtils.isNotBlank(actionType)){
			queryWrapper.in("ACTION_TYPE", Arrays.asList(actionType.split(",")));
		}
		//操作时间
		queryWrapper = logService.initQueryWrapperTime(queryWrapper,request);
		return queryWrapper;
	}

	@Override
	public List<MedicalDrug> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalDrug> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalDrug(MedicalDrug bean) {
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalDrug(MedicalDrug bean) {
		this.baseMapper.updateById(bean);
		//直接修改日志记录
		List<MedicalAuditLog> list =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
		if(list.size()>0){
			MedicalAuditLog log = list.get(0);
			log.setActionReason(bean.getCreateReason());
			logService.updateMedicalAuditLog(log);
		}
	}

	@Override
	public boolean isExistName(String code, String id) {
		QueryWrapper<MedicalDrug> queryWrapper = new QueryWrapper<MedicalDrug>();
		queryWrapper.eq("CODE", code);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalDrug> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalDrug(MedicalDrug bean) {
		MedicalDrug oldBean = this.baseMapper.selectById(bean.getId());
		Map<String,String> map = MedicalAuditLogConstants.contrastObj(oldBean,bean,null);
		if(map!=null){
			oldBean.setActionStaff(bean.getActionStaff());
  			oldBean.setActionStaffName(bean.getActionStaffName());
          	oldBean.setActionTime(bean.getActionTime());
			if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				this.baseMapper.updateById(oldBean);
				//直接修改日志记录
				List<MedicalAuditLog> list =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
				if(list.size()>0){
					MedicalAuditLog log = list.get(0);
					log.setUpdateJson(updateBeanStr);
					log.setActionContent(updateContentStr);
					log.setActionReason(bean.getUpdateReason());
					log.setActionTime(bean.getUpdateTime());
					logService.updateMedicalAuditLog(log);
				}
			}else{
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
				oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
				this.baseMapper.updateById(oldBean);
				//插入日志记录
				logService.insertMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
						bean.getUpdateReason(),bean.getUpdateStaff(),bean.getUpdateStaffName(),bean.getUpdateTime(), updateBeanStr,updateContentStr);
			}
		}
	}

	public void updateBeanBatch(List<MedicalDrug> list,List<MedicalDrug> oldlist) {
		Field[] fields = MedicalDrug.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalDrug bean = list.get(i);
			MedicalDrug oldBean = oldlist.get(i);
			Map<String,String> map = MedicalAuditLogConstants.contrastObj(oldBean,bean,fields);
			if(map!=null){
				oldBean.setActionStaff(bean.getActionStaff());
				oldBean.setActionStaffName(bean.getActionStaffName());
				oldBean.setActionTime(bean.getActionTime());
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					//this.baseMapper.updateById(oldBean);
					//直接修改日志记录
					List<MedicalAuditLog> loglist =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
					if(loglist.size()>0){
						MedicalAuditLog log = loglist.get(0);
						log.setUpdateJson(updateBeanStr);
						log.setActionContent(updateContentStr);
						log.setActionReason(bean.getUpdateReason());
						log.setActionTime(bean.getUpdateTime());
						//logService.updateMedicalAuditLog(log);
						logAddList.add(log);
					}
				}else{
					oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
					oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
					//this.baseMapper.updateById(oldBean);
					//插入日志记录
					/*logService.insertMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
							bean.getUpdateReason(),bean.getUpdateStaff(),bean.getUpdateStaffName(),bean.getUpdateTime(), updateBeanStr,updateContentStr);*/
					logAddList.add(logService.setMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
							bean.getUpdateReason(),bean.getUpdateStaff(),bean.getUpdateStaffName(),bean.getUpdateTime(), updateBeanStr,updateContentStr));
				}
			}
		}
		this.updateBatchById(oldlist,MedicalAuditLogConstants.BATCH_SIZE);
		if(logAddList.size()>0){
			logService.saveOrUpdateBatch(logAddList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	@Override
	@Transactional
	public void delMedicalDrug(MedicalDrug bean) {
		/*String[] ids_arr = bean.getId().split(",");
		for(String id:ids_arr){
			MedicalDrug oldBean = this.baseMapper.selectById(id);
			commonDelMedicalDrug(bean, oldBean);
		}*/
		String[] ids_arr = bean.getId().split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalDrug> list = new ArrayList<>();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		commonDelMedicalDrugBatch(bean, list);
	}

	//删除批量操作
	private void commonDelMedicalDrugBatch(MedicalDrug bean, List<MedicalDrug> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalDrug oldBean : oldlist) {
			oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
			oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			logAddList.add(logService.setMedicalAuditLog(TABLE_NAME, oldBean.getId(), oldBean.getAuditResult(), oldBean.getActionType(),
					bean.getDeleteReason(), bean.getDeleteStaff(), bean.getDeleteStaffName(), bean.getDeleteTime(), null, null));
		}
		this.updateBatchById(oldlist,MedicalAuditLogConstants.BATCH_SIZE);
		if (logAddList.size() > 0) {
			logService.saveOrUpdateBatch(logAddList, MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	//删除单条操作
	/*private void commonDelMedicalDrug(MedicalDrug bean, MedicalDrug oldBean) {
		oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
		oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
		this.baseMapper.updateById(oldBean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
				bean.getDeleteReason(),bean.getDeleteStaff(),bean.getDeleteStaffName(),bean.getDeleteTime(), null,null);
	}*/

	@Override
	@Transactional
	public int saveCleanMedicalDrug(QueryWrapper<MedicalDrug> queryWrapper, MedicalAuditLog bean) {
		int count = this.baseMapper.selectCount(queryWrapper);
		if(count>0){
			this.baseMapper.delete(queryWrapper);
			bean.setId(IdUtils.uuid());
			bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_CLEAN);//清理
			bean.setActionContent("影响记录数"+count+"条");
			this.logService.save(bean);
		}
		return count;

	}

	@Override
	@Transactional
	public int delAllMedicalDrug(QueryWrapper<MedicalDrug> queryWrapper, MedicalDrug bean)
			throws Exception {
		List<MedicalDrug> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalDrugBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception {
		String mappingFieldStr ="code,name,generalCode,generalName,productEname,productName,approveNumber,dosageLevelCode,dosageLevel,dosageCode,dosage," +
				"packageNum,packageUnitCode,packageUnit,useUnitCode,useUnit,packageUseRate,preparationUnit,preparationUnitCode,ybCode,ybName,"
				+ "specificationLevelCode,specificationLevel,specification,factoryLevelCode,factoryLevel,enterpriseCode,enterprise,"
				+ "type1Code,type1Name,type2Code,type2Name,type3Code,type3Name,type4Code,type4Name,type5Code,type5Name,"
				+ "act1Code,act1Name,act2Code,act2Name,act3Code,act3Name,act4Code,act4Name,medicare1Code,medicare1Name,"
				+ "medicare2Code,medicare2Name,medicare3Code,medicare3Name,medicare4Code,medicare4Name,remark,chargeClassCode,chargeClassName,createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalDrug> list = new ArrayList<MedicalDrug>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalDrug.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalDrug.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<String> codeSet = new HashSet<String>();
		List<MedicalDrug> addList = new ArrayList<MedicalDrug>();
		List<MedicalDrug> olnyUpdateList = new ArrayList<MedicalDrug>();
		List<MedicalDrug> updateList = new ArrayList<MedicalDrug>();
		List<MedicalDrug> oldUpdateList = new ArrayList<MedicalDrug>();
		List<MedicalDrug> deleteList = new ArrayList<MedicalDrug>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<String> codeExistSet = getAllCodeSet();//库中已存在的code
		System.out.println("校验开始："+DateUtils.now() );
		List<String> codesAdd = new ArrayList<>();//新增编码
		List<String> codesUpdate = new ArrayList<>();//修改编码
		List<String> codesDelete = new ArrayList<>();//删除编码
		Map<String,MedicalDrug> addMap = new HashMap<>();
		Map<String,MedicalDrug> updateMap = new HashMap<>();
		Map<String,MedicalDrug> deleteMap = new HashMap<>();
		List<String> errorMsg =  new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			MedicalDrug bean = list.get(i);
		    if (StringUtils.isBlank(bean.getCode())) {
				errorMsg.add("导入的数据中“药品编码”不能为空，如：第" + (i + 2) + "行数据“药品编码”为空");
		    }
		    if (StringUtils.isBlank(bean.getName())) {
				errorMsg.add("导入的数据中“药品名称”不能为空，如：第" + (i + 2) + "行数据“药品名称”为空");
		    }
		    //判断code在excel中是否重复
		    if(codeSet.contains(bean.getCode())){
				errorMsg.add("导入的数据中“药品编码”不能重复，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”在excel中重复");
		    }

			if (StringUtils.isBlank(bean.getActionType())) {
				errorMsg.add("导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空");
		    }
			if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(bean.getActionType())) {
				errorMsg.add("导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据");
			}
			if(StringUtils.isBlank(bean.getCode())){
				continue;
			}
			if("1".equals(bean.getActionType())) {//新增
				codesAdd.add(bean.getCode());
				addMap.put(bean.getCode(),bean);
			}else if("0".equals(bean.getActionType())) {//修改
				codesUpdate.add(bean.getCode());
				updateMap.put(bean.getCode(),bean);
			}else if("2".equals(bean.getActionType())) {//删除
				codesDelete.add(bean.getCode());
				deleteMap.put(bean.getCode(),bean);
			}
			codeSet.add(bean.getCode());
		}
		if(codesAdd.size()>0){
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalDrug> existList = getBeanByCode(codesAdd,stateList,"CODE".split(","));
			List<String> existCode = existList.stream().map(MedicalDrug::getCode).collect(Collectors.toList());
			if(existCode.size()>0){
				errorMsg.add("导入的数据中，新增数据中包含系统中待生效或者有效的数据，如：[" +
						StringUtils.join(existCode, ",") + "]");
			}
			if(errorMsg.size()==0){
				addMap.forEach((k, bean) -> {
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);

					//设置导入的新增人员
					bean.setId(IdUtils.uuid());
					bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
					bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
					bean.setCreateStaff(user.getId());
					bean.setCreateStaffName(user.getRealname());
					bean.setCreateTime(new Date());
					bean.setActionStaff(user.getId());
					bean.setActionStaffName(user.getRealname());
					bean.setActionTime(new Date());

					logList.add(logService.setMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
							bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null));

					addList.add(bean);
				});
			}
		}

		if(codesUpdate.size()>0){
			//不存在或者无效的记录
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalDrug> existList = getBeanByCode(codesUpdate,stateList,null);
			List<String> existCode = existList.stream().map(MedicalDrug::getCode).collect(Collectors.toList());
			List<String> notExistCode = codesUpdate.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
			if(notExistCode.size()>0){
				errorMsg.add("导入的数据中，修改数据中包含系统中不存在或者无效的记录，如：[" +
						StringUtils.join(notExistCode, ",") + "]");
			}
			//待生效、有效 ->
			existList.forEach(oldBean ->{
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					if(MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(oldBean.getActionType())){
						errorMsg.add("导入的数据中，包含正在删除审核中的记录，如：“"+oldBean.getCode()+"”，不允许修改");
					}else if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
						errorMsg.add("导入的数据中，包含其他用户新增的待审核的记录，如：“"+oldBean.getCode()+"”，不允许修改");
					}else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
						errorMsg.add("导入的数据中，包含其他用户正在修改的待审核的记录，如：“"+oldBean.getCode()+"”，不允许修改");
					}
				}
			});
			if(errorMsg.size()==0) {
				existList.forEach(oldBean -> {
					MedicalDrug bean = updateMap.get(oldBean.getCode());
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
					bean.setUpdateReason(bean.getCreateReason());
					bean.setCreateReason(oldBean.getCreateReason());
					bean.setId(oldBean.getId());
					if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())&&MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())){////新增待审核状态，进行修改
						bean.setActionStaff(user.getId());
						bean.setActionStaffName(user.getRealname());
						bean.setActionTime(new Date());
						bean.setCreateReason(bean.getUpdateReason());
						bean.setUpdateReason("");
						olnyUpdateList.add(bean);
					}else{
						bean.setUpdateStaff(user.getId());
						bean.setUpdateStaffName(user.getRealname());
						bean.setUpdateTime(new Date());
						bean.setActionStaff(user.getId());
						bean.setActionStaffName(user.getRealname());
						bean.setActionTime(new Date());
						updateList.add(bean);
						oldUpdateList.add(oldBean);
					}
				});
			}
		}
		if(codesDelete.size()>0){
			//不存在或者无效的记录
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalDrug> existList = getBeanByCode(codesDelete,stateList,null);
			List<String> existCode = existList.stream().map(MedicalDrug::getCode).collect(Collectors.toList());
			List<String> notExistCode = codesDelete.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
			if(notExistCode.size()>0){
				errorMsg.add("导入的数据中，删除数据中包含系统中不存在或者无效的记录，如：[" +
						StringUtils.join(notExistCode, ",") + "]");
			}
			//待生效、有效 ->
			existList.forEach(oldBean ->{
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					errorMsg.add("导入的数据中，删除数据中包含正在审核中的数据的记录，如：“"+oldBean.getCode()+"”，不允许删除");
				}
			});
			if(errorMsg.size()==0) {
				existList.forEach(oldBean -> {
					MedicalDrug bean = deleteMap.get(oldBean.getCode());
					bean.setId(oldBean.getId());
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
					bean.setDeleteReason(bean.getCreateReason());
					bean.setCreateReason(oldBean.getCreateReason());
					bean.setActionStaff(user.getId());
					bean.setActionStaffName(user.getRealname());
					bean.setActionTime(new Date());
					bean.setDeleteTime(new Date());
					oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
					oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
					//插入日志记录
					logList.add(logService.setMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
							bean.getDeleteReason(),bean.getDeleteStaff(),bean.getDeleteStaffName(),bean.getDeleteTime(), null,null));
					deleteList.add(oldBean);
				});
			}
		}
		if(errorMsg.size()>0){
			message = StringUtils.join(errorMsg, "\n");
		}
		if(StringUtils.isNotBlank(message)){
			message +="\n请核对数据后进行批量导入。";
			return Result.error(message);
		}else{
			System.out.println("开始插入时间："+DateUtils.now() );
			//批量新增
			if(addList.size()>0) {
				this.saveBatch(addList,MedicalAuditLogConstants.BATCH_SIZE);//直接插入
			}
			//批量修改
			if(olnyUpdateList.size()>0){
				this.updateBatchById(olnyUpdateList,MedicalAuditLogConstants.BATCH_SIZE);
			}
			if(updateList.size()>0){
				this.updateBeanBatch(updateList,oldUpdateList);
			}
			//批量删除
			if(deleteList.size()>0){
				this.updateBatchById(deleteList,MedicalAuditLogConstants.BATCH_SIZE);
			}
			//新增删除生成日志
			if(logList.size()>0){
				logService.saveBatch(logList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
			}
			message += "批量导入成功！";
			if(addList.size()>0) {
				message += "共新增"+addList.size()+"条数据。";
			}
			if((olnyUpdateList.size()+updateList.size())>0) {
				message += "共修改"+(olnyUpdateList.size()+updateList.size())+"条数据。";
			}
			if(deleteList.size()>0) {
				message += "共删除"+deleteList.size()+"条数据。";
			}
			System.out.println("结束导入时间："+DateUtils.now() );
			return Result.ok(message,list.size());
		}
	}

	private List<MedicalDrug> getBeanByCode(List<String> codes,List<String> stateList,String[] fileds){
		List<MedicalDrug> alllist = new ArrayList<>();
		List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(codes,1000);
		for(Set<String> strList:setList){
			QueryWrapper<MedicalDrug> queryWrapper = new QueryWrapper<MedicalDrug>();
			queryWrapper.in("CODE",strList);
			queryWrapper.in("STATE", stateList);
			if(fileds!=null&&fileds.length>0){
				queryWrapper.select(fileds);
			}
			alllist.addAll(this.baseMapper.selectList(queryWrapper));
		}
		return  alllist;
	}

	private MedicalDrug findBeanByCode(String code,String state) {
		QueryWrapper<MedicalDrug> queryWrapper = new QueryWrapper<MedicalDrug>();
		queryWrapper.eq("CODE", code);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalDrug> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalDrug> queryWrapper, OutputStream os,String suffix){
		boolean isSuc = true;
		try {
			List<MedicalDrug> list = this.list(queryWrapper);
			List<MedicalDrug> dataList = new ArrayList<MedicalDrug>();
	    	for(MedicalDrug exportBean:list){
	    		if(StringUtils.isNotBlank(exportBean.getActionType())){
	    			exportBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_MAP.get(exportBean.getActionType()));
	    		}
	    		if(StringUtils.isNotBlank(exportBean.getAuditResult())){
	    			exportBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_MAP.get(exportBean.getAuditResult()));
	    		}
	    		if(StringUtils.isNotBlank(exportBean.getState())){
	    			exportBean.setState(MedicalAuditLogConstants.STATE_MAP.get(exportBean.getState()));
	    		}
	    		dataList.add(exportBean);
	    	}

	    	String titleStr = "药品编码,药品名称,药品通用名编码,药品通用名,药品英文名称,药品商品名,批准文号,药品剂型级别编码,药品剂型级别名称,剂型代码,剂型名称," +
					"包装,最小包装单位编码,最小包装单位,使用单位编码,使用单位,包装使用转换率,最小制剂单位,最小制剂单位编码,国家医保药品编码,国家医保药品名称,"
					+ "药品规格级别编码,药品规格级别名称,规格,药品厂家级别编码,药品厂家级别名称,生产企业代码,生产企业名称,"
					+ "药理一级分类编码,药理一级分类名称,药理二级分类编码,药理二级分类名称,药理三级分类编码,药理三级分类名称,药理四级分类编码,药理四级分类名称,药理五级分类编码,药理五级分类名称,"
					+ "ATC药品1级代码,ATC药品1级名称,ATC药品2级代码,ATC药品2级名称,ATC药品3级代码,ATC药品3级名称,ATC药品4级代码,ATC药品4级名称,医保1级分类代码,医保1级分类名称,"
					+ "医保2级分类代码,医保2级分类名称,医保3级分类代码,医保3级分类名称,医保4级分类代码,医保4级分类名称,备注,收费类别编码,收费类别名称,"
	    			+ "最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "code,name,generalCode,generalName,productEname,productName,approveNumber,dosageLevelCode,dosageLevel,dosageCode," +
					"dosage,packageNum,packageUnitCode,packageUnit,useUnitCode,useUnit,packageUseRate,preparationUnit,preparationUnitCode,ybCode,ybName,"
					+ "specificationLevelCode,specificationLevel,specification,factoryLevelCode,factoryLevel,enterpriseCode,enterprise,"
					+ "type1Code,type1Name,type2Code,type2Name,type3Code,type3Name,type4Code,type4Name,type5Code,type5Name,"
					+ "act1Code,act1Name,act2Code,act2Name,act3Code,act3Name,act4Code,act4Name,medicare1Code,medicare1Name,"
					+ "medicare2Code,medicare2Name,medicare3Code,medicare3Name,medicare4Code,medicare4Name,remark,chargeClassCode,chargeClassName,"
					+ "actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalDrug.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalDrug.class,titles,fields,sheet, "");
				wwb.write();
		        wwb.close();
			}
    	} catch (Exception e) {
			e.printStackTrace();
			isSuc = false;
		}
    	return isSuc;
	}
}
