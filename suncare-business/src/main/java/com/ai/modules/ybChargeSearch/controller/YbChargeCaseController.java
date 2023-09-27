package com.ai.modules.ybChargeSearch.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeitemChecklist;
import com.ai.modules.ybChargeSearch.service.IYbChargeitemChecklistService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeCase;
import com.ai.modules.ybChargeSearch.service.IYbChargeCaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 违规案例库
 * @Author: jeecg-boot
 * @Date: 2023-01-13
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "违规案例库")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeCase")
public class YbChargeCaseController extends JeecgController<YbChargeCase, IYbChargeCaseService> {
    @Autowired
    private IYbChargeCaseService ybChargeCaseService;
	@Value(value = "${jeecg.path.upload}")
	private String uploadPath;
    @Autowired
    private IYbChargeitemChecklistService ybChargeitemChecklistService;
    @Autowired
    private ISysBaseAPI sysBaseAPI;

    /**
     * 分页列表查询
     *
     * @param ybChargeCase
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "违规案例库-分页列表查询", notes = "违规案例库-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YbChargeCase ybChargeCase,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        String itemname = ybChargeCase.getItemname();
        QueryWrapper<YbChargeCase> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotEmpty(itemname) && itemname.contains("#")){
            itemname=itemname.trim();
            ybChargeCase.setItemname("");
            queryWrapper = QueryGenerator.initQueryWrapper(ybChargeCase, req.getParameterMap());
            List<String> itemNames = Arrays.asList(itemname.split("#"));
            queryWrapper.in("itemname",itemNames);
        }else if(StrUtil.isNotEmpty(itemname)){
            ybChargeCase.setItemname("");
            queryWrapper = QueryGenerator.initQueryWrapper(ybChargeCase, req.getParameterMap());
            queryWrapper.like("itemname",itemname);
        }else{
            queryWrapper = QueryGenerator.initQueryWrapper(ybChargeCase, req.getParameterMap());
        }

        Page<YbChargeCase> page = new Page<YbChargeCase>(pageNo, pageSize);
        IPage<YbChargeCase> pageList = ybChargeCaseService.page(page, queryWrapper);

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        String realname = loginUser.getRealname();

        //是否管理员角色
        List<String> roleByUserName = ybChargeitemChecklistService.getRoleByUserName(loginUser.getUsername());
        boolean role = roleByUserName.contains("devAdmin");
        if(role){
            pageList.getRecords().stream().forEach(t -> {
                t.setIsOperation(true);
            });
        }else{
            pageList.getRecords().stream().forEach(t -> {
                String sorter = t.getSorter();
                if (sorter.equals(realname)) {
                    t.setIsOperation(true);
                } else {
                    t.setIsOperation(false);
                }
            });
        }
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param ybChargeCase
     * @return
     */
    @AutoLog(value = "诊疗项目案例库维护-添加")
    @ApiOperation(value = "违规案例库-添加", notes = "违规案例库-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbChargeCase ybChargeCase) {
        ybChargeCaseService.save(ybChargeCase);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param ybChargeCase
     * @return
     */
    @AutoLog(value = "诊疗项目案例库维护-修改")
    @ApiOperation(value = "违规案例库-编辑", notes = "违规案例库-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbChargeCase ybChargeCase) throws Exception {
        sysBaseAPI.addLog("诊疗项目案例库维护修改，id： " +ybChargeCase.getId() , CommonConstant.LOG_TYPE_2, 2);
        YbChargeCase query = ybChargeCaseService.getById(ybChargeCase.getId());
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> btnPermission = ybChargeitemChecklistService.getUserBtnPermission(loginUser.getUsername());
        String sorter = query.getSorter();
        String realname = loginUser.getRealname();
        if(!sorter.equals(realname) && !btnPermission.contains("案例库维护审核权限")){
            throw new Exception("无法修改其他整理人的数据!");
        }

        ybChargeCaseService.updateById(ybChargeCase);
        return Result.ok("编辑成功!");
    }

//    /**
//     * 通过id删除
//     *
//     * @param id
//     * @return
//     */
//    @AutoLog(value = "违规案例库-通过id删除")
//    @ApiOperation(value = "违规案例库-通过id删除", notes = "违规案例库-通过id删除")
//    @DeleteMapping(value = "/delete")
//    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
//        ybChargeCaseService.removeById(id);
//        return Result.ok("删除成功!");
//    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "诊疗项目案例库维护-批量删除")
    @ApiOperation(value = "违规案例库-批量删除", notes = "违规案例库-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) throws Exception {
        sysBaseAPI.addLog("诊疗项目案例库维护批量删除，ids： " +ids, CommonConstant.LOG_TYPE_2, 3);
        Collection<YbChargeCase> collections = ybChargeCaseService.listByIds(Arrays.asList(ids.split(",")));
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> btnPermission = ybChargeitemChecklistService.getUserBtnPermission(loginUser.getUsername());

        for(YbChargeCase query:collections){
            String sorter = query.getSorter();
            String realname = loginUser.getRealname();
            if(!sorter.equals(realname) && !btnPermission.contains("案例库维护审核权限")){
                throw new Exception("无法删除其他整理人的数据!");
            }
        }
        this.ybChargeCaseService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "违规案例库-通过id查询", notes = "违规案例库-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YbChargeCase ybChargeCase = ybChargeCaseService.getById(id);
        return Result.ok(ybChargeCase);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ybChargeCase
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YbChargeCase ybChargeCase) {
        return super.exportXls(request, ybChargeCase, YbChargeCase.class, "违规案例库");
    }


    /**
     * 批量审核
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "诊疗项目案例库维护-批量审核")
    @ApiOperation(value = "收费明细风控检查内容-批量审核", notes = "收费明细风控检查内容-批量审核")
    @GetMapping(value = "/batchExamine")
    public Result<?> batchExamine(@RequestParam(name = "ids", required = true) String ids) {
        sysBaseAPI.addLog("诊疗项目案例库维护批量审核，ids： " +ids , CommonConstant.LOG_TYPE_2, 2);
        List<String> strings = Arrays.asList(ids.split(","));
        ArrayList<YbChargeCase> list = new ArrayList<>();
        strings.stream().forEach(t -> {
			YbChargeCase yb = new YbChargeCase();
            yb.setId(t);
            yb.setExamineStatus("1");
            list.add(yb);
        });
		ybChargeCaseService.updateBatchById(list);

        return Result.ok("批量审核成功！");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.GET)
    @AutoLog(value = "诊疗项目案例库维护-excel导入数据")
    public Result<?> importExcel(@RequestParam(name = "filePath", required = true) String filePath,
                                 HttpServletRequest request) throws Exception {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String realname = loginUser.getRealname();
        File importFile = new File(uploadPath + "/" + filePath);
        if (!importFile.exists()) {
            throw new Exception("文件不存在");
        }
        sysBaseAPI.addLog("诊疗项目案例库维护导入，url： " +importFile, CommonConstant.LOG_TYPE_2, 1);


        // 获取上传文件对象
        FileInputStream fileInputStream = new FileInputStream(importFile);

        //导入校验
        ImportParams params = new ImportParams();
        params.setHeadRows(1);
        params.setNeedSave(true);
        //系统用户
        List<String> userRealNameList = ybChargeitemChecklistService.getUserRealName();
        try {
            //解析数据
            List<YbChargeCase> importList = ExcelImportUtil.importExcel(fileInputStream, YbChargeCase.class, params);
            if(importList.size()>2000){
                return Result.error("一次性导入数据最多2000条!");
            }

            //重复的数据
            List<YbChargeCase> repeatList = new ArrayList<>();
            //入库数据
            List<YbChargeCase> loadList = new ArrayList<>();

            StringBuffer sqlBuffer = new StringBuffer();

            int errNum = 0;
            String errMsg = "";
            SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < importList.size(); i++) {
                YbChargeCase bean = importList.get(i);
                String sorter = bean.getSorter();

                if(StrUtil.isEmpty(sorter)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:整理人不能为空!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                    continue;
                }

                if(!userRealNameList.contains(sorter)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:整理人在系统中不存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                    continue;
                }
//                if(!sorter.equals(realname)){
//                    errNum++;
//                    errMsg += "第" + (i + 1) + "行:整理人不是本账号!";
//                    if (errNum >= 5) {
//                        return Result.error("文件导入失败:" + errMsg);
//                    }
//                    continue;
//                }


                if(StringUtils.isNotBlank(bean.getStartEndDateStr())){
                    if(bean.getStartEndDateStr().split("到").length!=2){
                        errNum++;
                        errMsg += "第" + (i + 1) + "行:所属时间格式无法识别，正确格式为：yyyy-MM-ddd到yyyy-MM-dd!";
                        if (errNum >= 5) {
                            return Result.error("文件导入失败:" + errMsg);
                        }
                        continue;
                    }
                }

                if(StringUtils.isNotBlank(bean.getStartEndDateStr())){
                    String[] startAndEndTime = bean.getStartEndDateStr().split("到");
                    try {
                        bean.setStartdate(DateUtils.str2Date(startAndEndTime[0],date_sdf));
                        bean.setEnddate(DateUtils.str2Date(startAndEndTime[1],date_sdf));
                    }catch (Exception e) {
                        errNum++;
                        errMsg += "第" + (i + 1) + "行:所属时间格式无法识别，正确格式为：yyyy-MM-ddd到yyyy-MM-dd!";
                        if (errNum >= 5) {
                            return Result.error("文件导入失败:" + errMsg);
                        }
                        continue;
                    }
                }

                bean.setCreatedBy(loginUser.getUsername());
                bean.setCreatedByName(loginUser.getRealname());
                bean.setCreatedTime(new Date());
                bean.setExamineStatus("0");


                //重复SQL
                String wgType = bean.getWgType();
                String itemCode = bean.getItemCode();
                String wgItemName = bean.getWgItemName();

                sqlBuffer.append(" (wg_type = '"+wgType+"'");
                sqlBuffer.append(" and ");
                sqlBuffer.append(" item_code = '"+itemCode+"'");
                sqlBuffer.append(" and ");
                sqlBuffer.append(" wg_item_name = '"+wgItemName+"' )");
                if(i !=(importList.size()-1)){
                    sqlBuffer.append(" or ");
                }
            }


            if (errNum > 0) {
                return Result.error("文件导入失败:" + errMsg);
            }

            List<YbChargeCase> list= ybChargeCaseService.selectByStr(sqlBuffer.toString());
            if(list.size()>0){
                for(YbChargeCase t:importList){
                    if(!list.contains(t)){
                        loadList.add(t);
                    }else{
                        repeatList.add(t);
                    }
                }
            }else{
                loadList=importList;
            }

            ybChargeCaseService.saveBatch(loadList);

            if(repeatList.size()>0){
                return Result.error("文件导入部分失败:有重复的数据",repeatList);
            }


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



    /**
     * 手动批量导入
     *
     * @return
     */
    @AutoLog(value = "诊疗项目案例库维护-手动批量导入")
    @ApiOperation(value = "违规案例库-手动批量导入", notes = "违规案例库-手动批量导入")
    @PostMapping(value = "/saveBatch")
    public Result<?> saveBatch(@RequestBody List<YbChargeCase> data) {
        sysBaseAPI.addLog("诊疗项目案例库维护手动批量导入" , CommonConstant.LOG_TYPE_2, 1);
        data.stream().forEach(t -> {
            t.setId(IdUtils.uuid());
        });
        ybChargeCaseService.saveBatch(data);
        return Result.ok("批量导入成功！");
    }

}
