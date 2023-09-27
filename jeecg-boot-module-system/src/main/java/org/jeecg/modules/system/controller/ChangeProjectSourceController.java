package org.jeecg.modules.system.controller;

import cn.hutool.json.JSONObject;
import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.service.ISysDatasourceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projectSource")
@Api(tags="项目地")
@Slf4j
public class ChangeProjectSourceController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDatasourceService sysDatasourceService;

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Autowired
    private ISysRoleService sysRoleService;

    @ApiOperation("切换项目地")
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public Result<?> change(@RequestBody JSONObject jsonObject) throws Exception {
        Result result = new Result();
        String dataSource = jsonObject.getStr("dataSource");
        LoginUser loginUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        SysUser sysUser = sysUserService.getById(loginUser.getId());

        //重新生成token
        String token = JwtUtil.sign(sysUser.getUsername(), sysUser.getPassword(), UUIDGenerator.generate(), dataSource,loginUser.getSystemCode());

        //用户登录信息
        String username = sysUser.getUsername();
        // 设置token缓存有效时间
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + username, token, JwtUtil.EXPIRE_TIME * 2);
        // 获取用户部门信息
        com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();
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

        com.alibaba.fastjson.JSONObject userInfo = (com.alibaba.fastjson.JSONObject) com.alibaba.fastjson.JSONObject.toJSON(sysUser);
        userInfo.put("dataSource", dataSource);
        //获取项目地配置信息
        userInfo.put("dataSourceInfo", sysDatasourceService.getByCode(dataSource));

        obj.put("dataSource", dataSource);
        obj.put("token", token);
        obj.put("userInfo", userInfo);
        result.setResult(obj);
        result.success("切换成功");

        return result;
    }

    /**
     * 通过systemCode查询
     *
     * @param systemCode
     * @return
     */
    @AutoLog(value = "项目地配置-通过systemCode查询")
    @ApiOperation(value="项目地配置-通过code查询", notes="项目地配置-通过code查询")
    @GetMapping(value = "/queryBySystemCode")
    public Result<?> queryBySystemCode(@RequestParam(name="systemCode",required=true) String systemCode,String username) {
        List<SysDatasource> list = new ArrayList<>();
        if(StringUtils.isNotBlank(username)){
            //获取用户信息
            SysUser sysUser = sysUserService.getUserByName(username);
            if(sysUser==null){
                return Result.ok(list);
            }
            //获取用户角色
            List<SysUserRole> userRole = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, sysUser.getId())
                    .inSql(SysUserRole::getRoleId,"select ID from sys_role where system_code='"+systemCode+"' "));
            List<String> roleIdList = userRole.stream().map(t->t.getRoleId()).collect(Collectors.toList());
            //获取角色List
            if(roleIdList.size()==0){
                return Result.ok(list);
            }
            List<SysRole> roleList = sysRoleService.list(new QueryWrapper<SysRole>().lambda().in(SysRole::getId,roleIdList));
            List<String> dataSoureList = roleList.stream().filter(t->StringUtils. isNotBlank(t.getDataSource())).
                    flatMap(t-> Arrays.stream(t.getDataSource().split(","))).collect(Collectors.toList());
            if(dataSoureList.size()>0){
                list = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().
                        and(wrapper->wrapper.eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).or().eq(SysDatasource::getSystemCode, "")
                        )
                        .in(SysDatasource::getCode,dataSoureList)
                );
            }

        }else {
            list = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).or().eq(SysDatasource::getSystemCode, ""));
        }
        return Result.ok(list);
    }
}
