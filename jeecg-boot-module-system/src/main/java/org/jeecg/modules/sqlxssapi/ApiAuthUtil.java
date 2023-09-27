package org.jeecg.modules.sqlxssapi;

import com.ai.modules.api.util.ApiTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/12/6 17
 * @Description:
 */
@Component
@Slf4j
public class ApiAuthUtil {

    @Autowired
    @Lazy
    private ISysUserService sysUserService;
    @Autowired
    @Lazy
    private RedisUtil redisUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${engine.async}")
    private boolean async;

    // ApplicationRunnerImpl 程序启动初始化
    private static Map<String, Set<String>> API_AUTH_MAP = new HashMap<>();


    @PostConstruct
    public void run() {
        if (!async && ApiTokenUtil.IS_CENTER) {
            log.info("加载后端API权限控制");
            this.initApiAuth();
        }
    }

    /**
     * 获取禁止访问的url列表
     *
     * @param username
     * @return
     */
    public Set<String> getNoneApis(String username,boolean formRedis) {
        Set<String> noneApis = null;
        if(formRedis){
            noneApis = (Set<String>) redisUtil.get(CommonConstant.PREFIX_AUTH_API + username);
        }
        if (noneApis == null) {
            // 数据库获取已有角色
            Set<String> roleSet = sysUserService.getUserRolesSet(username);
            // 根据角色获取 禁止访问路径
            Set<String> noneApiSet = API_AUTH_MAP.entrySet().stream().filter(e -> !roleSet.contains(e.getKey())).map(Map.Entry::getValue).flatMap(Set::stream).collect(Collectors.toSet());
            // 根据角色获取 拥有权限路径
            Set<String> ownApiSet = roleSet.stream().map(r -> API_AUTH_MAP.get(r)).filter(Objects::nonNull).flatMap(Set::stream).collect(Collectors.toSet());
            // 移除已有权限
            noneApiSet.removeIf(r -> ownApiSet.stream().anyMatch(r::startsWith));
            redisUtil.set(CommonConstant.PREFIX_AUTH_API + username
                    , noneApis = noneApiSet
                    , JwtUtil.EXPIRE_TIME * 2);
        }
        return noneApis;
    }

    private void initApiAuth(){
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList("SELECT p.API_AUTH,a.role_id,b.ROLE_CODE FROM sys_permission p JOIN sys_role_permission a ON p.id=a.permission_id JOIN sys_role b ON a.role_id=b.id WHERE p.del_flag=0 AND p.API_AUTH IS NOT NULL ORDER BY a.role_id,p.API_AUTH ASC");
        if(list.size() == 0){
            return;
        }

        Map<String, Set<String>> apiMap = API_AUTH_MAP = new HashMap<>();
        String roleCodeNow = null;
        Set<String> apiSet = null;
        for(Map<String, Object> map: list){
            String roleCode = map.get("ROLE_CODE").toString();
            if(!roleCode.equals(roleCodeNow)){
                apiMap.put(roleCodeNow = roleCode, apiSet = new HashSet<>());
            }
            for(String path: map.get("API_AUTH").toString().split(",")){
                if(!path.startsWith("/")){
                    path = "/" + path;
                }
                /*if(!path.endsWith("/")){
                    path.substring(0, path.length() - 1);
                }*/
                apiSet.add(path);
            }
        }
    }
}
