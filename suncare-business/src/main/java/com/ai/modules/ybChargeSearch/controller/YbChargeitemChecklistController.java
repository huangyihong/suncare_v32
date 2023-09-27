package com.ai.modules.ybChargeSearch.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.entity.YbChargeitemChecklist;
import com.ai.modules.ybChargeSearch.service.IYbChargeitemChecklistService;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchTaskFunCountVo;
import com.ai.modules.ybChargeSearch.vo.YbChargeitemChecklistVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.ai.modules.ybChargeSearch.controller.YbChargeSearchTaskController.initTitleStyle;

/**
 * @Description: 收费明细风控检查内容
 * @Author: jeecg-boot
 * @Date: 2022-11-25
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "收费明细风控检查内容")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeitemChecklist")
public class YbChargeitemChecklistController extends JeecgController<YbChargeitemChecklist, IYbChargeitemChecklistService> {
    @Autowired
    private IYbChargeitemChecklistService ybChargeitemChecklistService;
    @Autowired
    IMedicalImportTaskService importTaskService;
    @Autowired
    private IMedicalDictService medicalDictService;
    @Value(value = "${jeecg.path.upload}")
    private String uploadPath;
    @Autowired
    private ISysBaseAPI sysBaseAPI;
//    @Autowired
//    private ISysUserService sysUserService;



    /**
     * 分页列表查询
     *
     * @param ybChargeitemChecklist
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-分页列表查询")
    @ApiOperation(value = "收费明细风控检查内容-分页列表查询", notes = "收费明细风控检查内容-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YbChargeitemChecklist ybChargeitemChecklist,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<YbChargeitemChecklist> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeitemChecklist, req.getParameterMap());
        Page<YbChargeitemChecklist> page = new Page<YbChargeitemChecklist>(pageNo, pageSize);
//        IPage<YbChargeitemChecklist> pageList = ybChargeitemChecklistService.page(page, queryWrapper);
        IPage<YbChargeitemChecklist> pageList=ybChargeitemChecklistService.getPage(page,queryWrapper);

//        String realname = loginUser.getRealname();
//        //是否管理员角色
//        List<String> roleByUserName = ybChargeitemChecklistService.getRoleByUserName(loginUser.getUsername());
//        boolean role = roleByUserName.contains("devAdmin");
//        if(role){
//            pageList.getRecords().stream().forEach(t -> {
//               t.setIsOperation(true);
//            });
//        }else{
//            pageList.getRecords().stream().forEach(t -> {
//                String sorter = t.getSorter();
//                if (sorter.equals(realname)) {
//                    t.setIsOperation(true);
//                } else {
//                    t.setIsOperation(false);
//                }
//            });
//        }


        return Result.ok(pageList);
    }


    /**
     * 从关键字库导入查询项目列表
     *
     * @param ybChargeitemChecklistVo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-从关键字库导入查询项目列表")
    @ApiOperation(value = "收费明细风控检查内容-从关键字库导入查询项目列表", notes = "收费明细风控检查内容-从关键字库导入查询项目列表")
    @GetMapping(value = "/keyWordsImportList")
    public Result<?> keyWordsImportList(YbChargeitemChecklistVo ybChargeitemChecklistVo,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) throws Exception {
        Page<YbChargeitemChecklistVo> page = new Page<YbChargeitemChecklistVo>(pageNo, pageSize);
        return Result.ok(ybChargeitemChecklistService.getKeyWordsImportList(ybChargeitemChecklistVo,page));
    }





    /**
     * 添加
     *
     * @param ybChargeitemChecklist
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-添加")
    @ApiOperation(value = "收费明细风控检查内容-添加", notes = "收费明细风控检查内容-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbChargeitemChecklist ybChargeitemChecklist) {
        ybChargeitemChecklistService.save(ybChargeitemChecklist);
        return Result.ok("添加成功！");
    }


    /**
     * 批量添加
     *
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-批量添加")
    @ApiOperation(value = "收费明细风控检查内容-批量添加", notes = "收费明细风控检查内容-批量添加")
    @PostMapping(value = "/saveBatch")
    public Result<?> saveBatch(@RequestBody List<YbChargeitemChecklist> list) throws Exception{
        ybChargeitemChecklistService.saveBatch(list);
        return Result.ok("批量添加成功！");
    }

    /**
     * 编辑
     *
     * @param ybChargeitemChecklist
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-编辑")
    @ApiOperation(value = "收费明细风控检查内容-编辑", notes = "收费明细风控检查内容-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbChargeitemChecklist ybChargeitemChecklist) throws Exception {
        sysBaseAPI.addLog("诊疗项目关键字维护修改，id： " +ybChargeitemChecklist.getId() , CommonConstant.LOG_TYPE_2, 2);
        ybChargeitemChecklistService.updateById(ybChargeitemChecklist);
        return Result.ok("编辑成功!");
    }

//    /**
//     * 通过id删除
//     *
//     * @param id
//     * @return
//     */
//    @AutoLog(value = "收费明细风控检查内容-通过id删除")
//    @ApiOperation(value = "收费明细风控检查内容-通过id删除", notes = "收费明细风控检查内容-通过id删除")
//    @DeleteMapping(value = "/delete")
//    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
//        ybChargeitemChecklistService.removeById(id);
//        return Result.ok("删除成功!");
//    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-批量删除")
    @ApiOperation(value = "收费明细风控检查内容-批量删除", notes = "收费明细风控检查内容-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        sysBaseAPI.addLog("诊疗项目关键字维护批量删除，ids： " +ids, CommonConstant.LOG_TYPE_2, 3);
        this.ybChargeitemChecklistService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }


    /**
     * 批量审核
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-批量审核")
    @ApiOperation(value = "收费明细风控检查内容-批量审核", notes = "收费明细风控检查内容-批量审核")
    @GetMapping(value = "/batchExamine")
    public Result<?> batchExamine(@RequestParam(name = "ids", required = true) String ids,
    @RequestParam(name = "examineStatus", required = true) String examineStatus) {
        sysBaseAPI.addLog("诊疗项目关键字维护批量审核，ids： " +ids, CommonConstant.LOG_TYPE_2, 2);
        List<String> strings = Arrays.asList(ids.split(","));
        ArrayList<YbChargeitemChecklist> list = new ArrayList<>();
        strings.stream().forEach(t -> {
            YbChargeitemChecklist yb = new YbChargeitemChecklist();
            yb.setId(t);
            yb.setExamineStatus(examineStatus);
            list.add(yb);
        });
        ybChargeitemChecklistService.updateBatchById(list);

        return Result.ok("批量审核成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "收费明细风控检查内容-通过id查询")
    @ApiOperation(value = "收费明细风控检查内容-通过id查询", notes = "收费明细风控检查内容-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YbChargeitemChecklist ybChargeitemChecklist = ybChargeitemChecklistService.getById(id);
        return Result.ok(ybChargeitemChecklist);
    }

//    /**
//     * 导出excel
//     *
//     * @param request
//     * @param ybChargeitemChecklist
//     */
//    @RequestMapping(value = "/exportXls")
//    public ModelAndView exportXls(HttpServletRequest request, YbChargeitemChecklist ybChargeitemChecklist) {
//        return super.exportXls(request, ybChargeitemChecklist, YbChargeitemChecklist.class, "检索关键字");
//    }


    /**
     * 导出excel
     *
     * @param ybChargeitemChecklist
     */
    @RequestMapping(value = "/exportXls")
    public void exportXls(HttpServletRequest req, HttpServletResponse response, YbChargeitemChecklist ybChargeitemChecklist) throws Exception {
        OutputStream os = response.getOutputStream();
        String title = "检索关键字" + System.currentTimeMillis();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

        String[] titleArr = {"收费项目A编码","收费项目A名称", "收费项目A关键字", "收费项目B编码", "收费项目B名称", "收费项目B关键字", "收费项目名称类型","重复收费类型","收费项目B违规判断","超量检查的类型","超量的数值(不含)","是否输出同一天的手术项目","违规案例提示","整理人"};
        String[] fieldArr = {"itemCode1", "packageItem1", "itemname", "itemCode2", "packageItem2", "itemname1", "itemType","item1Type","item1Wgtype","qtyType","qtyNum","isSameDay","wgCaseExample","sorter"};


        //导出数据
        QueryWrapper<YbChargeitemChecklist> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeitemChecklist, req.getParameterMap());
        List<YbChargeitemChecklist> list = ybChargeitemChecklistService.list(queryWrapper);


        // 生成一个表格
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("检索关键字");

        int startHang = 0;

        // 设置标题样式
        CellStyle titleStyle = initTitleStyle(workbook);

        Row rowTitle = sheet.createRow(startHang);
        rowTitle.setHeight((short) 500);
        //填充表头
        for (int i = 0, len = titleArr.length; i < len; i++) {
            String t = titleArr[i];
            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(t);
            cell.setCellStyle(titleStyle);
            sheet.setColumnWidth(i, 15 * 256);
        }

        startHang++;

        //填充值
        if (list.size() > 0) {
            int celNum = 0;
            for (YbChargeitemChecklist word : list) {
                Class<? extends YbChargeitemChecklist> aClass = word.getClass();
                Row row = sheet.createRow(startHang++);
                for (String field : fieldArr) {
                    Cell cell = row.createCell(celNum++);
                    Field declaredField = aClass.getDeclaredField(field);
                    declaredField.setAccessible(true);
                    MedicalDict annotation = declaredField.getAnnotation(MedicalDict.class);
                    Object o = declaredField.get(word);
                    if(annotation !=null && ObjectUtil.isNotEmpty(o)){
                        String code = declaredField.getAnnotation(MedicalDict.class).dicCode();
                        o = medicalDictService.queryDictTextByKey(code, o.toString().trim());
                    }

                    if (ObjectUtil.isEmpty(o)) {
                        o = "";
                    }
                    cell.setCellValue(String.valueOf(o));

                }
                celNum = 0;
            }
        }


        workbook.write(os);
        workbook.dispose();


    }





    /**
     * 通过excel导入数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.GET)
    public Result<?> importExcel(@RequestParam(name = "filePath", required = true) String filePath,
                                 HttpServletRequest request) throws Exception {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        File importFile = new File(uploadPath + "/" + filePath);
        if (!importFile.exists()) {
            throw new Exception("文件不存在");
        }
        sysBaseAPI.addLog("诊疗项目关键字维护导入，url： " +importFile, CommonConstant.LOG_TYPE_2, 1);
        // 获取上传文件对象
        FileInputStream fileInputStream = new FileInputStream(importFile);

        //字典数据
        Map<String, String> item1TypeDict = medicalDictService.queryNameMapByType("ITEM1_TYPE");
        Map<String, String> item1WgtypeDict = medicalDictService.queryNameMapByType("ITEM1_WGTYPE");
        Map<String, String> qtyTypeDict = medicalDictService.queryNameMapByType("QTY_TYPE");
        //导入校验
        ImportParams params = new ImportParams();
        params.setHeadRows(1);
        params.setNeedSave(true);
        try {
            //解析数据
            List<YbChargeitemChecklist> importList = ExcelImportUtil.importExcel(fileInputStream, YbChargeitemChecklist.class, params);
            int errNum = 0;
            String errMsg = "";
            for (int i = 0; i < importList.size(); i++) {
                YbChargeitemChecklist bean = importList.get(i);
                bean.setCreatedBy(loginUser.getUsername());
                bean.setCreatedByName(loginUser.getRealname());
                bean.setCreatedTime(new Date());

                //收费项目名称类型
                String itemType = bean.getItemType();
                bean.setItemType("yb");
                if (StrUtil.isNotEmpty(itemType)) {
                    if (itemType.equals("HIS收费项目名称")) {
                        bean.setItemType("his");
                    }
                }

                //重复收费类型
                String item1Type = item1TypeDict.get(bean.getItem1Type());
                item1Type = StringUtils.isNotBlank(item1Type) ? item1Type : "oneday";//默认同一天
                bean.setItem1Type(item1Type);

                //收费项目B违规判断
                String item1Wgtype = item1WgtypeDict.get(bean.getItem1Wgtype());
                item1Wgtype = StringUtils.isNotBlank(item1Wgtype) ? item1Wgtype : "haveB";//默认B项目存在违规(重复收费)
                bean.setItem1Wgtype(item1Wgtype);

                //超量检查的类型
                String qtyType = qtyTypeDict.get(bean.getQtyType());
//                qtyType = StringUtils.isNotBlank(qtyType) ? qtyType : "charge_qty";//默认一天超量
                bean.setQtyType(qtyType);

                //是否输出同一天的手术项目（默认否）
                String isSameDay = "是".equals(bean.getIsSameDay()) ? "1" : "0";
                bean.setIsSameDay(isSameDay);

                String itemname = bean.getItemname();

                //itemname1有值,itemname不能为空
                String itemname1 = bean.getItemname1();
                if (StrUtil.isNotEmpty(itemname1) && StrUtil.isEmpty(itemname)) {
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:收费项目名称B关键字有值,收费项目名称A关键字的值不能为空!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //收费项目A名称
                String packageItem1 = bean.getPackageItem1();
                if(StrUtil.hasBlank(packageItem1)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:收费项目A名称必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //收费项目A关键字
                String itemnameWord = bean.getItemname();
                if(StrUtil.hasBlank(itemnameWord)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:收费项目A关键字必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //整理人
                String sorter = bean.getSorter();
                if(StrUtil.hasBlank(sorter)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:整理人必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }


            }
            if (errNum > 0) {
                return Result.error("文件导入失败:" + errMsg);
            }

            ybChargeitemChecklistService.saveBatch(importList);
            return Result.ok("文件导入成功！数据行数:" + importList.size());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("文件导入失败:" + e.getMessage());
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
