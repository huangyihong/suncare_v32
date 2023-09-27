package com.ai.modules.ybFj.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.ybChargeSearch.entity.YbChargeitemChecklist;
import com.ai.modules.ybFj.dto.YbFjOrgDto;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjOrg;
import com.ai.modules.ybFj.service.IYbFjOrgService;
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
 * @Description: 飞检医疗机构信息
 * @Author: jeecg-boot
 * @Date: 2023-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "飞检医疗机构信息")
@RestController
@RequestMapping("/fj/org")
public class YbFjOrgController extends JeecgController<YbFjOrg, IYbFjOrgService> {
    @Autowired
    private IYbFjOrgService ybFjOrgService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadPath;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IMedicalDictService medicalDictService;



    /**
     * 分页列表查询
     *
     * @param ybFjOrg
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-分页列表查询")
    @ApiOperation(value = "飞检医疗机构信息-分页列表查询", notes = "飞检医疗机构信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<YbFjOrg>> queryPageList(YbFjOrg ybFjOrg,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                HttpServletRequest req) {
        QueryWrapper<YbFjOrg> queryWrapper = QueryGenerator.initQueryWrapper(ybFjOrg, req.getParameterMap());
        //省市县乡镇
        String typeCode = req.getParameter("typeCode");
        if (StringUtils.isNotBlank(typeCode)) {
            queryWrapper.and(wrapper -> wrapper
                    .eq("PROVINCE_CODE", typeCode)
                    .or().eq("CITY_CODE", typeCode)
                    .or().eq("COUNTY_CODE", typeCode)
                    .or().eq("TOWN_CODE", typeCode)
                    .or().eq("VILLAGE_CODE", typeCode));
        }
        Page<YbFjOrg> page = new Page<YbFjOrg>(pageNo, pageSize);
        IPage<YbFjOrg> pageList = ybFjOrgService.page(page, queryWrapper);
        Result<IPage<YbFjOrg>> result = new Result<IPage<YbFjOrg>>();
        result.setResult(pageList);
        return result;
    }

    /**
     * 添加
     *
     * @param dto
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-添加")
    @ApiOperation(value = "飞检医疗机构信息-添加", notes = "飞检医疗机构信息-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbFjOrgDto dto) throws Exception {
        ybFjOrgService.saveOrg(dto);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param dto
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-编辑")
    @ApiOperation(value = "飞检医疗机构信息-编辑", notes = "飞检医疗机构信息-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbFjOrgDto dto) throws Exception {
        ybFjOrgService.updateOrg(dto);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-通过id删除")
    @ApiOperation(value = "飞检医疗机构信息-通过id删除", notes = "飞检医疗机构信息-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        ybFjOrgService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-批量删除")
    @ApiOperation(value = "飞检医疗机构信息-批量删除", notes = "飞检医疗机构信息-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.ybFjOrgService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-通过id查询")
    @ApiOperation(value = "飞检医疗机构信息-通过id查询", notes = "飞检医疗机构信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<YbFjOrg> queryById(@RequestParam(name = "id", required = true) String id) {
        YbFjOrg ybFjOrg = ybFjOrgService.getById(id);
        Result<YbFjOrg> result = new Result<YbFjOrg>();
        result.setResult(ybFjOrg);
        return result;
    }

    @AutoLog(value = "飞检医疗机构信息-批量审核")
    @ApiOperation(value = "飞检医疗机构信息-批量审核", notes = "飞检医疗机构信息-批量审核")
    @DeleteMapping(value = "/audit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "auditState", value = "审核状态", paramType = "query"),
            @ApiImplicitParam(name = "auditOpinion", value = "审核意见", paramType = "query")
    })
    public Result<?> audit(@RequestParam(name = "ids", required = true) String ids,
                           @RequestParam(name = "auditState", required = true) String auditState,
                           @RequestParam(name = "auditOpinion") String auditOpinion) {
        this.ybFjOrgService.audit(ids, auditState, auditOpinion);
        return Result.ok("批量审核成功！");
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ybFjOrg
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YbFjOrg ybFjOrg) {
        return super.exportXls(request, ybFjOrg, YbFjOrg.class, "飞检医疗机构信息");
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
        // 获取上传文件对象
        FileInputStream fileInputStream = new FileInputStream(importFile);
        //字典数据
        Map<String, String> busstypeMap = medicalDictService.queryNameMapByType("JGFLGLDM");
        Map<String, String> orgtypeMap = medicalDictService.queryNameMapByType("JGLB");
        Map<String, String> ownershipCodeMap = medicalDictService.queryNameMapByType("JJLXBM");
        Map<String, String> hosplevelMap = medicalDictService.queryNameMapByType("YYJB");
        Map<String, String> hospgradeMap = medicalDictService.queryNameMapByType("YYDJ");

        //导入校验
        ImportParams params = new ImportParams();
        params.setHeadRows(1);
        params.setSheetNum(1);
        params.setNeedSave(true);

        try {
            //解析数据
            List<YbFjOrg> importList = ExcelImportUtil.importExcel(fileInputStream, YbFjOrg.class, params);
            importList=importList.stream().filter(item -> !(item.getOrgName() == null
            && item.getBusstype() == null && item.getSocialCode() == null && item.getOrgtype() == null
            && item.getHosplevel() == null && item.getHospgrade() == null && item.getOwnershipCode()== null
            && item.getResponsible() == null && item.getResponsiblePhone() == null && item.getOrgId() == null
            && item.getOrgUsedName() == null && item.getLegalperson() == null && item.getLegalpersonPhone() == null
            && item.getOrgAddress() == null && item.getBedAmount() == null)).collect(Collectors.toList());

            List<String> orgNameList = importList.stream().map(YbFjOrg::getOrgName).collect(Collectors.toList());
            List<YbFjOrg> list = ybFjOrgService.list((QueryWrapper) Wrappers.query().in("org_name", orgNameList));
            List<String> repeatNameList = list.stream().map(YbFjOrg::getOrgName).collect(Collectors.toList());

            int errNum = 0;
            String errMsg = "";

            for (int i = 0; i < importList.size(); i++) {
                YbFjOrg bean = importList.get(i);
                bean.setCreateUser(loginUser.getUsername());
                bean.setCreateUsername(loginUser.getRealname());
                bean.setCreateTime(new Date());



                //医疗机构性质名称
                String busstype = bean.getBusstype();
                String bt = busstypeMap.get(busstype);
                if(StrUtil.isEmpty(bt)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构性质名称的值不存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }else{
                    bean.setBusstype(bt);
                }

                //卫生机构类别
                String orgtype = bean.getOrgtype();
                String ot = orgtypeMap.get(orgtype);
                if(StrUtil.isEmpty(ot)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:卫生机构类别的值不存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }else{
                    bean.setOrgtype(ot);
                }

                //所有制形式
                String ownershipCode = bean.getOwnershipCode();
                String oc = ownershipCodeMap.get(ownershipCode);
                if(StrUtil.isEmpty(oc)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:所有制形式的值不存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }else{
                    bean.setOwnershipCode(oc);
                }

                //医疗机构级别
                String hosplevel = bean.getHosplevel();
                String hl = hosplevelMap.get(hosplevel);
                if(StrUtil.isEmpty(hl)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构级别的值不存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }else{
                    bean.setHosplevel(hl);
                }

                //医疗机构等级
                String hospgrade = bean.getHospgrade();
                String hg = hospgradeMap.get(hospgrade);
                if(StrUtil.isEmpty(hg)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构等级的值不存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }else{
                    bean.setHospgrade(hg);
                }


                //医疗机构名称
                String orgName = bean.getOrgName();
                if(StrUtil.hasBlank(orgName)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构名称必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }
                if(repeatNameList.size()>0 && repeatNameList.contains(orgName)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构名称已存在!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //统一社会信用代码
                String socialCode = bean.getSocialCode();
                if(StrUtil.hasBlank(socialCode)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构名称必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //医保负责人姓名
                String responsible = bean.getResponsible();
                if(StrUtil.hasBlank(responsible)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医保负责人姓名必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //负责人联系方式
                String responsiblePhone = bean.getResponsiblePhone();
                if(StrUtil.hasBlank(responsiblePhone)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:负责人联系方式必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }

                //医疗机构编码
                String orgId = bean.getOrgId();
                if(StrUtil.hasBlank(orgId)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构编码必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }


                //医疗机构详细地址
                String orgAddress = bean.getOrgAddress();
                if(StrUtil.hasBlank(orgAddress)){
                    errNum++;
                    errMsg += "第" + (i + 1) + "行:医疗机构详细地址必填!";
                    if (errNum >= 5) {
                        return Result.error("文件导入失败:" + errMsg);
                    }
                }




            }
            if (errNum > 0) {
                return Result.error("文件导入失败:" + errMsg);
            }



            ybFjOrgService.saveBatch(importList);
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
     * 批量审核
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "医疗机构信息-批量审核")
    @ApiOperation(value = "医疗机构信息-批量审核", notes = "医疗机构信息-批量审核")
    @GetMapping(value = "/batchExamine")
    public Result<?> batchExamine(@RequestParam(name = "ids", required = true) String ids,
                                  @RequestParam(name = "examineStatus", required = true) String examineStatus) {
        List<String> strings = Arrays.asList(ids.split(","));
        ArrayList<YbFjOrg> list = new ArrayList<>();
        strings.stream().forEach(t -> {
            YbFjOrg yb = new YbFjOrg();
            yb.setId(t);
            yb.setAuditState(examineStatus);
            list.add(yb);
        });
        ybFjOrgService.updateBatchById(list);

        return Result.ok("批量审核成功！");
    }

    /**

     * @return
     */
    @AutoLog(value = "医疗机构信息同步GP")
    @ApiOperation(value = "医疗机构信息同步GP", notes = "医疗机构信息同步GP")
    @GetMapping(value = "/dataImportGp")
    public Result<?> dataImportGp() throws Exception {
        Object dataImportGp = redisUtil.get("dataImportGp");
        if(ObjectUtil.isNotEmpty(dataImportGp)){
            throw new Exception("同步任务运行中!");
        }else{
            redisUtil.set("dataImportGp","dataImportGp",60*60);
        }
        try {
            ybFjOrgService.dataImportGp();
        } catch (Exception e) {
            return Result.error("同步失败!");
        }finally {
            redisUtil.del("dataImportGp");
        }
        return Result.ok("医疗机构信息GP同步成功!");
    }

