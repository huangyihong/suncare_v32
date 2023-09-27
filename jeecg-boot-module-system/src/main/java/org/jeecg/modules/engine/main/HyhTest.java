package org.jeecg.modules.engine.main;

import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.entity.YbFjUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class HyhTest {
    public static void main(String[] args) {
        String uploadpath = "/home/web/suncare_v4/upload";
        String uploadFilePath = "fjTemplate/20230210/通用数据导入模板-测试数据_1675999200714.xlsx";
        String uploadId="a0ec4e36b37f417bb81ebb4ee3a3bc9c";

        List<YbFjUploadResult> uploadResultList = new ArrayList<YbFjUploadResult>();
        String[] outputPathArr = new String[]{"","fjTemplate/20230210/行政处罚决定书_004_1675998626329.docx",
                "fjTemplate/20230210/当场行政处罚决定书_005_1675998659873.docx",
                "fjTemplate/20230210/行政处罚事先告知书_002_1675998582114.docx",
                "fjTemplate/20230210/行政处罚案卷_006_1675998672007.docx",
                ""};
        String[] codeArr = new String[]{"003","004","005","002","006","001"};
        for(int i=0;i<outputPathArr.length;i++){
            YbFjUploadResult resultBean = new YbFjUploadResult();
            resultBean.setId(IdUtils.uuid());
            resultBean.setUploadId(uploadId);
           /* resultBean.setTemplateImportId(templateImportId);
            resultBean.setTemplateCode(exportBean.getTemplateCode());*/
            //参数1 需要生成文档的类型编码
            resultBean.setExportType(codeArr[i]);
            //参数4 生成的结果文件存放的文件夹路径（全路径）
            resultBean.setFilePath(outputPathArr[i]);
            //参数5 生成结果文件的文件名（不含扩展名）
           /* resultBean.setFileName(resultFileName);
            resultBean.setFileFullpath(linsStr);*/
            resultBean.setTaskStatus("4");
            uploadResultList.add(resultBean);
        }


        for(YbFjUploadResult resultBean:uploadResultList){
            //生成文档的类型编码
            String targetType=resultBean.getExportType();

            //导入文件的文件名（全路径）
            String inputPath=uploadpath+"/"+uploadFilePath;

            //生成结果文件的模板格式文件（全路径）
            String templatePath= StringUtils.isNotBlank(resultBean.getFilePath())?uploadpath+"/"+resultBean.getFilePath():null;

            //生成的结果文件存放的文件夹路径（全路径）
            String outputPath=uploadpath+"/doc_result/"+uploadId+"/"+targetType;

            System.out.println(outputPath);
            deleteDir(outputPath);




            //生成结果文件的文件名（不含扩展名）
            String filename=targetType;

            String cmd = "/home/web/python_src/gendoc.sh %s %s %s %s %s";
            cmd = String.format(cmd, targetType,inputPath, templatePath,outputPath,filename);
            log.info("cmd:{}", cmd);
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
                process.waitFor();
            } catch (Exception e) {
                log.error("", e);
            }


            //读取txt文件
            String fileName = outputPath+"/result.txt";
            //读取文件
            List<String> lineLists = null;
            try {
                lineLists = Files
                        .lines(Paths.get(fileName), Charset.defaultCharset())
                        .flatMap(line -> Arrays.stream(line.split("\n")))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(String linsStr:lineLists){
                System.out.println(linsStr);
            }
        }
    }

    public static boolean deleteDir(String path){
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
            return false;
        }
        return true;
    }
}
