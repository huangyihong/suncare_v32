package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.FileZip;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.entity.*;
import com.ai.modules.ybFj.mapper.YbFjOcrMapper;
import com.ai.modules.ybFj.service.IYbFjOcrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
 * @Description: OCR识别工具
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Service
@Slf4j
public class YbFjOcrServiceImpl extends ServiceImpl<YbFjOcrMapper, YbFjOcr> implements IYbFjOcrService {
    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Transactional
    @Override
    public void add(YbFjOcr ybFjOcr) {
        ybFjOcr.setId(IdUtils.uuid());
        ybFjOcr.setTaskStatus("2");
        this.save(ybFjOcr);
        this.saveUploadResult(ybFjOcr);
    }

    @Override
    public void edit(YbFjOcr ybFjOcr) {
        ybFjOcr.setTaskStatus("2");
        ybFjOcr.setTaskDesc("");
        ybFjOcr.setExportName("");
        ybFjOcr.setExportPath("");
        this.saveUploadResult(ybFjOcr);
    }

    @Override
    public String downloadZip(String ids) throws Exception {
        List<String> idList = Arrays.asList(ids.split(","));
        QueryWrapper<YbFjOcr> queryWrapper = new QueryWrapper<YbFjOcr>();
        queryWrapper.in("id",idList);
        List<YbFjOcr> list = list(queryWrapper);
        List<String> fileList =  list.stream().filter(t->StringUtils.isNotBlank(t.getExportPath())).map(t->t.getExportPath()).collect(Collectors.toList());
        String zipPath = IdUtils.uuid()+".zip";
        FileZip.zip(uploadpath+"/"+zipPath,fileList,"");
        return zipPath;
    }

    @Transactional
    @Override
    public void delByIds(List<String> ids) {
        this.removeByIds(ids);
        for(String id:ids){
            String outputPath = uploadpath + "/ocr_result/"+id;
            File file = new File(outputPath);
            file.delete();
        }

    }


    //生成
    private void saveUploadResult(YbFjOcr ybFjOcr){
        try {

            //生成文档类型()
            String exportType = ybFjOcr.getExportType();

            //待OCR识别图片全路径
            String uploadFilePath = ybFjOcr.getFilePath();
            String inputPath = uploadpath + "/" + uploadFilePath;


            //获取文件名
            String fileOriginName ="";
            try {
                String name = new File(inputPath).getName();
                String exName = name.substring(name.lastIndexOf("."));
                if(name.contains("_")){
                    fileOriginName = name.substring(0, name.lastIndexOf("_"));
                    String originName = fileOriginName+exName;
                    ybFjOcr.setFileName(originName);
                    //修改源图片名(防止中文名)
                    File file = new File(inputPath);
                    String newFilePath = inputPath.substring(0, inputPath.lastIndexOf("/")) + "/" + ybFjOcr.getId()+exName;
                    file.renameTo(new File(newFilePath)); //改名
                    ybFjOcr.setFilePath(newFilePath.replace(uploadpath,""));
                    inputPath = newFilePath;
                }else{
                    String fileName = ybFjOcr.getFileName();
                    if(StrUtil.isNotEmpty(fileName)){
                        fileOriginName = fileName.substring(0,fileName.lastIndexOf("."));
                    }else{
                        fileOriginName = name.substring(0, name.lastIndexOf("."));
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("源图片不存在");
            }




            //生成的结果文件存放的文件夹路径
            String outputPath = uploadpath + "/ocr_result/"+ybFjOcr.getId();


            //初始化存放的目录文件夹
            deleteDir(outputPath);

            //调用python
            String cmd = "/home/web/python_src/ocr.sh %s %s %s %s";
            cmd = String.format(cmd, exportType, inputPath,outputPath,ybFjOcr.getId());
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
                String exportName =fileOriginName + linsStr.replace(outputPath+"/"+ybFjOcr.getId(), "");
                String filePath = linsStr.replace(uploadpath,"");
                ybFjOcr.setExportPath(filePath);
                ybFjOcr.setExportName(exportName);
            }

        }catch (Exception e){
            ybFjOcr.setTaskStatus("3");
            ybFjOcr.setTaskDesc(e.getMessage());
        }finally {
            this.updateById(ybFjOcr);
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


}
