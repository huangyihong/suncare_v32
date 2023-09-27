package org.jeecg.modules.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ai.common.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.core.instrument.util.StringUtils;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysPositionService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.SysUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

	@Autowired
	private SysUserMapper userMapper;
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private SysUserDepartMapper sysUserDepartMapper;
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Autowired
	private SysDepartMapper sysDepartMapper;
	@Autowired
	private ISysPositionService sysPositionService;

	@Autowired
	private ISysDepartService sysDepartService;

	@Autowired
	private ISysRoleService sysRoleService;

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword) {
        SysUser user = userMapper.getUserByName(username);
        String passwordEncode = PasswordUtil.encrypt(username, oldpassword, user.getSalt());
        if (!user.getPassword().equals(passwordEncode)) {
            return Result.error("旧密码输入错误!");
        }
        if (oConvertUtils.isEmpty(newpassword)) {
            return Result.error("新密码不允许为空!");
        }
        if (!newpassword.equals(confirmpassword)) {
            return Result.error("两次输入密码不一致!");
        }
		if (newpassword.equals(oldpassword)) {
			return Result.error("新旧密码不能相同!");
		}
        String password = PasswordUtil.encrypt(username, newpassword, user.getSalt());
        this.userMapper.update(new SysUser().setPassword(password).setUpdatePwdTime(new Date()), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
        return Result.ok("密码重置成功!");
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> changePassword(SysUser sysUser) {
        String salt = oConvertUtils.randomGen(8);
        sysUser.setSalt(salt);
        String password = sysUser.getPassword();
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setPassword(passwordEncode);
        this.userMapper.updateById(sysUser);
        return Result.ok("密码修改成功!");
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteUser(String userId) {
		//1.删除用户
		this.removeById(userId);
		//2.删除用户部门关联关系
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>();
		query.eq(SysUserDepart::getUserId, userId);
		sysUserDepartMapper.delete(query);
		//3.删除用户角色关联关系
		//TODO
		return false;
	}

	@Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteBatchUsers(String userIds) {
		//1.删除用户
		this.removeByIds(Arrays.asList(userIds.split(",")));
		//2.删除用户部门关系
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>();
		for(String id : userIds.split(",")) {
			query.eq(SysUserDepart::getUserId, id);
			this.sysUserDepartMapper.delete(query);
		}
		//3.删除用户角色关系
		//TODO
		return false;
	}

	@Override
	public SysUser getUserByName(String username) {
		return userMapper.getUserByName(username);
	}


	@Override
	@Transactional
	public void addUserWithRole(SysUser bean, String roles) {
		this.save(bean);
		if(oConvertUtils.isNotEmpty(roles)) {
			String[] arr = roles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(bean.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
	}

	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional
	public void editUserWithRole(SysUser bean, String roles) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		this.updateById(bean);
		//先删后加
		sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, bean.getId()).inSql(SysUserRole::getRoleId,"select ID from sys_role where system_code='"+user.getSystemCode()+"' "));
		if(oConvertUtils.isNotEmpty(roles)) {
			String[] arr = roles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(bean.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
	}


	@Override
	public List<String> getRole(String username) {
		return sysUserRoleMapper.getRoleByUserName(username);
	}

	/**
	 * 通过用户名获取用户角色集合
	 * @param username 用户名
     * @return 角色集合
	 */
	@Override
	public Set<String> getUserRolesSet(String username) {
		// 查询用户拥有的角色集合
		List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
		log.info("-------通过数据库读取用户拥有的角色Rules------username： " + username + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}

	/**
	 * 通过用户名获取用户权限集合
	 *
	 * @param username 用户名
	 * @return 权限集合
	 */
	@Override
	public Set<String> getUserPermissionsSet(String username) {
		Set<String> permissionSet = new HashSet<>();
		List<SysPermission> permissionList = sysPermissionMapper.queryByUser(username);
		for (SysPermission po : permissionList) {
//			// TODO URL规则有问题？
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(po.getPerms())) {
				permissionSet.add(po.getPerms());
			}
		}
		log.info("-------通过数据库读取用户拥有的权限Perms------username： "+ username+",Perms size: "+ (permissionSet==null?0:permissionSet.size()) );
		return permissionSet;
	}

	@Override
	public SysUserCacheInfo getCacheUser(String username) {
		SysUserCacheInfo info = new SysUserCacheInfo();
		info.setOneDepart(true);
//		SysUser user = userMapper.getUserByName(username);
//		info.setSysUserCode(user.getUsername());
//		info.setSysUserName(user.getRealname());


		LoginUser user = sysBaseAPI.getUserByName(username);
		if(user!=null) {
			info.setSysUserCode(user.getUsername());
			info.setSysUserName(user.getRealname());
			info.setSysOrgCode(user.getOrgCode());
		}

		//多部门支持in查询
		List<SysDepart> list = sysDepartMapper.queryUserDeparts(user.getId());
		List<String> sysMultiOrgCode = new ArrayList<String>();
		if(list==null || list.size()==0) {
			//当前用户无部门
			//sysMultiOrgCode.add("0");
		}else if(list.size()==1) {
			sysMultiOrgCode.add(list.get(0).getOrgCode());
		}else {
			info.setOneDepart(false);
			for (SysDepart dpt : list) {
				sysMultiOrgCode.add(dpt.getOrgCode());
			}
		}
		info.setSysMultiOrgCode(sysMultiOrgCode);

		return info;
	}

	// 根据部门Id查询
	@Override
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId,String username) {
		return userMapper.getUserByDepId(page, departId,username);
	}

	@Override
	public IPage<SysUser> getUserByDepartIdAndQueryWrapper(Page<SysUser> page, String departId, QueryWrapper<SysUser> queryWrapper) {
		LambdaQueryWrapper<SysUser> lambdaQueryWrapper = queryWrapper.lambda();

		lambdaQueryWrapper.eq(SysUser::getDelFlag, "0");
        lambdaQueryWrapper.inSql(SysUser::getId, "SELECT user_id FROM sys_user_depart WHERE dep_id = '" + departId + "'");

        return userMapper.selectPage(page, lambdaQueryWrapper);
	}

	@Override
	public IPage<SysUserSysDepartModel> queryUserByOrgCode(String orgCode, SysUser userParams, IPage page) {
		List<SysUserSysDepartModel> list = baseMapper.getUserByOrgCode(page, orgCode, userParams);
		Integer total = baseMapper.getUserByOrgCodeTotal(orgCode, userParams);

		IPage<SysUserSysDepartModel> result = new Page<>(page.getCurrent(), page.getSize(), total);
		result.setRecords(list);

		return result;
	}

	// 根据角色Id查询
	@Override
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page, String roleId, String username) {
		return userMapper.getUserByRoleId(page,roleId,username);
	}


	@Override
	public void updateUserDepart(String username,String orgCode) {
		baseMapper.updateUserDepart(username, orgCode);
	}


	@Override
	public SysUser getUserByPhone(String phone) {
		return userMapper.getUserByPhone(phone);
	}


	@Override
	public SysUser getUserByEmail(String email) {
		return userMapper.getUserByEmail(email);
	}

	@Override
	@Transactional
	public void addUserWithDepart(SysUser user, String selectedParts) {
//		this.save(user);  //保存角色的时候已经添加过一次了
		if(oConvertUtils.isNotEmpty(selectedParts)) {
			String[] arr = selectedParts.split(",");
			for (String deaprtId : arr) {
				SysUserDepart userDeaprt = new SysUserDepart(user.getId(), deaprtId);
				sysUserDepartMapper.insert(userDeaprt);
			}
		}
	}


	@Override
	@Transactional
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUserWithDepart(SysUser user, String departs) {
		this.updateById(user);  //更新角色的时候已经更新了一次了，可以再跟新一次
		//先删后加
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(departs)) {
			String[] arr = departs.split(",");
			for (String departId : arr) {
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
	}


	/**
	   * 校验用户是否有效
	 * @param sysUser
	 * @return
	 */
	@Override
	public Result<?> checkUserIsEffective(SysUser sysUser) {
		Result<?> result = new Result<Object>();
		//情况1：根据用户信息查询，该用户不存在
		if (sysUser == null) {
//			result.error500("该用户不存在，请注册");
			result.error500("用户名或密码错误");
			sysBaseAPI.addLog("用户登录失败，用户不存在！", CommonConstant.LOG_TYPE_1, null);
			return result;
		}
		//情况2：根据用户信息查询，该用户已注销
		if (CommonConstant.DEL_FLAG_1.toString().equals(sysUser.getDelFlag())) {
			sysBaseAPI.addLog("用户登录失败，用户名:" + sysUser.getUsername() + "已注销！", CommonConstant.LOG_TYPE_1, null);
			result.error500("该用户已注销");
			return result;
		}
		//情况3：根据用户信息查询，该用户已冻结
		if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
			sysBaseAPI.addLog("用户登录失败，用户名:" + sysUser.getUsername() + "已冻结！", CommonConstant.LOG_TYPE_1, null);
			result.error500("您的账号已被冻结。若要解冻，请联系管理员");
			return result;
		}
		return result;
	}

	@Override
	public boolean exportExcel(List<SysUserVO> listVO, OutputStream os, String suffix) throws Exception {
		String titleStr = "(系统登录)用户账号,用户名字,工号,职务,部门分配,角色分配"
				+ ",生日,性别,邮箱,手机号码,座机";
		String[] titles = titleStr.split(",");
		String fieldStr =  "username,realname,workNo,post,departs,roles"
				+ ",birthday,sexStr,email,phone,telephone";//导出的字段
		String[] fields = fieldStr.split(",");
		//性别
		Map<Integer,String> sexMap =  new HashMap<Integer, String>(){{put(1,"男");put(2,"女");}} ;
		for (SysUserVO bean : listVO) {
			bean.setSexStr(sexMap.get(bean.getSex()));
			if(StringUtils.isBlank(bean.getPost())){
				continue;
			}
			SysPosition positionBean = sysPositionService.getOne(new QueryWrapper<SysPosition>().eq("CODE",bean.getPost()));
			if(positionBean!=null){
				bean.setPost(positionBean.getName());
			}
		}
		if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			ExportXUtils.exportExl(listVO, SysUserVO.class, titles, fields, workbook, "用户信息");
			workbook.write(os);
			workbook.dispose();
		} else {
			// 创建文件输出流
			WritableWorkbook wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet("用户信息", 0);
			ExportUtils.exportExl(listVO, SysUserVO.class, titles, fields, sheet, "");
			wwb.write();
			wwb.close();
		}
		return false;
	}


	@Override
	public Result<?> importExcelNew(MultipartFile file, MultipartHttpServletRequest multipartRequest)throws Exception {
		String mappingFieldStr = "username,realname,workNo,post,password,departs,roles,birthday,sexStr,email,phone,telephone";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return importExcelNew(file,mappingFields);
	}

	private Result<?> importExcelNew(MultipartFile file, String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<SysUserVO> list = new ArrayList<>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(SysUserVO.class, mappingFields, 0, 1, file.getInputStream());
		} else {
			list = ExcelUtils.readSheet(SysUserVO.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
			return Result.error("上传文件内容为空");
		}
		String message = "";
		System.out.println("校验开始："+DateUtils.now() );

		//性别
		Map<String,Integer> sexMap =  new HashMap<String, Integer>(){{put("男",1);put("女",2);}};
		Map<String,SysUserVO> usernameMap = new HashMap<>();
		Map<String,List<String>> departNameMap = new HashMap<>();
		Map<String,List<String>> roleNameMap = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			SysUserVO bean = list.get(i);
			if (StringUtils.isBlank(bean.getUsername())) {
				message += "导入的数据中“(系统登录)用户账号”不能为空，如：第" + (i + 2) + "行数据“(系统登录)用户账号”为空\n";
				flag = false;
			}
			if (StringUtils.isBlank(bean.getRealname())) {
				message += "导入的数据中“用户名字”不能为空，如：第" + (i + 2) + "行数据“用户名字”为空\n";
				flag = false;
			}
			if (StringUtils.isBlank(bean.getWorkNo())) {
				message += "导入的数据中“工号”不能为空，如：第" + (i + 2) + "行数据“工号”为空\n";
				flag = false;
			}
			if (StringUtils.isBlank(bean.getPassword())) {
				message += "导入的数据中“登陆密码”不能为空，如：第" + (i + 2) + "行数据“登陆密码”为空\n";
				flag = false;
			}
			if (StringUtils.isBlank(bean.getDeparts())) {
				message += "导入的数据中“部门分配”不能为空，如：第" + (i + 2) + "行数据“部门分配”为空\n";
				flag = false;
			}else{
				//判断部门在系统中是否存在
				List<String> departs = new ArrayList<>();
				for(String departName:bean.getDeparts().split(";")){
					List<String> departIds = departNameMap.get(departName);
					if(departIds!=null&&departIds.size()>0){
						departs.addAll(departIds);
					}else{
						List<SysDepart> departList = sysDepartService.list(new QueryWrapper<SysDepart>().eq("DEPART_NAME",departName));
						if(departList.size()>0){
							departIds = departList.stream().map(SysDepart::getId).collect(Collectors.toList());
							departs.addAll(departIds);
							departNameMap.put(departName,departIds);
						}else{
							message += "导入的数据中“部门分配”在系统中不存在，如：第" + (i + 2) + "行“"+departName+"”数据不存在于系统中\n";
							flag = false;
							departs= new ArrayList<>();
							break;
						}
					}
				}
				if(departs.size()>0){
					bean.setDeparts(StringUtil.join(departs, ","));
				}
			}
			if (StringUtils.isBlank(bean.getRoles())) {
				message += "导入的数据中“角色分配”不能为空，如：第" + (i + 2) + "行数据“角色分配”为空\n";
				flag = false;
			}else{
				//判断角色在系统中是否存在
				List<String> roles = new ArrayList<>();
				for(String roleName:bean.getRoles().split(";")){
					List<String> roleIds = roleNameMap.get(roleName);
					if(roleIds!=null&&roleIds.size()>0) {
						roles.addAll(roleIds);
					}else{
						List<SysRole> roleList = sysRoleService.list(new QueryWrapper<SysRole>().eq("ROLE_NAME",roleName));
						if(roleList.size()>0){
							roleIds = roleList.stream().map(SysRole::getId).collect(Collectors.toList());
							roles.addAll(roleIds);
							roleNameMap.put(roleName,roleIds);
						}else{
							message += "导入的数据中“角色分配”在系统中不存在，如：第" + (i + 2) + "行“"+roleName+"”数据不存在于系统中\n";
							flag = false;
							roles= new ArrayList<>();
							break;
						}
					}
				}
				if(roles.size()>0){
					bean.setRoles(StringUtil.join(roles, ","));
				}
			}
			//判断职务是否存在
			if(StringUtils.isNotBlank(bean.getPost())){
				SysPosition positionBean = sysPositionService.getOne(new QueryWrapper<SysPosition>().eq("NAME",bean.getPost()));
				if(positionBean!=null){
					bean.setPost(positionBean.getCode());
				}else{
					message += "导入的数据中“(系统登录)用户账号”在excel文件中重复，如：第" + (i + 2) + "行数据错误\n";
					flag = false;
				}
			}
			//校验账号
			if(usernameMap.get(bean.getUsername())!=null){
				message += "导入的数据中“职务”在系统中不存在，如：第" + (i + 2) + "行数据错误\n";
				flag = false;
			}else{
				usernameMap.put(bean.getUsername(),bean);
			}
			if(!flag) {
				continue;
			}
		}
		if(org.apache.commons.lang3.StringUtils.isNotBlank(message)){
			message +="请核对数据后进行导入。";
			return Result.error(message);
		}else{
			System.out.println("开始插入时间："+ DateUtils.now() );
			for(SysUserVO beanVO:list){
				//判断用户是否存在
				SysUser u = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, beanVO.getUsername()));
				SysUser user = new SysUser();
				BeanUtil.copyProperties(beanVO, user);
				String salt = oConvertUtils.randomGen(8);
				if(u!=null){//修改
					user.setUpdateTime(new Date());
					//user.setPassword(sysUser.getPassword());
					user.setSalt(salt);
					String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
					user.setPassword(passwordEncode);
					this.editUserWithRole(user, beanVO.getRoles());
					this.editUserWithDepart(user, beanVO.getDeparts());
				}else{//新增
					user.setCreateTime(new Date());//设置创建时间
					user.setSalt(salt);
					String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
					user.setPassword(passwordEncode);
					user.setStatus(1);
					user.setDelFlag("0");
					this.addUserWithRole(user, beanVO.getRoles());
					this.addUserWithDepart(user, beanVO.getDeparts());
				}
			}
			System.out.println("结束导入时间："+ DateUtils.now() );
			message += "导入成功，共导入"+list.size()+"条数据。";
			return Result.ok(message);
		}
	}

	// 根据角色Id查询
	@Override
	public List<SysUser> getUserByRoleId( String roleId, String username) {
		return userMapper.getUserByRoleId(roleId,username);
	}

}
