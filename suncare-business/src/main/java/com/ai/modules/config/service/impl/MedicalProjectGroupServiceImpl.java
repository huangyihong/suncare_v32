package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrugGroup;
import com.ai.modules.config.entity.MedicalProjectGroup;
import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.ai.modules.config.mapper.MedicalProjectGroupItemMapper;
import com.ai.modules.config.mapper.MedicalProjectGroupMapper;
import com.ai.modules.config.service.IMedicalProjectGroupService;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


/**
 * @Description: 医疗项目组
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Service
public class MedicalProjectGroupServiceImpl extends ServiceImpl<MedicalProjectGroupMapper, MedicalProjectGroup> implements IMedicalProjectGroupService {

	@Autowired
    MedicalProjectGroupItemMapper medicalProjectGroupItemMapper;

	@Autowired
	MedicalProjectGroupItemServiceImpl medicalProjectGroupItemService;

    @Override
    @Transactional
    public void saveGroup(MedicalProjectGroup medicalProjectGroup, String codes, String names, String tableTypes) {
        this.save(medicalProjectGroup);
        String groupId = medicalProjectGroup.getGroupId();
        // 插入子项
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            String[] tableTypeArray = tableTypes.split(",");
            MedicalProjectGroupItem ProjectGroupItem = new MedicalProjectGroupItem();
            ProjectGroupItem.setGroupId(groupId);
            for(int i = 0, len = codeArray.length; i < len; i++){
            	ProjectGroupItem.setItemId(IdUtils.uuid());
            	ProjectGroupItem.setIsOrder((long)i);
            	ProjectGroupItem.setCode(codeArray[i]);
            	ProjectGroupItem.setValue(nameArray[i]);
            	ProjectGroupItem.setTableType(tableTypeArray[i]);
            	this.medicalProjectGroupItemMapper.insert(ProjectGroupItem);
            }
        }
    }

    @Override
    @Transactional
    public void updateGroup(MedicalProjectGroup medicalProjectGroup, String codes, String names, String tableTypes) {
        String groupId = medicalProjectGroup.getGroupId();
        this.updateById(medicalProjectGroup);
        // 删除子项
        this.medicalProjectGroupItemMapper.delete(new QueryWrapper<MedicalProjectGroupItem>()
                .eq("GROUP_ID", groupId));
        // 插入子项
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            String[] tableTypeArray = tableTypes.split(",");
            MedicalProjectGroupItem ProjectGroupItem = new MedicalProjectGroupItem();
            ProjectGroupItem.setGroupId(groupId);
            for(int i = 0, len = codeArray.length; i < len; i++){
            	ProjectGroupItem.setItemId(IdUtils.uuid());
            	ProjectGroupItem.setIsOrder((long)i);
            	ProjectGroupItem.setCode(codeArray[i]);
            	ProjectGroupItem.setValue(nameArray[i]);
            	ProjectGroupItem.setTableType(tableTypeArray[i]);
                this.medicalProjectGroupItemMapper.insert(ProjectGroupItem);
            }
        }
    }

	@Override
	@Transactional
	public void removeGroupById(String id) {
		this.removeById(id);
		 // 删除子项
        this.medicalProjectGroupItemMapper.delete(new QueryWrapper<MedicalProjectGroupItem>()
                .eq("GROUP_ID", id));
	}

	@Override
	@Transactional
	public void removeGroupByIds(List<String> idList) {
		// TODO Auto-generated method stub
		this.removeByIds(idList);
		// 删除子项
		this.medicalProjectGroupItemMapper.delete(new QueryWrapper<MedicalProjectGroupItem>()
	                .in("GROUP_ID",idList));
	}

	@Override
	public List<MedicalGroupVO> queryGroupItem(MedicalGroupVO bean) {
		return this.baseMapper.queryGroupItem(bean);
	}

	@Override
	public int queryGroupItemCount(MedicalGroupVO bean) {
		return this.baseMapper.queryGroupItemCount(bean);
	}

	@Override
	public boolean exportExcel(List<MedicalGroupVO> list, OutputStream os, String suffix) throws Exception{
		boolean isSuc = true;

    	String titleStr = "医疗服务项目组编码,医疗服务项目组名称,YX_医疗服务项目编码,YX_医疗服务项目名称,子项所属表,更改标识(1新增0修改2删除)";
		String[] titles= titleStr.split(",");
		String fieldStr = "groupCode,groupName,code,value,tableType,actionType";
		String[] fields=fieldStr.split(",");
		if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
			SXSSFWorkbook workbook = new SXSSFWorkbook();
		    ExportXUtils.exportExl(list,MedicalGroupVO.class,titles,fields,workbook,"医疗服务项目组");
		    workbook.write(os);
	        workbook.dispose();
		}else {
			 // 创建文件输出流
	        WritableWorkbook wwb = Workbook.createWorkbook(os);
	        WritableSheet sheet = wwb.createSheet("医疗服务项目组", 0);
			ExportUtils.exportExl(list,MedicalGroupVO.class,titles,fields,sheet, "");
			wwb.write();
	        wwb.close();
		}
    	return isSuc;
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception {
		String mappingFieldStr = "groupCode,groupName,code,value,tableType,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return importExcel(file, user,mappingFields);

	}

	private Result<?> importExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalGroupVO> list = new ArrayList<MedicalGroupVO>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalGroupVO.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalGroupVO.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		Set<String> codeSet = new HashSet<String>();
		Set<String> groupNameSet = new HashSet<String>();
		//20220414太慢
		//Set<String> groupCodeItemCodeExistSet = this.getAllGroupCodeAndItemCode();
		Map<String,Set<String>> groupCodeItemCodeExistSet = new HashMap<>();
		List<MedicalGroupVO> addList = new ArrayList<MedicalGroupVO>();
		List<MedicalGroupVO> updateList = new ArrayList<MedicalGroupVO>();
		List<MedicalGroupVO> deleteList = new ArrayList<MedicalGroupVO>();
		Map<String, MedicalProjectGroup> groupMap = new HashMap<String,MedicalProjectGroup>();
		System.out.println("校验开始："+DateUtils.now() );
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			MedicalGroupVO bean = list.get(i);
			 if (StringUtils.isBlank(bean.getGroupCode())) {
		        message += "导入的数据中“项目组编码”不能为空，如：第" + (i + 2) + "行数据“项目组编码”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getGroupName())) {
		        message += "导入的数据中“项目组名称”不能为空，如：第" + (i + 2) + "行数据“项目组名称”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getCode())) {
		        message += "导入的数据中“医疗服务项目编码”不能为空，如：第" + (i + 2) + "行数据“医疗服务项目编码”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getValue())) {
		        message += "导入的数据中“医疗服务项目名称”不能为空，如：第" + (i + 2) + "行数据“医疗服务项目名称”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getTableType())) {
		        message += "导入的数据中“子项指向表”不能为空，如：第" + (i + 2) + "行数据“子项指向表”为空\n";
		    	flag = false;
		    }else{
				bean.setTableType(bean.getTableType().toUpperCase());
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
		    //判断code在excel中是否重复
		    if(codeSet.contains(bean.getGroupCode()+"&&"+bean.getCode())){
		    	message += "导入的数据中相同的医疗服务项目组中“医疗服务项目编码”不能重复，如：第" + (i + 2) + "行数据项目组编码为“"+bean.getGroupCode()+"医疗服务项目编码为“"+bean.getCode()+"”在excel中重复\n";
		    	flag = false;
		    }
			//根据项目组编码查找库中数据
			MedicalProjectGroup groupBean = groupMap.get(bean.getGroupCode());
			if(groupBean==null) {//组code首次判断
				//判断groupName在excel中是否重复
				if(groupNameSet.contains(bean.getGroupName())){
					message += "导入的数据中相同的项目组名称不能重复，如：第" + (i + 2) + "行数据项目组名称为“"+bean.getGroupName()+"”在excel中重复\n";
					flag = false;
				}
				groupBean = this.baseMapper.selectOne(new QueryWrapper<MedicalProjectGroup>().eq("GROUP_CODE", bean.getGroupCode()));
				if(groupBean!=null) {//库中已存在组信息
					groupBean.setUpdateStaff(user.getId());
					groupBean.setUpdateStaffName(user.getRealname());
					groupBean.setUpdateTime(new Date());
				}else{
					groupBean = new MedicalProjectGroup();
					groupBean.setGroupCode(bean.getGroupCode());
					groupBean.setGroupName(bean.getGroupName());
					groupBean.setCreateStaff(user.getId());
					groupBean.setCreateStaffName(user.getRealname());
					groupBean.setCreateTime(new Date());
				}
				groupNameSet.add(bean.getGroupName());
			}
			//判断组编码组名称是否重复
			if(!groupBean.getGroupName().equals(bean.getGroupName())||StringUtils.isBlank(groupBean.getGroupId())){//修改组名称或者新增组记录
				if(this.isExistName(bean.getGroupCode(),bean.getGroupName(),groupBean.getGroupId())){//库中存在
					message += "导入的数据中项目组编码或者项目组名称已存在，如：第" + (i + 2) + "行数据项目组编码为“"+bean.getGroupCode()+"项目组名称为“"+bean.getGroupName()+"”在库中已存在\n";
					flag = false;
				}else{
					groupBean.setGroupName(bean.getGroupName());
					if(StringUtils.isBlank(groupBean.getGroupId())){
						groupBean.setGroupId(IdUtils.uuid());
					}
				}
			}
			groupMap.put(bean.getGroupCode(), groupBean);
			if(!flag) {
				continue;
			}
			//根据项目组编码查找库中数据
			if(groupCodeItemCodeExistSet.get(bean.getGroupCode())==null){
				if(groupBean!=null){
					List<MedicalProjectGroupItem> itemList = this.medicalProjectGroupItemMapper.selectList(new QueryWrapper<MedicalProjectGroupItem>().eq("GROUP_ID",groupBean.getGroupId()));
					Set<String> itemCodeSet = new HashSet<String>();
					for(MedicalProjectGroupItem itemBean:itemList){
						itemCodeSet.add(itemBean.getCode());
					}
					groupCodeItemCodeExistSet.put(groupBean.getGroupCode(),itemCodeSet);
				}
			}
		    if("1".equals(bean.getActionType())) {//新增
		    	if(groupCodeItemCodeExistSet.get(bean.getGroupCode())!=null&&groupCodeItemCodeExistSet.get(bean.getGroupCode()).contains(bean.getCode())) {
		    		message += "导入的数据中相同的医疗服务项目组中“医疗服务项目编码”在库中已存在，无法新增，如：第" + (i + 2) + "行数据医疗服务项目组编码为“"+bean.getGroupCode()+"医疗服务项目编码为“"+bean.getCode()+"”\n";
			    	flag = false;
		    	}
		    	addList.add(bean);
		    }else if("0".equals(bean.getActionType())) {//修改
		    	if(groupCodeItemCodeExistSet.get(bean.getGroupCode())==null||!groupCodeItemCodeExistSet.get(bean.getGroupCode()).contains(bean.getCode())) {
		    		message += "导入的数据中该数据在库中不存在，无法修改，如：第" + (i + 2) + "行数据医疗服务项目组编码为“"+bean.getGroupCode()+"医疗服务项目编码为“"+bean.getCode()+"”\n";
			    	flag = false;
		    	}
		    	updateList.add(bean);
		    }else if("2".equals(bean.getActionType())) {//删除
		    	if(groupCodeItemCodeExistSet.get(bean.getGroupCode())==null||!groupCodeItemCodeExistSet.get(bean.getGroupCode()).contains(bean.getCode())) {
		    		message += "导入的数据中该数据在库中不存在，无法删除，如：第" + (i + 2) + "行数据医疗服务项目组编码为“"+bean.getGroupCode()+"医疗服务项目编码为“"+bean.getCode()+"”\n";
			    	flag = false;
		    	}
		    	deleteList.add(bean);
		    }
		    if(!flag) {
		    	 continue;
		    }
          	codeSet.add(bean.getGroupCode()+"&&"+bean.getCode());

		}
		if(StringUtils.isNotBlank(message)){
			message +="请核对数据后进行导入。";
			return Result.error(message);
		}else{
			System.out.println("开始插入时间："+DateUtils.now() );
			this.saveGroupAndItem(addList,user,groupMap);
			this.updateGroupAndItem(updateList,user,groupMap);
			this.deleteGroupAndItem(deleteList,user,groupMap);
			message += "导入成功，共导入"+list.size()+"条数据。";
			System.out.println("结束导入时间："+DateUtils.now() );
			return Result.ok(message,list.size());
		}
	}

	private Map<String,Object> getAllGroupCode() {
		Set<String> groupCodeExistSet = new HashSet<String>();//库中已存在的code
		Map<String,String> groupIdExistMap = new HashMap<String,String>();
		QueryWrapper<MedicalProjectGroup> queryWrapper = new QueryWrapper<MedicalProjectGroup>();
		List<MedicalProjectGroup> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalProjectGroup bean:list) {
			groupCodeExistSet.add(bean.getGroupCode());
			groupIdExistMap.put(bean.getGroupCode(), bean.getGroupId());
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("groupCodeExistSet", groupCodeExistSet);
		map.put("groupIdExistMap", groupIdExistMap);
		return map;
	}

	private Set<String> getAllGroupCodeAndItemCode() {
		Set<String> groupCodeItemCodeExistSet = new HashSet<String>();//库中已存在的code
		List<MedicalGroupVO> list = this.queryGroupItem(new MedicalGroupVO());
		for(MedicalGroupVO bean:list) {
			groupCodeItemCodeExistSet.add(bean.getGroupCode()+"&&"+bean.getCode());
		}
		return groupCodeItemCodeExistSet;
	}

	private void saveGroupAndItem(List<MedicalGroupVO> addList,LoginUser user,Map<String,MedicalProjectGroup> groupMap) {
		if(addList.size()==0){
			return;
		}
		Set<String> groupCodeSet = new HashSet<>();
		List<MedicalProjectGroup> groupList = new ArrayList<>();
		List<MedicalProjectGroupItem> groupItemList = new ArrayList<>();
		int i =0;
		for (MedicalGroupVO groupVo : addList) {
			MedicalProjectGroup groupBean = groupMap.get(groupVo.getGroupCode());
			if(!groupCodeSet.contains(groupVo.getGroupCode())){//组code首次操作
				groupList.add(groupBean);
				groupCodeSet.add(groupVo.getGroupCode());
			}
			MedicalProjectGroupItem itemBean = new MedicalProjectGroupItem();
			itemBean.setGroupId(groupBean.getGroupId());
			itemBean.setItemId(IdUtils.uuid());
			itemBean.setIsOrder((long)i++);
			itemBean.setCode(groupVo.getCode());
			itemBean.setValue(groupVo.getValue());
			itemBean.setTableType(groupVo.getTableType());
			groupItemList.add(itemBean);
		}
		this.saveOrUpdateBatch(groupList,MedicalAuditLogConstants.BATCH_SIZE);
		this.medicalProjectGroupItemService.saveOrUpdateBatch(groupItemList,MedicalAuditLogConstants.BATCH_SIZE);
	}

	private void updateGroupAndItem(List<MedicalGroupVO> updateList,LoginUser user,Map<String,MedicalProjectGroup> groupMap) {
		if(updateList.size()==0){
			return;
		}
		Set<String> groupCodeSet = new HashSet<>();
		List<MedicalProjectGroup> groupList = new ArrayList<>();
		List<MedicalProjectGroupItem> groupItemList = new ArrayList<>();
		for (MedicalGroupVO groupVo : updateList) {
			MedicalProjectGroup groupBean = groupMap.get(groupVo.getGroupCode());
			if(!groupCodeSet.contains(groupVo.getGroupCode())){//组code首次操作
				groupList.add(groupBean);
				groupCodeSet.add(groupVo.getGroupCode());
			}
			MedicalProjectGroupItem itemBean = this.medicalProjectGroupItemMapper.selectOne(new QueryWrapper<MedicalProjectGroupItem>().eq("GROUP_ID",groupBean.getGroupId()).eq("CODE",groupVo.getCode()));
			if(itemBean!=null) {
				itemBean.setCode(groupVo.getCode());
				itemBean.setValue(groupVo.getValue());
				itemBean.setTableType(groupVo.getTableType());
				groupItemList.add(itemBean);
			}
		}
		this.saveOrUpdateBatch(groupList,MedicalAuditLogConstants.BATCH_SIZE);
		this.medicalProjectGroupItemService.saveOrUpdateBatch(groupItemList,MedicalAuditLogConstants.BATCH_SIZE);
	}

	private void deleteGroupAndItem(List<MedicalGroupVO> deleteList,LoginUser user,Map<String,MedicalProjectGroup> groupMap) {
		if(deleteList.size()==0){
			return;
		}
		Set<String> groupCodeSet = new HashSet<>();
		for (MedicalGroupVO groupVo : deleteList) {
			MedicalProjectGroup groupBean = groupMap.get(groupVo.getGroupCode());
			if(!groupCodeSet.contains(groupVo.getGroupCode())){
				groupBean.setGroupName(groupVo.getGroupName());
				groupBean.setUpdateStaff(user.getId());
				groupBean.setUpdateStaffName(user.getRealname());
				groupBean.setUpdateTime(new Date());
				this.updateById(groupBean);
				groupCodeSet.add(groupVo.getGroupCode());
			}
			this.medicalProjectGroupItemMapper.delete(new QueryWrapper<MedicalProjectGroupItem>().eq("GROUP_ID",groupBean.getGroupId()).eq("CODE",groupVo.getCode()));
			int count = this.medicalProjectGroupItemMapper.selectCount(new QueryWrapper<MedicalProjectGroupItem>().eq("GROUP_ID",groupBean.getGroupId()));
			if(count==0) {
				this.baseMapper.deleteById(groupBean.getGroupId());
			}
		}
	}

	private int saveGroupAndItem(Map<String,List<MedicalGroupVO>> addMap,LoginUser user) {
		int addGroupNum = 0;
		for (String key : addMap.keySet()) {
			List<MedicalGroupVO> itemList = addMap.get(key);
			MedicalProjectGroup medicalProjectGroup = new MedicalProjectGroup();
			medicalProjectGroup.setGroupCode(itemList.get(0).getGroupCode());
			medicalProjectGroup.setGroupName(itemList.get(0).getGroupName());
			medicalProjectGroup.setCreateStaff(user.getId());
			medicalProjectGroup.setCreateStaffName(user.getRealname());
			medicalProjectGroup.setCreateTime(new Date());
			this.save(medicalProjectGroup);
			 // 插入子项
			insertGroupItem(itemList, medicalProjectGroup);
			addGroupNum++;
		}
		return addGroupNum;
    }

	private void insertGroupItem(List<MedicalGroupVO> itemList, MedicalProjectGroup medicalProjectGroup) {
		MedicalProjectGroupItem ProjectGroupItem = new MedicalProjectGroupItem();
		ProjectGroupItem.setGroupId(medicalProjectGroup.getGroupId());
		for(int i = 0, len = itemList.size(); i < len; i++) {
			ProjectGroupItem.setItemId(IdUtils.uuid());
			ProjectGroupItem.setIsOrder((long)i);
			ProjectGroupItem.setCode(itemList.get(i).getCode());
			ProjectGroupItem.setValue(itemList.get(i).getValue());
			this.medicalProjectGroupItemMapper.insert(ProjectGroupItem);
		}
	}

	private int updateGroupAndItem(Map<String,List<MedicalGroupVO>> updateMap,LoginUser user) {
		int updateGroupNum = 0;
		for (String key : updateMap.keySet()) {
			List<MedicalGroupVO> itemList = updateMap.get(key);
			String groupId = itemList.get(0).getGroupId();
			MedicalProjectGroup medicalProjectGroup = new MedicalProjectGroup();
			medicalProjectGroup.setGroupId(groupId);
			medicalProjectGroup.setGroupCode(itemList.get(0).getGroupCode());
			medicalProjectGroup.setGroupName(itemList.get(0).getGroupName());
			medicalProjectGroup.setUpdateStaff(user.getId());
			medicalProjectGroup.setUpdateStaffName(user.getRealname());
			medicalProjectGroup.setUpdateTime(new Date());
	        this.updateById(medicalProjectGroup);
	        // 删除子项
	        this.medicalProjectGroupItemMapper.delete(new QueryWrapper<MedicalProjectGroupItem>()
	                .eq("GROUP_ID", groupId));
			 // 插入子项
			insertGroupItem(itemList, medicalProjectGroup);
			updateGroupNum++;
		}
		return updateGroupNum;
    }

	@Override
	public boolean isExistName(String groupCode,String groupName,String groupId) {
		QueryWrapper<MedicalProjectGroup> queryWrapper = new QueryWrapper<MedicalProjectGroup>();
		queryWrapper.and(wrapper ->wrapper.eq("GROUP_CODE", groupCode).or().eq(DbDataEncryptUtil.decryptFunc("GROUP_NAME"), groupName));
		if(StringUtils.isNotBlank(groupId)){
			queryWrapper.notIn("GROUP_ID", groupId);
		}
		List<MedicalProjectGroup> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	public List<MedicalGroupVO> queryGroupItemByGroupCodes(List<String> groupCodes) {
		return this.baseMapper.queryGroupItemByGroupCodes(groupCodes.toArray(new String[0]));
	}

	@Override
	public List<MedicalGroupVO> queryGroupItem2(QueryWrapper<MedicalProjectGroup> queryWrapper) {
		return this.baseMapper.queryGroupItem2(queryWrapper);
	}

	@Override
	public int queryGroupItemCount2(QueryWrapper<MedicalProjectGroup> queryWrapper) {
		return this.baseMapper.queryGroupItemCount2(queryWrapper);
	}
}
