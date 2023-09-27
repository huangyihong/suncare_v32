package com.ai.modules.config.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ai.modules.config.entity.MedicalPolicy;
import com.ai.modules.config.entity.MedicalPolicyBasis;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 新版本政策法规
 * @Author: jeecg-boot
 * @Date:   2021-08-04
 * @Version: V1.0
 */
public interface IMedicalPolicyService extends IService<MedicalPolicy> {
	public void saveDocToSolr(MedicalPolicy medicalPolicy)  throws Exception;
	public void deleteDocFromSolr(String id)throws Exception;

	public boolean exportExcel(List<MedicalPolicy> list, OutputStream os, String suffix) throws Exception;
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;
	public boolean isExistName(String name, String id);
	 public void deleteByIds(List<String> idList);
	public MedicalPolicy getBeanByPolicyCode(String name);

	public List<Map<String ,Object>> queryPolicyDocContent(String policyId ,String searchContent ) throws Exception;
}
