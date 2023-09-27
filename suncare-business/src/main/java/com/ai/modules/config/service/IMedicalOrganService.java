package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗机构
 * @Author: jeecg-boot
 * @Date:   2019-12-31
 * @Version: V1.0
 */
public interface IMedicalOrganService extends IService<MedicalOrgan> {
    IPage<MedicalOrgan> pageByMasterInfoJoin(Page<MedicalOrgan> page, QueryWrapper<MedicalOrgan> queryWrapper, String dataSource);

    /**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalOrgan> getQueryWrapper(MedicalOrgan medicalOrgan,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalOrgan> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalOrgan(MedicalOrgan bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalOrgan(MedicalOrgan bean);

	/**
	 * 编码是否重复
	 * @param code
	 * @param id
	 * @return
	 */
	boolean isExistName(String code,String id);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalOrgan(MedicalOrgan bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalOrgan(MedicalOrgan bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalOrgan(QueryWrapper<MedicalOrgan> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalOrgan(QueryWrapper<MedicalOrgan> queryWrapper, MedicalOrgan bean) throws Exception;

	/**
	 * 批量新增修改导入
	 * @param file
	 * @param user
	 * @return
	 */
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

	/**
	 * 导出excel
	 * @param queryWrapper
	 * @param os
	 * @return
	 */
	public boolean exportExcel(QueryWrapper<MedicalOrgan> queryWrapper, OutputStream os, String suffix);

	/**
	 *
	 * 功能描述：下载导入过程中出现重复的机构
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年10月11日 上午10:22:37</p>
	 *
	 * @param serialNum
	 * @param os
	 * @param suffix
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public boolean exportExcel(String serialNum, OutputStream os, String suffix);

	/**
	 * 根据code获取name
	 * @param code
	 * @return
	 */
	public String getNameByCode(String code);

	public Map<String, String> getMapByCode(String codes);

	public List<Map<String, Object>> getRegionList(String paretnCode, String type);

    List<MedicalCodeNameVO> listMasterInfoJoinSelectMaps(QueryWrapper<MedicalOrgan> queryWrapper, String dataSource);

	/**
	 * 名称是否重复
     * @param bean
     * @param code
	 * @return
	 */
	boolean isExistOrg(MedicalOrgan bean, String code);
}
