package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.entity.MedicalDrugGroup;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: 药品组
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
public interface IMedicalDrugGroupService extends IService<MedicalDrugGroup> {

	void saveGroup(MedicalDrugGroup medicalDrugGroup, String codes, String names, String tableTypes);

	void updateGroup(MedicalDrugGroup medicalDrugGroup, String codes, String names, String tableTypes);

	void removeGroupById(String id);

	void removeGroupByIds(List<String> idList);

	List<MedicalGroupVO> queryGroupItem(MedicalGroupVO bean);

	int queryGroupItemCount(MedicalGroupVO bean);

	List<MedicalGroupVO> queryGroupItemByGroupCodes(List<String> groupCodes);

	/**
	 * 导出excel
	 * @param list
	 * @param os
	 * @return
	 */
	public boolean exportExcel(List<MedicalGroupVO> list, OutputStream os, String suffix) throws Exception;

	/**
	 * 导入
	 * @param file
	 * @return
	 */
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

	/**
	 * 通过分类查询分组（1医疗项目分组 5疾病分组 7药品分组 ）
	 * @param kinds
	 * @return
	 */
	Map<String, List<MedicalDict>> queryGroupByKinds(String[] kinds);

	/**
	 * 编码是否重复
	 *
	 * @param groupCode
	 * @param groupName
	 * @param groupId
	 * @return
	 */
	boolean isExistName(String groupCode, String groupName, String groupId);

	public String getGroupNameByGroupCode(String kind, String groupCode);

	public Map<String,String> getBeanByGroupCode(String kind, String groupCode);

    Map<String, String> getMapByGroupCode(String kind, String groupCodes);

	List<MedicalGroupVO> queryGroupItem2(QueryWrapper<MedicalDrugGroup> queryWrapper);
	int queryGroupItemCount2(QueryWrapper<MedicalDrugGroup> queryWrapper);
}
