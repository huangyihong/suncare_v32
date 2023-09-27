package com.ai.modules.config.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.*;
import com.alibaba.fastjson.JSONObject;
import net.sf.saxon.expr.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.mapper.MedicalAuditLogMapper;
import com.ai.modules.config.mapper.MedicalOtherDictMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @Description: 其他字典
 * @Author: jeecg-boot
 * @Date:   2019-12-18
 * @Version: V1.0
 */
@Service
public class MedicalOtherDictServiceImpl extends ServiceImpl<MedicalOtherDictMapper, MedicalOtherDict> implements IMedicalOtherDictService {

	@Autowired
	IMedicalAuditLogService logService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RedisUtil redisUtil;

	private static final String TABLE_NAME="MEDICAL_OTHER_DICT";//表名

	@Override
	public QueryWrapper<MedicalOtherDict> getQueryWrapper(MedicalOtherDict medicalOtherDict,HttpServletRequest request) throws Exception {
		medicalOtherDict.setState("");
		medicalOtherDict.setAuditResult("");
		medicalOtherDict.setActionType("");
		QueryWrapper<MedicalOtherDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalOtherDict, request.getParameterMap());
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
	public List<MedicalOtherDict> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalOtherDict> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalOtherDict(MedicalOtherDict bean) {
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalOtherDict(MedicalOtherDict bean) {
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
	public boolean isExistName(String code, String dictEname, String id) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("CODE", code);
//		queryWrapper.eq("DICT_CNAME", dictCname);
		queryWrapper.eq("DICT_ENAME", dictEname);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalOtherDict(MedicalOtherDict bean) {
		MedicalOtherDict oldBean = this.baseMapper.selectById(bean.getId());
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

	public void updateBeanBatch(List<MedicalOtherDict> list, List<MedicalOtherDict> oldlist) {
		Field[] fields = MedicalOtherDict.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalOtherDict bean = list.get(i);
			MedicalOtherDict oldBean = oldlist.get(i);
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
	public void delMedicalOtherDict(MedicalOtherDict bean) {
		List<MedicalOtherDict> list =getListByIds(bean.getId());
		commonDelMedicalOtherDictBatch(bean, list);
	}

	//删除单条操作
	private void commonDelMedicalOtherDictBatch(MedicalOtherDict bean, List<MedicalOtherDict> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalOtherDict oldBean : oldlist) {
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
	public int saveCleanMedicalOtherDict(QueryWrapper<MedicalOtherDict> queryWrapper, MedicalAuditLog bean) {
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
	public int delAllMedicalOtherDict(QueryWrapper<MedicalOtherDict> queryWrapper, MedicalOtherDict bean)
			throws Exception {
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalOtherDictBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception {
		String mappingFieldStr = "dictCname,dictEname,parentCode,parentValue,code,value,isOrder,remark,createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalOtherDict> list = new ArrayList<MedicalOtherDict>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalOtherDict.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalOtherDict.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<String> codeSet = new HashSet<String>();
		List<MedicalOtherDict> addList = new ArrayList<MedicalOtherDict>();
		List<MedicalOtherDict> olnyUpdateList = new ArrayList<MedicalOtherDict>();
		List<MedicalOtherDict> updateList = new ArrayList<MedicalOtherDict>();
		List<MedicalOtherDict> oldUpdateList = new ArrayList<MedicalOtherDict>();
		List<MedicalOtherDict> deleteList = new ArrayList<MedicalOtherDict>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<String> codeExistSet = getAllCodeSet();//库中已存在的code
		System.out.println("校验开始："+DateUtils.now() );
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			MedicalOtherDict bean = list.get(i);
			if (StringUtils.isBlank(bean.getDictCname())) {
		        message += "导入的数据中“字典中文名称”不能为空，如：第" + (i + 2) + "行数据“字典中文名称”为空\n";
		    	flag = false;
		    }
			if (StringUtils.isBlank(bean.getDictEname())) {
		        message += "导入的数据中“字典英文名称”不能为空，如：第" + (i + 2) + "行数据“字典英文名称”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getCode())) {
		        message += "导入的数据中“项目编码”不能为空，如：第" + (i + 2) + "行数据“项目编码”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getValue())) {
		        message += "导入的数据中“项目名称”不能为空，如：第" + (i + 2) + "行数据“项目名称”为空\n";
		    	flag = false;
		    }
		    //判断code在excel中是否重复
		    if(codeSet.contains(bean.getDictEname()+"&&"+bean.getCode())){
		    	message += "导入的数据中相同的字典的“项目编码”不能重复，如：第" + (i + 2) + "行数据字典英文名称为“"+bean.getDictEname()+"项目编码为“"+bean.getCode()+"”在excel中重复\n";
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
				if(isExistName(bean.getCode(),bean.getDictEname(),null)){
			    	message += "导入的数据中，新增数据中包含系统中待生效或者有效的数据，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”\n";
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
			    MedicalOtherDict oldBean = this.findBeanByEnCode(bean.getCode(),bean.getDictEname(),state);
			    if(oldBean==null){
			    	state = MedicalAuditLogConstants.STATE_WX;//无效
			    	oldBean = this.findBeanByEnCode(bean.getCode(),bean.getDictEname(),state);
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
				MedicalOtherDict oldBean = this.findBeanByEnCode(bean.getCode(),bean.getDictEname(),state);
				if(oldBean==null){
					state = MedicalAuditLogConstants.STATE_WX;//无效
			    	oldBean = this.findBeanByEnCode(bean.getCode(),bean.getDictEname(),state);
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
			codeSet.add(bean.getDictEname()+"&&"+bean.getCode());
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


	private MedicalOtherDict findBeanByEnCode(String code,String dictEname,String state) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("DICT_ENAME", dictEname);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public String queryValueByCode(String dictCname, String dictEname, String parentCode,
			String parentValue,String code) {
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)&&
				StringUtils.isBlank(parentCode)&&StringUtils.isBlank(parentValue)) {
			return code;
		}
		if(StringUtils.isBlank(code)) {
			return "";
		}
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		if(StringUtils.isNotBlank(dictCname)){
			queryWrapper.eq("DICT_CNAME", dictCname);
		}
		if(StringUtils.isNotBlank(dictEname)){
			queryWrapper.eq("DICT_ENAME", dictEname);
		}
		if(StringUtils.isNotBlank(parentCode)){
			queryWrapper.eq("PARENT_CODE", parentCode);
		}
		if(StringUtils.isNotBlank(parentValue)){
			queryWrapper.eq("PARENT_VALUE", parentValue);
		}
		if(StringUtils.isNotBlank(parentValue)){
			queryWrapper.eq("PARENT_VALUE", parentValue);
		}
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0).getValue();
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalOtherDict> queryWrapper,OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			List<MedicalOtherDict> list = this.list(queryWrapper);
	        List<MedicalOtherDict> dataList = new ArrayList<MedicalOtherDict>();
	    	Map<String,List<Map<String,Object>>> typeMap = new HashMap<String,List<Map<String,Object>>>();
	    	for(MedicalOtherDict exportBean:list){
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
	    	String titleStr = "字典中文名称,字典英文名称,项目父级编码,项目父级名称,项目编码,项目名称,排序序号,备注,新增批次号,"
	    			+ "最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "dictCname,dictEname,parentCode,parentValue,code,value,isOrder,remark,importBatch,"
					+ "actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalOtherDict.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalOtherDict.class,titles,fields,sheet, "");
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
	public List<Map<String, Object>> getTypeList(String dictEname,String typeCode) {
//		String sql = "select t.code,t.value from (select code,value,parent_code from medical_other_dict where dict_ename=? ) t start with t.code=? connect by prior t.parent_code=t.code order by level desc";
//		return jdbcTemplate.queryForList(sql,dictEname,typeCode);
		String sql = "SELECT\n" +
				"	t.CODE,\n" +
				DbDataEncryptUtil.decryptFunc("t.VALUE")+" VALUE,\n" +
				"	t.PARENT_CODE\n" +
				"FROM\n" +
				"	medical_other_dict t\n" +
				"	JOIN ( SELECT oDictTreePids (?, ? ) codes) t2 ON find_in_set( t.CODE, t2.codes ) \n" +
				"WHERE\n" +
				"	t.DICT_ENAME = ? order by PARENT_CODE";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,typeCode, dictEname, dictEname);
		if(list==null||list.size()<2){
			return list;
		}
		//根据PARENT_CODE排序
		return (List<Map<String, Object>>) list.stream()
				.filter(d-> StringUtils.isBlank((String)d.get("PARENT_CODE"))||"0".equals((String)d.get("PARENT_CODE")))
				.flatMap(d-> Stream.concat(Stream.of(d), getChildNode(d,list))).collect(Collectors.toList());
	}

	private Stream<Map<String, Object>> getChildNode(Map<String, Object> root, List<Map<String, Object>> alllist) {
		return alllist.stream()
				.filter(d-> Objects.equals((String)d.get("PARENT_CODE"),(String)root.get("CODE")))
				.flatMap(d->Stream.concat(Stream.of(d), getChildNode(d,alllist)));
	}

	@Override
	//@Cacheable(value = CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname")
	public List<MedicalOtherDict> getOtherDictListByDictEname(String dictEname) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("DICT_ENAME", dictEname);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		return list;
	}

	@Override
	@Cacheable(value = CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':'+#code")
	public String getValueByCode(String dictEname, String code) {
		if (code == null || "".equals(code)) {
			return null;
		}
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("DICT_ENAME", dictEname);
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0).getValue();
		}
		return null;
	}

	@Override
	@Cacheable(value = CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':'+#value")
	public String getCodeByValue(String dictEname, String value) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("DICT_ENAME", dictEname);
		queryWrapper.eq(DbDataEncryptUtil.decryptFunc("VALUE"), value);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0).getCode();
		}
		return null;
	}

	@Override
	public Map<String, String> queryNameMapByType(String dictEname) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>()
				.eq("STATE", MedicalAuditLogConstants.STATE_YX)
				.eq("DICT_ENAME", dictEname);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		Map<String, String> map = new HashMap<>();
		for(MedicalOtherDict bean: list){
			map.put(bean.getValue(), bean.getCode());
		}
		return map;
	}

	@Override
	public Map<String, String> queryMapByType(String dictEname) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>()
				.eq("STATE", MedicalAuditLogConstants.STATE_YX)
				.eq("DICT_ENAME", dictEname);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		Map<String, String> map = new HashMap<>();
		for(MedicalOtherDict bean: list){
			map.put(bean.getCode(), bean.getValue());
		}
		return map;
	}

	@Override
	@CacheEvict(value=CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':'+#code")
	public void clearCacheByCode(String dictEname, String code) {

	}

	@Override
	@CacheEvict(value=CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':'+#value")
	public void clearCacheByValue(String dictEname, String value) {

	}

	@Override
	public Map<String, String> getMapByCode(String dictEname, String codes) {
		Map<String, String> map = new HashMap<>();
		List<String> valueList = Arrays.asList(codes.split(","));
		if(valueList.size()>1000){
			List<List<String>> setList = getBatchList(valueList,1000);
			for(List<String> strList:setList){
				queryListByCodes(strList, map, dictEname);
			}
		}else{
			queryListByCodes(valueList, map, dictEname);
		}
		return map;
	}

	@Override
	public Map<String, String> getMapByValue(String dictEname, String values) {
		Map<String, String> map = new HashMap<>();
		List<String> valueList = Arrays.asList(values.split(","));
		if(valueList.size()>1000){
			List<List<String>> setList = getBatchList(valueList,1000);
			for(List<String> strList:setList){
				queryListByValues(strList, map, dictEname);
			}
		}else{
			queryListByValues(valueList, map, dictEname);
		}
		return map;
	}

	private void queryListByCodes(List<String> valueList, Map<String, String> map,String dictEname) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("DICT_ENAME", dictEname);
		queryWrapper.in("CODE", valueList);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalOtherDict bean:list){
			map.put(bean.getCode(),bean.getValue());
		}
	}

	private void queryListByValues(List<String> valueList, Map<String, String> map,String dictEname) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		queryWrapper.eq("DICT_ENAME", dictEname);
		queryWrapper.in("VALUE", valueList);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalOtherDict bean:list){
			map.put(bean.getValue(),bean.getCode());
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

	@Override
	//@Cacheable(value = CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':TreeList'")
	public List<JSONObject> getTreeAllList(String dictCname, String dictEname) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		if(StringUtils.isNotBlank(dictCname)) {
			queryWrapper.eq("DICT_CNAME", dictCname);
		}
		if(StringUtils.isNotBlank(dictEname)) {
			queryWrapper.eq("DICT_ENAME", dictEname);
		}
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		queryWrapper.orderByAsc("CODE");
		queryWrapper.select("VALUE","CODE","PARENT_CODE");
		List<MedicalOtherDict> allList = this.baseMapper.selectList(queryWrapper);
//		//所有的一级 ,parentId = 0 或者为空
//		List<JSONObject> parentList = allList.stream()
//				.filter(item ->  StringUtils.isBlank(item.getParentCode())||"0".equals(item.getParentCode()))
//				.map(item -> {
//					JSONObject node = new JSONObject();
//					node.put("label",item.getValue());
//					node.put("value",item.getCode());
//					node.put("children",this.getChildrens(item, allList));
//					return node;
//				})
//				.collect(Collectors.toList());

		//上面递归遍历太慢  改为 Map存储找对应关系
		List<JSONObject> parentList2 = arrayToTree(allList);
		return parentList2;
	}

	private List<JSONObject> arrayToTree(List<MedicalOtherDict> items) {
		List result = new ArrayList();// 存放结果集
		JSONObject itemMap = new JSONObject();
		for(MedicalOtherDict item:items){
			String id = item.getCode();
			String pid = item.getParentCode();
			JSONObject node = (JSONObject) itemMap.get(id);
			if(node==null){
				node = new JSONObject();
				node.put("children",new ArrayList<>());
			}
			node.put("label",item.getValue());
			node.put("value",item.getCode());
			itemMap.put(id,node);
			if( StringUtils.isBlank(pid)||"0".equals(pid)){
				result.add(node);
			}else{
				JSONObject pnode = (JSONObject) itemMap.get(pid);
				if (pnode==null) {
					pnode  = new JSONObject();
					pnode.put("children",new ArrayList<>());
				}
				((ArrayList) pnode.get("children")).add(node);
			}
		}
		return result;
	}

	private List<JSONObject> getChildrens(MedicalOtherDict root, List<MedicalOtherDict> allList){
		List<JSONObject> treeMenus = allList.stream()
				//如果菜单中的父菜单Id == 当前菜单的id，则说明是子菜单
				.filter(item ->  Objects.equals(item.getParentCode(), root.getCode()))
				.map(item -> {
					JSONObject node = new JSONObject();
					node.put("label",item.getValue());
					node.put("value",item.getCode());
					node.put("children",this.getChildrens(item, allList));
					return node;
				})
				.collect(Collectors.toList());
		return treeMenus;
	}

}
