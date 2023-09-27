package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.vo.MedicalColConfigVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 表字段配置
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
public interface IMedicalColConfigService extends IService<MedicalColConfig> {

    List<MedicalColConfigVO> getRuleColConfig(String tableName);

    List<MedicalColConfig> getGradeColConfig();

    List<MedicalColConfig> getGroupByColConfig();

    MedicalColConfig getMedicalColConfig(String colName, String tabName);

    MedicalColConfig getMedicalColConfigByCache(String colName, String tabName);

	boolean isExist(String tabName, String colName, String id);

	Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

	/**
	 * 导出excel
	 * @param list
	 * @param os
	 * @return
	 */
	public boolean exportExcel(List<MedicalColConfig> list, OutputStream os);

	void clearCache();

	void clearCacheByCol(String colName, String tableName);
}
