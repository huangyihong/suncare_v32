package com.ai.modules.medical.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.service.IMedicalClinicalAccessGroupService;
import com.ai.modules.medical.service.IMedicalClinicalInfoService;
import com.ai.modules.medical.service.IMedicalClinicalRangeGroupService;
import com.ai.modules.medical.vo.MedicalClinicalIOVO;
import com.ai.modules.medical.vo.MedicalClinicalVO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.service.IMedicalClinicalService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;

import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 临床路径主体
 * @Author: jeecg-boot
 * @Date: 2020-03-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "临床路径主体")
@RestController
@RequestMapping("/medical/medicalClinical")
public class MedicalClinicalController extends JeecgController<MedicalClinical, IMedicalClinicalService> {
    @Autowired
    private IMedicalClinicalService medicalClinicalService;

    @Autowired
    private IMedicalClinicalInfoService medicalClinicalInfoService;

    @Autowired
    private IMedicalClinicalAccessGroupService medicalClinicalAccessGroupService;

    @Autowired
    private IMedicalClinicalRangeGroupService medicalClinicalRangeGroupService;

    /**
     * 分页列表查询
     *
     * @param medicalClinical
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "临床路径主体-分页列表查询")
    @ApiOperation(value = "临床路径主体-分页列表查询", notes = "临床路径主体-分页列表查询")
    @RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
    public Result<?> queryPageList(MedicalClinical medicalClinical,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalClinical> queryWrapper = QueryGenerator.initQueryWrapper(medicalClinical, req.getParameterMap());
        Page<MedicalClinical> page = new Page<MedicalClinical>(pageNo, pageSize);
        IPage<MedicalClinical> pageList = medicalClinicalService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param medicalClinical
     * @return
     *//*
	@AutoLog(value = "临床路径主体-添加")
	@ApiOperation(value="临床路径主体-添加", notes="临床路径主体-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalClinical medicalClinical) {
		medicalClinicalService.save(medicalClinical);
		return Result.ok("添加成功！");
	}

	*//**
     * 编辑
     *
     * @param medicalClinical
     * @return
     *//*
	@AutoLog(value = "临床路径主体-编辑")
	@ApiOperation(value="临床路径主体-编辑", notes="临床路径主体-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalClinical medicalClinical) {
		medicalClinicalService.updateById(medicalClinical);
		return Result.ok("编辑成功!");
	}*/

    /**
     * 添加
     *
     * @param medicalClinicalVO
     * @return
     */
    @AutoLog(value = "临床路径-添加")
    @ApiOperation(value = "临床路径-添加", notes = "临床路径-添加")
    @PostMapping(value = "/addClinical")
    @Transactional
    public Result<?> add(@RequestBody MedicalClinicalVO medicalClinicalVO) {
        String clinicalId = IdUtils.uuid();
        JSONObject baseInfo = medicalClinicalVO.getBaseInfo();
        // 固定ID
        baseInfo.put("clinicalId", clinicalId);
        // 主体归纳
        MedicalClinical medicalClinical = baseInfo.toJavaObject(MedicalClinical.class);
        String code = medicalClinical.getClinicalCode();
        List<MedicalClinical> list = this.medicalClinicalService.list(new QueryWrapper<MedicalClinical>().eq("CLINICAL_CODE",code));
        if(list.size() > 0){
            return Result.error("临床路径编码已存在");
        }
        // 扩展信息归纳
        MedicalClinicalInfo medicalClinicalInfo = baseInfo.toJavaObject(MedicalClinicalInfo.class);

        Double maxOrder = this.medicalClinicalService.getMaxOrderNo();
        Double order = maxOrder == null?1.0:maxOrder.intValue() + 1.0;
        medicalClinical.setOrderNo(order);
        if(StringUtils.isNotBlank(medicalClinicalInfo.getClinicalFile())){
            // 政策依据 = 文件名
            List<String> filePaths = Arrays.asList(medicalClinicalInfo.getClinicalFile().split(","));
            String fileNameJoins = filePaths.stream().map(filePath -> filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("_"))).collect(Collectors.joining(","));
            medicalClinicalInfo.setRuleBasis(fileNameJoins);
        }

