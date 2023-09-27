package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDiseaseDiag;
import com.ai.modules.config.mapper.MedicalDiseaseDiagMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalDiseaseDiagService;
import com.ai.modules.config.service.IMedicalOtherDictService;
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
 * @Description: ICD国际疾病维护
 * @Author: jeecg-boot
 * @Date:   2019-12-27
 * @Version: V1.0
 */
@Service
public class MedicalDiseaseDiagServiceImpl extends ServiceImpl<MedicalDiseaseDiagMapper, MedicalDiseaseDiag> implements IMedicalDiseaseDiagService {
	@Autowired
	IMedicalAuditLogService logService;

	@Autowired
	IMedicalOtherDictService otherDictService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final String TABLE_NAME="MEDICAL_DISEASE_DIAG";//表名

	private static final String DICT_ENAME="STD_ICDClassify";//字典名称

	@Override
	public QueryWrapper<MedicalDiseaseDiag> getQueryWrapper(MedicalDiseaseDiag medicalDiseaseDiag,HttpServletRequest request) throws Exception {
		medicalDiseaseDiag.setState("");
		medicalDiseaseDiag.setAuditResult("");
		medicalDiseaseDiag.setActionType("");
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = QueryGenerator.initQueryWrapper(medicalDiseaseDiag, request.getParameterMap());
		String typeCode_1 = request.getParameter("typeCode_1");
		if(StringUtils.isNotBlank(typeCode_1)&&!"0".equals(typeCode_1)){
			queryWrapper.and(wrapper -> wrapper.eq("TYPE1_CODE", typeCode_1).or().eq("TYPE2_CODE", typeCode_1).or().
												eq("TYPE3_CODE", typeCode_1).or().eq("TYPE4_CODE", typeCode_1).or().eq("TYPE_CODE", typeCode_1));
		}
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
	public List<MedicalDiseaseDiag> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalDiseaseDiag> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalDiseaseDiag(MedicalDiseaseDiag bean) {
		setTypeCodes(bean,null,null);//设置各级分类编码和分类名称
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	private void setTypeCodes(MedicalDiseaseDiag bean,Map<String,List<Map<String,Object>>> typeMap,List<Map<String,Object>> typeList) {
		String typeCode = bean.getTypeCode();
		if(typeMap==null){
			typeMap = new HashMap<String,List<Map<String,Object>>>();
		}
		if(typeList==null||typeList.size()==0){
			typeList = otherDictService.getTypeList(DICT_ENAME,typeCode);
			if(typeList==null){
				return;
			}
			typeMap.put(typeCode, typeList);
		}
		for(int i=0;i<typeList.size();i++){
			String code = (String)typeList.get(i).get("CODE");
			String name = (String)typeList.get(i).get("VALUE");
			if(i==0){
				bean.setType1Code(code);
				bean.setType1Name(name);
			}
			if(i==1){
				bean.setType2Code(code);
				bean.setType2Name(name);
			}
			if(i==2){
				bean.setType3Code(code);
				bean.setType3Name(name);
			}
			if(i==3){
				bean.setType4Code(code);
				bean.setType4Name(name);
			}
		}
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalDiseaseDiag(MedicalDiseaseDiag bean) {
		setTypeCodes(bean,null,null);//设置各级分类编码和分类名称
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
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = new QueryWrapper<MedicalDiseaseDiag>();
		queryWrapper.eq("CODE", code);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalDiseaseDiag> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalDiseaseDiag(MedicalDiseaseDiag bean) {
		setTypeCodes(bean,null,null);//设置各级分类编码和分类名称
		MedicalDiseaseDiag oldBean = this.baseMapper.selectById(bean.getId());
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
	public void updateBeanBatch(List<MedicalDiseaseDiag> list, List<MedicalDiseaseDiag> oldlist) {
		Field[] fields = MedicalDiseaseDiag.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalDiseaseDiag bean = list.get(i);
			MedicalDiseaseDiag oldBean = oldlist.get(i);
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
	public void delMedicalDiseaseDiag(MedicalDiseaseDiag bean) {
		List<MedicalDiseaseDiag> list =getListByIds(bean.getId());
		commonDelMedicalDiseaseDiagBatch(bean, list);
	}

	//删除单条操作
	private void commonDelMedicalDiseaseDiagBatch(MedicalDiseaseDiag bean, List<MedicalDiseaseDiag> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalDiseaseDiag oldBean : oldlist) {
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
	public int saveCleanMedicalDiseaseDiag(QueryWrapper<MedicalDiseaseDiag> queryWrapper, MedicalAuditLog bean) {
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
	public int delAllMedicalDiseaseDiag(QueryWrapper<MedicalDiseaseDiag> queryWrapper, MedicalDiseaseDiag bean)
			throws Exception {
		List<MedicalDiseaseDiag> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalDiseaseDiagBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception {
		String mappingFieldStr = "typeCode,typeName,code,name,remark,createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalDiseaseDiag> list = new ArrayList<MedicalDiseaseDiag>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalDiseaseDiag.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalDiseaseDiag.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<String> codeSet = new HashSet<String>();
		List<MedicalDiseaseDiag> addList = new ArrayList<MedicalDiseaseDiag>();
		List<MedicalDiseaseDiag> olnyUpdateList = new ArrayList<MedicalDiseaseDiag>();
		List<MedicalDiseaseDiag> updateList = new ArrayList<MedicalDiseaseDiag>();
		List<MedicalDiseaseDiag> oldUpdateList = new ArrayList<MedicalDiseaseDiag>();
		List<MedicalDiseaseDiag> deleteList = new ArrayList<MedicalDiseaseDiag>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<String> codeExistSet = getAllCodeSet();//库中已存在的code
		System.out.println("校验开始："+DateUtils.now() );
		List<String> codesAdd = new ArrayList<>();//新增编码
		List<String> codesUpdate = new ArrayList<>();//修改编码
		List<String> codesDelete = new ArrayList<>();//删除编码
		Map<String,MedicalDiseaseDiag> addMap = new HashMap<>();
		Map<String,MedicalDiseaseDiag> updateMap = new HashMap<>();
		Map<String,MedicalDiseaseDiag> deleteMap = new HashMap<>();
		List<String> errorMsg =  new ArrayList<>();
		Map<String,List<Map<String,Object>>> typeMap = new HashMap<String,List<Map<String,Object>>>();
		for (int i = 0; i < list.size(); i++) {
			MedicalDiseaseDiag bean = list.get(i);
			if (StringUtils.isBlank(bean.getTypeCode())) {
				errorMsg.add("导入的数据中“父级分类编码”不能为空，如：第" + (i + 2) + "行数据“父级分类编码”为空");
			}
			if (StringUtils.isBlank(bean.getCode())) {
				errorMsg.add("导入的数据中“疾病编码”不能为空，如：第" + (i + 2) + "行数据“疾病编码”为空");
			}
			if (StringUtils.isBlank(bean.getName())) {
				errorMsg.add("导入的数据中“疾病名称”不能为空，如：第" + (i + 2) + "行数据“疾病名称”为空");
			}
			//判断code在excel中是否重复
			if(codeSet.contains(bean.getCode())){
				errorMsg.add("导入的数据中“疾病编码”不能重复，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”在excel中重复");
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
			List<MedicalDiseaseDiag> existList = getBeanByCode(codesAdd,stateList,"CODE".split(","));
			List<String> existCode = existList.stream().map(MedicalDiseaseDiag::getCode).collect(Collectors.toList());
			if(existCode.size()>0){
				errorMsg.add("导入的数据中，新增数据中包含系统中待生效或者有效的数据，如：[" +
						StringUtils.join(existCode, ",") + "]");
			}
			if(errorMsg.size()==0){
				addMap.forEach((k, bean) -> {
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);

					/**设置各级目录 start**/
					String typeCode = bean.getTypeCode();
					List<Map<String,Object>> typeList = typeMap.get(typeCode);
					this.setTypeCodes(bean,typeMap,typeList);
					/**设置各级目录 end**/

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
			List<MedicalDiseaseDiag> existList = getBeanByCode(codesUpdate,stateList,null);
			List<String> existCode = existList.stream().map(MedicalDiseaseDiag::getCode).collect(Collectors.toList());
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
					MedicalDiseaseDiag bean = updateMap.get(oldBean.getCode());
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
					bean.setUpdateReason(bean.getCreateReason());
					bean.setCreateReason(oldBean.getCreateReason());

					/**设置各级目录 start**/
					String typeCode = bean.getTypeCode();
					List<Map<String,Object>> typeList = typeMap.get(typeCode);
					this.setTypeCodes(bean,typeMap,typeList);
					/**设置各级目录 end**/

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
			List<MedicalDiseaseDiag> existList = getBeanByCode(codesDelete,stateList,null);
			List<String> existCode = existList.stream().map(MedicalDiseaseDiag::getCode).collect(Collectors.toList());
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
					MedicalDiseaseDiag bean = deleteMap.get(oldBean.getCode());
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

	private List<MedicalDiseaseDiag> getBeanByCode(List<String> codes,List<String> stateList,String[] fileds){
		List<MedicalDiseaseDiag> alllist = new ArrayList<>();
		List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(codes,1000);
		for(Set<String> strList:setList){
			QueryWrapper<MedicalDiseaseDiag> queryWrapper = new QueryWrapper<MedicalDiseaseDiag>();
			queryWrapper.in("CODE",strList);
			queryWrapper.in("STATE", stateList);
			if(fileds!=null&&fileds.length>0){
				queryWrapper.select(fileds);
			}
			alllist.addAll(this.baseMapper.selectList(queryWrapper));
		}
		return  alllist;
	}

	private MedicalDiseaseDiag findBeanByCode(String code,String state) {
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = new QueryWrapper<MedicalDiseaseDiag>();
		queryWrapper.eq("CODE", code);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalDiseaseDiag> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalDiseaseDiag> queryWrapper, OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			List<MedicalDiseaseDiag> list = this.list(queryWrapper);
			List<MedicalDiseaseDiag> dataList = new ArrayList<MedicalDiseaseDiag>();
	    	for(MedicalDiseaseDiag exportBean:list){
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
	    	String titleStr = "章组编码,章名称,节编码,节名称,类目编码,类目名称,亚目编码,亚目名称,"
	    			+ "疾病编码,疾病名称,描述,最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "type1Code,type1Name,type2Code,type2Name,type3Code,type3Name,type4Code,type4Name,"
	    			+ "code,name,remark,actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalDiseaseDiag.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalDiseaseDiag.class,titles,fields,sheet, "");
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
