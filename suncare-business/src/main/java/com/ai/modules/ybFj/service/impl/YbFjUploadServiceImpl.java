package com.ai.modules.ybFj.service.impl;

import com.ai.common.utils.FileZip;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.ybFj.entity.YbFjTemplateExport;
import com.ai.modules.ybFj.entity.YbFjTemplateImport;
import com.ai.modules.ybFj.entity.YbFjUpload;
import com.ai.modules.ybFj.entity.YbFjUploadResult;
import com.ai.modules.ybFj.mapper.YbFjUploadMapper;
import com.ai.modules.ybFj.service.IYbFjTemplateExportService;
import com.ai.modules.ybFj.service.IYbFjTemplateImportService;
import com.ai.modules.ybFj.service.IYbFjUploadResultService;
import com.ai.modules.ybFj.service.IYbFjUploadService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 飞检项目上传文件
 * @Author: jeecg-boot
 * @Date:   2023-02-06
 * @Version: V1.0
 */
@Service
@Slf4j
public class YbFjUploadServiceImpl extends ServiceImpl<YbFjUploadMapper, YbFjUpload> implements IYbFjUploadService {

    @Autowired
    private IYbFjTemplateImportService ybFjTemplateImportService;
    @Autowired
    private IYbFjTemplateExportService ybFjTemplateExportService;
    @Autowired
    private IYbFjUploadResultService ybFjUploadResultService;
    @Autowired
    private IMedicalDictService medicalDictService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Override
    @Transactional
    public void add(YbFjUpload ybFjUpload) {
        ybFjUpload.setId(IdUtils.uuid());
        //本地测试
        //ybFjUpload.setId("1234567");
        ybFjUpload.setTaskStatus("2");
        this.save(ybFjUpload);
        this.saveUploadResult(ybFjUpload);

    }

    @Override
    @Transactional
    public void edit(YbFjUpload ybFjUpload) {
        //先删除结果信息和已生成的文件
        List<YbFjUploadResult> uploadResultList = ybFjUploadResultService.list(new QueryWrapper<YbFjUploadResult>().eq("UPLOAD_ID",ybFjUpload.getId()));
        this.deleteResultAndFile(uploadResultList);
        ybFjUpload.setTaskStatus("2");
        ybFjUpload.setTaskDesc("");
        this.saveUploadResult(ybFjUpload);
    }

    private void deleteResultAndFile(List<YbFjUploadResult> uploadResultList) {
        List<String> resultIds = new ArrayList<>();
        for(YbFjUploadResult resultBean:uploadResultList){
            if(StringUtils.isNotBlank(resultBean.getFileFullpath())){
                File file = new File(resultBean.getFileFullpath());
                if(file.exists()){
                    file.delete();
                }
            }
            resultIds.add(resultBean.getId());
        }
        if(resultIds.size()>0){
            ybFjUploadResultService.removeByIds(resultIds);
        }
    }

