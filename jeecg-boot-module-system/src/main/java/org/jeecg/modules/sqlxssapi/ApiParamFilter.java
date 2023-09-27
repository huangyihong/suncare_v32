package org.jeecg.modules.sqlxssapi;

import com.ai.modules.api.util.ApiTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.SqlInjectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 2021.8.18 zhangpeng
 */
@Slf4j
public class ApiParamFilter extends BasicHttpAuthenticationFilter {


    private static Logger apiAuthLog = LoggerFactory.getLogger("API_AUTH");



    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if(!ApiTokenUtil.IS_CENTER){
            return true;
        }
        try {

            String username = JwtUtil.getUserNameByToken((HttpServletRequest) request);
            boolean isInterface = username.startsWith("interface");
            HttpServletRequest req = (HttpServletRequest) request;
            // url路径权限验证    禁止访问路径 - 拥有权限路径 = 禁止访问
            if (!isInterface) {
//			applicationRunner.initApiAuth();
//			long startTime = System.currentTimeMillis();
                String url = WebUtils.getPathWithinApplication(req);
                Set<String> noneApis = SpringContextUtils.getBean(ApiAuthUtil.class).getNoneApis(username,true);
                // 禁止权限验证
                if (noneApis.stream().anyMatch(r -> {
                    // 通配符  缺省匹配
                    if (r.endsWith("/*")) {
                        return url.startsWith(r.substring(0, r.length() - 1));
                    } else {
                        // 精确匹配
                        return url.equals(r);
                    }
                })) {
//			throw new AuthenticationException("无权限访问");
//				log.info("禁止访问列表：" + ownApis.toString());
                    apiAuthLog.info(username + "-权限禁止API：" + url);
                    throw new AuthenticationException("无权限访问");
                }
//			log.info("权限验证时间：" + (System.currentTimeMillis() - startTime) + "ms，" + noneApis.size() );
                // sql 注入过滤
                this.doFilterSqlInject(req, username);
            }
            return true;
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage(), e);
//			throw new AuthenticationException("Token失效，请重新登录", e);
        }
    }


    private void doFilterSqlInject(HttpServletRequest request, String username){
        String method = request.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            String value = null;
            try {
                value = getBodyString(request.getReader());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(StringUtils.isNotBlank(value)){
                try {
                    SqlInjectionUtil.filterContent(value);
                } catch (RuntimeException e){
                    String url = WebUtils.getPathWithinApplication(request);
                    apiAuthLog.info(username + "：" + url + "-SQL注入：" + value);
                }

            }
        }
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                try {
                    SqlInjectionUtil.filterContent(value);
                } catch (RuntimeException e){
                    String url = WebUtils.getPathWithinApplication(request);
                    apiAuthLog.info(username + "：" + url + "-SQL注入：" + value);
                }
            }
        }

    }

    // 获取request请求body中参数
    private String getBodyString(BufferedReader br) throws IOException {
        String inputLine;
        StringBuilder str = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            str.append(inputLine);
        }
        br.close();
        return str.toString();

    }



}
