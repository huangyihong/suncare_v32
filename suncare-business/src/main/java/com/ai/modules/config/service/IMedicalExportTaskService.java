package com.ai.modules.config.service;

import java.io.FileOutputStream;
import java.math.BigDecimal;

import org.jeecg.common.system.vo.LoginUser;

import com.ai.modules.config.entity.MedicalExportTask;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 导出文件任务
 * @Author: jeecg-boot
 * @Date:   2020-01-06
 * @Version: V1.0
 */
public interface IMedicalExportTaskService extends IService<MedicalExportTask> {

	/**
	 * 插入导出任务
	 * @param uploadpath
	 * @param title
	 * @param user
	 * @return
	 */
	public MedicalExportTask saveExportExcel(String uploadpath, String title,LoginUser user);

    MedicalExportTask saveExportExcel(String uploadpath, String title, int count, LoginUser user,String suffix, boolean remote);

    /**
	 * 计算文件大小，单位kb
	 * @param filePath
	 * @return
	 */
	public BigDecimal getFileSize(String filePath);

	/**
	 * 根据文件路径获取输出流
	 * @param outPath
	 * @return
	 */
	public FileOutputStream getFileOutputStream(String outPath) throws Exception;

    MedicalExportTask saveExportExcelFixTitle(String uploadpath, String fileName, int count, LoginUser user);

	MedicalExportTask findByName(String fileName);

}
