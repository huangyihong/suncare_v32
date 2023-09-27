package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.engine.util.AliyunApiUtil;
import com.ai.modules.system.service.ISysDatasourceService;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.shiro.vo.DefContants;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.common.util.encryption.EncryptedString;
import org.jeecg.modules.shiro.authc.RSAEncrypt;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysLogService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.utils.ComputerInfo;
import org.jeecg.utils.SMS4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Api(tags="用户登录")
@Slf4j
public class LoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Autowired
	private ISysLogService logService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
    private ISysDepartService sysDepartService;

	@Autowired
	private IMedicalDictService medicalDictService;

	@Autowired
	private ISysDatasourceService sysDatasourceService;

	@Value("${jeecg.sms.defaultCode}")
	private String defaultCode;


	private static final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";

	// 登录失败次数存储redis key
	public static final String LOGIN_FAIL_COUNT_KEY = "loginFailCount::";
	// 30分钟重置登录失败次数
	private static final long LOGIN_FAIL_COUNT_MINUTE = 30;

	@ApiOperation("登录接口")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Result<?> login(@RequestBody SysLoginModel sysLoginModel) throws Exception {
		SysUser sysUser;

		Object resultObj = validUserInfo(sysLoginModel);
		if(SysUser.class.equals(resultObj.getClass())){
			sysUser = (SysUser)resultObj;
		} else {
			return (Result) resultObj;
		}
		String captcha = sysLoginModel.getCaptcha();
		String dataSource = sysLoginModel.getDataSource();
		String systemCode = sysLoginModel.getSystemCode();

		//获取字典中的验证码
		String dictSmsCode = medicalDictService.queryDictKeyByText("K", "K1");
		if(StringUtils.isNotBlank(dictSmsCode)){
			dictSmsCode = StringUtils.reverse(dictSmsCode).toLowerCase();
		}

		Result result = new Result();
		if(StringUtils.isNotBlank(sysLoginModel.getCheckKey())){
			//验证图形验证码，只针对insight系统
			if(!defaultCode.equals(captcha) && !dictSmsCode.equals(captcha)) {
				//针对insight特殊处理
				Object checkCode = redisUtil.get(sysLoginModel.getCheckKey());
				if (checkCode == null) {
					result.error500("验证码失效");
					return result;
				}
				if (!checkCode.equals(sysLoginModel.getCaptcha())) {
					result.error500("验证码错误");
					return result;
				}
			}
		}else{
			//验证手机验证码
			if(!defaultCode.equals(captcha) && !dictSmsCode.equals(captcha)) {
				String phoneNo = sysUser.getPhone();
				JSONObject json = (JSONObject) redisUtil.get(phoneNo);
				if(json == null) {
					result.error500("验证码失效，请重新发送");
					return result;
				}
				int validCount = json.getIntValue("validCount");
				if(validCount == 4){
					result.error500("验证次数达到上限，请重新发送");
					return result;
				}
				String checkCode = json.getString("captcha");
				if(!checkCode.equals(captcha)) {
					// 增加已验证次数
					json.put("validCount", validCount + 1);
					redisUtil.set(phoneNo, json);

					result.error500("验证码错误，请重新输入");
					return result;
				}
				// 重置发送次数
				redisUtil.del(phoneNo + "-sendCount");
			}
		}

		//判断机器码
		String txtPath = System.getProperty("user.dir")+"/config/logincode.txt";
		String configRegistrationCode = StringUtils.chomp(SMS4.readTxtFile(txtPath));
		result = checkRegistrationCode(configRegistrationCode);
		if(result.getCode()==501){
			return result;
		}

		// 生成token
		String token = JwtUtil.sign(sysUser.getUsername(), sysUser.getPassword(), UUIDGenerator.generate(), dataSource,systemCode);
		//用户登录信息
		userInfo(sysUser,dataSource, token, result);
		sysBaseAPI.addLog("用户名: " + sysUser.getUsername() + ",登录成功！", CommonConstant.LOG_TYPE_1, null);

		return result;
	}

	//写入注册码
	@GetMapping(value = "/registrationCodeSave")
	public Result<?> registrationCodeSave(@RequestParam(name="registrationCode") String registrationCode) throws Exception {
		/*String txtPath = ClassUtils.getDefaultClassLoader().getResource("config.properties").getPath();
		txtPath = StringUtils.replace(txtPath ,"config.properties"  ,"logincode.txt");*/
		String txtPath = System.getProperty("user.dir")+"/config/logincode.txt";
		SMS4.writeTxtFile(txtPath,registrationCode);
		return Result.ok();
	}

	private Result<?> checkRegistrationCode(String configRegistrationCode){
		String machineCode= SMS4.myCrypt(ComputerInfo.getMacAddress(), "jkjladfsfaaadfdd!13123", SMS4.ENCRYPT);
		String registrationCode=SMS4.myCrypt(machineCode, "8718723qweqwe123ddd", SMS4.ENCRYPT);
		Result result = new Result();
		try{
			String[]  configRegistrationCodeArr = configRegistrationCode.split(";");
			if(configRegistrationCodeArr.length==2&&registrationCode.equalsIgnoreCase(configRegistrationCodeArr[0])){
				//判断注册时间是否过期
				String date = SMS4.myCrypt(configRegistrationCodeArr[1],"8718723qweqwe123ddd",SMS4.DECRYPT);
				if(StringUtils.isNotBlank(date)){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date endTime = sdf.parse(date);
					Calendar cl = Calendar.getInstance();
					cl.setTime(endTime);
					cl.add(Calendar.MONTH, 3);
					if(cl.getTime().before(new Date())){//已过期
						throw new Exception("注册码已过期");
					}
				}else{
					throw new Exception("注册码格式不正确");
				}
			}else{
				throw new Exception("注册码不正确");
			}
		}catch (Exception e){
			log.error("请根据机器码联系系统维护人员获取验证码，机器码：" +machineCode);
			result.error500("登录错误,"+e.getMessage()+",错误码:"+machineCode);
			result.setCode(501);
			result.setResult(machineCode);
		}
		return result;
	}


	@RequestMapping(value = "/sendSms", method = RequestMethod.POST)
	public Result<?> sendSms(@RequestBody SysLoginModel sysLoginModel) throws Exception {
		SysUser sysUser;
		Object resultObj = validUserInfo(sysLoginModel);
		if(SysUser.class.equals(resultObj.getClass())){
			sysUser = (SysUser)resultObj;
		} else {
			return (Result) resultObj;
		}
		String phoneNum = sysUser.getPhone();
		if(StringUtils.isBlank(phoneNum)){
			return Result.error("未设置手机号，请联系管理员");
		}
		// 判断发送间隔
		JSONObject json = (JSONObject) redisUtil.get(phoneNum);
		if(json != null){
			long sendTime = json.getLong("sendTime");
			long expireTime = (System.currentTimeMillis() - sendTime) / 1000;
			if(expireTime < 60){
				return Result.error("发送间隔时间太短");
			}
		}
		// 判断24小时内发送次数
		String sendCountKey = "phoneSend:count:" + phoneNum;
		Integer sendCount = (Integer) redisUtil.get(sendCountKey);
		if(sendCount != null && sendCount == 8){
			return Result.error("今日发送次数达到上限");
		}

		String smsTemplateId = "SMS_214820624";
		String captcha = RandomUtil.randomNumbers(4);
		String templateParam = "{\"code\":\"%s\"}";
		templateParam = String.format(templateParam, captcha);
		log.info(phoneNum + "验证码：" + captcha);
		SendSmsResponse smsResponse = AliyunApiUtil.sendSms(phoneNum, smsTemplateId, templateParam);
//		SendSmsResponse smsResponse = new SendSmsResponse();
//		smsResponse.setCode("OK");
		if("OK".equals(smsResponse.getCode())) {
			json = new JSONObject();
			json.put("captcha", captcha);
			json.put("sendTime", System.currentTimeMillis());
			json.put("validCount", 0);
			//短信发送成功，验证码写入redis中3分钟内有效
			redisUtil.set(phoneNum, json, 180);
			if(sendCount == null){
				redisUtil.set(sendCountKey, 1 ,60 * 60 * 24);
			} else {
				redisUtil.set(sendCountKey, sendCount + 1, redisUtil.getExpire(sendCountKey));
			}

			sysBaseAPI.addLog("用户名: " + sysUser.getUsername() + ":" + phoneNum + ",发送短信验证码", CommonConstant.LOG_TYPE_1, null);
		} else {
			return Result.error(smsResponse.getMessage());
//			throw new Exception("验证码发送失败：" + smsResponse.getMessage());
		}
		String phone = sysUser.getPhone();
		if(StringUtils.isNotBlank(sysUser.getPhone())){
			phone = phone.substring(0,3) + "****" + phone.substring(phone.length() - 4);
		}

		return Result.ok("已发送验证码至：" + phone);
	}

	private Object validUserInfo(SysLoginModel sysLoginModel) throws Exception {
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();

		//1. 校验用户是否有效
		SysUser sysUser = sysUserService.getUserByName(username);
		Result result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}

		String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALrGEXx/zC59C0b3Myr12iqNhE+Ca9LTLxy/Iyaootj9z2nhrXL5AZpOLLlBpgxRbfR/ZHVMOMXo1fsYi3SFqPCO9Uu3cuItNtBButgE6GGBPHqDQ3/H0Km0Q8xsnEj9FGWaeQf6ZI4a/ETWgulCd8P6mi2wBWKPzN+Ms8UO/AYLAgMBAAECgYEAnOmYeUMYtBnDpqw5pacE0ekwtme41Fct5nXCP1E/9xNM9QRvZv9B1HRG6KD5srGaseVw7YbKz6JsW1bkmlFMetLIpO0RzqaadQQtDF9mCe8GJFgFcl9Z69WaSNe5TNgK+aqwWxfp2hC82b3V89JZcu9+6TdvmhFzuE7HYQ4rhQECQQD5sCsKrVDhY0S9d7lERc679wWbazBDFbxhpQCU6ktjIvTqhK3Ee0L8BvtkF45/0D5E7eqJBuycfmEtdYNY962BAkEAv37Bb0cm6/TktMevjmUI7fRgPzTbtU6xmyS/eYXAiX+cbpKDQ9KyWjGj/T9NM2s5u0MrcU8H1H2GaKfFFhtRiwJBALeIjArcHQCfhE6062TI306BJAj7AE2/c2pe7A+KLQHhFyAviL9NT46L30vSmPdQbgUi0OwKP/BEYVAV+gdyf4ECQEM0YP6Eogw9LeakDeCTZMbd9Mk568F9lKc2BlzHrAKVXnkPwjGTSDSPJ1ZBWP6qquAleqGRctYIF5uzBV/0xnECQQDVqIYuZdjYAmkFqdf7c7rT5GSHp5Dk+HXS4Vl8Zha6Ad93aKIswX1UiH80uVWBo5umWuir5ZtmNKrhJYztp4h3";

		password = RSAEncrypt.decrypt(password, privateKey);