    @AutoLog(value = "飞检医疗机构信息-通过orgId查询")
    @ApiOperation(value = "飞检医疗机构信息-通过orgId查询", notes = "飞检医疗机构信息-通过orgId查询")
    @GetMapping(value = "/queryByOrgid")
    public Result<YbFjOrg> queryByOrgid(@RequestParam(name = "orgId", required = true) String orgId) {
        YbFjOrg ybFjOrg = ybFjOrgService.findOrg(orgId);
        Result<YbFjOrg> result = new Result<YbFjOrg>();
        result.setResult(ybFjOrg);
        return result;
    }

    @AutoLog(value = "飞检医疗机构信息-查找院端操作员所属医院")
    @ApiOperation(value = "飞检医疗机构信息-查找院端操作员所属医院", notes = "飞检医疗机构信息-查找院端操作员所属医院")
    @GetMapping(value = "/queryByUserid")
    public Result<YbFjOrg> queryByUserid(@RequestParam(name = "userId", required = true) String userId) {
        YbFjOrg ybFjOrg = ybFjOrgService.findOrgByUser(userId);
        Result<YbFjOrg> result = new Result<YbFjOrg>();
        result.setResult(ybFjOrg);
        return result;
    }


    /**
     * 用户信息列表
     *
     * @param orgUserVo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-用户信息列表")
    @ApiOperation(value = "飞检医疗机构信息-用户信息列表", notes = "飞检医疗机构信息-用户信息列表")
    @GetMapping(value = "/userList")
    public Result<IPage<OrgUserVo>> queryUserPageList(OrgUserVo orgUserVo,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        Page<OrgUserVo> page = new Page<OrgUserVo>(pageNo, pageSize);
        IPage<OrgUserVo> pageList = ybFjOrgService.getOrgUser(page, orgUserVo);
        Result<IPage<OrgUserVo>> result = new Result<IPage<OrgUserVo>>();
        result.setResult(pageList);
        return result;
    }

    /**
     * 医疗机构用户信息列表
     *
     * @param orgUserVo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "飞检医疗机构信息-医疗机构用户信息列表")
    @ApiOperation(value = "飞检医疗机构信息-医疗机构用户信息列表", notes = "飞检医疗机构信息-医疗机构用户信息列表")
    @GetMapping(value = "/userOrgList")
    public Result<IPage<OrgUserVo>> queryUserOrgPageList(OrgUserVo orgUserVo,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest req) {
        Page<OrgUserVo> page = new Page<OrgUserVo>(pageNo, pageSize);
        IPage<OrgUserVo> pageList = ybFjOrgService.getUserOrgList(page, orgUserVo);
        Result<IPage<OrgUserVo>> result = new Result<IPage<OrgUserVo>>();
        result.setResult(pageList);
        return result;
    }

}