        medicalClinicalService.save(medicalClinical);
        medicalClinicalInfoService.save(medicalClinicalInfo);

        List<MedicalClinicalAccessGroup> approveGroup = medicalClinicalVO.getApproveGroup();
        List<MedicalClinicalAccessGroup> rejectGroup = medicalClinicalVO.getRejectGroup();
        List<MedicalClinicalRangeGroup> drugRanges = medicalClinicalVO.getDrugRange();
        List<MedicalClinicalRangeGroup> projectRanges = medicalClinicalVO.getProjectRange();
        // 合并范围数组
        List<MedicalClinicalRangeGroup> rangeGroupList = new ArrayList<>();
        if (drugRanges != null && drugRanges.size() > 0) {
            rangeGroupList.addAll(drugRanges);
        }
        if (projectRanges != null && projectRanges.size() > 0) {
            rangeGroupList.addAll(projectRanges);
        }
        if (rangeGroupList.size() > 0) {
            rangeGroupList.forEach(group -> group.setClinicalId(clinicalId));
            medicalClinicalRangeGroupService.saveBatch(rangeGroupList);
        }
        List<MedicalClinicalAccessGroup> accessGroups = new ArrayList<>();
        if (approveGroup != null && approveGroup.size() > 0) {
            accessGroups.addAll(approveGroup);
        }

        if (rejectGroup != null && rejectGroup.size() > 0) {
            accessGroups.addAll(rejectGroup);
        }

        if(accessGroups.size() > 0){
            accessGroups.forEach(group -> group.setClinicalId(clinicalId));
            medicalClinicalAccessGroupService.saveBatch(accessGroups);
        }


        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalClinicalVO
     * @return
     */
    @AutoLog(value = "临床路径-编辑")
    @ApiOperation(value = "临床路径-编辑", notes = "临床路径-编辑")
    @PutMapping(value = "/editClinical")
    @Transactional
    public Result<?> edit(@RequestBody MedicalClinicalVO medicalClinicalVO) {
        String clinicalId = medicalClinicalVO.getClinicalId();
        JSONObject baseInfo = medicalClinicalVO.getBaseInfo();
        if (baseInfo != null) {
            // 固定ID
            baseInfo.put("clinicalId", clinicalId);
            // 主体归纳
            MedicalClinical medicalClinical = baseInfo.toJavaObject(MedicalClinical.class);
            // 扩展信息归纳
            MedicalClinicalInfo medicalClinicalInfo = baseInfo.toJavaObject(MedicalClinicalInfo.class);
            if(StringUtils.isNotBlank(medicalClinicalInfo.getClinicalFile())){
                // 政策依据 = 文件名
                List<String> filePaths = Arrays.asList(medicalClinicalInfo.getClinicalFile().split(","));
                String fileNameJoins = filePaths.stream().map(filePath -> filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("_"))).collect(Collectors.joining(","));
                medicalClinicalInfo.setRuleBasis(fileNameJoins);
            }
            medicalClinicalService.updateById(medicalClinical);
            try {
                medicalClinicalInfoService.updateById(medicalClinicalInfo);
            } catch (Exception e){
                log.error("更新临床路径其他信息失败：" + e.getMessage());
            }
        }
        List<MedicalClinicalAccessGroup> approveGroup = medicalClinicalVO.getApproveGroup();
        List<MedicalClinicalAccessGroup> rejectGroup = medicalClinicalVO.getRejectGroup();
        List<MedicalClinicalRangeGroup> drugRanges = medicalClinicalVO.getDrugRange();
        List<MedicalClinicalRangeGroup> projectRanges = medicalClinicalVO.getProjectRange();

