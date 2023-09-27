package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDiseaseGroup;
import com.ai.modules.config.entity.MedicalDiseaseGroupItem;
import com.ai.modules.config.mapper.MedicalDiseaseGroupItemMapper;
import com.ai.modules.config.mapper.MedicalDiseaseGroupMapper;
import com.ai.modules.config.service.IMedicalDiseaseGroupService;
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
 * @Description: 疾病组
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Service
public class MedicalDiseaseGroupServiceImpl extends ServiceImpl<MedicalDiseaseGroupMapper, MedicalDiseaseGroup> implements IMedicalDiseaseGroupService {

	@Autowired
	MedicalDiseaseGroupItemMapper medicalDiseaseGroupItemMapper;

	@Autowired
	MedicalDiseaseGroupItemServiceImpl medicalDiseaseGroupItemService;

    @Override
    @Transactional
    public void saveGroup(MedicalDiseaseGroup medicalDiseaseGroup, String codes, String names, String tableTypes) {
        this.save(medicalDiseaseGroup);
        // 插入子项
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            String[] tableTypeArray = tableTypes.split(",");
            this.saveGroupItems(medicalDiseaseGroup,codeArray,nameArray,tableTypeArray);
        }
    }

	@Override
	@Transactional
	public void saveGroupItems(MedicalDiseaseGroup medicalDiseaseGroup, String[] codes, String[] names, String[] tableTypes) {
		String groupId = medicalDiseaseGroup.getGroupId();
		// 插入子项
		MedicalDiseaseGroupItem DiseaseGroupItem = new MedicalDiseaseGroupItem();
		DiseaseGroupItem.setGroupId(groupId);
		for(int i = 0, len = codes.length; i < len; i++){
			DiseaseGroupItem.setItemId(IdUtils.uuid());
			DiseaseGroupItem.setIsOrder((long)i);
			DiseaseGroupItem.setCode(codes[i]);
			DiseaseGroupItem.setValue(names[i]);
			DiseaseGroupItem.setTableType(tableTypes[i]);
			this.medicalDiseaseGroupItemMapper.insert(DiseaseGroupItem);
		}
	}

    @Override
    @Transactional
    public void updateGroup(MedicalDiseaseGroup medicalDiseaseGroup, String codes, String names, String tableTypes) {
        String groupId = medicalDiseaseGroup.getGroupId();
        this.updateById(medicalDiseaseGroup);
        // 删除子项
        this.medicalDiseaseGroupItemMapper.delete(new QueryWrapper<MedicalDiseaseGroupItem>()
                .eq("GROUP_ID", groupId));
        // 插入子项
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            String[] tableTypeArray = tableTypes.split(",");
			this.saveGroupItems(medicalDiseaseGroup,codeArray,nameArray,tableTypeArray);
		}
    }

	@Override
	@Transactional
	public void removeGroupById(String id) {
		this.removeById(id);
		 // 删除子项
        this.medicalDiseaseGroupItemMapper.delete(new QueryWrapper<MedicalDiseaseGroupItem>()
                .eq("GROUP_ID", id));
	}

	@Override
	@Transactional
	public void removeGroupByIds(List<String> idList) {
		// TODO Auto-generated method stub
		this.removeByIds(idList);
		// 删除子项
		this.medicalDiseaseGroupItemMapper.delete(new QueryWrapper<MedicalDiseaseGroupItem>()
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

    	String titleStr = "疾病组编码,疾病组名称,YX疾病编码,YX疾病名称,子项所属表,更改标识(1新增0修改2删除)";
    	String[] titles= titleStr.split(",");
    	String fieldStr = "groupCode,groupName,code,value,tableType,actionType";
		String[] fields=fieldStr.split(",");
		if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
			SXSSFWorkbook workbook = new SXSSFWorkbook();
		    ExportXUtils.exportExl(list,MedicalGroupVO.class,titles,fields,workbook,"疾病组");
		    workbook.write(os);
	        workbook.dispose();
		}else {
			 // 创建文件输出流
	        WritableWorkbook wwb = Workbook.createWorkbook(os);
	        WritableSheet sheet = wwb.createSheet("疾病组", 0);
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
		Map<String,MedicalDiseaseGroup> groupMap = new HashMap<String,MedicalDiseaseGroup>();
		System.out.println("校验开始："+DateUtils.now() );
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			MedicalGroupVO bean = list.get(i);
			 if (StringUtils.isBlank(bean.getGroupCode())) {
		        message += "导入的数据中“疾病组编码”不能为空，如：第" + (i + 2) + "行数据“疾病组编码”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getGroupName())) {
		        message += "导入的数据中“疾病组名称”不能为空，如：第" + (i + 2) + "行数据“疾病组名称”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getCode())) {
		        message += "导入的数据中“疾病编码”不能为空，如：第" + (i + 2) + "行数据“疾病编码”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getValue())) {
		        message += "导入的数据中“疾病名称”不能为空，如：第" + (i + 2) + "行数据“疾病名称”为空\n";
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
		    //判断code在excel中是否重复
		    if(codeSet.contains(bean.getGroupCode()+"&&"+bean.getCode())){
				message += "导入的数据中相同的疾病组中“疾病编码”不能重复，如：第" + (i + 2) + "行数据疾病组编码为“"+bean.getGroupCode()+"疾病编码为“"+bean.getCode()+"”在excel中重复\n";
				flag = false;
			}
			if(!flag) {
				continue;
			}
		    //根据疾病组编码查找库中数据
			MedicalDiseaseGroup groupBean = groupMap.get(bean.getGroupCode());
			if(groupBean==null) {//组code首次判断
				//判断groupName在excel中是否重复
				if(groupNameSet.contains(bean.getGroupName())){
					message += "导入的数据中相同的疾病组名称不能重复，如：第" + (i + 2) + "行数据疾病组名称为“"+bean.getGroupName()+"”在excel中重复\n";
					flag = false;
				}
				groupBean = this.baseMapper.selectOne(new QueryWrapper<MedicalDiseaseGroup>().eq("GROUP_CODE", bean.getGroupCode()));
				if(groupBean!=null) {//库中已存在组信息
					groupBean.setUpdateStaff(user.getId());
					groupBean.setUpdateStaffName(user.getRealname());
					groupBean.setUpdateTime(new Date());
				}else{
					groupBean = new MedicalDiseaseGroup();
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
					message += "导入的数据中疾病组编码或者疾病组名称已存在，如：第" + (i + 2) + "行数据疾病组编码为“"+bean.getGroupCode()+"疾病组名称为“"+bean.getGroupName()+"”在库中已存在\n";
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

			if(groupCodeItemCodeExistSet.get(bean.getGroupCode())==null){
				if(groupBean!=null){
					List<MedicalDiseaseGroupItem> itemList = this.medicalDiseaseGroupItemMapper.selectList(new QueryWrapper<MedicalDiseaseGroupItem>().eq("GROUP_ID",groupBean.getGroupId()));
					Set<String> itemCodeSet = new HashSet<String>();
					for(MedicalDiseaseGroupItem itemBean:itemList){
						itemCodeSet.add(itemBean.getCode());
					}
					groupCodeItemCodeExistSet.put(groupBean.getGroupCode(),itemCodeSet);
				}
			}
			if("1".equals(bean.getActionType())) {//新增
		    	if(groupCodeItemCodeExistSet.get(bean.getGroupCode())!=null&&groupCodeItemCodeExistSet.get(bean.getGroupCode()).contains(bean.getCode())) {
		    		message += "导入的数据中相同的疾病组中“疾病编码”在库中已存在，无法新增，如：第" + (i + 2) + "行数据疾病组编码为“"+bean.getGroupCode()+"疾病编码为“"+bean.getCode()+"”\n";
			    	flag = false;
		    	}
		    	addList.add(bean);
		    }else if("0".equals(bean.getActionType())) {//修改
		    	if(groupCodeItemCodeExistSet.get(bean.getGroupCode())==null||!groupCodeItemCodeExistSet.get(bean.getGroupCode()).contains(bean.getCode())) {
		    		message += "导入的数据中该数据在库中不存在，无法修改，如：第" + (i + 2) + "行数据疾病组编码为“"+bean.getGroupCode()+"疾病编码为“"+bean.getCode()+"”\n";
			    	flag = false;
		    	}
		    	updateList.add(bean);
		    }else if("2".equals(bean.getActionType())) {//删除
				if(groupCodeItemCodeExistSet.get(bean.getGroupCode())==null||!groupCodeItemCodeExistSet.get(bean.getGroupCode()).contains(bean.getCode())) {
		    		message += "导入的数据中该数据在库中不存在，无法删除，如：第" + (i + 2) + "行数据疾病组编码为“"+bean.getGroupCode()+"疾病编码为“"+bean.getCode()+"”\n";
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
		QueryWrapper<MedicalDiseaseGroup> queryWrapper = new QueryWrapper<MedicalDiseaseGroup>();
		List<MedicalDiseaseGroup> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalDiseaseGroup bean:list) {
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

	private void saveGroupAndItem(List<MedicalGroupVO> addList,LoginUser user,Map<String,MedicalDiseaseGroup> groupMap) {
    	if(addList.size()==0){
    		return;
		}
    	Set<String> groupCodeSet = new HashSet<>();
    	List<MedicalDiseaseGroup> groupList = new ArrayList<>();
    	List<MedicalDiseaseGroupItem> groupItemList = new ArrayList<>();
		int i =0;
		for (MedicalGroupVO groupVo : addList) {
			MedicalDiseaseGroup groupBean = groupMap.get(groupVo.getGroupCode());
			if(!groupCodeSet.contains(groupVo.getGroupCode())){//组code首次操作
				groupList.add(groupBean);
				groupCodeSet.add(groupVo.getGroupCode());
			}
			MedicalDiseaseGroupItem itemBean = new MedicalDiseaseGroupItem();
			itemBean.setGroupId(groupBean.getGroupId());
			itemBean.setItemId(IdUtils.uuid());
			itemBean.setIsOrder((long)i++);
			itemBean.setCode(groupVo.getCode());
			itemBean.setValue(groupVo.getValue());
			itemBean.setTableType(groupVo.getTableType());
			groupItemList.add(itemBean);
		}
		this.saveOrUpdateBatch(groupList,MedicalAuditLogConstants.BATCH_SIZE);
		this.medicalDiseaseGroupItemService.saveOrUpdateBatch(groupItemList,MedicalAuditLogConstants.BATCH_SIZE);
	}

	private void updateGroupAndItem(List<MedicalGroupVO> updateList,LoginUser user,Map<String,MedicalDiseaseGroup> groupMap) {
		if(updateList.size()==0){
			return;
		}
		Set<String> groupCodeSet = new HashSet<>();
		List<MedicalDiseaseGroup> groupList = new ArrayList<>();
		List<MedicalDiseaseGroupItem> groupItemList = new ArrayList<>();
    	for (MedicalGroupVO groupVo : updateList) {
			MedicalDiseaseGroup groupBean = groupMap.get(groupVo.getGroupCode());
			if(!groupCodeSet.contains(groupVo.getGroupCode())){//组code首次操作
				groupList.add(groupBean);
				groupCodeSet.add(groupVo.getGroupCode());
			}
			MedicalDiseaseGroupItem itemBean = this.medicalDiseaseGroupItemMapper.selectOne(new QueryWrapper<MedicalDiseaseGroupItem>().eq("GROUP_ID",groupBean.getGroupId()).eq("CODE",groupVo.getCode()));
			if(itemBean!=null) {
				itemBean.setCode(groupVo.getCode());
				itemBean.setValue(groupVo.getValue());
				itemBean.setTableType(groupVo.getTableType());
				groupItemList.add(itemBean);
			}
		}
		this.saveOrUpdateBatch(groupList,MedicalAuditLogConstants.BATCH_SIZE);
		this.medicalDiseaseGroupItemService.saveOrUpdateBatch(groupItemList,MedicalAuditLogConstants.BATCH_SIZE);
	}

	private void deleteGroupAndItem(List<MedicalGroupVO> deleteList,LoginUser user,Map<String,MedicalDiseaseGroup> groupMap) {
		if(deleteList.size()==0){
			return;
		}
		Set<String> groupCodeSet = new HashSet<>();
    	for (MedicalGroupVO groupVo : deleteList) {
			MedicalDiseaseGroup groupBean = groupMap.get(groupVo.getGroupCode());
			if(!groupCodeSet.contains(groupVo.getGroupCode())){
				groupBean.setGroupName(groupVo.getGroupName());
				groupBean.setUpdateStaff(user.getId());
				groupBean.setUpdateStaffName(user.getRealname());
				groupBean.setUpdateTime(new Date());
				this.updateById(groupBean);
				groupCodeSet.add(groupVo.getGroupCode());
			}
			this.medicalDiseaseGroupItemMapper.delete(new QueryWrapper<MedicalDiseaseGroupItem>().eq("GROUP_ID",groupBean.getGroupId()).eq("CODE",groupVo.getCode()));
			int count = this.medicalDiseaseGroupItemMapper.selectCount(new QueryWrapper<MedicalDiseaseGroupItem>().eq("GROUP_ID",groupBean.getGroupId()));
			if(count==0) {
				this.baseMapper.deleteById(groupBean.getGroupId());
			}
		}
	}

	private int saveGroupAndItem(Map<String,List<MedicalGroupVO>> addMap,LoginUser user) {
		int addGroupNum = 0;
		for (String key : addMap.keySet()) {
			List<MedicalGroupVO> itemList = addMap.get(key);
			MedicalDiseaseGroup medicalDiseaseGroup = new MedicalDiseaseGroup();
			medicalDiseaseGroup.setGroupCode(itemList.get(0).getGroupCode());
			medicalDiseaseGroup.setGroupName(itemList.get(0).getGroupName());
			medicalDiseaseGroup.setCreateStaff(user.getId());
			medicalDiseaseGroup.setCreateStaffName(user.getRealname());
			medicalDiseaseGroup.setCreateTime(new Date());
			this.save(medicalDiseaseGroup);
			 // 插入子项
			insertGroupItem(itemList, medicalDiseaseGroup);
			addGroupNum++;
		}
		return addGroupNum;
    }

	private void insertGroupItem(List<MedicalGroupVO> itemList, MedicalDiseaseGroup medicalDiseaseGroup) {
		MedicalDiseaseGroupItem DiseaseGroupItem = new MedicalDiseaseGroupItem();
		DiseaseGroupItem.setGroupId(medicalDiseaseGroup.getGroupId());
		for(int i = 0, len = itemList.size(); i < len; i++) {
			DiseaseGroupItem.setItemId(IdUtils.uuid());
			DiseaseGroupItem.setIsOrder((long)i);
			DiseaseGroupItem.setCode(itemList.get(i).getCode());
			DiseaseGroupItem.setValue(itemList.get(i).getValue());
			this.medicalDiseaseGroupItemMapper.insert(DiseaseGroupItem);
		}
	}

	private int updateGroupAndItem(Map<String,List<MedicalGroupVO>> updateMap,LoginUser user) {
		int updateGroupNum = 0;
		for (String key : updateMap.keySet()) {
			List<MedicalGroupVO> itemList = updateMap.get(key);
			String groupId = itemList.get(0).getGroupId();
			MedicalDiseaseGroup medicalDiseaseGroup = new MedicalDiseaseGroup();
			medicalDiseaseGroup.setGroupId(groupId);
			medicalDiseaseGroup.setGroupCode(itemList.get(0).getGroupCode());
			medicalDiseaseGroup.setGroupName(itemList.get(0).getGroupName());
			medicalDiseaseGroup.setUpdateStaff(user.getId());
			medicalDiseaseGroup.setUpdateStaffName(user.getRealname());
			medicalDiseaseGroup.setUpdateTime(new Date());
	        this.updateById(medicalDiseaseGroup);
	        // 删除子项
	        this.medicalDiseaseGroupItemMapper.delete(new QueryWrapper<MedicalDiseaseGroupItem>()
	                .eq("GROUP_ID", groupId));
			 // 插入子项
			insertGroupItem(itemList, medicalDiseaseGroup);
			updateGroupNum++;
		}
		return updateGroupNum;
    }

	@Override
	public boolean isExistName(String groupCode, String groupName, String groupId) {
		QueryWrapper<MedicalDiseaseGroup> queryWrapper = new QueryWrapper<MedicalDiseaseGroup>();
		queryWrapper.and(wrapper ->wrapper.eq("GROUP_CODE", groupCode).or().eq(DbDataEncryptUtil.decryptFunc("GROUP_NAME"), groupName));
		if(StringUtils.isNotBlank(groupId)){
			queryWrapper.notIn("GROUP_ID", groupId);
		}
		if(this.baseMapper.selectCount(queryWrapper)>0){
			return true;
		}
		return false;
	}

	@Override
	public List<MedicalGroupVO> queryGroupItem2(QueryWrapper<MedicalDiseaseGroup> queryWrapper) {
		return this.baseMapper.queryGroupItem2(queryWrapper);
	}

	@Override
	public int queryGroupItemCount2(QueryWrapper<MedicalDiseaseGroup> queryWrapper) {
		return this.baseMapper.queryGroupItemCount2(queryWrapper);
	}
}