    //生成
    private void saveUploadResult(YbFjUpload ybFjUpload){
        try {
            //导入模板
            String templateImportId = ybFjUpload.getTemplateImportId();
            YbFjTemplateImport ybFjTemplateImport = ybFjTemplateImportService.getById(templateImportId);
            //参数2 导入文件的文件名（全路径）
            String uploadFilePath = ybFjUpload.getFilePath();
            //导出模板
            QueryWrapper<YbFjTemplateExport> queryWrapper = new QueryWrapper<YbFjTemplateExport>();
            queryWrapper.in("TEMPLATE_CODE", ybFjUpload.getTemplateCodes().split(","));
            queryWrapper.eq("USE_STATUS", "1");//在用
            List<YbFjTemplateExport> ybFjTemplateExportList = this.ybFjTemplateExportService.list(queryWrapper);

            List<YbFjUploadResult> uploadResultList = new ArrayList<YbFjUploadResult>();
            for (YbFjTemplateExport exportBean : ybFjTemplateExportList) {
                //调用python
                //生成文档的类型编码
                String targetType = exportBean.getPythonProcCode();

                //导入文件的文件名（全路径）
                String inputPath = uploadpath + "/" + uploadFilePath;

                //生成结果文件的模板格式文件（全路径）
                String templatePath = StringUtils.isNotBlank(exportBean.getTemplatePath()) ? uploadpath + "/" + exportBean.getTemplatePath() : null;

                //生成的结果文件存放的文件夹路径（全路径）
                String outputPath = uploadpath + "/doc_result/" + ybFjUpload.getId() + "/" + targetType;
                //生成结果文件的文件名（不含扩展名）
                String filenamePrefix = targetType;

                deleteDir(outputPath);

                String cmd = "/home/web/python_src/gendoc.sh %s %s %s %s %s";
                cmd = String.format(cmd, targetType,inputPath, templatePath,outputPath,filenamePrefix);
                log.info("cmd:{}", cmd);
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
                    process.waitFor();
                } catch (Exception e) {
                    log.error("", e);
                    throw new Exception("调用python执行失败");
                }

                //读取txt文件
                String fileName = outputPath + "/result.txt";
                //判断文件是否存在
                if (!new File(fileName).exists()) {
                    log.error("未生成结果文件");
                    throw new Exception("未生成结果文件");
                }
                //读取文件
                List<String> lineLists = null;
                try (Stream<String> stream = Files.lines(Paths.get(fileName), Charset.defaultCharset())){
                    lineLists =stream.flatMap(line -> Arrays.stream(line.split("\n")))
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    throw new Exception("读取生成文件异常");
                }

                for (String linsStr : lineLists) {
                    String resultFileName = linsStr.replace(outputPath + "/" + filenamePrefix, "");
                    String filePath = linsStr.replace(uploadpath,"");
                    //本地测试
                    //resultFileName = linsStr.replace("/home/web/suncare_v4/upload" + "/doc_result/"  + "a0ec4e36b37f417bb81ebb4ee3a3bc9c/" + targetType + "/" + filenamePrefix, "");
                    //filePath = linsStr.replace("/home/web/suncare_v4/upload", "");
                    //linsStr =  linsStr.replace("/home/web/suncare_v4/upload", uploadpath);

                    //保存明细
                    YbFjUploadResult resultBean = new YbFjUploadResult();
                    resultBean.setId(IdUtils.uuid());
                    resultBean.setUploadId(ybFjUpload.getId());
                    resultBean.setTemplateImportId(templateImportId);
                    resultBean.setTemplateCode(exportBean.getTemplateCode());
                    resultBean.setExportType(targetType);
                    resultBean.setFilePath(filePath);
                    resultBean.setFileName(resultFileName);
                    resultBean.setFileFullpath(linsStr);
                    resultBean.setTaskStatus("2");
                    uploadResultList.add(resultBean);
                }
            }
            ybFjUploadResultService.saveBatch(uploadResultList);
        }catch (Exception e){
            ybFjUpload.setTaskStatus("3");
            ybFjUpload.setTaskDesc(e.getMessage());
        }finally {
            this.updateById(ybFjUpload);
        }
    }

    public static void deleteDir(String path){
        File file = new File(path);
        if(file.exists()){
            String[] content = file.list();//取得当前目录下所有文件和文件夹
            for(String name : content){
                File temp = new File(path, name);
                if(temp.isDirectory()){//判断是否是目录
                    deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                    temp.delete();//删除空目录
                }else{
                    if(!temp.delete()){//直接删除文件
                        System.err.println("Failed to delete " + name);
                    }
                }
            }
        }
        if(!file.exists()&&!file.isDirectory()){
            file .mkdirs();
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        //先删除结果信息和已生成的文件
        List<YbFjUploadResult> uploadResultList = ybFjUploadResultService.list(new QueryWrapper<YbFjUploadResult>().eq("UPLOAD_ID",id));
        this.deleteResultAndFile(uploadResultList);
        this.removeById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        //先删除结果信息和已生成的文件
        List<YbFjUploadResult> uploadResultList = ybFjUploadResultService.list(new QueryWrapper<YbFjUploadResult>().in("UPLOAD_ID",idList));
        this.deleteResultAndFile(uploadResultList);
        this.removeByIds(idList);
    }

    @Override
    public String downloadZip(String ids,String templateCode,String resultIds) throws Exception {
        List<String> idList = Arrays.asList(ids.split(","));
        QueryWrapper<YbFjUploadResult> queryWrapper = new QueryWrapper<YbFjUploadResult>();
        queryWrapper.in("UPLOAD_ID",idList);
        if(StringUtils.isNotBlank(templateCode)){
            queryWrapper.eq("TEMPLATE_CODE",templateCode);
        }
        if(StringUtils.isNotBlank(resultIds)){
            queryWrapper.in("ID",Arrays.asList(resultIds.split(",")));
        }
        List<YbFjUploadResult> uploadResultList = ybFjUploadResultService.list(queryWrapper);
        List<String> fileList =  uploadResultList.stream().filter(t->StringUtils.isNotBlank(t.getFileFullpath())).map(t->t.getFileFullpath()).collect(Collectors.toList());
        String zipPath = IdUtils.uuid()+".zip";
        FileZip.zip(uploadpath+"/"+zipPath,fileList,"");
        return zipPath;
    }
}