//		log.info(password);

		//2. 校验用户名或密码是否正确
		String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
		String syspassword = sysUser.getPassword();
		String sysUserId = sysUser.getId();
		if (!syspassword.equals(userpassword)) {
			// 输错五次冻结
			String msg = "用户名或密码错误";
			String key = LOGIN_FAIL_COUNT_KEY + sysUserId;
			Integer count = (Integer) redisUtil.get(key);
			if(count == null){
				count = 1;
				redisUtil.set(key, count, LOGIN_FAIL_COUNT_MINUTE * 60);
			} else {
				count++;
				if(count >= 5){
					msg += "，您的账号已被冻结，请联系管理员！";
					UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
					updateWrapper.eq("ID", sysUserId);
					updateWrapper.set("STATUS", CommonConstant.USER_FREEZE);
					sysUserService.update(updateWrapper);

					redisUtil.del(key);
				} else {
					msg +=  "，连续输错5次将被冻结，您还有" + (5 - count) + "次机会";
					redisUtil.set(key, count);
				}
			}

			result.error500(msg);
			return result;
		}

		//获取用户的角色 是否有devAdmin角色
        Set<String> roleSet = sysUserService.getUserRolesSet(username);
        if(roleSet.stream().filter(t->t.equals("devAdmin")).collect(Collectors.toList()).size()>0){
            return sysUser;
        }

        Date nowDate = new Date();
		if(sysUser.getLoginTime() != null
				&& TimeUtil.getBetweenDays(nowDate, sysUser.getLoginTime()) > 30
		){
			UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq("ID", sysUser.getId());
			updateWrapper.setSql("LOGIN_TIME = null");
			updateWrapper.set("STATUS", CommonConstant.USER_FREEZE);
			sysUserService.update(updateWrapper);

			sysBaseAPI.addLog("用户登录失败，用户名:" + sysUser.getUsername() + "已冻结！", CommonConstant.LOG_TYPE_1, null);
			result.error500("出于安全原因，您的账号已被冻结。若要解冻，请联系管理员");
			return result;
		}
		if(sysUser.getUpdatePwdTime() != null
				&& TimeUtil.getBetweenDays(nowDate, sysUser.getUpdatePwdTime()) > 180
		){
			UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq("ID", sysUser.getId());
			updateWrapper.setSql("UPDATE_PWD_TIME = null");
			updateWrapper.set("STATUS", CommonConstant.USER_FREEZE);
			sysUserService.update(updateWrapper);

			sysBaseAPI.addLog("用户登录失败，用户名:" + sysUser.getUsername() + "已冻结！", CommonConstant.LOG_TYPE_1, null);
			result.error500("出于安全原因，您的账号已被冻结。若要解冻，请联系管理员");
			return result;
		}

		return sysUser;
	}

	/*@ApiOperation("登录接口")
	@RequestMapping(value = "/validLoginInfo", method = RequestMethod.POST)
	public Result<JSONObject> validLoginInfo(@RequestBody SysLoginModel sysLoginModel){
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();
		String dataSource = sysLoginModel.getDataSource();
		//update-begin--Author:scott  Date:20190805 for：暂时注释掉密码加密逻辑，有点问题
		//前端密码加密，后端进行密码解密
		//password = AesEncryptUtil.desEncrypt(sysLoginModel.getPassword().replaceAll("%2B", "\\+")).trim();//密码解密
		//update-begin--Author:scott  Date:20190805 for：暂时注释掉密码加密逻辑，有点问题

		//update-begin-author:taoyan date:20190828 for:校验验证码
		Object checkCode = redisUtil.get(sysLoginModel.getCheckKey());
		if(checkCode==null) {
			result.error500("验证码失效");
			return result;
		}
		if(!checkCode.equals(sysLoginModel.getCaptcha())) {
			result.error500("验证码错误");
			return result;
		}
		//update-end-author:taoyan date:20190828 for:校验验证码

		//1. 校验用户是否有效
		SysUser sysUser = sysUserService.getUserByName(username);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}

		//2. 校验用户名或密码是否正确
		String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
		String syspassword = sysUser.getPassword();
		if (!syspassword.equals(userpassword)) {
			result.error500("用户名或密码错误");
			return result;
		}

		String phone = sysUser.getPhone();
		if(StringUtils.isBlank(phone)){
			result.error500("用户手机号为空，请联系管理员设置");
			return result;
		}

		//用户登录信息
//		userInfo(sysUser,dataSource, result);
		sysBaseAPI.addLog("用户名: " + username + ",登录成功！", CommonConstant.LOG_TYPE_1, null);

		return result;
	}*/

	/**
	 * 退出登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public Result<Object> logout(HttpServletRequest request,HttpServletResponse response) {
		//用户退出逻辑
	    String token = request.getHeader(DefContants.X_ACCESS_TOKEN);
	    if(oConvertUtils.isEmpty(token)) {
	    	return Result.error("退出登录失败！");
	    }
		String username = JwtUtil.getUsername(token);
		String cacheToken = (String) redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + username);

		if(cacheToken != null){
			String key = JwtUtil.getKey(token);
			String cacheKey = JwtUtil.getKey(cacheToken);
			if(key != null && !key.equals(cacheKey)){
				return Result.error("登录被顶替!");
			}
		}

		LoginUser sysUser = sysBaseAPI.getUserByName(username);
	    if(sysUser!=null) {
	    	sysBaseAPI.addLog("用户名: "+sysUser.getRealname()+",退出成功！", CommonConstant.LOG_TYPE_1, null);
	    	log.info(" 用户名:  "+sysUser.getRealname()+",退出成功！ ");
	    	//清空用户登录Token缓存
//	    	redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
	    	redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + username);
	    	//清空用户登录Shiro权限缓存
	    	redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
	    	return Result.ok("退出登录成功！");
	    }else {
	    	return Result.error("Token无效!");
	    }
	}

	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("loginfo")
	public Result<JSONObject> loginfo() {
		Result<JSONObject> result = new Result<JSONObject>();
		JSONObject obj = new JSONObject();
		//update-begin--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
		// 获取一天的开始和结束时间
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date dayStart = calendar.getTime();
		calendar.add(Calendar.DATE, 1);
		Date dayEnd = calendar.getTime();
		// 获取系统访问记录
		Long totalVisitCount = logService.findTotalVisitCount();
		obj.put("totalVisitCount", totalVisitCount);
		Long todayVisitCount = logService.findTodayVisitCount(dayStart,dayEnd);
		obj.put("todayVisitCount", todayVisitCount);
		Long todayIp = logService.findTodayIp(dayStart,dayEnd);
		//update-end--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
		obj.put("todayIp", todayIp);
		result.setResult(obj);
		result.success("登录成功");
		return result;
	}

	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("visitInfo")
	public Result<List<Map<String,Object>>> visitInfo() {
		Result<List<Map<String,Object>>> result = new Result<List<Map<String,Object>>>();
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date dayEnd = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date dayStart = calendar.getTime();
        List<Map<String,Object>> list = logService.findVisitCount(dayStart, dayEnd);
		result.setResult(oConvertUtils.toLowerCasePageList(list));
		return result;
	}


	/**
	 * 登陆成功选择用户当前部门
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/selectDepart", method = RequestMethod.PUT)
	public Result<JSONObject> selectDepart(@RequestBody SysUser user) {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = user.getUsername();
		if(oConvertUtils.isEmpty(username)) {
			LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
			username = sysUser.getUsername();
		}
		String orgCode= user.getOrgCode();
		this.sysUserService.updateUserDepart(username, orgCode);
		SysUser sysUser = sysUserService.getUserByName(username);
		JSONObject obj = new JSONObject();
		obj.put("userInfo", sysUser);
		result.setResult(obj);
		return result;
	}

	/**
	 * 短信登录接口
	 *
	 * @param jsonObject
	 * @return
	 */
	@PostMapping(value = "/sms")
	public Result<String> sms(@RequestBody JSONObject jsonObject) {
		Result<String> result = new Result<String>();
		String mobile = jsonObject.get("mobile").toString();
		String smsmode=jsonObject.get("smsmode").toString();
		log.info(mobile);
		Object object = redisUtil.get(mobile);
		if (object != null) {
			result.setMessage("验证码10分钟内，仍然有效！");
			result.setSuccess(false);
			return result;
		}

		//随机数
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
    	obj.put("code", captcha);
		try {
			boolean b = false;
			//注册模板
			if (CommonConstant.SMS_TPL_TYPE_1.equals(smsmode)) {
				SysUser sysUser = sysUserService.getUserByPhone(mobile);
				if(sysUser!=null) {
					result.error500(" 手机号已经注册，请直接登录！");
					sysBaseAPI.addLog("手机号已经注册，请直接登录！", CommonConstant.LOG_TYPE_1, null);
					return result;
				}
				b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.REGISTER_TEMPLATE_CODE);
			}else {
				//登录模式，校验用户有效性
				SysUser sysUser = sysUserService.getUserByPhone(mobile);
				result = sysUserService.checkUserIsEffective(sysUser);
				if(!result.isSuccess()) {
					return result;
				}

				/**
				 * smsmode 短信模板方式  0 .登录模板、1.注册模板、2.忘记密码模板
				 */
				if (CommonConstant.SMS_TPL_TYPE_0.equals(smsmode)) {
					//登录模板
					b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
				} else if(CommonConstant.SMS_TPL_TYPE_2.equals(smsmode)) {
					//忘记密码模板
					b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
				}
			}

			if (b == false) {
				result.setMessage("短信验证码发送失败,请稍后重试");
				result.setSuccess(false);
				return result;
			}
			//验证码10分钟内有效
			redisUtil.set(mobile, captcha, 600);
			//update-begin--Author:scott  Date:20190812 for：issues#391
			//result.setResult(captcha);
			//update-end--Author:scott  Date:20190812 for：issues#391
			result.setSuccess(true);

		} catch (ClientException e) {
			e.printStackTrace();
			result.error500(" 短信接口未配置，请联系管理员！");
			return result;
		}
		return result;
	}


	/**
	 * 手机号登录接口
	 *
	 * @param jsonObject
	 * @return
	 */
	@ApiOperation("手机号登录接口")
	@PostMapping("/phoneLogin")
	public Result<JSONObject> phoneLogin(@RequestBody JSONObject jsonObject) {
		Result<JSONObject> result = new Result<JSONObject>();
		String phone = jsonObject.getString("mobile");

		//校验用户有效性
		SysUser sysUser = sysUserService.getUserByPhone(phone);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}

		String smscode = jsonObject.getString("captcha");
		Object code = redisUtil.get(phone);
		if (!smscode.equals(code)) {
			result.setMessage("手机验证码错误");
			return result;
		}
		String dataSource = jsonObject.getString("dataSource");
		// 生成token
