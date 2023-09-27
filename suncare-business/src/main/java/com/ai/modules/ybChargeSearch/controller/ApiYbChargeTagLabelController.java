package com.ai.modules.ybChargeSearch.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.ExcelXUtils;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.service.IYbChargeTagLabelService;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import io.swagger.annotations.ApiOperation;
import jxl.Workbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/apiYbChargeTagLabel/ybChargeTagLabel")
public class ApiYbChargeTagLabelController {

    @Autowired
    private IYbChargeTagLabelService ybChargeTagLabelService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;








    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(@RequestParam("file")MultipartFile file,MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
        String taskType = request.getParameter("taskType");
        // 判断文件名是否为空
        if (file == null) {
            return Result.error("上传文件为空");
        }
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (name == null || ("").equals(name) && size == 0) {
            return Result.error("上传文件内容为空");
        }

        List<List<String>> list = new ArrayList<>();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(0, 0, file.getInputStream());
        }

        //获取项目地数据源
        DatasourceAndDatabaseVO dbVO = getDatasourceAndDatabaseVO();

        if(StringUtils.isBlank(taskType)){
            Result.error("参数异常");
        }

        //格式校验 非空校验
        List<Map<String,Object>> dataList = ybChargeTagLabelService.checkImportExcel(list,taskType,true);
        if(dataList.size()==0){
            return Result.error("数据为空");
        }

        //判断是否存在已标注数据
        int existCount = ybChargeTagLabelService.getExistCount(dataList,dbVO,taskType);

        if(existCount>0){
            String bizPath = request.getParameter("bizPath");
            String dbpath = CommonUtil.upload(file,bizPath);
            return Result.error("已存在数据条数:"+existCount+"条",dbpath);
        }

        //导入数据
        ybChargeTagLabelService.insertImportData(dataList,dbVO,taskType);

        return Result.ok();
    }



    /**
     * 通过filePath
     *
     * @param filePath
     * @return
     */
    @AutoLog(value = "覆盖导入")
    @ApiOperation(value = "覆盖导入", notes = "覆盖导入")
    @GetMapping(value = "/importForUpdate")
    public Result<?> importForUpdate(@RequestParam(name = "filePath", required = true) String filePath,String taskType) throws Exception {
        //  获取文件
        File file = new File(uploadpath+"/"+filePath);
        String name = file.getName();
        List<List<String>> list = new ArrayList<>();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(0, 0, new FileInputStream(file));
        } else {
            list = ExcelUtils.readSheet(0, 0, new FileInputStream(file));
        }

        //获取项目地数据源
        DatasourceAndDatabaseVO dbVO = getDatasourceAndDatabaseVO();

        if(StringUtils.isBlank(taskType)){
            Result.error("参数异常");
        }

        List<Map<String,Object>> dataList = ybChargeTagLabelService.checkImportExcel(list,taskType,false);
        if(dataList.size()==0){
            return Result.error("数据为空");
        }

        //删除已存在数据 并导入数据
        ybChargeTagLabelService.deleteAndInsertImportData(dataList,dbVO,taskType);

        //删除文件
        file.delete();

        return Result.ok();
    }

    private DatasourceAndDatabaseVO getDatasourceAndDatabaseVO() {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = user.getDataSource();
        Map<String, String> map = new HashMap<>();
        map.put("dataSource", dataSource);
        DatasourceAndDatabaseVO dbVO = ApiTokenUtil.getObj("/apiYbChargeSearch/ybChargeSearchTask/getDatasourceAndDatabase", map, DatasourceAndDatabaseVO.class);
        if (dbVO == null) {
            Result.error("获取项目地数据源信息失败");
        }
        if(dbVO.getSysDatasource()==null){
            Result.error("获取项目地信息失败");
        }
        if(dbVO.getSysDatabase()==null){
            Result.error("获取项目地关联数据源信息失败");
        }
        return dbVO;
    }


    /**
     * 判断dwb_visitid_tag是否存在该违规标签数据
     *
     * @param
     * @return
     */
    @AutoLog(value = "是否存在该违规标签数据")
    @ApiOperation(value="是否存在该违规标签数据", notes="是否存在该违规标签数据")
    @GetMapping(value = "/dwbVisitTagCountByTagname")
    public Result<?> visitTagCount(@RequestParam(name="taskId",required=true) String taskId,@RequestParam(name="tagId",required=true) String tagId,@RequestParam(name="tagName",required=true) String tagName) throws Exception{
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, String> map = new HashMap<>();
        map.put("id", taskId);
        YbChargeSearchTask ybChargeSearchTask = ApiTokenUtil.getObj("/ybChargeSearch/ybChargeSearchTask/queryById", map, YbChargeSearchTask.class);
        if(ybChargeSearchTask==null){
            return Result.error("参数异常");
        }

        //获取项目地数据源
        DatasourceAndDatabaseVO dbVO = getDatasourceAndDatabaseVO();

        int existCount = ybChargeTagLabelService.dwbVisitTagCountByTagname(dbVO,ybChargeSearchTask,tagName,tagId);
        return Result.ok(existCount);
    }




    /**
     * 通过filePath
     *
     * @param taskId
     * @return
     */
    @AutoLog(value = "覆盖导入违规信息")
    @ApiOperation(value = "覆盖导入违规信息", notes = "覆盖导入违规信息")
    @GetMapping(value = "/dwbVisitTagImportForUpdate")
    public Result<?> dwbVisitTagImportForUpdate(@RequestParam(name="taskId",required=true) String taskId,
                                                @RequestParam(name="tagId",required=true) String tagId,
                                                @RequestParam(name="tagName",required=true) String tagName,
                                                @RequestParam(name="updateFlag",required=true) boolean updateFlag) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("id", taskId);
        YbChargeSearchTask ybChargeSearchTask = ApiTokenUtil.getObj("/ybChargeSearch/ybChargeSearchTask/queryById", map, YbChargeSearchTask.class);
        if(ybChargeSearchTask==null){
            return Result.error("参数异常");
        }

        //  获取文件
        File outFile = new File(ybChargeSearchTask.getFileFullpath());
        if (!outFile.exists()) {
            return Result.error("任务结果文件不存在");
        }



        String name = outFile.getName();
        List<List<String>> list = new ArrayList<>();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            //获取sheet页
            int sheetNum = new XSSFWorkbook(new FileInputStream(outFile)).getNumberOfSheets();
            if(sheetNum>1){
                return Result.error("任务结果文件有多个sheet页,无法导入");
            }
            list = ExcelXUtils.readSheet(0, 0, new FileInputStream(outFile));
        } else {
            int sheetNum = Workbook.getWorkbook(new FileInputStream(outFile)).getNumberOfSheets();
            if(sheetNum>1){
                return Result.error("任务结果文件有多个sheet页,无法导入");
            }
            list = ExcelUtils.readSheet(0, 0, new FileInputStream(outFile));
        }

        List<Map<String,Object>> dataList = ybChargeTagLabelService.getTaskFileData(list,ybChargeSearchTask.getTaskType());
        if(dataList.size()==0){
            return Result.error("数据为空");
        }

        //获取项目地数据源
        DatasourceAndDatabaseVO dbVO = getDatasourceAndDatabaseVO();

        if(updateFlag){
            //删除已存在的数据
            ybChargeTagLabelService.deleteExistDwbVisitTagAndImport(dataList,dbVO,ybChargeSearchTask,tagName,tagId);
        }else{
            //导入数据
            ybChargeTagLabelService.dwbVisitTagImport(dataList,dbVO,ybChargeSearchTask,tagName,tagId);

        }
        return Result.ok();
    }




}
