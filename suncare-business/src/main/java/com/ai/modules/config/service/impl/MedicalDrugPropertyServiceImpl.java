package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrugProperty;
import com.ai.modules.config.mapper.MedicalDrugPropertyMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalDrugPropertyService;
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

/**
 * @Description: 药品属性表
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Service
public class MedicalDrugPropertyServiceImpl extends ServiceImpl<MedicalDrugPropertyMapper, MedicalDrugProperty> implements IMedicalDrugPropertyService {
	@Autowired
	IMedicalAuditLogService logService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final String TABLE_NAME="MEDICAL_DRUG_PROPERTY";//表名

	@Override
	public QueryWrapper<MedicalDrugProperty> getQueryWrapper(MedicalDrugProperty medicalDrugProperty,HttpServletRequest request) throws Exception {
		medicalDrugProperty.setState("");
		medicalDrugProperty.setAuditResult("");
		medicalDrugProperty.setActionType("");
		QueryWrapper<MedicalDrugProperty> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugProperty, request.getParameterMap());
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
	public List<MedicalDrugProperty> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalDrugProperty> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalDrugProperty(MedicalDrugProperty bean) {
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalDrugProperty(MedicalDrugProperty bean) {
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
	public boolean isExistOrderNum(int orderNum, String id) {
		QueryWrapper<MedicalDrugProperty> queryWrapper = new QueryWrapper<MedicalDrugProperty>();
		queryWrapper.eq("ORDER_NUM", orderNum);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalDrugProperty> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalDrugProperty(MedicalDrugProperty bean) {
		MedicalDrugProperty oldBean = this.baseMapper.selectById(bean.getId());
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

	public void updateBeanBatch(List<MedicalDrugProperty> list, List<MedicalDrugProperty> oldlist) {
		Field[] fields = MedicalDrugProperty.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalDrugProperty bean = list.get(i);
			MedicalDrugProperty oldBean = oldlist.get(i);
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
	public void delMedicalDrugProperty(MedicalDrugProperty bean) {
		List<MedicalDrugProperty> list =getListByIds(bean.getId());
		commonDelMedicalDrugPropertyBatch(bean, list);
	}

	//删除单条操作
	private void commonDelMedicalDrugPropertyBatch(MedicalDrugProperty bean, List<MedicalDrugProperty> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalDrugProperty oldBean : oldlist) {
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
	public int saveCleanMedicalDrugProperty(QueryWrapper<MedicalDrugProperty> queryWrapper, MedicalAuditLog bean) {
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
	public int delAllMedicalDrugProperty(QueryWrapper<MedicalDrugProperty> queryWrapper, MedicalDrugProperty bean)
			throws Exception {
		List<MedicalDrugProperty> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalDrugPropertyBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception {
		String mappingFieldStr = "orderNum,code,name,dosageCode,dosageName,specificaion,presdrugSign,presdrugSignname,nonrxtypecode,nonrxtypename,"
				+ "poisonousSign,poisonousSignname,narcoticSign,narcoticSignname,psych1Sign,psych1Signname,psych2Sign,psych2Signname,psych3Sign,psych3Signname,"
				+ "biologicSign,biologicSignname,bloodprodSign,bloodprodSignname,radioSign,radioSignname,createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalDrugProperty> list = new ArrayList<MedicalDrugProperty>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalDrugProperty.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalDrugProperty.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<Integer> orderNumSet = new HashSet<Integer>();
		List<MedicalDrugProperty> addList = new ArrayList<MedicalDrugProperty>();
		List<MedicalDrugProperty> olnyUpdateList = new ArrayList<MedicalDrugProperty>();
		List<MedicalDrugProperty> updateList = new ArrayList<MedicalDrugProperty>();
		List<MedicalDrugProperty> oldUpdateList = new ArrayList<MedicalDrugProperty>();
		List<MedicalDrugProperty> deleteList = new ArrayList<MedicalDrugProperty>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<Integer> orderNumExistSet = getAllOrderNumSet();//库中已存在的序号
		System.out.println("校验开始："+DateUtils.now() );
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			MedicalDrugProperty bean = list.get(i);
			if (bean.getOrderNum()==null) {
		        message += "导入的数据中“序号”不能为空，如：第" + (i + 2) + "行数据“序号”为空\n";
		    	flag = false;
		    }
			if (StringUtils.isBlank(bean.getCode())) {
		        message += "导入的数据中“ATC药品编码”不能为空，如：第" + (i + 2) + "行数据“ATC药品编码”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getName())) {
		        message += "导入的数据中“ATC药品名称”不能为空，如：第" + (i + 2) + "行数据“ATC药品名称”为空\n";
		    	flag = false;
		    }
		    //判断序号在excel中是否重复
		    if(orderNumSet.contains(bean.getOrderNum())){
		    	message += "导入的数据中“序号”不能重复，如：第" + (i + 2) + "行数据“序号"+bean.getOrderNum()+"”在excel中重复\n";
		    	flag = false;
		    }
			if (StringUtils.isBlank(bean.getActionType())) {
		        message += "导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空\n";
		    	flag = false;
		    }
			if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(bean.getActionType())) {
				message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
				flag = false;
			}
			if(!flag) {
		    	 continue;
		    }
			if("1".equals(bean.getActionType())) {//新增
				if(isExistOrderNum(bean.getOrderNum(),null)){
			    	message += "导入的数据中，新增数据中包含系统中待生效或者有效的序号数据，如：第" + (i + 2) + "行数据“序号"+bean.getOrderNum()+"”\n";
			    	flag = false;
			    }
			    if(!flag) {
			    	 continue;
			    }
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
			}else if("0".equals(bean.getActionType())) {//修改
				String state = MedicalAuditLogConstants.STATE_DSX+","+MedicalAuditLogConstants.STATE_YX;//待生效、有效
				MedicalDrugProperty oldBean = this.findBeanByOrderNum(bean.getOrderNum(),state);
			    if(oldBean==null){
			    	state = MedicalAuditLogConstants.STATE_WX;//无效
			    	oldBean = this.findBeanByOrderNum(bean.getOrderNum(),state);
			    	if(oldBean==null){
			    		message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，不允许修改\n";
				    	flag = false;
			    	}else{
			    		message += "导入的数据中，包含数据状态为无效的数据，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，不允许修改\n";
				    	flag = false;
			    	}
			    }else {
			    	if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
				    	if(MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(oldBean.getActionType())){
				    		message += "导入的数据中，包含正在删除审核中的记录，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，不允许修改\n";
					    	flag = false;
				    	}else if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
				    		message += "导入的数据中，包含其他用户新增的待审核的记录，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，不允许修改\n";
					    	flag = false;
				    	}else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
				    		message += "导入的数据中，包含其他用户正在修改的待审核的记录，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，不允许修改\n";
					    	flag = false;
				    	}
				    }
			    	if(!flag) {
				    	 continue;
				    }
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
			    }
			}else if("2".equals(bean.getActionType())) {//删除
				String state = MedicalAuditLogConstants.STATE_DSX+","+MedicalAuditLogConstants.STATE_YX;//待生效、有效
				MedicalDrugProperty oldBean = this.findBeanByOrderNum(bean.getOrderNum(),state);
				if(oldBean==null){
					state = MedicalAuditLogConstants.STATE_WX;//无效
			    	oldBean = this.findBeanByOrderNum(bean.getOrderNum(),state);
			    	if(oldBean==null){
			    		message += "导入的数据中，包含在系统中不存在的记录，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，无法删除\n";
				    	flag = false;
			    	}else{
			    		message += "导入的数据中，包含数据状态为无效的数据，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，无效数据无需删除，请进行清理操作\n";
				    	flag = false;
			    	}
				}else {
					if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
						message += "导入的数据中，包含正在审核中的数据的记录，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”，不允许删除\n";
				    	flag = false;
				    }
					if(!flag) {
				    	 continue;
				    }
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
				}
			}
			orderNumSet.add(bean.getOrderNum());
		}
		if(StringUtils.isNotBlank(message)){
			message +="请核对数据后进行批量导入。";
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

	private MedicalDrugProperty findBeanByOrderNum(int orderNum,String state) {
		QueryWrapper<MedicalDrugProperty> queryWrapper = new QueryWrapper<MedicalDrugProperty>();
		queryWrapper.eq("ORDER_NUM", orderNum);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalDrugProperty> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalDrugProperty> queryWrapper, OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			List<MedicalDrugProperty> list = this.list(queryWrapper);
			List<MedicalDrugProperty> dataList = new ArrayList<MedicalDrugProperty>();
	    	Map<String,List<Map<String,Object>>> typeMap = new HashMap<String,List<Map<String,Object>>>();
	    	for(MedicalDrugProperty exportBean:list){
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

	    	String titleStr = "序号,ATC药品编码,ATC药品名称,剂型代码,剂型名称,规格,处方药标志,处方药标志名称,"
	    			+ "非处方药分类,非处方药分类名称,毒性药品标志,毒性药品标志名称,麻醉药品标志,麻醉药品标志名称,"
	    			+ "一类精神药品标志,一类精神药品标志名称,二类精神药品标志,二类精神药品标志名称,三类精神药品标志,三类精神药品标志名称,"
	    			+ "生物制品标志,生物制品标志名称,血液制品类标志,血液制品类标志名称,放射性药品标志,放射性药品标志名称,"
	    			+ "描述,最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr ="orderNum,code,name,dosageCode,dosageName,specificaion,presdrugSign,presdrugSignname,"
	    			+ "nonrxtypecode,nonrxtypename,poisonousSign,poisonousSignname,narcoticSign,narcoticSignname,"
	    			+ "psych1Sign,psych1Signname,psych2Sign,psych2Signname,psych3Sign,psych3Signname,"
					+ "biologicSign,biologicSignname,bloodprodSign,bloodprodSignname,radioSign,radioSignname,"
					+ "actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalDrugProperty.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalDrugProperty.class,titles,fields,sheet, "");
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
	public int getMaxOrderNum() {
//		String sql = "select nvl(max(order_num),0) as order_num  from MEDICAL_DRUG_PROPERTY";
		String sql = "select ifnull(max(order_num),0) as ORDER_NUM  from MEDICAL_DRUG_PROPERTY";
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}
}
