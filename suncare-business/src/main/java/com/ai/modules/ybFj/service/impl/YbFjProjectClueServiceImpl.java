package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.ai.common.emport.MultipartExcelImport;
import com.ai.common.export.ExcelExportUtil;
import com.ai.common.export.ExportColModel;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueDto;
import com.ai.modules.ybFj.entity.*;
import com.ai.modules.ybFj.handle.ClueMultipartExcelImport;
import com.ai.modules.ybFj.mapper.YbFjProjectClueMapper;
import com.ai.modules.ybFj.service.*;
import com.ai.modules.ybFj.vo.ClueDtlTotalVo;
import com.ai.modules.ybFj.vo.StatProjectClueVo;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.ai.modules.ybFj.vo.YbFjProjectClueCutVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description: 飞检项目线索
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class YbFjProjectClueServiceImpl extends ServiceImpl<YbFjProjectClueMapper, YbFjProjectClue> implements IYbFjProjectClueService {

    @Autowired
    private IYbFjProjectOrgService projectOrgService;
    @Autowired
    private IYbFjProjectClueDtlService clueDtlService;
    @Autowired
    private IYbFjProjectClueFileService clueFileService;
    @Autowired
    private IYbFjProjectCluePushService cluePushService;
    @Autowired
    private IYbFjProjectClueOnsiteService clueOnsiteService;
    @Autowired
    private IYbFjProjectClueOnsiteDtlService clueOnsiteDtlService;
    @Autowired
    private IYbFjProjectClueCutService clueCutService;
    @Autowired
    private IYbFjProjectClueCutDtlService clueCutDtlService;

    /**
     *
     * 功能描述：保存线索明细
     * @author zhangly
     * @date 2023-03-09 09:34:46
     *
     * @param clue
     * @param clueDtlList
     *
     * @return void
     *
     */
    private ClueDtlTotalVo saveClueDtl(YbFjProjectClue clue, List<Map<String, String>> clueDtlList) {
        int sl = 0;
        BigDecimal fee = BigDecimal.ZERO;
        if(clueDtlList!=null && clueDtlList.size()>0) {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<YbFjProjectClueDtl> dtlList = new ArrayList<YbFjProjectClueDtl>();
            for(Map<String, String> map : clueDtlList) {
                YbFjProjectClueDtl dtl = BeanUtil.mapToBean(map, YbFjProjectClueDtl.class, true);
                dtl.setClueDtlId(IdUtils.uuid());
                dtl.setClueId(clue.getClueId());
                dtl.setProjectId(clue.getProjectId());
                dtl.setProjectOrgId(clue.getProjectOrgId());
                dtl.setCreateTime(DateUtils.getDate());
                dtl.setCreateUser(user.getUsername());
                dtl.setCreateUsername(user.getRealname());
                dtlList.add(dtl);

                if(dtl.getSl()!=null) {
                    sl = sl + dtl.getSl();
                }
                if(dtl.getWgFee()!=null) {
                    fee = fee.add(dtl.getWgFee());
                }
            }
            clueDtlService.saveBatch(dtlList);
        }
        return new ClueDtlTotalVo(sl, fee);
    }

    @Override
    public void importProjectClue(String projectOrgId, MultipartFile file) throws Exception {
        String xmlPath = "/templates/fj/project_clue.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        MultipartExcelImport clueExcelImport = new MultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueList = clueExcelImport.handle(false);
        clueExcelImport.exception();
        xmlPath = "/templates/fj/project_clue_dtl.xml";
        xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        ClueMultipartExcelImport clueDtlExcelImport = new ClueMultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueDtlList = clueDtlExcelImport.handleClue("线索明细表");
        clueDtlExcelImport.exception();

        if(clueList.size()==0) {
            throw new Exception("请上传线索汇总表");
        }
        //保存线索汇总
        Map<String, String> clueMap = clueList.get(0);
        YbFjProjectClue clue = BeanUtil.mapToBean(clueMap, YbFjProjectClue.class, true);
        String clueId = IdUtils.uuid();
        clue.setClueId(clueId);
        clue.setProjectOrgId(projectOrgId);

        YbFjProjectOrg projectOrg = projectOrgService.getById(projectOrgId);
        clue.setProjectId(projectOrg.getProjectId());
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setCreateTime(DateUtils.getDate());
        clue.setCreateUser(user.getUsername());
        clue.setCreateUsername(user.getRealname());
        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        this.save(clue);

        //保存线索明细
        this.saveClueDtl(clue, clueDtlList);
        //保存线索文件
        this.saveClueFile(clue, file);
    }

    @Override
    public void coverProjectClue(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClue clue = this.getById(clueId);
        if(!DcFjConstants.CLUE_STEP_SUBMIT.equals(clue.getCurrStep())) {
            throw new Exception("线索已推送到其他环节，不允许修改！");
        }

        String xmlPath = "/templates/fj/project_clue.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        ClueMultipartExcelImport clueExcelImport = new ClueMultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueList = clueExcelImport.handleClue("线索汇总表");
        clueExcelImport.exception();
        xmlPath = "/templates/fj/project_clue_dtl.xml";
        xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        ClueMultipartExcelImport clueDtlExcelImport = new ClueMultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueDtlList = clueDtlExcelImport.handleClue("线索明细表");
        clueDtlExcelImport.exception();

        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(clueList!=null && clueList.size()>0) {
            //覆盖线索汇总
            Map<String, String> clueMap = clueList.get(0);
            YbFjProjectClue source = BeanUtil.mapToBean(clueMap, YbFjProjectClue.class, true);
            BeanUtil.copyProperties(source, clue, CopyOptions.create().ignoreNullValue());
        }
        clue.setUpdateTime(DateUtils.getDate());
        clue.setUpdateUser(user.getUsername());
        clue.setUpdateUsername(user.getRealname());

        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        clueDtlService.remove(wrapper);
        //覆盖线索明细
        ClueDtlTotalVo dtlTotalVo = this.saveClueDtl(clue, clueDtlList);
        clue.setDtlAmount(dtlTotalVo.getTotalSl());
        clue.setDtlWgFee(dtlTotalVo.getTotalWgFee());
        //删除线索文件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("clue_id", clue.getClueId());
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //保存线索文件
        this.saveClueFile(clue, file);
        //更新线索汇总
        this.updateById(clue);
    }

    @Override
    public void appendProjectClue(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClue clue = this.getById(clueId);
        if(!DcFjConstants.CLUE_STEP_SUBMIT.equals(clue.getCurrStep())) {
            throw new Exception("线索已推送到其他环节，不允许修改！");
        }

        String xmlPath = "/templates/fj/project_clue_dtl.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        MultipartExcelImport clueDtlExcelImport = new MultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueDtlList = clueDtlExcelImport.handle(false);
        clueDtlExcelImport.exception();

        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setUpdateTime(DateUtils.getDate());
        clue.setUpdateUser(user.getUsername());
        clue.setUpdateUsername(user.getRealname());
        this.updateById(clue);
        //追加线索明细
        this.saveClueDtl(clue, clueDtlList);
        //保存线索文件
        this.saveClueFile(clue, file);
    }

    /**
     *
     * 功能描述：保存上传的线索附件
     * @author zhangly
     * @date 2023-03-09 09:35:04
     *
     * @param clue
     * @param file
     *
     * @return void
     *
     */
    private void saveClueFile(YbFjProjectClue clue, MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        int index = filename.lastIndexOf(".");
        // 文件扩展名
        String extname = filename.substring(index+1);
        String newname = DateUtils.getDate("yyyyMMddHHmmssSSS")+"."+extname;
        String path = File.separator + "fj" + File.separator + DateUtils.getDate("yyyyMM");
        File folder = new File(CommonUtil.UPLOAD_PATH + path);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        String savePath = folder + File.separator + newname;
        File saveFile = new File(savePath);
        FileCopyUtils.copy(file.getBytes(), saveFile);

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjProjectClueFile clueFile = new YbFjProjectClueFile();
        clueFile.setFileId(IdUtils.uuid());
        clueFile.setClueId(clue.getClueId());
        clueFile.setProjectId(clue.getProjectId());
        clueFile.setProjectOrgId(clue.getProjectOrgId());
        clueFile.setStepType(DcFjConstants.CLUE_STEP_SUBMIT);
        clueFile.setStepGroup(DcFjConstants.CLUE_STEP_SUBMIT);
        clueFile.setFileType(DcFjConstants.FILE_TYPE_EXCEL);
        clueFile.setOperType(DcFjConstants.FILE_OPER_TYPE_UP);
        clueFile.setFileSrcname(filename);
        clueFile.setFileName(newname);
        clueFile.setFilePath(path + File.separator + newname);
        clueFile.setFileSize(saveFile.length());
        clueFile.setCreateTime(DateUtils.getDate());
        clueFile.setCreateUser(user.getUsername());
        clueFile.setCreateUsername(user.getRealname());
        clueFileService.save(clueFile);
    }

    @Override
    public void downTemplate(String filePath, String alias, HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if(!alias.contains(".")) {
                alias = alias + ".xlsx";
            }
            String filename = new String(alias.getBytes("UTF-8"),"iso-8859-1");
            response.setContentType("application/octet-stream");
            response.setHeader("content-type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;fileName=" + filename);
            inputStream = new BufferedInputStream(this.getClass().getResourceAsStream(filePath));
            outputStream = new BufferedOutputStream(response.getOutputStream());
            int len;
            while ((len = inputStream.read()) != -1) {
                outputStream.write(len);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.info("文件下载失败" + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void auditProjectClue(String clueId, String auditState, String auditOpinion) {
        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setClueId(clueId);
        clue.setAuditState(auditState);
        clue.setAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setAuditTime(DateUtils.getDate());
        clue.setAuditUser(user.getUsername());
        clue.setAuditUserName(user.getRealname());
        this.updateById(clue);
    }

    @Override
    public void auditProjectClues(String clueIds, String auditState, String auditOpinion) {
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", clueIds.split(","));

        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setAuditState(auditState);
        clue.setAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setAuditTime(DateUtils.getDate());
        clue.setAuditUser(user.getUsername());
        clue.setAuditUserName(user.getRealname());
        this.update(clue, wrapper);
    }

    @Override
    public void auditHospClue(String clueId, String auditState, String auditOpinion) {
        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setClueId(clueId);
        clue.setHospAuditState(auditState);
        clue.setHospAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setHospAuditTime(DateUtils.getDate());
        clue.setHospAuditUser(user.getUsername());
        clue.setHospAuditUserName(user.getRealname());
        this.updateById(clue);
    }

    @Override
    public void auditHospClues(String clueIds, String auditState, String auditOpinion) {
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", clueIds.split(","));

        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setHospAuditState(auditState);
        clue.setHospAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setHospAuditTime(DateUtils.getDate());
        clue.setHospAuditUser(user.getUsername());
        clue.setHospAuditUserName(user.getRealname());
        this.update(clue, wrapper);
    }

    @Override
    public void auditCutClue(String clueId, String auditState, String auditOpinion) {
        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setClueId(clueId);
        clue.setCutAuditState(auditState);
        clue.setCutAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setCutAuditTime(DateUtils.getDate());
        clue.setCutAuditUser(user.getUsername());
        clue.setCutAuditUserName(user.getRealname());
        this.updateById(clue);
    }

    @Override
    public void auditCutClues(String clueIds, String auditState, String auditOpinion) {
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", clueIds.split(","));

        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setCutAuditState(auditState);
        clue.setCutAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setCutAuditTime(DateUtils.getDate());
        clue.setCutAuditUser(user.getUsername());
        clue.setCutAuditUserName(user.getRealname());
        this.update(clue, wrapper);
    }

    @Override
    public void saveProjectClue(YbFjProjectClueDto dto) throws Exception {
        if(StringUtils.isBlank(dto.getProjectOrgId())) {
            throw new Exception("projectOrgId参数不能为空");
        }
        YbFjProjectClue clue = BeanUtil.toBean(dto, YbFjProjectClue.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String clueId = IdUtils.uuid();
        clue.setClueId(clueId);
        YbFjProjectOrg projectOrg = projectOrgService.getById(dto.getProjectOrgId());
        clue.setProjectId(projectOrg.getProjectId());
        clue.setCreateTime(DateUtils.getDate());
        clue.setCreateUser(user.getUsername());
        clue.setCreateUsername(user.getRealname());
        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        this.save(clue);
    }

    @Override
    public void updateProjectClue(YbFjProjectClueDto dto) throws Exception {
        if(StringUtils.isBlank(dto.getClueId())) {
            throw new Exception("clueId参数不能为空");
        }
        if(StringUtils.isBlank(dto.getProjectOrgId())) {
            throw new Exception("projectOrgId参数不能为空");
        }
        YbFjProjectClue clue = this.getById(dto.getClueId());
        if(!DcFjConstants.CLUE_STEP_SUBMIT.equals(clue.getCurrStep())) {
            throw new Exception("线索已推送到其他环节，不允许修改！");
        }
        BeanUtil.copyProperties(dto, clue, CopyOptions.create().ignoreNullValue());
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setUpdateTime(DateUtils.getDate());
        clue.setUpdateUser(user.getUsername());
        clue.setUpdateUsername(user.getRealname());
        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        this.updateById(clue);
    }

    @Override
    public void removeProjectClue(String clueId) throws Exception {
        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        clueDtlService.remove(wrapper);
        //删除附件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("clue_id", clueId);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //删除线索汇总
        this.removeById(clueId);
    }

    @Override
    public void removeProjectClues(String clueIds) throws Exception {
        String[] ids = clueIds.split(",");
        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", ids);
        clueDtlService.remove(wrapper);
        //删除附件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.in("clue_id", ids);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //删除线索汇总
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public void pushProjectClue(String clueIds, String nextStep, String prevStep) throws Exception {
        String[] ids = clueIds.split(",");
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", ids);
        YbFjProjectClue clue = new YbFjProjectClue();
        clue.setCurrStep(nextStep);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(DcFjConstants.CLUE_STEP_HOSP.equals(nextStep)) {
            //推送到医院复核环节，状态设置=待反馈
            QueryWrapper<YbFjProjectClue> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("clue_id", ids);
            queryWrapper.isNotNull("hosp_audit_state");
            int count = this.count(queryWrapper);
            if(count>0) {
                throw new Exception("选项中存在已推送到医院复核环节的线索");
            }
            clue.setHospAuditState(DcFjConstants.HOSP_STATE_INIT);
            clue.setHospStepTime(DateUtils.getDate());
            clue.setHospStepUser(user.getUsername());
            clue.setHospStepUserName(user.getRealname());
        } else if(DcFjConstants.CLUE_STEP_CUT.equals(nextStep)) {
            //推送到线上核减环节
            QueryWrapper<YbFjProjectClueCut> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("clue_id", ids);
            int count = clueCutService.count(queryWrapper);
            if(count>0) {
                throw new Exception("选项中存在已推送到线上核减环节的线索");
            }
            clue.setCutAuditState(DcFjConstants.CUT_STATE_INIT);
            clue.setCutStepTime(DateUtils.getDate());
            clue.setCutStepUser(user.getUsername());
            clue.setCutStepUserName(user.getRealname());

            //数据同步
            SyncClueDto dto = new SyncClueDto();
            dto.setPrevStep(prevStep);
            dto.setClueIds(Arrays.asList(ids));
            dto.setCreateTime(DateUtils.getDate());
            dto.setCreateUser(user.getUsername());
            dto.setCreateUsername(user.getRealname());
            clueCutService.insertCutClue(dto);
            clueCutDtlService.insertCutClueDtl(dto);
            //拷贝文件
            QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
            fileQueryWrapper.in("clue_id", ids);
            fileQueryWrapper.eq("step_group", prevStep);
            fileQueryWrapper.eq("step_type", prevStep);
            List<YbFjProjectClueFile> fileList = clueFileService.list(fileQueryWrapper);
            this.copyClueFile(DcFjConstants.CLUE_STEP_CUT, fileList);
        } else if(DcFjConstants.CLUE_STEP_ONSITE.equals(nextStep)) {
            //推送到现场核查环节
            QueryWrapper<YbFjProjectClueOnsite> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("clue_id", ids);
            int count = clueOnsiteService.count(queryWrapper);
            if(count>0) {
                throw new Exception("选项中存在已推送到现场核查环节的线索");
            }
            clue.setOnsiteStepTime(DateUtils.getDate());
            clue.setOnsiteStepUser(user.getUsername());
            clue.setOnsiteStepUserName(user.getRealname());
            //数据同步
            SyncClueDto dto = new SyncClueDto();
            dto.setPrevStep(prevStep);
            dto.setClueIds(Arrays.asList(ids));
            dto.setCreateTime(DateUtils.getDate());
            dto.setCreateUser(user.getUsername());
            dto.setCreateUsername(user.getRealname());
            clueOnsiteService.insertOnsiteClue(dto);
            clueOnsiteDtlService.insertOnsiteClueDtl(dto);
            //拷贝文件
            QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
            fileQueryWrapper.in("clue_id", ids);
            fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
            fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
            List<YbFjProjectClueFile> fileList = clueFileService.list(fileQueryWrapper);
            this.copyClueFile(DcFjConstants.CLUE_STEP_SUBMIT, fileList);
        }
        this.update(clue, wrapper);
    }

    private void copyClueFile(String nextStep, List<YbFjProjectClueFile> fileList) throws Exception {
        if(fileList!=null && fileList.size()>0) {
            YbFjProjectClue clue = this.getById(fileList.get(0).getFileId());
            List<YbFjProjectClueFile> copyList = new ArrayList<>();
            for(YbFjProjectClueFile record : fileList) {
                String fileSrcname = record.getFileSrcname();
                String filePath = CommonUtil.UPLOAD_PATH + record.getFilePath();
                int index = fileSrcname.lastIndexOf(".");
                // 文件扩展名
                String extname = fileSrcname.substring(index+1);
                String newname = DateUtils.getDate("yyyyMMddHHmmssSSS")+"."+extname;
                String path = File.separator + "fj" + File.separator + DateUtils.getDate("yyyyMM");
                File folder = new File(CommonUtil.UPLOAD_PATH + path);
                if(!folder.exists()) {
                    folder.mkdirs();
                }
                File file = new File(filePath);
                String copyPath = folder + File.separator + newname;
                File saveFile = new File(copyPath);
                FileCopyUtils.copy(file, saveFile);
                YbFjProjectClueFile clueFile = this.getProjectClueFile(nextStep, record, newname, path, file.length());
                copyList.add(clueFile);
            }
            clueFileService.saveBatch(copyList);
        }
    }

    private YbFjProjectClueFile getProjectClueFile(String nextStep, YbFjProjectClueFile clueFile, String newname, String path, long size) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjProjectClueFile copyFile = new YbFjProjectClueFile();
        copyFile.setFileId(IdUtils.uuid());
        copyFile.setClueId(clueFile.getClueId());
        copyFile.setProjectId(clueFile.getProjectId());
        copyFile.setProjectOrgId(clueFile.getProjectOrgId());
        copyFile.setStepGroup(nextStep);
        copyFile.setStepType(nextStep);
        copyFile.setFileType(clueFile.getFileType());
        copyFile.setOperType(DcFjConstants.FILE_OPER_TYPE_UP);
        copyFile.setFileSrcname(clueFile.getFileSrcname());
        copyFile.setFileName(newname);
        copyFile.setFilePath(path + File.separator + newname);
        copyFile.setFileSize(size);
        copyFile.setCreateTime(DateUtils.getDate());
        copyFile.setCreateUser(user.getUsername());
        copyFile.setCreateUsername(user.getRealname());
        return copyFile;
    }

    @Override
    public void downloadProjectClueFilesZip(String clueId, HttpServletResponse response) throws Exception {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        wrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
        wrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        wrapper.orderByAsc("create_time");
        List<YbFjProjectClueFile> fileList = clueFileService.list(wrapper);
        clueFileService.downloadZip(response, fileList);
    }

    @Override
    public void pushProjectClueToOrg(String clueIds) throws Exception {
        String[] ids = clueIds.split(",");
        YbFjProjectClue clue = this.getById(ids[0]);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<YbFjProjectCluePush> pushList = new ArrayList<>();
        for(String clueId : ids) {
            YbFjProjectCluePush push = new YbFjProjectCluePush();
            push.setPushId(IdUtils.uuid());
            push.setClueId(clueId);
            push.setProjectId(clue.getProjectId());
            push.setPushTime(DateUtils.getDate());
            push.setPushUser(user.getUsername());
            push.setPushUsername(user.getRealname());
            pushList.add(push);
        }
        cluePushService.saveBatch(pushList);
    }

    @Override
    public StatProjectClueVo statisticsProjectClue(String projectOrgId) {
        return baseMapper.statisticsProjectClue(projectOrgId);
    }

    @Override
    public Integer statisticsProjectClueAmount(String projectId) {
        return baseMapper.statisticsProjectClueAmount(projectId);
    }

    @Override
    public StatStepClueVo statisticsStepClue(String statType, QueryWrapper<YbFjProjectClue> wrapper) {
        if(DcFjConstants.CLUE_STEP_HOSP.equals(statType)) {
            return baseMapper.statisticsHospClue(wrapper);
        } else if(DcFjConstants.CLUE_STEP_CUT.equals(wrapper)) {
            return baseMapper.statisticsCutClue(wrapper);
        }
        return baseMapper.statisticsSubmitClue(wrapper);
    }

    @Override
    public StatStepClueVo statisticsStepClueByOrg(String orgId) {
        return baseMapper.statisticsCutClueByOrg(orgId, new QueryWrapper<YbFjProjectClue>());
    }

    @Override
    public void exportProjectClues(List<YbFjProjectClue> dataList, HttpServletResponse response) throws Exception {
        List<ExportColModel> headerList = this.getHeaderList(false);
        ExcelExportUtil util = ExcelExportUtil.getInstance();
        util.export(response, "线索汇总表", headerList, JSON.toJSONString(dataList));
    }

    @Override
    public void exportProjectClues(String projectOrgId, String stepType, HttpServletResponse response) throws Exception {
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("project_org_id", projectOrgId);
        if(DcFjConstants.CLUE_STEP_HOSP.equals(stepType)) {
            fileQueryWrapper.inSql("clue_id", "select clue_id from yb_fj_project_clue where hosp_audit_state is not null and project_org_id='"+projectOrgId+"'");
        } else if(DcFjConstants.CLUE_STEP_CUT.equals(stepType)) {
            fileQueryWrapper.inSql("clue_id", "select clue_id from yb_fj_project_clue where cut_audit_state is not null and project_org_id='"+projectOrgId+"'");
        }
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        fileQueryWrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        fileQueryWrapper.isNotNull("clue_id");
        List<YbFjProjectClueFile> fileList = clueFileService.list(fileQueryWrapper);

        //导出zip
        SXSSFWorkbook workbook = null;
        ZipOutputStream zos = null;
        int len = 0;
        try {
            response.setContentType("application/zip");
            response.setHeader("content-disposition", "attachment;filename=" + DateUtils.getDate("yyyyMMddHHmmss")+".zip");
            zos = new ZipOutputStream(response.getOutputStream());
            //导出线索明细
            clueFileService.downloadZip(zos, fileList);
            //导出线索汇总
            QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
            if(DcFjConstants.CLUE_STEP_HOSP.equals(stepType)) {
                wrapper.isNotNull("hosp_audit_state");
            } else if(DcFjConstants.CLUE_STEP_CUT.equals(stepType)) {
                wrapper.isNotNull("cut_audit_state");
            }
            wrapper.eq("project_org_id", projectOrgId);
            wrapper.orderByDesc("create_time").orderByAsc("seq");
            List<YbFjProjectClue> dataList = this.list(wrapper);
            List<ExportColModel> headerList = this.getHeaderList(false);
            ExcelExportUtil util = ExcelExportUtil.getInstance();
            workbook = util.exportExcelSingleHead("线索汇总表", headerList, JSON.toJSONString(dataList));
            ZipEntry ze = new ZipEntry("线索汇总表.xlsx");
            zos.putNextEntry(ze);
            workbook.write(zos);
            zos.closeEntry();
        } catch (Exception e) {
            throw e;
        } finally {
            if(zos != null){
                try{
                    zos.close();
                }catch(Exception e){
                }
            }
            if(null != workbook) {
                workbook.close();
            }
        }
    }

    @Override
    public List<ExportColModel> getHeaderList(boolean isOrg) {
        List<ExportColModel> headerList = new ArrayList<>();
        ExportColModel model = new ExportColModel();
        model.setTitle("ID");
        model.setField("clueId");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("问题类别");
        model.setField("issueType");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("问题类型");
        model.setField("issueSubtype");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("项目名称");
        model.setField("clueName");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("项目类别");
        model.setField("clueType");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("涉及数量");
        model.setField("caseAmount");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("涉及金额（元）");
        model.setField("caseFee");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("涉及医保基金（元）");
        model.setField("caseFundFee");
        headerList.add(model);
        model = new ExportColModel();
        model.setTitle("违规说明");
        model.setField("caseRemark");
        headerList.add(model);
        if(isOrg) {
            //医院端
            model.setTitle("核减数量");
            model.setField("cutAmount");
            headerList.add(model);
            model.setTitle("核减金额");
            model.setField("cutFee");
            headerList.add(model);
        }
        return headerList;
    }

    @Override
    public IPage<YbFjProjectClueCutVo> queryProjectClueByOrg(IPage<YbFjProjectClueCutVo> page, String orgId, YbFjProjectClue clue) {
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.eq("a.project_id", clue.getProjectId());
        if(StringUtils.isNotBlank(clue.getIssueType())) {
            wrapper.eq("issue_type", clue.getIssueType());
        }
        if(StringUtils.isNotBlank(clue.getIssueSubtype())) {
            wrapper.eq("issue_subtype", clue.getIssueSubtype());
        }
        if(StringUtils.isNotBlank(clue.getClueName())) {
            wrapper.eq("clue_name", clue.getClueName());
        }
        if(StringUtils.isNotBlank(clue.getClueType())) {
            wrapper.eq("clue_type", clue.getClueType());
        }
        wrapper.orderByDesc("a.create_time");
        return baseMapper.queryProjectClueByOrg(page, orgId, wrapper);
    }

    @Override
    public List<YbFjProjectClueCutVo> queryProjectClueByOrg(String orgId, YbFjProjectClue clue) {
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.eq("a.project_id", clue.getProjectId());
        if(StringUtils.isNotBlank(clue.getIssueType())) {
            wrapper.eq("issue_type", clue.getIssueType());
        }
        if(StringUtils.isNotBlank(clue.getIssueSubtype())) {
            wrapper.eq("issue_subtype", clue.getIssueSubtype());
        }
        if(StringUtils.isNotBlank(clue.getClueName())) {
            wrapper.eq("clue_name", clue.getClueName());
        }
        if(StringUtils.isNotBlank(clue.getClueType())) {
            wrapper.eq("clue_type", clue.getClueType());
        }
        wrapper.orderByDesc("a.create_time");
        return baseMapper.queryProjectClueByOrg(orgId, wrapper);
    }

    @Override
    public void exportOrgClientClues(List<YbFjProjectClueCutVo> dataList, HttpServletResponse response) throws Exception {
        List<ExportColModel> headerList = this.getHeaderList(true);
        ExcelExportUtil util = ExcelExportUtil.getInstance();
        util.export(response, "线索汇总表", headerList, JSON.toJSONString(dataList));
    }

    private void saveClueFile(YbFjProjectOrg projectOrg, MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        int index = filename.lastIndexOf(".");
        // 文件扩展名
        String extname = filename.substring(index+1);
        String newname = DateUtils.getDate("yyyyMMddHHmmssSSS")+"."+extname;
        String path = File.separator + "fj" + File.separator + DateUtils.getDate("yyyyMM");
        File folder = new File(CommonUtil.UPLOAD_PATH + path);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        String savePath = folder + File.separator + newname;
        File saveFile = new File(savePath);
        FileCopyUtils.copy(file.getBytes(), saveFile);

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjProjectClueFile clueFile = new YbFjProjectClueFile();
        clueFile.setFileId(IdUtils.uuid());
        clueFile.setProjectId(projectOrg.getProjectId());
        clueFile.setProjectOrgId(projectOrg.getProjectOrgId());
        clueFile.setStepType(DcFjConstants.CLUE_STEP_SUBMIT);
        clueFile.setStepGroup(DcFjConstants.CLUE_STEP_SUBMIT);
        clueFile.setFileType(DcFjConstants.FILE_TYPE_EXCEL);
        clueFile.setOperType(DcFjConstants.FILE_OPER_TYPE_UP);
        clueFile.setFileSrcname(filename);
        clueFile.setFileName(newname);
        clueFile.setFilePath(path + File.separator + newname);
        clueFile.setFileSize(saveFile.length());
        clueFile.setCreateTime(DateUtils.getDate());
        clueFile.setCreateUser(user.getUsername());
        clueFile.setCreateUsername(user.getRealname());
        clueFileService.save(clueFile);
    }

    @Override
    public void importClue(String projectOrgId, MultipartFile file) throws Exception {
        String xmlPath = "/templates/fj/project_clue.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        MultipartExcelImport clueExcelImport = new MultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueMapList = clueExcelImport.handle(false);
        clueExcelImport.exception();
        if(clueMapList.size()==0) {
            throw new Exception("请上传线索汇总表");
        }
        //保存线索汇总
        YbFjProjectOrg projectOrg = projectOrgService.getById(projectOrgId);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<YbFjProjectClue> clueList = new ArrayList<>();
        int seq = 1;
        for(Map<String, String> clueMap : clueMapList) {
            YbFjProjectClue clue = BeanUtil.mapToBean(clueMap, YbFjProjectClue.class, true);
            String clueId = IdUtils.uuid();
            clue.setClueId(clueId);
            clue.setProjectOrgId(projectOrgId);
            clue.setProjectId(projectOrg.getProjectId());
            clue.setCreateTime(DateUtils.getDate());
            clue.setCreateUser(user.getUsername());
            clue.setCreateUsername(user.getRealname());
            clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
            clue.setDtlState(DcFjConstants.STATE_NO);
            clue.setSeq(seq++);
            clueList.add(clue);
        }
        this.saveBatch(clueList);
        //保存线索文件
        this.saveClueFile(projectOrg, file);
    }

    @Override
    public void coverClue(String projectOrgId, MultipartFile file) throws Exception {
        //删除线索汇总
        QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        this.remove(wrapper);
        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> dtlQueryWrapper = new QueryWrapper<>();
        dtlQueryWrapper.eq("project_org_id", projectOrgId);
        clueDtlService.remove(dtlQueryWrapper);
        //删除附件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("project_org_id", projectOrgId);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
        clueFileService.deleteClueFile(fileQueryWrapper);
        importClue(projectOrgId, file);
    }

    @Override
    public void importClueDtl(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClue clue = this.getById(clueId);
        if(!DcFjConstants.CLUE_STEP_SUBMIT.equals(clue.getCurrStep())) {
            throw new Exception("线索已推送到其他环节，不允许修改！");
        }

        String xmlPath = "/templates/fj/project_clue_dtl.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        MultipartExcelImport clueDtlExcelImport = new MultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueDtlList = clueDtlExcelImport.handle(false);
        clueDtlExcelImport.exception();

        if(clueDtlList.size()==0) {
            throw new Exception("请上传线索明细表");
        }

        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        if(!DcFjConstants.STATE_YES.equals(clue.getDtlState())) {
            clue.setDtlState(DcFjConstants.STATE_YES);
        }
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setUpdateTime(DateUtils.getDate());
        clue.setUpdateUser(user.getUsername());
        clue.setUpdateUsername(user.getRealname());
        //保存线索明细
        ClueDtlTotalVo dtlTotalVo = this.saveClueDtl(clue, clueDtlList);
        int sl = clue.getDtlAmount()==null ? 0 : clue.getDtlAmount();
        BigDecimal fee = clue.getDtlWgFee()==null ? BigDecimal.ZERO : clue.getDtlWgFee();
        sl = sl + dtlTotalVo.getTotalSl();
        fee = fee.add(dtlTotalVo.getTotalWgFee());
        clue.setDtlAmount(sl);
        clue.setDtlWgFee(fee);
        //保存线索文件
        this.saveClueFile(clue, file);
        //更新线索汇总
        this.updateById(clue);
    }

    @Override
    public void coverClueDtl(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClue clue = this.getById(clueId);
        if(!DcFjConstants.CLUE_STEP_SUBMIT.equals(clue.getCurrStep())) {
            throw new Exception("线索已推送到其他环节，不允许修改！");
        }
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        String xmlPath = "/templates/fj/project_clue_dtl.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        MultipartExcelImport clueDtlExcelImport = new MultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueDtlList = clueDtlExcelImport.handle(false);
        clueDtlExcelImport.exception();

        if(clueDtlList.size()==0) {
            throw new Exception("请上传线索明细表");
        }
        //重新设置为待审核状态
        clue.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        if(!DcFjConstants.STATE_YES.equals(clue.getDtlState())) {
            clue.setDtlState(DcFjConstants.STATE_YES);
        }
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        clue.setUpdateTime(DateUtils.getDate());
        clue.setUpdateUser(user.getUsername());
        clue.setUpdateUsername(user.getRealname());

        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        clueDtlService.remove(wrapper);
        //覆盖线索明细
        ClueDtlTotalVo dtlTotalVo = this.saveClueDtl(clue, clueDtlList);
        clue.setDtlAmount(dtlTotalVo.getTotalSl());
        clue.setDtlWgFee(dtlTotalVo.getTotalWgFee());
        //删除线索文件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("clue_id", clue.getClueId());
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //保存线索文件
        this.saveClueFile(clue, file);
        //更新线索汇总
        this.updateById(clue);
    }
}
