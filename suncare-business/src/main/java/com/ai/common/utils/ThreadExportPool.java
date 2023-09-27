package com.ai.common.utils;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalExportTask;
import com.ai.modules.config.service.IMedicalExportTaskService;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.internal.Function;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2020/3/30 14
 * @Description:
 */
@Slf4j
@Component
public class ThreadExportPool extends ThreadUtils.FixPool {

    @Autowired
    private IMedicalExportTaskService serviceExportTask;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    ThreadExportPool() {
        super(5);
    }

    public void add(String title, String suffix, int count, Function<OutputStream, Result> function) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MedicalExportTask exportTask = serviceExportTask.saveExportExcel(uploadpath, title, count, user, suffix, false);
        executor.submit(new EngineFunctionRunnable(user.getDataSource(), user.getToken(), () -> {

            try {
                log.info("开始导出文件：" + exportTask.getFileName());
                exportTask.setStatus("00");
                serviceExportTask.updateById(exportTask);
                FileOutputStream os = serviceExportTask.getFileOutputStream(exportTask.getFileFullpath());
                Result result = function.apply(os);
                if (!result.isSuccess()) {
                    throw new Exception(result.getMessage());
                }
                if (result.getResult() != null && result.getResult() instanceof Integer) {
                    exportTask.setRecordCount((Integer) result.getResult());
                }
                exportTask.setFileSize(serviceExportTask.getFileSize(exportTask.getFileFullpath()));
                exportTask.setStatus("01");
            } catch (Exception e) {
                log.error("", e);
                exportTask.setStatus("02");
                String msg = e.getMessage();
                if (msg.length() > 1000) {
                    msg = msg.substring(0, 1000);
                }
                exportTask.setErrorMsg(msg);
            } finally {
                exportTask.setOverTime(new Date());
                serviceExportTask.updateById(exportTask);

            }
        }));
    }

    public void saveRemoteTask(String title, String suffix, int count, Function<String, Result> function) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MedicalExportTask exportTask = serviceExportTask.saveExportExcel(uploadpath, title, count, user, suffix, true);
        try {
            log.info("开始导出文件(远程)：" + exportTask.getFileName());
            exportTask.setStatus("00");
            this.updateTaskRemote(exportTask);
            Result result = function.apply(exportTask.getFileFullpath());
            if (!result.isSuccess()) {
                throw new Exception(result.getMessage());
            }
            if (result.getResult() != null && result.getResult() instanceof Integer) {
                exportTask.setRecordCount((Integer) result.getResult());
            }
            exportTask.setFileSize(serviceExportTask.getFileSize(exportTask.getFileFullpath()));
            exportTask.setStatus("01");
        } catch (Exception e) {
            log.error("", e);
            exportTask.setStatus("02");
            String msg = e.getMessage();
            if (msg.length() > 1000) {
                msg = msg.substring(0, 1000);
            }
            exportTask.setErrorMsg(msg);
        } finally {
            exportTask.setOverTime(new Date());
            this.updateTaskRemote(exportTask);

        }
    }

    public void addRemote(String title, String suffix, int count, Function<OutputStream, Result> function) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MedicalExportTask exportTask = serviceExportTask.saveExportExcel(uploadpath, title, count, user, suffix, true);

        executor.submit(new EngineFunctionRunnable(user.getDataSource(), user.getToken(), () -> {

            try {
                log.info("开始导出文件(远程)：" + exportTask.getFileName());
                exportTask.setStatus("00");
                this.updateTaskRemote(exportTask);
                FileOutputStream os = serviceExportTask.getFileOutputStream(exportTask.getFileFullpath());
                Result result = function.apply(os);
                if (!result.isSuccess()) {
                    throw new Exception(result.getMessage());
                }
                if (result.getResult() != null && result.getResult() instanceof Integer) {
                    exportTask.setRecordCount((Integer) result.getResult());
                }
                exportTask.setFileSize(serviceExportTask.getFileSize(exportTask.getFileFullpath()));
                exportTask.setStatus("01");
            } catch (Exception e) {
                log.error("", e);
                exportTask.setStatus("02");
                String msg = e.getMessage();
                if (msg.length() > 1000) {
                    msg = msg.substring(0, 1000);
                }
                exportTask.setErrorMsg(msg);
            } finally {
                exportTask.setOverTime(new Date());
                this.updateTaskRemote(exportTask);

            }


        }));
    }

    public void addRemoteMulti(String title, String suffix, List<Integer> counts, Function<List<OutputStream>, Result> function) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        AtomicInteger index = new AtomicInteger(1);

        List<MedicalExportTask> taskList = counts.stream().map(count ->
                serviceExportTask.saveExportExcel(uploadpath,
                        title + (counts.size() > 1?"_(" + index.getAndIncrement() + ")":""), count, user, suffix, true)).
                collect(Collectors.toList());

        executor.submit(new EngineFunctionRunnable(user.getDataSource(), user.getToken(), () -> {

            try {

                List<OutputStream> osList = taskList.stream().peek(exportTask -> {
                    log.info("开始导出文件：" + exportTask.getFileName());
                    exportTask.setStatus("00");
                }).map(exportTask -> {
                    try {
                        return serviceExportTask.getFileOutputStream(exportTask.getFileFullpath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }).collect(Collectors.toList());

                this.updateTasksRemote(taskList);

                Result result = function.apply(osList);
                if (!result.isSuccess()) {
                    throw new Exception(result.getMessage());
                }

                for (MedicalExportTask exportTask : taskList) {
                    exportTask.setFileSize(serviceExportTask.getFileSize(exportTask.getFileFullpath()));
                    exportTask.setStatus("01");
                }

            } catch (Exception e) {
                log.error("", e);
                String msg = e.getMessage();
                if (msg.length() > 1000) {
                    msg = msg.substring(0, 1000);
                }
                for (MedicalExportTask exportTask : taskList) {
                    exportTask.setErrorMsg(msg);
                    exportTask.setStatus("02");
                }
            } finally {
                for (MedicalExportTask exportTask : taskList) {
                    exportTask.setOverTime(new Date());
                }
                this.updateTasksRemote(taskList);

            }


        }));
    }

    private void updateTaskRemote(MedicalExportTask exportTask){
        ApiTokenUtil.putBodyApi("/config/medicalExportTask/edit", exportTask);

    }

    private void updateTasksRemote(List<MedicalExportTask> exportTasks){
        ApiTokenUtil.putBodyApi("/config/medicalExportTask/editBatch", exportTasks);

    }

    // 批量创建多个文件导出 返回每个文件的输出流
    public void addUnRandom(List<String> fileNames, Function<List<OutputStream>, Result> function) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<MedicalExportTask> taskList = fileNames.stream().map(fileName -> serviceExportTask.saveExportExcelFixTitle(uploadpath, fileName, -1, user)).collect(Collectors.toList());
        executor.submit(new EngineFunctionRunnable(user.getDataSource(), user.getToken(), () -> {
            try {

                List<OutputStream> osList = taskList.stream().peek(exportTask -> {
                    log.info("开始导出文件：" + exportTask.getFileName());
                    exportTask.setStatus("00");
                    serviceExportTask.updateById(exportTask);
                }).map(exportTask -> {
                    try {
                        return serviceExportTask.getFileOutputStream(exportTask.getFileFullpath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }).collect(Collectors.toList());
                Result result = function.apply(osList);
                if (!result.isSuccess()) {
                    throw new Exception(result.getMessage());
                }

                for (MedicalExportTask exportTask : taskList) {
                    exportTask.setFileSize(serviceExportTask.getFileSize(exportTask.getFileFullpath()));
                    exportTask.setStatus("01");
                }
            } catch (Exception e) {
                log.error("", e);
                String msg = e.getMessage();
                if (msg.length() > 1000) {
                    msg = msg.substring(0, 1000);
                }
                for (MedicalExportTask exportTask : taskList) {
                    exportTask.setErrorMsg(msg);
                    exportTask.setStatus("02");
                }
            } finally {
                for (MedicalExportTask exportTask : taskList) {
                    exportTask.setOverTime(new Date());
                }
                serviceExportTask.updateBatchById(taskList);

            }


        }));
    }

    // 批量创建多个文件导出 返回每个文件的输出流
    public void addUnRandomSync(List<String> fileNames, Function<List<OutputStream>, Result> function) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<MedicalExportTask> taskList = fileNames.stream().map(fileName -> serviceExportTask.saveExportExcelFixTitle(uploadpath, fileName, -1, user)).collect(Collectors.toList());

        try {

            List<OutputStream> osList = taskList.stream().peek(exportTask -> {
                log.info("开始导出文件：" + exportTask.getFileName());
                exportTask.setStatus("00");
                serviceExportTask.updateById(exportTask);
            }).map(exportTask -> {
                try {
                    return serviceExportTask.getFileOutputStream(exportTask.getFileFullpath());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }).collect(Collectors.toList());
            Result result = function.apply(osList);
            if (!result.isSuccess()) {
                throw new Exception(result.getMessage());
            }

            for (MedicalExportTask exportTask : taskList) {
                exportTask.setFileSize(serviceExportTask.getFileSize(exportTask.getFileFullpath()));
                exportTask.setStatus("01");
            }
        } catch (Exception e) {
            log.error("", e);
            String msg = e.getMessage();
            if (msg.length() > 1000) {
                msg = msg.substring(0, 1000);
            }
            for (MedicalExportTask exportTask : taskList) {
                exportTask.setErrorMsg(msg);
                exportTask.setStatus("02");
            }
        } finally {
            for (MedicalExportTask exportTask : taskList) {
                exportTask.setOverTime(new Date());
            }
            serviceExportTask.updateBatchById(taskList);

        }


    }
}
