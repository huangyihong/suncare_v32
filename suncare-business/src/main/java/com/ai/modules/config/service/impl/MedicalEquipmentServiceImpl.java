package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalEquipment;
import com.ai.modules.config.mapper.MedicalEquipmentMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalEquipmentService;
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
 * @Description: 医疗器械信息表
 * @Author: jeecg-boot
 * @Date:   2020-05-09
 * @Version: V1.0
 */
@Service
public class MedicalEquipmentServiceImpl extends ServiceImpl<MedicalEquipmentMapper, MedicalEquipment> implements IMedicalEquipmentService {
	@Autowired
	IMedicalAuditLogService logService;

	private static final String TABLE_NAME="MEDICAL_EQUIPMENT";//表名

	@Override
	public QueryWrapper<MedicalEquipment> getQueryWrapper(MedicalEquipment medicalEquipment,HttpServletRequest request) throws Exception {
		medicalEquipment.setState("");
		medicalEquipment.setAuditResult("");
		medicalEquipment.setActionType("");
		QueryWrapper<MedicalEquipment> queryWrapper = QueryGenerator.initQueryWrapper(medicalEquipment, request.getParameterMap());
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
	public List<MedicalEquipment> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalEquipment> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalEquipment(MedicalEquipment bean) {
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalEquipment(MedicalEquipment bean) {
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
	public boolean isExistName(String productcode, String id) {
		QueryWrapper<MedicalEquipment> queryWrapper = new QueryWrapper<MedicalEquipment>();
		queryWrapper.eq("PRODUCTCODE", productcode);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalEquipment> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalEquipment(MedicalEquipment bean) {
		MedicalEquipment oldBean = this.baseMapper.selectById(bean.getId());
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

	public void updateBeanBatch(List<MedicalEquipment> list, List<MedicalEquipment> oldlist) {
		Field[] fields = MedicalEquipment.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalEquipment bean = list.get(i);
			MedicalEquipment oldBean = oldlist.get(i);
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
	public void delMedicalEquipment(MedicalEquipment bean) {
		List<MedicalEquipment> list =getListByIds(bean.getId());
		commonDelMedicalEquipmentBatch(bean, list);
	}

	//删除单条操作
	private void commonDelMedicalEquipmentBatch(MedicalEquipment bean, List<MedicalEquipment> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalEquipment oldBean : oldlist) {
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
	public int saveCleanMedicalEquipment(QueryWrapper<MedicalEquipment> queryWrapper, MedicalAuditLog bean) {
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
	public int delAllMedicalEquipment(QueryWrapper<MedicalEquipment> queryWrapper, MedicalEquipment bean)
			throws Exception {
		List<MedicalEquipment> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalEquipmentBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception {
		String mappingFieldStr = "orderId,productcode,productname,brandname,specificaioncode,specificaion,isPackageCode,isPackageName,productdiscription,equipmentParentcode,equipmentParentname,"
				+ "intendeduse,productArtno,codingsyetemname,unitsnumber,isDirectIdentifyCode,isDirectIdentifyName,isSameSaleIdentifyCode,isSameSaleIdentifyName,directIdentifyCode,"
				+ "equipmentclassification,equipmentClassOldcode,equipmentClassOldname,equipmentClassCode,equipmentClassName,registrantcode,registrantname,registrantenglishname,sfdaNo,productClassCode,productClassName,"
				+ "isDisposableCode,isDisposableName,maxreusetimes,isSterilepackageCode,isSterilepackageName,medicalinsurancecode,storageOperateDescrip,sizedescrip,primarykeycode,publicversionno,nmpaversiontime,commentnote,chargeClassCode,chargeClassName,createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalEquipment> list = new ArrayList<MedicalEquipment>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalEquipment.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalEquipment.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<String> codeSet = new HashSet<String>();
		List<MedicalEquipment> addList = new ArrayList<MedicalEquipment>();
		List<MedicalEquipment> olnyUpdateList = new ArrayList<MedicalEquipment>();
		List<MedicalEquipment> updateList = new ArrayList<MedicalEquipment>();
		List<MedicalEquipment> oldUpdateList = new ArrayList<MedicalEquipment>();
		List<MedicalEquipment> deleteList = new ArrayList<MedicalEquipment>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<String> codeExistSet = getAllCodeSet();//库中已存在的code
		System.out.println("校验开始："+DateUtils.now() );
		List<String> codesAdd = new ArrayList<>();//新增编码
		List<String> codesUpdate = new ArrayList<>();//修改编码
		List<String> codesDelete = new ArrayList<>();//删除编码
		Map<String,MedicalEquipment> addMap = new HashMap<>();
		Map<String,MedicalEquipment> updateMap = new HashMap<>();
		Map<String,MedicalEquipment> deleteMap = new HashMap<>();
		List<String> errorMsg =  new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			MedicalEquipment bean = list.get(i);
			if (bean.getOrderId()==null) {
				errorMsg.add("导入的数据中“流水号Id”不能为空，如：第" + (i + 2) + "行数据“流水号Id”为空");
			}
			if (StringUtils.isBlank(bean.getProductcode())) {
				errorMsg.add("导入的数据中“产品编码”不能为空，如：第" + (i + 2) + "行数据“产品编码”为空");
			}
			if (StringUtils.isBlank(bean.getProductname())) {
				errorMsg.add("导入的数据中“产品名称”不能为空，如：第" + (i + 2) + "行数据“产品名称”为空");
			}
			//判断code在excel中是否重复
			if(codeSet.contains(bean.getProductcode()+"")){
				errorMsg.add("导入的数据中“产品编码”不能重复，如：第" + (i + 2) + "行数据“"+bean.getProductcode()+"”在excel中重复");
			}
			if (StringUtils.isBlank(bean.getActionType())) {
				errorMsg.add("导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空");
			}
			if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(bean.getActionType())) {
				errorMsg.add("导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据");
			}
			if(StringUtils.isBlank(bean.getProductcode())){
				continue;
			}
			if("1".equals(bean.getActionType())) {//新增
				codesAdd.add(bean.getProductcode());
				addMap.put(bean.getProductcode(),bean);
			}else if("0".equals(bean.getActionType())) {//修改
				codesUpdate.add(bean.getProductcode());
				updateMap.put(bean.getProductcode(),bean);
			}else if("2".equals(bean.getActionType())) {//删除
				codesDelete.add(bean.getProductcode());
				deleteMap.put(bean.getProductcode(),bean);
			}
			codeSet.add(bean.getProductcode());
		}


		if(codesAdd.size()>0){
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalEquipment> existList = getBeanByCode(codesAdd,stateList,"CODE".split(","));
			List<String> existCode = existList.stream().map(MedicalEquipment::getProductcode).collect(Collectors.toList());
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
			List<MedicalEquipment> existList = getBeanByCode(codesUpdate,stateList,null);
			List<String> existCode = existList.stream().map(MedicalEquipment::getProductcode).collect(Collectors.toList());
			List<String> notExistCode = codesUpdate.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
			if(notExistCode.size()>0){
				errorMsg.add("导入的数据中，修改数据中包含系统中不存在或者无效的记录，如：[" +
						StringUtils.join(notExistCode, ",") + "]");
			}
			//待生效、有效 ->
			existList.forEach(oldBean ->{
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					if(MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(oldBean.getActionType())){
						errorMsg.add("导入的数据中，包含正在删除审核中的记录，如：“"+oldBean.getProductcode()+"”，不允许修改");
					}else if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
						errorMsg.add("导入的数据中，包含其他用户新增的待审核的记录，如：“"+oldBean.getProductcode()+"”，不允许修改");
					}else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
						errorMsg.add("导入的数据中，包含其他用户正在修改的待审核的记录，如：“"+oldBean.getProductcode()+"”，不允许修改");
					}
				}
			});
			if(errorMsg.size()==0) {
				existList.forEach(oldBean -> {
					MedicalEquipment bean = updateMap.get(oldBean.getProductcode());
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
			List<MedicalEquipment> existList = getBeanByCode(codesDelete,stateList,null);
			List<String> existCode = existList.stream().map(MedicalEquipment::getProductcode).collect(Collectors.toList());
			List<String> notExistCode = codesDelete.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
			if(notExistCode.size()>0){
				errorMsg.add("导入的数据中，删除数据中包含系统中不存在或者无效的记录，如：[" +
						StringUtils.join(notExistCode, ",") + "]");
			}
			//待生效、有效 ->
			existList.forEach(oldBean ->{
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					errorMsg.add("导入的数据中，删除数据中包含正在审核中的数据的记录，如：“"+oldBean.getProductcode()+"”，不允许删除");
				}
			});
			if(errorMsg.size()==0) {
				existList.forEach(oldBean -> {
					MedicalEquipment bean = deleteMap.get(oldBean.getProductcode());
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

	private List<MedicalEquipment> getBeanByCode(List<String> codes,List<String> stateList,String[] fileds){
		List<MedicalEquipment> alllist = new ArrayList<>();
		List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(codes,1000);
		for(Set<String> strList:setList){
			QueryWrapper<MedicalEquipment> queryWrapper = new QueryWrapper<MedicalEquipment>();
			queryWrapper.in("PRODUCTCODE",strList);
			queryWrapper.in("STATE", stateList);
			if(fileds!=null&&fileds.length>0){
				queryWrapper.select(fileds);
			}
			alllist.addAll(this.baseMapper.selectList(queryWrapper));
		}
		return  alllist;
	}

	private MedicalEquipment findBeanByCode(String productcode,String state) {
		QueryWrapper<MedicalEquipment> queryWrapper = new QueryWrapper<MedicalEquipment>();
		queryWrapper.eq("PRODUCTCODE", productcode);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalEquipment> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalEquipment> queryWrapper, OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			List<MedicalEquipment> list = this.list(queryWrapper);
			List<MedicalEquipment> dataList = new ArrayList<MedicalEquipment>();
	    	Map<String,List<Map<String,Object>>> typeMap = new HashMap<String,List<Map<String,Object>>>();
	    	for(MedicalEquipment exportBean:list){
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

	    	String titleStr = "流水号ID,产品编码,产品名称,商品名称,规格型号编码,规格型号,是否为包类/组套类产品编码,是否为包类/组套类产品名称,产品描述,产品父级分类编码,产品父级分类名称,"
	    			+ "预期用途,产品货号或编号,医疗器械唯一标识编码体系名称,最小销售单元中使用单元的数量,是否有本体直接标识编码,是否有本体直接标识名称,本体产品标识与最小销售单元产品标识是否一致编码,本体产品标识与最小销售单元产品标识是否一致名称,本体产品标识,"
	    			+ "器械类别,原分类编码,原分类名称,分类编码,分类名称,医疗器械注册人/备案人编码,医疗器械注册人/备案人名称,医疗器械注册人/备案人英文名称,注册证编号或者备案凭证编号,产品类别编码,产品类别名称,"
	    			+ "是否标记为一次性使用编码,是否标记为一次性使用名称,最大重复使用次数,是否为无菌包装编码,是否为无菌包装名称,医保编码,特殊储存或操作条件,特殊尺寸说明,主键编号,公开的版本号,NMPA版本的发布时间,备注,收费类别编码,收费类别名称,"
	    			+ "最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr ="orderId,productcode,productname,brandname,specificaioncode,specificaion,isPackageCode,isPackageName,productdiscription,equipmentParentcode,equipmentParentname,"
					+ "intendeduse,productArtno,codingsyetemname,unitsnumber,isDirectIdentifyCode,isDirectIdentifyName,isSameSaleIdentifyCode,isSameSaleIdentifyName,directIdentifyCode,"
					+ "equipmentclassification,equipmentClassOldcode,equipmentClassOldname,equipmentClassCode,equipmentClassName,registrantcode,registrantname,registrantenglishname,sfdaNo,productClassCode,productClassName,"
					+ "isDisposableCode,isDisposableName,maxreusetimes,isSterilepackageCode,isSterilepackageName,medicalinsurancecode,storageOperateDescrip,sizedescrip,primarykeycode,publicversionno,nmpaversiontime,commentnote,chargeClassCode,chargeClassName,"
	    			+ "actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalEquipment.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalEquipment.class,titles,fields,sheet, "");
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
	public MedicalEquipment getBeanByCode(String code) {
		QueryWrapper<MedicalEquipment> queryWrapper = new QueryWrapper<MedicalEquipment>();
		queryWrapper.eq("PRODUCTCODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalEquipment> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0);
		}
		return null;
	}
}
