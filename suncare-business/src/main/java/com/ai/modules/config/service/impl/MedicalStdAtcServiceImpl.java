package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalStdAtc;
import com.ai.modules.config.mapper.MedicalStdAtcMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalStdAtcService;
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
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @Description: ATC药品级别信息
 * @Author: jeecg-boot
 * @Date:   2019-12-20
 * @Version: V1.0
 */
@Service

public class MedicalStdAtcServiceImpl extends ServiceImpl<MedicalStdAtcMapper, MedicalStdAtc> implements IMedicalStdAtcService {
	@Autowired
	IMedicalAuditLogService logService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final String TABLE_NAME="MEDICAL_STD_ATC";//表名

	@Override
	public QueryWrapper<MedicalStdAtc> getQueryWrapper(MedicalStdAtc medicalStdAtc, HttpServletRequest request) throws Exception {
		medicalStdAtc.setState("");
		medicalStdAtc.setAuditResult("");
		medicalStdAtc.setActionType("");
		QueryWrapper<MedicalStdAtc> queryWrapper = QueryGenerator.initQueryWrapper(medicalStdAtc, request.getParameterMap());
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
	public List<MedicalStdAtc> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalStdAtc> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalStdAtc(MedicalStdAtc bean) {
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalStdAtc(MedicalStdAtc bean) {
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
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.eq("CODE", code);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalStdAtc(MedicalStdAtc bean) {
		MedicalStdAtc oldBean = this.baseMapper.selectById(bean.getId());
		Map<String,String> map = MedicalAuditLogConstants.contrastObj(oldBean,bean,null);
		if(map!=null){
			oldBean.setActionStaff(bean.getActionStaff());
  			oldBean.setActionStaffName(bean.getActionStaffName());
          	oldBean.setActionTime(bean.getActionTime());
			if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
//				mapper.updateByPrimaryKeySelective(oldBean);
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
//				mapper.updateByPrimaryKeySelective(oldBean);
				this.baseMapper.updateById(oldBean);
				//插入日志记录
				logService.insertMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
						bean.getUpdateReason(),bean.getUpdateStaff(),bean.getUpdateStaffName(),bean.getUpdateTime(), updateBeanStr,updateContentStr);
			}
		}
	}

	public void updateBeanBatch(List<MedicalStdAtc> list, List<MedicalStdAtc> oldlist) {
		Field[] fields = MedicalStdAtc.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalStdAtc bean = list.get(i);
			MedicalStdAtc oldBean = oldlist.get(i);
			Map<String,String> map = MedicalAuditLogConstants.contrastObj(oldBean,bean,fields);
			if(map!=null){
				oldBean.setActionStaff(bean.getActionStaff());
				oldBean.setActionStaffName(bean.getActionStaffName());
				oldBean.setActionTime(bean.getActionTime());
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					//直接修改日志记录
					List<MedicalAuditLog> loglist =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
					if(loglist.size()>0){
						MedicalAuditLog log = loglist.get(0);
						log.setUpdateJson(updateBeanStr);
						log.setActionContent(updateContentStr);
						log.setActionReason(bean.getUpdateReason());
						log.setActionTime(bean.getUpdateTime());
						logAddList.add(log);
					}
				}else{
					oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
					oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
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
	public void delMedicalStdAtc(MedicalStdAtc bean) {
		List<MedicalStdAtc> list =getListByIds(bean.getId());
		commonDelMedicalStdAtcBatch(bean, list);
	}

	//删除单条操作
	private void commonDelMedicalStdAtcBatch(MedicalStdAtc bean, List<MedicalStdAtc> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalStdAtc oldBean : oldlist) {
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

	@Override
	@Transactional
	public int saveCleanMedicalStdAtc(QueryWrapper<MedicalStdAtc> queryWrapper, MedicalAuditLog bean) {
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
	public int delAllMedicalStdAtc(QueryWrapper<MedicalStdAtc> queryWrapper, MedicalStdAtc bean)
			throws Exception {
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalStdAtcBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception {
		String mappingFieldStr = "code,name,"
				+ "act1Code,act1Name,act2Code,act2Name,act3Code,act3Name,act4Code,act4Name,"
				+ "chargeClassCode,chargeClassName,dosageCode,dosage,remark,"
				+ "createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+ DateUtils.now() );
		List<MedicalStdAtc> list = new ArrayList<MedicalStdAtc>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalStdAtc.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalStdAtc.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<String> codeSet = new HashSet<String>();
		List<MedicalStdAtc> addList = new ArrayList<MedicalStdAtc>();
		List<MedicalStdAtc> olnyUpdateList = new ArrayList<MedicalStdAtc>();
		List<MedicalStdAtc> updateList = new ArrayList<MedicalStdAtc>();
		List<MedicalStdAtc> oldUpdateList = new ArrayList<MedicalStdAtc>();
		List<MedicalStdAtc> deleteList = new ArrayList<MedicalStdAtc>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<String> codeExistSet = getAllCodeSet();//库中已存在的code
		System.out.println("校验开始："+DateUtils.now() );
		List<String> codesAdd = new ArrayList<>();//新增编码
		List<String> codesUpdate = new ArrayList<>();//修改编码
		List<String> codesDelete = new ArrayList<>();//删除编码
		Map<String,MedicalStdAtc> addMap = new HashMap<>();
		Map<String,MedicalStdAtc> updateMap = new HashMap<>();
		Map<String,MedicalStdAtc> deleteMap = new HashMap<>();
		List<String> errorMsg =  new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			MedicalStdAtc bean = list.get(i);
			if (StringUtils.isBlank(bean.getCode())) {
				errorMsg.add("导入的数据中“ATC药品编码”不能为空，如：第" + (i + 2) + "行数据“ATC药品编码”为空");
		    }
		    if (StringUtils.isBlank(bean.getName())) {
				errorMsg.add("导入的数据中“ATC药品名称”不能为空，如：第" + (i + 2) + "行数据“ATC药品名称”为空");
		    }
		    //判断code在excel中是否重复
		    if(codeSet.contains(bean.getCode())){
				errorMsg.add("导入的数据中“ATC药品编码”不能重复，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”在excel中重复");
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
			List<MedicalStdAtc> existList = getBeanByCode(codesAdd,stateList,"CODE".split(","));
			List<String> existCode = existList.stream().map(MedicalStdAtc::getCode).collect(Collectors.toList());
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
			List<MedicalStdAtc> existList = getBeanByCode(codesUpdate,stateList,null);
			List<String> existCode = existList.stream().map(MedicalStdAtc::getCode).collect(Collectors.toList());
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
					MedicalStdAtc bean = updateMap.get(oldBean.getCode());
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
			List<MedicalStdAtc> existList = getBeanByCode(codesDelete,stateList,null);
			List<String> existCode = existList.stream().map(MedicalStdAtc::getCode).collect(Collectors.toList());
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
					MedicalStdAtc bean = deleteMap.get(oldBean.getCode());
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

	private List<MedicalStdAtc> getBeanByCode(List<String> codes,List<String> stateList,String[] fileds){
		List<MedicalStdAtc> alllist = new ArrayList<>();
		List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(codes,1000);
		for(Set<String> strList:setList){
			QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
			queryWrapper.in("CODE",strList);
			queryWrapper.in("STATE", stateList);
			if(fileds!=null&&fileds.length>0){
				queryWrapper.select(fileds);
			}
			alllist.addAll(this.baseMapper.selectList(queryWrapper));
		}
		return  alllist;
	}

	private MedicalStdAtc findBeanByCode(String code,String state) {
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.eq("CODE", code);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalStdAtc> queryWrapper, OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			List<MedicalStdAtc> list = this.list(queryWrapper);
			List<MedicalStdAtc> dataList = new ArrayList<MedicalStdAtc>();
	    	Map<String,List<Map<String,Object>>> typeMap = new HashMap<String,List<Map<String,Object>>>();
	    	for(MedicalStdAtc exportBean:list){
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

	    	String titleStr = "ATC药品编码,ATC药品名称,"
					+ "ATC药品1级代码,ATC药品1级名称,ATC药品2级代码,ATC药品2级名称,ATC药品3级代码,ATC药品3级名称,ATC药品4级代码,ATC药品4级名称,"
	    			+ "收费类别编码,收费类别名称,剂型代码,剂型名称,备注,"
	    			+ "最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "code,name,"
					+ "act1Code,act1Name,act2Code,act2Name,act3Code,act3Name,act4Code,act4Name,"
	    			+ "chargeClassCode,chargeClassName,dosageCode,dosage,remark,"
					+ "actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalStdAtc.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalStdAtc.class,titles,fields,sheet, "");
				wwb.write();
		        wwb.close();
			}
    	} catch (Exception e) {
			e.printStackTrace();
			isSuc = false;
		}
    	return isSuc;
	}

	@Override
	public List<Map<String, Object>> queryCascader(String parentCode,int levelNum,String state) {
		if(levelNum>4){
			return  new ArrayList<>();
		}else{
			String code = "ACT"+levelNum+"_CODE";
			String name = "ACT"+levelNum+"_NAME";
			String sql = "select distinct "+code+" CODE,"+name+" NAME from MEDICAL_STD_ATC where 1=1 and  "+code+" is not null ";
			if(StringUtils.isNotBlank(state)){
				sql += "and STATE in ('"+StringUtils.join(state.split(","), "','")+"')";
			}
			if(StringUtils.isNotBlank(parentCode)&&levelNum>1){
				sql += "and ACT"+(levelNum-1)+"_CODE = '"+parentCode+"'";
			}
			sql += "order by "+code;
			List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql);
			return list;
		}
	}

	@Override
	public String getCodeByName(String name) {
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.eq("NAME", name);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0).getCode();
		}
		return null;
	}

	@Override
	public String getNameByCode(String code) {
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0).getName();
		}
		return null;
	}

	@Override
	public MedicalStdAtc getBeanByCode(String code) {
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public Map<String, String> getMapByCode(String codes) {
		Map<String, String> map = new HashMap<>();
		List<String> valueList = Arrays.asList(codes.split(","));
		if(valueList.size()>1000){
			List<List<String>> setList = getBatchList(valueList,1000);
			for(List<String> strList:setList){
				queryListByCodes(strList, map);
			}
		}else{
			queryListByCodes(valueList, map);
		}
		return map;
	}

	private void queryListByCodes(List<String> codeList, Map<String, String> map) {
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.in("CODE", codeList);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalStdAtc bean:list){
			map.put(bean.getCode(),bean.getName());
		}
	}

	@Override
	public Map<String, String> getMapByName(String values) {
		Map<String, String> map = new HashMap<>();
		List<String> valueList = Arrays.asList(values.split(","));
		if(valueList.size()>1000){
			List<List<String>> setList = getBatchList(valueList,1000);
			for(List<String> strList:setList){
				queryListByNames(strList, map);
			}
		}else{
			queryListByNames(valueList, map);
		}
		return map;
	}

	private void queryListByNames(List<String> valueList, Map<String, String> map) {
		QueryWrapper<MedicalStdAtc> queryWrapper = new QueryWrapper<MedicalStdAtc>();
		queryWrapper.in("NAME", valueList);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalStdAtc> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalStdAtc bean:list){
			map.put(bean.getName(),bean.getCode());
		}
	}

	private List<List<String>> getBatchList(List<String> list,int batchSize) {
		List<List<String>> batchList = new ArrayList<List<String>>();
		List<String> strList = new ArrayList<String>();
		for (String str : list) {
			if (strList.size() > batchSize) {
				batchList.add(strList);
				strList = new ArrayList<String>();
			}
			strList.add(str);
		}
		if (strList.size() > 0) {
			batchList.add(strList);
		}
		return batchList;
	}

}