        if (drugRanges != null) {
            medicalClinicalRangeGroupService.remove(new QueryWrapper<MedicalClinicalRangeGroup>()
                    .eq("CLINICAL_ID", clinicalId)
                    .eq("GROUP_TYPE", "drug"));
            if (drugRanges.size() > 0) {
                drugRanges.forEach(group -> group.setClinicalId(clinicalId));
                medicalClinicalRangeGroupService.saveBatch(drugRanges);
            }
        }
        if (projectRanges != null) {
            medicalClinicalRangeGroupService.remove(new QueryWrapper<MedicalClinicalRangeGroup>()
                    .eq("CLINICAL_ID", clinicalId)
                    .eq("GROUP_TYPE", "project"));
            if (projectRanges.size() > 0) {
                projectRanges.forEach(group -> group.setClinicalId(clinicalId));
                medicalClinicalRangeGroupService.saveBatch(projectRanges);
            }

        }
        if (approveGroup != null) {
            medicalClinicalAccessGroupService.remove(new QueryWrapper<MedicalClinicalAccessGroup>()
                    .eq("CLINICAL_ID",clinicalId)
                    .eq("GROUP_TYPE", "approve")
            );
            for(MedicalClinicalAccessGroup group: approveGroup){
                group.setClinicalId(clinicalId);
            }
            medicalClinicalAccessGroupService.saveBatch(approveGroup);
        }
        if (rejectGroup != null) {
            medicalClinicalAccessGroupService.remove(new QueryWrapper<MedicalClinicalAccessGroup>()
                    .eq("CLINICAL_ID",clinicalId)
                    .eq("GROUP_TYPE", "reject")
            );
            for(MedicalClinicalAccessGroup group: rejectGroup){
                group.setClinicalId(clinicalId);
            }
            medicalClinicalAccessGroupService.saveBatch(rejectGroup);
        }
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "临床路径-通过id删除")
    @ApiOperation(value = "临床路径-通过id删除", notes = "临床路径-通过id删除")
    @DeleteMapping(value = "/deleteClinical")
    public Result<?> deleteClinical(@RequestParam(name = "id") String id) {
        medicalClinicalService.removeById(id);
        medicalClinicalInfoService.removeById(id);
        medicalClinicalRangeGroupService.remove(new QueryWrapper<MedicalClinicalRangeGroup>()
                .eq("CLINICAL_ID", id));
        medicalClinicalAccessGroupService.remove(new QueryWrapper<MedicalClinicalAccessGroup>().
                eq("CLINICAL_ID", id));
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "临床路径-批量删除")
    @ApiOperation(value = "临床路径-批量删除", notes = "临床路径-批量删除")
    @DeleteMapping(value = "/deleteClinicalBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        medicalClinicalService.removeByIds(idList);
        medicalClinicalInfoService.removeByIds(idList);
        medicalClinicalRangeGroupService.remove(new QueryWrapper<MedicalClinicalRangeGroup>()
                .in("CLINICAL_ID", idList));
        medicalClinicalAccessGroupService.remove(new QueryWrapper<MedicalClinicalAccessGroup>()
                .in("CLINICAL_ID", idList));
        return Result.ok("批量删除成功！");
    }

    @AutoLog(value = "临床路径-批量删除")
    @ApiOperation(value = "临床路径-批量删除", notes = "临床路径-批量删除")
    @PutMapping(value = "/changeStatus")
    public Result<?> changeStatus(@RequestParam(name = "ids") String ids, @RequestParam(name = "status") String status) {

        String finalStatus = "1".equals(status)? "1" : "0";;
        List<MedicalClinical> list = Arrays.stream(ids.split(",")).map(id -> {
            MedicalClinical bean = new MedicalClinical();
            bean.setClinicalId(id);
            bean.setPublicStatus(finalStatus);
            return bean;
        }).collect(Collectors.toList());
        medicalClinicalService.updateBatchById(list);
        return Result.ok("修改成功！");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     *//*
    @AutoLog(value = "临床路径主体-通过id删除")
    @ApiOperation(value = "临床路径主体-通过id删除", notes = "临床路径主体-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalClinicalService.removeById(id);
        return Result.ok("删除成功!");
    }*/

    /**
     * 批量删除
     *
     * @param ids
     * @return
     *//*
    @AutoLog(value = "临床路径主体-批量删除")
    @ApiOperation(value = "临床路径主体-批量删除", notes = "临床路径主体-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalClinicalService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }*/

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "临床路径主体-通过id查询")
    @ApiOperation(value = "临床路径主体-通过id查询", notes = "临床路径主体-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalClinical medicalClinical = medicalClinicalService.getById(id);
        return Result.ok(medicalClinical);
    }

    /**
     * 导出excel
     *
     * @param req
     * @param response
     * @param medicalClinical
     */
    @RequestMapping(value = "/exportXls")
    public void exportXls(HttpServletRequest req, HttpServletResponse response, MedicalClinical medicalClinical) throws Exception {
        QueryWrapper<MedicalClinical> queryWrapper = QueryGenerator.initQueryWrapper(medicalClinical, req.getParameterMap());

        String title = "临床路径配置_导出" + System.currentTimeMillis();
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

        OutputStream os = response.getOutputStream();
        this.medicalClinicalService.exportExcel(queryWrapper, os);

    }

    @RequestMapping(value = "/exportXlsThread")
    public Result exportXlsThread(HttpServletRequest req, HttpServletResponse response, MedicalClinical medicalClinical) throws Exception {
        QueryWrapper<MedicalClinical> queryWrapper = QueryGenerator.initQueryWrapper(medicalClinical, req.getParameterMap());

        int count = this.medicalClinicalService.count(queryWrapper);
        ThreadUtils.EXPORT_POOL.add("临床路径配置","xls", count, (os) -> {
            try {
                this.medicalClinicalService.exportExcel(queryWrapper, os);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
            return Result.ok();
        });

        return Result.ok("等待导出");

    }


    /**
     * 通过excel导入数据
     *
     * @param file
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(@RequestParam("file") MultipartFile file, HttpServletResponse response) {

        int[] rowCounts = new int[3];

        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                long beginTime = System.currentTimeMillis();
                int[] nums = this.medicalClinicalService.importExcel(file);
                long endTime = System.currentTimeMillis();

                log.info("[" + name + "]导入时间：" + (endTime - beginTime) / 1000 + "秒");

                for (int i = 0; i < rowCounts.length; i++) {
                    rowCounts[i] += nums[i];
                }
            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        }

        return Result.ok("操作成功，导入 " + rowCounts[0] + " 条临床路径主记录，" + rowCounts[1] + " 条药品组记录，" + rowCounts[2] + " 条服务项目组记录");
    }

    /**
     * 批量导入附件
     *
     * @param file
     * @param response
     * @return
     */
    @RequestMapping(value = "/importFiles", method = RequestMethod.POST)
    public Result<?> importFiles(@RequestParam("file") MultipartFile file, HttpServletRequest req,HttpServletResponse response) {

        // 获取文件名
        String fileName = file.getOriginalFilename();
        try {
            int index = fileName.indexOf("_");
            if (index < 0) {
                throw new Exception("文件名缺少下划线");
            }
            String code = fileName.substring(0,index);
            MedicalClinicalInfo bean = this.medicalClinicalInfoService.getByCode(code);
            if(bean == null){
                throw new Exception("临床路径编码不存在：" + code);
            }
            // 校验文件名是否重复
            if(StringUtils.isNotBlank(bean.getClinicalFile())){
                String[] filePaths = bean.getClinicalFile().split(",");
                for(String path: filePaths){
                    // 去掉时间戳
                    if(fileName.equals(CommonUtil.pathToFileName(path))){
                        throw new Exception("文件已存在");
                    }
                }
            }

            String path = CommonUtil.upload(file,req.getParameter("bizPath"));
            if(StringUtils.isBlank(bean.getClinicalFile())){
                bean.setClinicalFile(path);
                bean.setRuleBasis(path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("_")));
            } else {
                bean.setClinicalFile(bean.getClinicalFile() + ","+path);
                // 政策依据 = 文件名
                List<String> filePaths = Arrays.asList(bean.getClinicalFile().split(","));
                String fileNameJoins = filePaths.stream().map(filePath -> filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("_"))).collect(Collectors.joining(","));
                bean.setRuleBasis(fileNameJoins);
            }

            this.medicalClinicalInfoService.updateById(bean);

            return Result.ok(path);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


}
