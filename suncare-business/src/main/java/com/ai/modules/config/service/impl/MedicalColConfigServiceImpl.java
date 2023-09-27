package com.ai.modules.config.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExcelUtils;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.mapper.MedicalColConfigMapper;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.vo.MedicalColConfigImport;
import com.ai.modules.config.vo.MedicalColConfigVO;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @Description: 表字段配置
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
@Service
public class MedicalColConfigServiceImpl extends ServiceImpl<MedicalColConfigMapper, MedicalColConfig> implements IMedicalColConfigService {

	@Autowired
	private IMedicalDictService medicalDictService;

    @Override
    public List<MedicalColConfigVO> getRuleColConfig(String tableName) {
        return this.baseMapper.getRuleSelectCol(tableName);
    }

    @Override
    public List<MedicalColConfig> getGradeColConfig() {
        return this.baseMapper.selectList(
                new QueryWrapper<MedicalColConfig>()
                        .eq("TAB_NAME","DWB_MASTER_INFO")
                        .like("JKLX","1"));
    }

    @Override
    public List<MedicalColConfig> getGroupByColConfig() {
        return this.baseMapper.selectList(
                new QueryWrapper<MedicalColConfig>()
                        .eq("TAB_NAME","DWB_MASTER_INFO")
                        .like("IS_GROUPBY_COL ","1"));
    }