//		String token = JwtUtil.sign(username, syspassword,dataSource);
		//用户信息
//		userInfo(sysUser,dataSource, result);
		//添加日志
		sysBaseAPI.addLog("用户名: " + sysUser.getUsername() + ",登录成功！", CommonConstant.LOG_TYPE_1, null);

		return result;
	}


	/*private Result<JSONObject> userInfoCache(SysUser sysUser,String dataSource, Result<JSONObject> result) {
		String syspassword = sysUser.getPassword();
		String username = sysUser.getUsername();
		// 生成token
		String token = JwtUtil.sign(username, syspassword, UUIDGenerator.generate(), dataSource,"");
		// 获取用户部门信息
		JSONObject obj = new JSONObject();
		List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
		obj.put("departs", departs);
		if (departs == null || departs.size() == 0) {
			obj.put("multi_depart", 0);
		} else if (departs.size() == 1) {
			sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
			obj.put("multi_depart", 1);
		} else {
			obj.put("multi_depart", 2);
		}

		JSONObject userInfo = (JSONObject) JSONObject.toJSON(sysUser);
		userInfo.put("dataSource", dataSource);

		obj.put("dataSource", dataSource);
		obj.put("token", token);
		obj.put("userInfo", userInfo);

		String key = MD5Util.MD5Encode(token + System.currentTimeMillis(), "utf-8");

//		redisUtil.set(key, )
		result.setResult(obj);
		result.success("登录成功");
		return result;
	}*/


	/**
	 * 用户信息
	 *
	 * @param sysUser
	 * @param result
	 * @return
	 */
	private Result<JSONObject> userInfo(SysUser sysUser,String dataSource, String token, Result result) {
		String syspassword = sysUser.getPassword();
		String username = sysUser.getUsername();
        // 设置token缓存有效时间
//		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token, JwtUtil.EXPIRE_TIME*2 / 1000);
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + username, token, JwtUtil.EXPIRE_TIME * 2);
//		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, );
		// 重置登录次数
		redisUtil.del(LOGIN_FAIL_COUNT_KEY + sysUser.getId());
		// 获取用户部门信息
		JSONObject obj = new JSONObject();
		List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
		obj.put("departs", departs);
		if (departs == null || departs.size() == 0) {
			obj.put("multi_depart", 0);
		} else if (departs.size() == 1) {
			sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
			obj.put("multi_depart", 1);
		} else {
			obj.put("multi_depart", 2);
		}

		// 获取菜单的时候更新
		/*UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("ID", sysUser.getId());
		updateWrapper.set("LOGIN_TIME", new Date());
		sysUserService.update(updateWrapper);*/

		JSONObject userInfo = (JSONObject) JSONObject.toJSON(sysUser);
		userInfo.put("dataSource", dataSource);
		//获取项目地配置信息
		userInfo.put("dataSourceInfo", sysDatasourceService.getByCode(dataSource));


		obj.put("dataSource", dataSource);
		obj.put("token", token);
		obj.put("userInfo", userInfo);
		result.setResult(obj);
		result.success("登录成功");
		return result;
	}

	/**
	 * 获取加密字符串
	 * @return
	 */
	@GetMapping(value = "/getEncryptedString")
	public Result<Map<String,String>> getEncryptedString(){
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		Map<String,String> map = new HashMap<String,String>();
		map.put("key", EncryptedString.key);
		map.put("iv",EncryptedString.iv);
		result.setResult(map);
		return result;
	}

	/**
	 * 获取校验码
	 */
	/*@ApiOperation("获取验证码")
	@GetMapping(value = "/getCheckCode")
	public Result<Map<String,String>> getCheckCode(){
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		Map<String,String> map = new HashMap<String,String>();
		try {
			String code = RandomUtil.randomString(BASE_CHECK_CODES,4);
			String key = MD5Util.MD5Encode(code+System.currentTimeMillis(), "utf-8");
			redisUtil.set(key, code, 60);
			map.put("key", key);
			map.put("code",code);
			result.setResult(map);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
		}
		return result;
	}
*/
	/**
	 * 获取校验码
	 */
	@ApiOperation("获取验证码图片")
	@GetMapping(value = "/getCheckImg")
	public Result<Map<String,String>> getCode(){
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		Map<String,String> map = new HashMap<String,String>();
		try {
			String code = RandomUtil.randomString(BASE_CHECK_CODES,4);
			String key = MD5Util.MD5Encode(code + System.currentTimeMillis(), "utf-8");

			BufferedImage image = new BufferedImage(70, 26, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			// 生成背景
			createBackground(g, 70, 26);
			// 生成字符
			createCharacter(g, code);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);

			String data = DatatypeConverter.printBase64Binary(baos.toByteArray());
			String imageData = "data:image/png;base64," + data;

			redisUtil.set(key, code.toLowerCase(), 60);
			map.put("key", key);
//			map.put("code",code);
			map.put("img",imageData);
			result.setResult(map);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
		}
		return result;
	}

	/**
	 * app登录
	 * @param sysLoginModel
	 * @return
	 * @throws Exception
	 */
	/*@RequestMapping(value = "/mLogin", method = RequestMethod.POST)
	public Result<JSONObject> mLogin(@RequestBody SysLoginModel sysLoginModel) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();

		//1. 校验用户是否有效
		SysUser sysUser = sysUserService.getUserByName(username);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}

		//2. 校验用户名或密码是否正确
		String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
		String syspassword = sysUser.getPassword();
		if (!syspassword.equals(userpassword)) {
			result.error500("用户名或密码错误");
			return result;
		}

		String orgCode = sysUser.getOrgCode();
		if(oConvertUtils.isEmpty(orgCode)) {
			//如果当前用户无选择部门 查看部门关联信息
			List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
			if (departs == null || departs.size() == 0) {
				result.error500("用户暂未归属部门,不可登录!");
				return result;
			}
			orgCode = departs.get(0).getOrgCode();
			sysUser.setOrgCode(orgCode);
			this.sysUserService.updateUserDepart(username, orgCode);
		}
		JSONObject obj = new JSONObject();
		//用户登录信息
		obj.put("userInfo", sysUser);

		// 生成token
		String token = JwtUtil.sign(username, syspassword);
		// 设置超时时间
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token, JwtUtil.EXPIRE_TIME*2 / 1000);
		//token 信息
		obj.put("token", token);
		result.setResult(obj);
		result.setSuccess(true);
		result.setCode(200);
		sysBaseAPI.addLog("用户名: " + username + ",登录成功[移动端]！", CommonConstant.LOG_TYPE_1, null);
		return result;
	}*/



	private Color getRandColor(int fc, int bc) {
		int f = fc;
		int b = bc;
		Random random=new Random();
		if(f>255) {
			f=255;
		}
		if(b>255) {
			b=255;
		}
		return new Color(f+random.nextInt(b-f),f+random.nextInt(b-f),f+random.nextInt(b-f));
	}

	private void createBackground(Graphics g, int w, int h) {
		// 填充背景
		g.setColor(getRandColor(220,250));
		g.fillRect(0, 0, w, h);
		// 加入干扰线条
		for (int i = 0; i < 8; i++) {
			g.setColor(getRandColor(40,150));
			Random random = new Random();
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int x1 = random.nextInt(w);
			int y1 = random.nextInt(h);
			g.drawLine(x, y, x1, y1);
		}
	}

	private void createCharacter(Graphics g, String code) {

		String[] fontTypes = {"Arial","Arial Black","AvantGarde Bk BT","Calibri"};
		Random random = new Random();
		for (int i = 0; i < code.length(); i++) {
			String r = code.charAt(i) + "";//random.nextInt(10));
			g.setColor(new Color(50 + random.nextInt(100), 50 + random.nextInt(100), 50 + random.nextInt(100)));
			g.setFont(new Font(fontTypes[random.nextInt(fontTypes.length)],Font.BOLD,26));
			g.drawString(r, 15 * i + 5, 19 + random.nextInt(8));
//			g.drawString(r, i*w/4, h-5);
		}
	}
}
