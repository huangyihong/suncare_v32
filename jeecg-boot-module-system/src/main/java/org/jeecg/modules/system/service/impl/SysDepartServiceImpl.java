package org.jeecg.modules.system.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.config.vo.StdHoslevelFundpaypropImport;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.netty.util.internal.StringUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * <p>
 * 部门表 服务实现类
 * <p>
 *
 * @Author Steve
 * @Since 2019-01-22
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements ISysDepartService {

	@Autowired
	private ISysDictService sysDictService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * queryTreeList 对应 queryTreeList 查询所有的部门数据,以树结构形式响应给前端
	 */
	@Cacheable(value = CacheConstant.SYS_DEPARTS_CACHE)
	@Override
	public List<SysDepartTreeModel> queryTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
		return listResult;
	}

	@Cacheable(value = CacheConstant.SYS_DEPART_IDS_CACHE)
	@Override
	public List<DepartIdModel> queryDepartIdTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<DepartIdModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToDepartIdTreeList(list);
		return listResult;
	}

	/**
	 * saveDepartData 对应 add 保存用户在页面添加的新的部门对象数据
	 */
	@Override
	@Transactional
	public void saveDepartData(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			if (sysDepart.getParentId() == null) {
				sysDepart.setParentId("");
			}
			String s = UUID.randomUUID().toString().replace("-", "");
			sysDepart.setId(s);
			// 先判断该对象有无父级ID,有则意味着不是最高级,否则意味着是最高级
			// 获取父级ID
			String parentId = sysDepart.getParentId();
			String[] codeArray = generateOrgCode(parentId);
			sysDepart.setOrgCode(codeArray[0]);
			String orgType = codeArray[1];
			sysDepart.setOrgType(String.valueOf(orgType));
			sysDepart.setCreateTime(new Date());
			sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
			this.save(sysDepart);
		}

	}

	/**
	 * saveDepartData 的调用方法,生成部门编码和部门类型
	 *
	 * @param parentId
	 * @return
	 */
	private String[] generateOrgCode(String parentId) {
		//update-begin--Author:Steve  Date:20190201 for：组织机构添加数据代码调整
				LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
				LambdaQueryWrapper<SysDepart> query1 = new LambdaQueryWrapper<SysDepart>();
				String[] strArray = new String[2];
		        // 创建一个List集合,存储查询返回的所有SysDepart对象
		        List<SysDepart> departList = new ArrayList<>();
				// 定义新编码字符串
				String newOrgCode = "";
				// 定义旧编码字符串
				String oldOrgCode = "";
				// 定义部门类型
				String orgType = "";
				// 如果是最高级,则查询出同级的org_code, 调用工具类生成编码并返回
				if (StringUtil.isNullOrEmpty(parentId)) {
					// 线判断数据库中的表是否为空,空则直接返回初始编码
					query1.eq(SysDepart::getParentId, "").or().isNull(SysDepart::getParentId);
					query1.orderByDesc(SysDepart::getOrgCode);
					departList = this.list(query1);
					if(departList == null || departList.size() == 0) {
						strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
						strArray[1] = "1";
						return strArray;
					}else {
					SysDepart depart = departList.get(0);
					oldOrgCode = depart.getOrgCode();
					orgType = depart.getOrgType();
					newOrgCode = YouBianCodeUtil.getNextYouBianCode(oldOrgCode);
					}
				} else { // 反之则查询出所有同级的部门,获取结果后有两种情况,有同级和没有同级
					// 封装查询同级的条件
					query.eq(SysDepart::getParentId, parentId);
					// 降序排序
					query.orderByDesc(SysDepart::getOrgCode);
					// 查询出同级部门的集合
					List<SysDepart> parentList = this.list(query);
					// 查询出父级部门
					SysDepart depart = this.getById(parentId);
					// 获取父级部门的Code
					String parentCode = depart.getOrgCode();
					// 根据父级部门类型算出当前部门的类型
					orgType = String.valueOf(Integer.valueOf(depart.getOrgType()) + 1);
					// 处理同级部门为null的情况
					if (parentList == null || parentList.size() == 0) {
						// 直接生成当前的部门编码并返回
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, null);
					} else { //处理有同级部门的情况
						// 获取同级部门的编码,利用工具类
						String subCode = parentList.get(0).getOrgCode();
						// 返回生成的当前部门编码
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, subCode);
					}
				}
				// 返回最终封装了部门编码和部门类型的数组
				strArray[0] = newOrgCode;
				strArray[1] = orgType;
				return strArray;
		//update-end--Author:Steve  Date:20190201 for：组织机构添加数据代码调整
	}


	/**
	 * removeDepartDataById 对应 delete方法 根据ID删除相关部门数据
	 *
	 */
	/*
	 * @Override
	 *
	 * @Transactional public boolean removeDepartDataById(String id) {
	 * System.out.println("要删除的ID 为=============================>>>>>"+id); boolean
	 * flag = this.removeById(id); return flag; }
	 */

	/**
	 * updateDepartDataById 对应 edit 根据部门主键来更新对应的部门数据
	 */
	@Override
	@Transactional
	public Boolean updateDepartDataById(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			sysDepart.setUpdateTime(new Date());
			sysDepart.setUpdateBy(username);
			this.updateById(sysDepart);
			return true;
		} else {
			return false;
		}

	}

	@Override
	@Transactional
	public void deleteBatchWithChildren(List<String> ids) {
		List<String> idList = new ArrayList<String>();
		for(String id: ids) {
			idList.add(id);
			this.checkChildrenExists(id, idList);
		}
		this.removeByIds(idList);

	}
	/**
	 * <p>
	 * 根据关键字搜索相关的部门数据
	 * </p>
	 */
	@Override
	public List<SysDepartTreeModel> searhBy(String keyWord) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.like(SysDepart::getDepartName, keyWord);
		//update-begin--Author:huangzhilin  Date:20140417 for：[bugfree号]组织机构搜索回显优化--------------------
		SysDepartTreeModel model = new SysDepartTreeModel();
		List<SysDepart> departList = this.list(query);
		List<SysDepartTreeModel> newList = new ArrayList<>();
		if(departList.size() > 0) {
			for(SysDepart depart : departList) {
				model = new SysDepartTreeModel(depart);
				model.setChildren(null);
	    //update-end--Author:huangzhilin  Date:20140417 for：[bugfree号]组织机构搜索功回显优化----------------------
				newList.add(model);
			}
			return newList;
		}
		return null;
	}

	/**
	 * 根据部门id删除并且删除其可能存在的子级任何部门
	 */
	@Override
	public boolean delete(String id) {
		List<String> idList = new ArrayList<>();
		idList.add(id);
		this.checkChildrenExists(id, idList);
		//清空部门树内存
		//FindsDepartsChildrenUtil.clearDepartIdModel();
		boolean ok = this.removeByIds(idList);
		return ok;
	}

	/**
	 * delete 方法调用
	 * @param id
	 * @param idList
	 */
	private void checkChildrenExists(String id, List<String> idList) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getParentId,id);
		List<SysDepart> departList = this.list(query);
		if(departList != null && departList.size() > 0) {
			for(SysDepart depart : departList) {
				idList.add(depart.getId());
				this.checkChildrenExists(depart.getId(), idList);
			}
		}
	}

	@Override
	public List<SysDepart> queryUserDeparts(String userId) {
		return baseMapper.queryUserDeparts(userId);
	}

	@Override
	public List<SysDepart> queryDepartsByUsername(String username) {
		return baseMapper.queryDepartsByUsername(username);
	}


	@Override
	public Result<?> importExcelNew(MultipartFile file, MultipartHttpServletRequest multipartRequest, String username)throws Exception {
		String mappingFieldStr = "departName,parentId,orgCategory,departOrder,province,roleType,templateIds,address,memo";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return importExcelNew(file, username,mappingFields);
	}

	private Result<?> importExcelNew(MultipartFile file, String username, String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<SysDepart> list = new ArrayList<>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(SysDepart.class, mappingFields, 0, 1, file.getInputStream());
		} else {
			list = ExcelUtils.readSheet(SysDepart.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
			return Result.error("上传文件内容为空");
		}
		String message = "";
		System.out.println("校验开始："+DateUtils.now() );

		//上级部门
		Map<String,String> parentMap = new HashMap<String,String>();
		//机构类型
		Map<String,String> orgCategoryMap =  new HashMap<String, String>(){{put("公司","1");put("部门","2");put("岗位","3");}} ;
		//省市县
		Map<String,String> provinceMap = new HashMap<String,String>();
		//用户报告类别
		List<DictModel> roleTypeList = sysDictService.queryDictItemsByCode("dur_role_type");
		Map<String,String> roleTypeMap = new HashMap<String,String>();
		for(DictModel dictBean:roleTypeList){
			roleTypeMap.put(dictBean.getText(),dictBean.getValue());
		}
		//分析模板
		List<Map<String, Object>> templateList = this.queryTemplateList();
		Map<String,String> templateMap = new HashMap<String,String>();
		for(Map<String, Object> templateBean:templateList){
			templateMap.put((String)templateBean.get("TEMPLATE_NAME"),(String)templateBean.get("TEMPLATE_ID"));
		}
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			SysDepart bean = list.get(i);
			if (StringUtils.isBlank(bean.getDepartName())) {
				message += "导入的数据中“机构名称”不能为空，如：第" + (i + 2) + "行数据“机构名称”为空\n";
				flag = false;
			}
			if (StringUtils.isBlank(bean.getOrgCategory())) {
				message += "导入的数据中“机构类型”不能为空，如：第" + (i + 2) + "行数据“机构类型”为空\n";
				flag = false;
			}
			if(StringUtils.isBlank(orgCategoryMap.get(bean.getOrgCategory()))){
				message += "导入的数据中“机构类型”值错误，机构类型可选值：公司、部门、岗位，如：第" + (i + 2) + "行数据错误\n";
				flag = false;
			}else{
				bean.setOrgCategory(orgCategoryMap.get(bean.getOrgCategory()));
			}
			if(StringUtils.isNotBlank(bean.getParentId())){
				String parentStr = bean.getParentId();
				String parentIdStr = "";
				if(StringUtils.isNotBlank(parentMap.get(bean.getParentId()))){
					parentIdStr = parentMap.get(bean.getParentId());
				}else{
					List<Map<String, Object>> sysDepartParentList = this.getSysDepartList(parentStr.split("/"));
					if(sysDepartParentList==null||sysDepartParentList.size()==0){
						message += "导入的数据中“上级部门”在系统中不存在，如：第" + (i + 2) + "行“"+bean.getParentId()+"”数据不存在于系统中\n";
						flag = false;
					}else{
						parentIdStr = (String)sysDepartParentList.get(0).get("ID");
						parentMap.put(parentStr,parentIdStr);
					}
				}
				if(StringUtils.isNotBlank(parentIdStr)){
					bean.setParentId(parentIdStr);
				}
			}else{
				bean.setOrgCategory("1");//一级部门机构类型为公司
			}
			//解析省市县数据
			if(StringUtils.isNotBlank(bean.getProvince())){
				String provinceStr = bean.getProvince();
				String provinceIdStr = "";
				if(StringUtils.isNotBlank(provinceMap.get(bean.getProvince()))){
					provinceIdStr = provinceMap.get(bean.getProvince());
				}else{
					String[] provinceStrArr = provinceStr.split("/");
					for(int k=0;k<provinceStrArr.length;k++){
						List<Map<String, Object>> areaProvinceInfoList = this.getAreaProvinceInfoList(provinceStrArr[k],(k+1)+"");
						if(areaProvinceInfoList==null||areaProvinceInfoList.size()==0){
							message += "导入的数据中“省市县”在系统中不存在，如：第" + (i + 2) + "行“"+provinceStrArr[k]+"”数据不存在于系统省市县中\n";
							flag = false;
							provinceIdStr = "";
							break;
						}else{
							if(k>0){
								provinceIdStr += "/";
							}
							provinceIdStr += (String)areaProvinceInfoList.get(0).get("ITEM_NO");
						}
					}
				}

				if(StringUtils.isNotBlank(provinceIdStr)){
					provinceMap.put(provinceStr,provinceIdStr);
					String[] provinceArr = provinceIdStr.split("/");
					if(provinceArr.length>0){
						bean.setProvince(provinceArr[0]);
					}
					if(provinceArr.length>1){
						bean.setCity(provinceArr[1]);
					}
					if(provinceArr.length>2){
						bean.setCounty(provinceArr[2]);
					}
				}
			}
			//解析用户报告类别
			if(StringUtils.isNotBlank(bean.getRoleType())){
				String roleTypeIdStr = "";
				String[] roleTypeArr = bean.getRoleType().split("/");
				for(int k=0;k<roleTypeArr.length;k++){
					if(StringUtils.isBlank(roleTypeMap.get(roleTypeArr[k]))){
						message += "导入的数据中“用户报告类别”在系统中不存在，如：第" + (i + 2) + "行“"+roleTypeArr[k]+"”数据不存在于系统中\n";
						flag = false;
						roleTypeIdStr = "";
						break;
					}else{
						if(k>0){
							roleTypeIdStr += ",";
						}
						roleTypeIdStr += roleTypeMap.get(roleTypeArr[k]);
					}
				}
				if(StringUtils.isNotBlank(roleTypeIdStr)){
					bean.setRoleType(roleTypeIdStr);
				}
			}
			//解析分析模板名称
			if(StringUtils.isNotBlank(bean.getTemplateIds())){
				String templateIdStr = "";
				String[] templateIdArr = bean.getTemplateIds().split("/");
				for(int k=0;k<templateIdArr.length;k++){
					if(StringUtils.isBlank(templateMap.get(templateIdArr[k]))){
						message += "导入的数据中“分析模板名称”在系统中不存在，如：第" + (i + 2) + "行“"+templateIdArr[k]+"”数据不存在于系统中\n";
						flag = false;
						templateIdStr = "";
						break;
					}else{
						if(k>0){
							templateIdStr += ",";
						}
						templateIdStr += templateMap.get(templateIdArr[k]);
					}
				}
				if(StringUtils.isNotBlank(templateIdStr)){
					bean.setTemplateIds(templateIdStr);
				}
			}

			if(!flag) {
				continue;
			}
		}
		if(StringUtils.isNotBlank(message)){
			message +="请核对数据后进行导入。";
			return Result.error(message);
		}else{
			System.out.println("开始插入时间："+ DateUtils.now() );
			for(SysDepart bean:list){
				this.saveDepartData(bean,username);
			}
			System.out.println("结束导入时间："+ DateUtils.now() );
			message += "导入成功，共导入"+list.size()+"条数据。";
			return Result.ok(message);
		}
	}


	private List<Map<String, Object>> getSysDepartList(String[] parentStrArr) {
		String sql = " ";
		if(parentStrArr.length>1){
			for(int k=0;k<parentStrArr.length-1;k++){
				sql = "and parent_id in (select id from sys_depart where depart_name='"+parentStrArr[k]+"' "+sql+")";
			}
		}
		sql = "select * from sys_depart where depart_name=? "+sql+" order by org_code desc";
		return jdbcTemplate.queryForList(sql,parentStrArr[parentStrArr.length-1]);
	}


	private List<Map<String, Object>> getAreaProvinceInfoList(String itemName,String itemLevel) {
		String sql = "select ITEM_NO from Dur_Area_Info t where t.item_name=? and t.item_level=?";
		return jdbcTemplate.queryForList(sql,itemName,itemLevel);
	}

	private List<Map<String, Object>> queryTemplateList() {
		String sql = "select * from Dur_Analysis_Template t where t.is_deleted='0' and t.status='1' ";
		return jdbcTemplate.queryForList(sql);
	}

}
