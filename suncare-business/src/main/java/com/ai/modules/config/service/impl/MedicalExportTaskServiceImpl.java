package com.ai.modules.config.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ai.modules.api.util.ApiTokenUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalExportTask;
import com.ai.modules.config.mapper.MedicalExportTaskMapper;
import com.ai.modules.config.service.IMedicalExportTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 导出文件任务
 * @Author: jeecg-boot
 * @Date:   2020-01-06
 * @Version: V1.0
 */
@Service
public class MedicalExportTaskServiceImpl extends ServiceImpl<MedicalExportTaskMapper, MedicalExportTask> implements IMedicalExportTaskService {

	@Override
	public MedicalExportTask saveExportExcel(String uploadpath, String title,LoginUser user) {
		String bizPath = "excelfiles";
		String timeStr =  new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
	    String relativePath = bizPath + File.separator + title + timeStr +".xls";
	    String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();

        MedicalExportTask exportTask = new MedicalExportTask();
        exportTask.setId(IdUtils.uuid());
        exportTask.setFileName(title + timeStr + ".xls");
        exportTask.setFilePath(relativePath);
        exportTask.setFileFullpath(outPath);
        exportTask.setStatus("-1");
        exportTask.setCreateTime(new Date());
        exportTask.setCreateUser(user.getRealname());
        this.save(exportTask);
        return exportTask;
	}


	@Override
	public MedicalExportTask saveExportExcel(String uploadpath, String title,int count,LoginUser user,String suffix, boolean remote) {
		String bizPath = "excelfiles";
		String timeStr =  new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
	    String relativePath = bizPath + File.separator + title + "_" + timeStr + "."+ suffix;
	    String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();

        MedicalExportTask exportTask = new MedicalExportTask();
        exportTask.setId(IdUtils.uuid());
        exportTask.setFileName(title + "_" + timeStr + "."+ suffix);
        exportTask.setFilePath(relativePath);
        exportTask.setFileFullpath(outPath);
        exportTask.setRecordCount(count);
        exportTask.setStatus("-1");
        //        exportTask.setCreateTime(new Date());
//        exportTask.setCreateUser(user.getRealname());
        if(remote) {
            exportTask.setDataSource(user.getDataSource());
            // 新增导出进度接口
            ApiTokenUtil.postBodyApi("/config/medicalExportTask/add", exportTask);
        } else {
            this.save(exportTask);
        }

        return exportTask;
	}

	@Override
	public BigDecimal getFileSize(String filePath) {
		double kb = 1024;
		long size = new File(filePath).length();
		double fileSize = size / kb;
		return new BigDecimal(fileSize);
	}

	@Override
	public FileOutputStream getFileOutputStream(String outPath) throws Exception {
		File outFile = new File(outPath);
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        FileOutputStream os = new FileOutputStream(outPath);
        return os;
	}

    @Override
    public MedicalExportTask saveExportExcelFixTitle(String uploadpath, String fileName, int count, LoginUser user) {
        String bizPath = "excelfiles";
        String relativePath = bizPath + File.separator + fileName;
        String outPath = new File(uploadpath+ File.separator +relativePath).getAbsolutePath();

        MedicalExportTask exportTask = new MedicalExportTask();
        exportTask.setId(IdUtils.uuid());
        exportTask.setFileName(fileName);
        exportTask.setFilePath(relativePath);
        exportTask.setFileFullpath(outPath);
        exportTask.setRecordCount(count);
        exportTask.setStatus("-1");
        exportTask.setCreateTime(new Date());
        exportTask.setCreateUser(user.getRealname());
        this.save(exportTask);
        return exportTask;
	}

    @Override
    public MedicalExportTask findByName(String fileName) {
	    List<MedicalExportTask> list = this.list(new QueryWrapper<MedicalExportTask>().eq("FILE_NAME",fileName));
        return list.size() > 0? list.get(0): null;
    }


}