	@Override
	public MedicalColConfig getMedicalColConfig(String colName, String tabName) {
		List<MedicalColConfig> list = baseMapper.selectList(
				new QueryWrapper<MedicalColConfig>()
				.eq("COL_NAME", colName)
				.eq("TAB_NAME", tabName));
		if(list!=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	@Cacheable(value = CacheConstant.MEDICAL_COL_CONFIG_CACHE,key = "#tabName+':'+#colName")
	public MedicalColConfig getMedicalColConfigByCache(String colName, String tabName) {
		List<MedicalColConfig> list = baseMapper.selectList(
				new QueryWrapper<MedicalColConfig>()
				.eq("COL_NAME", colName)
				.eq("TAB_NAME", tabName));
		if(list!=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean isExist(String tabName, String colName, String id) {
		QueryWrapper<MedicalColConfig> queryWrapper = new QueryWrapper<MedicalColConfig>();
		queryWrapper.eq("TAB_NAME", tabName);
		queryWrapper.eq("COL_NAME", colName);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<MedicalColConfig> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
		String mappingFieldStr = "tabName,colName,colChnName,isWhereColStr,jklx,isDisplayColStr,colValueType,whereInputTypeStr,selectType,dataType,colOrderStr";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return importExcel(file, user,mappingFields);
	}

	private Result<?> importExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		List<MedicalColConfigImport> list = ExcelUtils.readSheet(MedicalColConfigImport.class, mappingFields, 0, 1, file.getInputStream());
		if(list.size() == 0) {
		    return Result.error("上传文件内容为空");
		}
		String message = "";
		System.out.println("校验开始："+DateUtils.now() );
		/**字典数据 start**/
		List<MedicalDictItemVO> whereInputTypeList = medicalDictService.queryByType("WHERE_INPUT_TYPE",MedicalConstant.DICT_KIND_COMMON);
		Map<String,String> whereInputTypeMap = new HashMap<String,String>();
		for(MedicalDictItemVO dictItem:whereInputTypeList) {
			whereInputTypeMap.put(dictItem.getValue(), dictItem.getCode());
		}
		/**字典数据 end**/
		List<MedicalColConfig> updateList = new ArrayList<MedicalColConfig>();
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			MedicalColConfigImport bean = list.get(i);
			if (StringUtils.isBlank(bean.getTabName())) {
		        message += "导入的数据中“表英文名”不能为空，如：第" + (i + 2) + "行数据“表英文名”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getColName())) {
		        message += "导入的数据中“字段英文名”不能为空，如：第" + (i + 2) + "行数据“字段英文名”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getColChnName())) {
		        message += "导入的数据中“字段中文名”不能为空，如：第" + (i + 2) + "行数据“字段中文名”为空\n";
		    	flag = false;
		    }
		    if (StringUtils.isBlank(bean.getColOrderStr())) {
		    	try {
		    	    Double.valueOf(bean.getColOrderStr());
		    	}catch (NumberFormatException e) {
		    		message += "导入的数据中“排序号”需为数值，如：第" + (i + 2) + "行数据“排序号”值类型不正确\n";
		    		flag = false;
		    	}
		    }
		    MedicalColConfig oldBean = new MedicalColConfig();
		    if(StringUtils.isNotBlank(bean.getTabName())&&StringUtils.isNotBlank(bean.getColName())) {
		    	bean.setTabName(bean.getTabName().toUpperCase());
		    	oldBean = this.getMedicalColConfig(bean.getColName().toUpperCase(), bean.getTabName().toUpperCase());
		    	if(oldBean==null) {
			    	oldBean = this.getMedicalColConfig(bean.getColName().toLowerCase(), bean.getTabName().toUpperCase());
		    	}
			    if(oldBean==null) {
			    	message += "导入的数据不存在与系统中，如：第" + (i + 2) + "行数据\n";
				    flag = false;
			    }
		    }
		    if(!flag) {
		    	 continue;
		    }
		    if("是".equals(bean.getIsWhereColStr())) {
		    	bean.setIsWhereColStr("1");
		    }else {
		    	bean.setIsWhereColStr("0");
		    }
		    if("是".equals(bean.getJklx())) {
		    	bean.setJklx("1");
		    }else{
		    	bean.setJklx("0");
		    }
		    if("是".equals(bean.getIsDisplayColStr())) {
		    	bean.setIsDisplayColStr("1");
		    }else{
		    	bean.setIsDisplayColStr("0");
		    }
		    if(StringUtils.isNotBlank(bean.getWhereInputTypeStr())) {
		    	bean.setWhereInputTypeStr(whereInputTypeMap.get(bean.getWhereInputTypeStr()));
		    }
		    if("字符串".equals(bean.getDataType())) {
		    	bean.setDataType("VARCHAR");
		    }else if("数值".equals(bean.getDataType())) {
		    	bean.setDataType("NUMBER");
		    }else if("日期".equals(bean.getDataType())) {
		    	bean.setDataType("DATE");
		    }
		    oldBean.setColChnName(bean.getColChnName());
		    if(StringUtils.isNotBlank(bean.getIsWhereColStr())) {
		    	oldBean.setIsWhereCol(Integer.parseInt(bean.getIsWhereColStr()));
		    }
		    if(StringUtils.isNotBlank(bean.getIsDisplayColStr())) {
		    	oldBean.setIsDisplayCol(Integer.parseInt(bean.getIsDisplayColStr()));
		    }
		    oldBean.setJklx(bean.getJklx());
		    oldBean.setColValueType(bean.getColValueType());
		    if(StringUtils.isNotBlank(bean.getWhereInputTypeStr())) {
		    	oldBean.setWhereInputType(Integer.parseInt(bean.getWhereInputTypeStr()));
		    }
		    oldBean.setSelectType(bean.getSelectType());
		    oldBean.setDataType(bean.getDataType());
		    if(StringUtils.isNotBlank(bean.getColOrderStr())) {
		    	oldBean.setColOrder(Double.valueOf(bean.getColOrderStr()));
		    }
		    oldBean.setUpdateStaff(user.getId());
		    oldBean.setUpdateTime(new Date());
		    updateList.add(oldBean);
		}
		if(StringUtils.isNotBlank(message)){
			message +="请核对数据后进行导入。";
			return Result.error(message);
		}else{
			System.out.println("开始插入时间："+DateUtils.now() );
			this.saveOrUpdateBatch(updateList);
			System.out.println("结束导入时间："+DateUtils.now() );
			message += "导入成功，共导入"+list.size()+"条数据。";
			return Result.ok(message,list.size());
		}
	}

	@Override
	public boolean exportExcel(List<MedicalColConfig> list, OutputStream os){
		boolean isSuc = true;
		/**字典数据 start**/
		List<MedicalDictItemVO> whereInputTypeList = medicalDictService.queryByType("WHERE_INPUT_TYPE",MedicalConstant.DICT_KIND_COMMON);
		Map<String,String> whereInputTypeMap = new HashMap<String,String>();
		for(MedicalDictItemVO dictItem:whereInputTypeList) {
			whereInputTypeMap.put(dictItem.getCode(),dictItem.getValue());
		}
		/**字典数据 end**/
		try {
	    	String titleStr = "表英文名,字段英文名,字段中文名,是否是条件过滤字段,是否是指标（评分）字段,是否是结果显示字段,字段业务分类,输入方式,下拉字典选项,数据类型,排序号";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "tabName,colName,colChnName,isWhereColStr,jklx,isDisplayColStr,colValueType,whereInputTypeStr,selectType,dataType,colOrderStr";//导入的字段
			String[] fields=fieldStr.split(",");
			List<MedicalColConfigImport> exportList = new ArrayList<MedicalColConfigImport>();
			for(MedicalColConfig bean:list) {
				MedicalColConfigImport dataBean = new MedicalColConfigImport();
				BeanUtils.copyProperties(bean,dataBean);
				if(bean.getIsWhereCol()==null){
					dataBean.setIsWhereColStr("");
				}else if(bean.getIsWhereCol()==1) {
					dataBean.setIsWhereColStr("是");
			    }else if(bean.getIsWhereCol()==0){
			    	dataBean.setIsWhereColStr("否");
			    }
			    if("1".equals(bean.getJklx())) {
			    	dataBean.setJklx("是");
			    }else if("0".equals(bean.getJklx())){
			    	dataBean.setJklx("否");
			    }
			    if(bean.getIsDisplayCol()==null){
					dataBean.setIsDisplayColStr("");
				}else if(bean.getIsDisplayCol()==1) {
					dataBean.setIsDisplayColStr("是");
			    }else if(bean.getIsDisplayCol()==0){
			    	dataBean.setIsDisplayColStr("否");
			    }
			    if(bean.getWhereInputType()!=null) {
			    	dataBean.setWhereInputTypeStr(whereInputTypeMap.get(bean.getWhereInputType()+""));
			    }
			    if(bean.getDataType()!=null&&bean.getDataType().toUpperCase().contains("VARCHAR")) {
			    	dataBean.setDataType("字符串");
			    }else if(bean.getDataType()!=null&&bean.getDataType().toUpperCase().contains("NUMBER")) {
			    	dataBean.setDataType("数值");
			    }else if(bean.getDataType()!=null&&bean.getDataType().toUpperCase().contains("DATE")) {
			    	dataBean.setDataType("日期");
			    }
			    dataBean.setColOrderStr(bean.getColOrder()+"");
			    exportList.add(dataBean);
			}
			ExcelUtils.writeOneSheet(exportList,titles,fields,"数据",os);
    	} catch (Exception e) {
			e.printStackTrace();
			isSuc = false;
		}
    	return isSuc;
	}

	@Override
	@CacheEvict(value=CacheConstant.MEDICAL_COL_CONFIG_CACHE, allEntries=true)
	public void clearCache() {

	}

	@Override
	@CacheEvict(value=CacheConstant.MEDICAL_COL_CONFIG_CACHE, key="#tableName+':'+#colName")
	public void clearCacheByCol(String colName, String tableName) {

	}
}
