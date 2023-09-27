package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import com.ai.common.emport.MultipartExcelImport;
import com.ai.common.export.ExcelExportUtil;
import com.ai.common.export.ExportColModel;
import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueCutDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.*;
import com.ai.modules.ybFj.mapper.YbFjProjectClueOnsiteMapper;
import com.ai.modules.ybFj.service.*;
import com.ai.modules.ybFj.vo.ClueDtlTotalVo;
import com.ai.modules.ybFj.vo.StatOnsiteClueVo;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description: 飞检项目线索现场核查
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class YbFjProjectClueOnsiteServiceImpl extends ServiceImpl<YbFjProjectClueOnsiteMapper, YbFjProjectClueOnsite> implements IYbFjProjectClueOnsiteService {

    @Autowired
    private IYbFjProjectClueOnsiteDtlService clueOnsiteDtlService;
    @Autowired
    private IYbFjProjectClueFileService clueFileService;
    @Autowired
    private IYbFjProjectOrgService projectOrgService;
    @Autowired
    private IYbFjOrgService orgService;
    @Autowired
    private IMedicalDictService dictService;
    @Autowired
    private IYbFjProjectService projectService;
    @Autowired
    private IYbFjTemplateExportService templateExportService;
    @Autowired
    @Lazy
    private IYbFjProjectClueService clueService;

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
    private ClueDtlTotalVo saveClueDtl(YbFjProjectClueOnsite clue, List<Map<String, String>> clueDtlList) {
        int sl = 0;
        BigDecimal fee = BigDecimal.ZERO;
        if(clueDtlList!=null && clueDtlList.size()>0) {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<YbFjProjectClueOnsiteDtl> dtlList = new ArrayList<YbFjProjectClueOnsiteDtl>();
            for(Map<String, String> map : clueDtlList) {
                YbFjProjectClueOnsiteDtl dtl = BeanUtil.mapToBean(map, YbFjProjectClueOnsiteDtl.class, true);
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
            clueOnsiteDtlService.saveBatch(dtlList);
        }
        return new ClueDtlTotalVo(sl, fee);
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
    private void saveClueFile(YbFjProjectClueOnsite clue, MultipartFile file) throws Exception {
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
        clueFile.setStepType(DcFjConstants.CLUE_STEP_ONSITE);
        clueFile.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
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
        clueFile.setStepType(DcFjConstants.CLUE_STEP_ONSITE);
        clueFile.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
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
    public void importOnsiteClue(String projectOrgId, MultipartFile file) throws Exception {
        String xmlPath = "/templates/fj/onsite_clue.xml";
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
        List<YbFjProjectClueOnsite> clueList = new ArrayList<>();
        int seq = 1;
        for(Map<String, String> clueMap : clueMapList) {
            YbFjProjectClueOnsite clue = cn.hutool.core.bean.BeanUtil.mapToBean(clueMap, YbFjProjectClueOnsite.class, true);
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
    public void coverOnsiteClue(String projectOrgId, MultipartFile file) throws Exception {
        //删除线索汇总
        QueryWrapper<YbFjProjectClueOnsite> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        this.remove(wrapper);
        //删除线索明细
        QueryWrapper<YbFjProjectClueOnsiteDtl> Dtlwrapper = new QueryWrapper<>();
        Dtlwrapper.eq("project_org_id", projectOrgId);
        clueOnsiteDtlService.remove(Dtlwrapper);
        //删除线索文件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("project_org_id", projectOrgId);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_ONSITE);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_ONSITE);
        clueFileService.deleteClueFile(fileQueryWrapper);

        importOnsiteClue(projectOrgId, file);
    }

    @Override
    public void importOnsiteClueDtl(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClueOnsite clue = this.getById(clueId);
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
    public void coverOnsiteClueDtl(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClueOnsite clue = this.getById(clueId);
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
        QueryWrapper<YbFjProjectClueOnsiteDtl> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        clueOnsiteDtlService.remove(wrapper);
        //覆盖线索明细
        ClueDtlTotalVo dtlTotalVo = this.saveClueDtl(clue, clueDtlList);
        clue.setDtlAmount(dtlTotalVo.getTotalSl());
        clue.setDtlWgFee(dtlTotalVo.getTotalWgFee());
        //删除线索文件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("clue_id", clue.getClueId());
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_ONSITE);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_ONSITE);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //保存线索文件
        this.saveClueFile(clue, file);
        //更新线索汇总
        this.updateById(clue);
    }

    @Override
    public void appendOnsiteClue(String clueId, MultipartFile file) throws Exception {
        YbFjProjectClueOnsite clue = this.getById(clueId);

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

    @Override
    public void updateProjectClueOnsite(YbFjProjectClueOnsiteDto dto) {
        YbFjProjectClueOnsite onsite = this.getById(dto.getClueId());
        CopyOptions copyOptions = CopyOptions.create().ignoreNullValue();
        copyOptions.setIgnoreProperties("projectId", "projectOrgId");
        BeanUtil.copyProperties(dto, onsite, copyOptions);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        onsite.setUpdateTime(DateUtils.getDate());
        onsite.setUpdateUser(user.getUsername());
        onsite.setUpdateUsername(user.getRealname());
        this.updateById(onsite);
    }

    @Override
    public void updateProjectClueOnsite(List<YbFjProjectClueOnsiteDto> dtoList) {
        List<YbFjProjectClueOnsite> onsiteList = new ArrayList<>();
        for(YbFjProjectClueOnsiteDto dto : dtoList) {
            YbFjProjectClueOnsite onsite = this.getById(dto.getClueId());
            CopyOptions copyOptions = CopyOptions.create().ignoreNullValue();
            copyOptions.setIgnoreProperties("projectId", "projectOrgId");
            BeanUtil.copyProperties(dto, onsite, copyOptions);
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            onsite.setUpdateTime(DateUtils.getDate());
            onsite.setUpdateUser(user.getUsername());
            onsite.setUpdateUsername(user.getRealname());
            onsiteList.add(onsite);
        }
        this.updateBatchById(onsiteList);
    }

    @Override
    public void saveProjectClueOnsiteCut(YbFjProjectClueCutDto dto) throws Exception {
        YbFjProjectClueOnsite onsite = this.getById(dto.getClueId());
        onsite.setCutAmount(dto.getCutAmount());
        onsite.setCutPersonCnt(dto.getCutPersonCnt());
        onsite.setCutFee(dto.getCutFee());
        onsite.setCutFundFee(dto.getCutFundFee());
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        onsite.setUpdateTime(DateUtils.getDate());
        onsite.setUpdateUser(user.getUsername());
        onsite.setUpdateUsername(user.getRealname());
        this.updateById(onsite);
    }

    @Override
    public void removeProjectClueOnsite(String clueId) {
        //删除线索明细
        QueryWrapper<YbFjProjectClueOnsiteDtl> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        clueOnsiteDtlService.remove(wrapper);
        //删除附件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("clue_id", clueId);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_ONSITE);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //删除线索汇总
        this.removeById(clueId);
    }

    @Override
    public void removeProjectClueOnsites(String clueIds) {
        String[] ids = clueIds.split(",");
        //删除线索明细
        QueryWrapper<YbFjProjectClueOnsiteDtl> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", ids);
        clueOnsiteDtlService.remove(wrapper);
        //删除附件
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.in("clue_id", ids);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_ONSITE);
        clueFileService.deleteClueFile(fileQueryWrapper);
        //删除线索汇总
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public int insertOnsiteClue(SyncClueDto dto) {
        return baseMapper.insertOnsiteClue(dto);
    }

    @Override
    public void exportOnsiteClues(String projectOrgId, HttpServletResponse response) throws Exception {
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("project_org_id", projectOrgId);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_ONSITE);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_ONSITE);
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
            QueryWrapper<YbFjProjectClueOnsite> wrapper = new QueryWrapper<>();
            wrapper.eq("project_org_id", projectOrgId);
            wrapper.orderByDesc("create_time").orderByAsc("seq");
            List<YbFjProjectClueOnsite> dataList = this.list(wrapper);
            List<ExportColModel> headerList = clueService.getHeaderList(false);
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
    public void importOnsiteClues(String projectOrgId, MultipartFile file) throws Exception {
        if(StringUtils.isBlank(projectOrgId)) {
            throw new Exception("projectOrgId参数不能为空");
        }
        String xmlPath = "/templates/fj/onsite_clue_hz.xml";
        InputStream xmlInputStream = YbFjProjectClueServiceImpl.class.getResourceAsStream(xmlPath);
        long limitFlow = DcFjConstants.LIMIT_FLOW;
        MultipartExcelImport clueExcelImport = new MultipartExcelImport(file, xmlInputStream, limitFlow);
        List<Map<String, String>> clueList = clueExcelImport.handle(false);
        clueExcelImport.exception();

        YbFjProjectOrg projectOrg = projectOrgService.getById(projectOrgId);
        if(clueList!=null && clueList.size()>0) {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<YbFjProjectClueOnsite> onsiteList = new ArrayList<YbFjProjectClueOnsite>();
            for(Map<String, String> map : clueList) {
                YbFjProjectClueOnsite onsite = BeanUtil.mapToBean(map, YbFjProjectClueOnsite.class, true);
                onsite.setProjectId(projectOrg.getProjectId());
                onsite.setProjectOrgId(projectOrg.getProjectOrgId());
                onsite.setCreateTime(DateUtils.getDate());
                onsite.setCreateUser(user.getUsername());
                onsite.setCreateUsername(user.getRealname());
                onsiteList.add(onsite);
            }
            this.updateBatchById(onsiteList);
        }
    }

    private QueryWrapper<YbFjProjectClueOnsite> buileQueryWrapper(YbFjProjectClueOnsiteDto onsite) throws Exception {
        if(StringUtils.isBlank(onsite.getProjectOrgId())) {
            throw new Exception("projectOrgId参数不能为空");
        }
        QueryWrapper<YbFjProjectClueOnsite> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", onsite.getProjectOrgId());
        if(StringUtils.isNotBlank(onsite.getClueId())) {
            String clueId = onsite.getClueId();
            String[] ids = clueId.split(",");
            wrapper.in("clue_id", ids);
        }
        if(StringUtils.isNotBlank(onsite.getIssueType())) {
            wrapper.eq("issue_type", onsite.getIssueType());
        }
        if(StringUtils.isNotBlank(onsite.getIssueSubtype())) {
            wrapper.eq("issue_subtype", onsite.getIssueSubtype());
        }
        if(StringUtils.isNotBlank(onsite.getClueName())) {
            wrapper.eq("clue_name", onsite.getClueName());
        }
        if(StringUtils.isNotBlank(onsite.getClueType())) {
            wrapper.eq("clue_type", onsite.getClueType());
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    @Override
    public List<YbFjProjectClueOnsite> queryOnsiteClues(YbFjProjectClueOnsiteDto dto) throws Exception {
        return list(buileQueryWrapper(dto));
    }

    @Override
    public IPage<YbFjProjectClueOnsite> queryOnsiteClues(IPage<YbFjProjectClueOnsite> page, YbFjProjectClueOnsiteDto dto) throws Exception {
        return page(page, buileQueryWrapper(dto));
    }

    @Override
    public void uploadOnsiteFile(String projectOrgId, MultipartFile file) throws Exception {
        YbFjProjectOrg projectOrg = projectOrgService.getById(projectOrgId);
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
        clueFile.setStepType(DcFjConstants.FILE_STEP_ONSITE_UPLOAD);
        clueFile.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
        if(DcFjConstants.FILE_EXT_XLS.equalsIgnoreCase(extname)
                || DcFjConstants.FILE_EXT_XLSX.equalsIgnoreCase(extname)) {
            clueFile.setFileType(DcFjConstants.FILE_TYPE_EXCEL);
        } else if(DcFjConstants.FILE_EXT_DOC.equalsIgnoreCase(extname)
                || DcFjConstants.FILE_EXT_DOCX.equalsIgnoreCase(extname)) {
            clueFile.setFileType(DcFjConstants.FILE_TYPE_WORD);
        } else if(DcFjConstants.FILE_EXT_PDF.equalsIgnoreCase(extname)) {
            clueFile.setFileType(DcFjConstants.FILE_TYPE_PDF);
        } else {
            clueFile.setFileType(extname);
        }
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
    public void exportOnsiteFeedback(YbFjProjectClueOnsiteDto dto, HttpServletResponse response) throws Exception {
        Workbook workbook = this.exportOnsiteFeedback(dto);
        OutputStream ouputStream = null;
        try {
            String fileName = "签字反馈表";
            fileName = new String(fileName.getBytes(), "ISO8859-1");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
            ouputStream = response.getOutputStream();
            workbook.write(ouputStream);
            ouputStream.flush();
            ouputStream.close();
        } catch(Exception e) {
            log.error("", e);
        } finally {
            try {
                if(null != ouputStream) {
                    ouputStream.close();
                }
            } catch (Exception e) {}
            try {
                if(null != workbook) {
                    workbook = null;
                }
            } catch (Exception e) {}
        }
    }

    @Override
    public void downloadOnsiteClueFilesZip(String clueId, HttpServletResponse response) throws Exception {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        wrapper.eq("step_type", DcFjConstants.CLUE_STEP_ONSITE);
        wrapper.eq("step_group", DcFjConstants.CLUE_STEP_ONSITE);
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        wrapper.orderByAsc("create_time");
        List<YbFjProjectClueFile> fileList = clueFileService.list(wrapper);
        clueFileService.downloadZip(response, fileList);
    }

    private Workbook exportOnsiteFeedback(YbFjProjectClueOnsiteDto dto) throws Exception {
        List<YbFjProjectClueOnsite> dataList = this.queryOnsiteClues(dto);
        YbFjProjectOrg projectOrg = projectOrgService.getById(dto.getProjectOrgId());
        YbFjOrg org = orgService.findOrg(projectOrg.getOrgId());
        XSSFWorkbook workbook = null;
        InputStream is = null;
        try {
            String xmlPath = "/templates/fj/onsite_feedback.xlsx";
            is = this.getClass().getResourceAsStream(xmlPath);
            workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);
            for(int r=1; r<=2; r++) {
                Row titleRow = sheet.getRow(r);
                for(int c=0,len=titleRow.getPhysicalNumberOfCells(); c<len; c++) {
                    Cell cell = titleRow.getCell(c);
                    String value = cell.getStringCellValue();
                    if(StringUtils.isNotBlank(value) && value.contains("${")) {
                        value = StringUtils.replace(value, "${org_name}", org.getOrgName());
                        value = StringUtils.replace(value, "${org_address}", org.getOrgAddress());
                        value = StringUtils.replace(value, "${time}", DateUtils.getDate("yyyy-MM-dd"));
                        value = StringUtils.replace(value, "${social_code}", org.getSocialCode());
                        value = StringUtils.replace(value, "${hospgrade}", dictService.queryDictTextByKey("YYDJ", org.getHospgrade()));
                        cell.setCellValue(value);
                    }
                }
            }
            //涉及人次合计
            BigDecimal totalCaseAmount = BigDecimal.ZERO;
            //涉及金额合计
            BigDecimal totalCaseFee = BigDecimal.ZERO;
            //核减后金额合计
            BigDecimal totalFee = BigDecimal.ZERO;
            //涉及医保基金金额合计
            BigDecimal totalCaseFundFee = BigDecimal.ZERO;
            //数据起始行号
            int start = 5;
            //页脚起始行号
            int footer = 15;
            int startFooter = 15;
            int length = dataList.size();
            if(length>10) {
                //导出数据条数>10，复制页脚行
                startFooter = startFooter + length - 10;
                CellCopyPolicy.Builder builder = new CellCopyPolicy.Builder();
                CellCopyPolicy cellCopyPolicy = builder.build();
                for(int i=2; i>=0; i--) {
                    //复制行
                    sheet.copyRows(footer+i, footer+i, startFooter+i, cellCopyPolicy);
                    //移除行
                    removeRow(sheet,footer+i);
                }
            }
            CellStyle seqCellStyle = this.createBodyCellStyle(workbook, true);
            CellStyle cellStyle = this.createBodyCellStyle(workbook, false);
            for(int i=0; i<length; i++) {
                YbFjProjectClueOnsite record = dataList.get(i);
                Row row = sheet.createRow(start++);
                row.setHeight((short) 600);
                int index = 0;
                //序号
                Cell cell = row.createCell(index++);
                cell.setCellStyle(seqCellStyle);
                cell.setCellValue(i+1);
                //问题类别
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getIssueType());
                //问题类型
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getIssueSubtype());
                //项目名称
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getClueName());
                //问题情形描述
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getCaseRemark());
                //涉及人次
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getCaseAmount());
                if(record.getCaseAmount()!=null) {
                    totalCaseAmount = totalCaseAmount.add(BigDecimal.valueOf(record.getCaseAmount()));
                }
                //涉及金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                if(record.getCaseFee()!=null) {
                    cell.setCellValue(String.valueOf(record.getCaseFee()));
                    totalCaseFee = totalCaseFee.add(record.getCaseFee());
                }
                //项目类型
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getClueType());
                //核减后金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                BigDecimal fee = record.getCaseFundFee();
                if(record.getCutFee()!=null) {
                    fee = record.getCaseFundFee().subtract(record.getCutFee());
                }
                if(fee.compareTo(BigDecimal.ZERO)<0) {
                    fee = BigDecimal.ZERO;
                }
                cell.setCellValue(String.valueOf(fee));
                totalFee = totalFee.add(fee);
                //涉及医保基金金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                if(record.getCaseFundFee()!=null) {
                    cell.setCellValue(String.valueOf(record.getCaseFundFee()));
                    totalCaseFundFee = totalCaseFundFee.add(record.getCaseFundFee());
                }
                //问题认定依据
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getSureBasis());
            }
            //合计行
            Row row = sheet.getRow(startFooter);
            //涉及人次
            Cell cell = row.createCell(5);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(String.valueOf(totalCaseAmount));
            //涉及金额
            cell = row.createCell(6);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(String.valueOf(totalCaseFee));
            //核减后金额
            cell = row.createCell(8);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(String.valueOf(totalFee));
            //涉及医保基金金额
            cell = row.createCell(9);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(String.valueOf(totalCaseFundFee));
            Row footerRow = sheet.getRow(startFooter+2);
            for(int c=0,len=footerRow.getPhysicalNumberOfCells(); c<len; c++) {
                cell = footerRow.getCell(c);
                String value = cell.getStringCellValue();
                if(StringUtils.isNotBlank(value) && value.contains("${")) {
                    value = StringUtils.replace(value, "${sure_time}", DateUtils.getDate("yyyy年MM月dd日"));
                    cell.setCellValue(value);
                }
            }
        } catch(Exception e) {
            throw e;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {

            }
        }
        return workbook;
    }

    /**
     *
     * 功能描述：移除行
     * @author zhangly
     * @date 2023-03-17 13:39:33
     *
     * @param sheet
     * @param r
     *
     * @return void
     *
     */
    private void removeRow(Sheet sheet, int r) {
        Row row = sheet.getRow(r);
        //循环去除 合并单元格
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            removeMergedRegion(sheet, r, i);
        }
        sheet.removeRow(row);
    }

    private void removeMergedRegion(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();//获取所有的单元格
        boolean find = false;
        int index = 0;//用于保存要移除的那个单元格序号
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i); //获取第i个单元格
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    index = i;
                    find = true;
                    break;
                }
            }
        }
        if(find) {
            //移除合并单元格
            sheet.removeMergedRegion(index);
        }
    }

    private CellStyle createBodyCellStyle(Workbook workbook, boolean seq){
        CellStyle bodystyle = workbook.createCellStyle();
        // 设置字体
        Font font = workbook.createFont();
        // 字体大小
        font.setFontHeightInPoints((short) 10);
        bodystyle.setFont(font);
        // 左右
        if(seq) {
            bodystyle.setAlignment(HorizontalAlignment.CENTER);
        } else {
            bodystyle.setAlignment(HorizontalAlignment.LEFT);
        }
        // 上下居中
        bodystyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 自动换行
        //bodystyle.setWrapText(true);
        // 设置单元格的边框为粗体
        bodystyle.setBorderRight(BorderStyle.THIN);
        bodystyle.setBorderLeft(BorderStyle.THIN);
        bodystyle.setBorderTop(BorderStyle.THIN);
        bodystyle.setBorderBottom(BorderStyle.THIN);
        // 设置单元格的边框颜色
        bodystyle.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        bodystyle.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        bodystyle.setTopBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        bodystyle.setBottomBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        return bodystyle;
    }

    private CellStyle createBodyNumberCellStyle(Workbook workbook, boolean integer) {
        CellStyle bodystyle = this.createBodyCellStyle(workbook, false);
        if(integer) {
            bodystyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,#0"));
        } else {
            bodystyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
        }
        return bodystyle;
    }

    @Override
    public String exportOnsiteClueTemplate(YbFjProjectClueOnsiteDto dto) throws Exception {
        if(StringUtils.isBlank(dto.getProjectOrgId())) {
            throw new Exception("projectOrgId参数不能为空");
        }
        List<YbFjProjectClueOnsite> dataList = this.queryOnsiteClues(dto);
        YbFjProjectOrg projectOrg = projectOrgService.getById(dto.getProjectOrgId());
        YbFjProject project = projectService.getById(projectOrg.getProjectId());
        YbFjOrg org = orgService.findOrg(projectOrg.getOrgId());
        OutputStream os = null;
        XSSFWorkbook workbook = null;
        InputStream is = null;
        try {
            String path = CommonUtil.UPLOAD_PATH + File.separator + "fj" + File.separator + "out" + File.separator + DateUtils.getDate("yyyyMM") + File.separator + "data";
            File folder = new File(path);
            if(!folder.exists()) {
                folder.mkdirs();
            }
            String filePath = path + File.separator + DateUtils.getDate("yyyyMMddHHmmssSSS")+".xlsx";
            os = new FileOutputStream(filePath);
            String xmlPath = "/templates/fj/project_clue_out.xlsx";
            is = this.getClass().getResourceAsStream(xmlPath);
            workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);
            //数据起始行号
            int start = 13;
            int length = dataList.size();
            CellStyle cellStyle = this.createBodyCellStyle(workbook, false);
            CellStyle integerCellStyle = this.createBodyNumberCellStyle(workbook, true);
            CellStyle decimalCellStyle = this.createBodyNumberCellStyle(workbook, false);
            for(int i=0; i<length; i++) {
                YbFjProjectClueOnsite record = dataList.get(i);
                Row row = sheet.createRow(start++);
                row.setHeight((short) 600);
                int index = 1;
                //医院名称
                Cell cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org.getOrgName());
                //机构性质
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                String text = org.getOwnershipCode();
                if("01".equals(text) || "02".equals(text)) {
                    text = "公立";
                } else {
                    text = "私立";
                }
                String hosplevel = org.getHosplevel();
                if("1".equals(hosplevel)) {
                    hosplevel = "一级";
                } else if("2".equals(hosplevel)) {
                    hosplevel = "二级";
                } else if("3".equals(hosplevel)) {
                    hosplevel = "三级";
                } else {
                    hosplevel = "";
                }
                cell.setCellValue(text+hosplevel);
                //机构地址
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org.getOrgAddress());
                //统一社会信用代码
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org.getSocialCode());
                //法定代表人
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org.getLegalperson());
                //联系电话
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org.getLegalpersonPhone());
                //案卷名称
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //行动名称
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(project.getActionTitle());
                //行动依据文件
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //文书文号
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //检查内容
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //检查小组
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(project.getActionTeam());
                //调查开始时间
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(DateUtils.formatDate(project.getActionDate(), "yyyy年MM月dd日"));
                //调查结束时间
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //检查时间范围
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                text = DateUtils.formatDate(project.getCheckStartdate(), "yyyy年MM月dd日") + "至" + DateUtils.formatDate(project.getCheckEnddate(), "yyyy年MM月dd日");
                cell.setCellValue(text);
                //检查人员1
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //检查人员2
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //序号
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(i+1);
                //问题类别
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getIssueType());
                //问题类型
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getIssueSubtype());
                //项目名称
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getClueName());
                //问题情形描述
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getCaseRemark());
                //涉及人次
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                if(record.getCaseAmount()!=null) {
                    cell.setCellValue(record.getCaseAmount());
                }
                //涉及金额
                cell = row.createCell(index++);
                cell.setCellStyle(decimalCellStyle);
                if(record.getCaseFee()!=null) {
                    cell.setCellValue(record.getCaseFee().doubleValue());
                }
                //项目类型
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getClueType());
                //涉及医保基金金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                if(record.getCaseFundFee()!=null) {
                    cell.setCellValue(record.getCaseFee().doubleValue());
                }
                //问题认定依据
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(record.getSureBasis());
                //核减金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                if(record.getCutFee()!=null) {
                    cell.setCellValue(record.getCutFee().doubleValue());
                }
                //核减原因
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //核减材料
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //退回金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //处罚金额
                BigDecimal punishFee = BigDecimal.ZERO;
                if(record.getCaseFundFee()!=null) {
                    punishFee = record.getCaseFundFee();
                }
                if(record.getCutFee()!=null) {
                    punishFee = punishFee.subtract(record.getCutFee());
                }
                if(punishFee.compareTo(BigDecimal.ZERO)<0) {
                    punishFee = BigDecimal.ZERO;
                }
                cell.setCellValue(punishFee.doubleValue());
                //罚款倍数
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("1");
                //罚款金额
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                if(record.getCaseFundFee()!=null) {
                    cell.setCellValue(punishFee.doubleValue());
                }
                //违规第几条
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
                //违规第几款
                cell = row.createCell(index++);
                cell.setCellStyle(cellStyle);
            }
            workbook.write(os);
            os.flush();
            return filePath;
        } catch(Exception e) {
            throw e;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {

            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {

            }
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void outOnsiteClueTemplate(HttpServletResponse response, YbFjProjectClueOnsiteDto dto, String[] templateCodes) throws Exception {
        String outDirPath = CommonUtil.UPLOAD_PATH + "/fj/out/" + DateUtils.getDate("yyyyMM") + "/" + IdUtils.uuid();
        //导入文件的文件名（全路径）
        String inputPath = null;
        try {
            inputPath = this.exportOnsiteClueTemplate(dto);
            //导出模板
            QueryWrapper<YbFjTemplateExport> queryWrapper = new QueryWrapper<YbFjTemplateExport>();
            queryWrapper.in("TEMPLATE_CODE", templateCodes);
            queryWrapper.eq("USE_STATUS", "1");//在用
            queryWrapper.orderByAsc("python_proc_code");
            List<YbFjTemplateExport> ybFjTemplateExportList = templateExportService.list(queryWrapper);
            List<YbFjProjectClueFile> clueFileList = new ArrayList<>();
            for (YbFjTemplateExport exportBean : ybFjTemplateExportList) {
                //调用python77
                //生成文档的类型编码
                String targetType = exportBean.getPythonProcCode();
                //生成结果文件的模板格式文件（全路径）
                String templatePath = StringUtils.isNotBlank(exportBean.getTemplatePath()) ? CommonUtil.UPLOAD_PATH + "/" + exportBean.getTemplatePath() : null;

                //生成的结果文件存放的文件夹路径（全路径）
                String outputPath = outDirPath + "/" + targetType;
                File outFile = new File(outputPath);
                if(!outFile.exists()) {
                    outFile.mkdirs();
                }
                //生成结果文件的文件名（不含扩展名）
                String filenamePrefix = targetType;

                String cmd = "/home/web/python_src/gendoc.sh %s %s %s %s %s";
                cmd = String.format(cmd, targetType, inputPath, templatePath, outputPath, filenamePrefix);
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
                String dict = dictService.queryDictTextByKey("FJ_FILE_TYPE", targetType);
                if(StringUtils.isBlank(dict)) {
                    dict = targetType;
                }

                LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                YbFjProjectOrg projectOrg = projectOrgService.getById(dto.getProjectOrgId());
                for (String linsStr : lineLists) {
                    String resultFileName = linsStr.replace(outputPath + "/" + filenamePrefix, "");
                    String filePath = linsStr.replace(CommonUtil.UPLOAD_PATH,"");
                    File file = new File(linsStr);
                    int index = resultFileName.lastIndexOf(".");
                    // 文件扩展名
                    String extname = resultFileName.substring(index+1);
                    YbFjProjectClueFile clueFile = new YbFjProjectClueFile();
                    clueFile.setFileId(IdUtils.uuid());
                    clueFile.setProjectId(projectOrg.getProjectId());
                    clueFile.setProjectOrgId(projectOrg.getProjectOrgId());
                    clueFile.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
                    clueFile.setStepType(DcFjConstants.CLUE_STEP_ONSITE);
                    if(DcFjConstants.FILE_EXT_XLS.equalsIgnoreCase(extname)
                            || DcFjConstants.FILE_EXT_XLSX.equalsIgnoreCase(extname)) {
                        clueFile.setFileType(DcFjConstants.FILE_TYPE_EXCEL);
                    } else if(DcFjConstants.FILE_EXT_DOC.equalsIgnoreCase(extname)
                            || DcFjConstants.FILE_EXT_DOCX.equalsIgnoreCase(extname)) {
                        clueFile.setFileType(DcFjConstants.FILE_TYPE_WORD);
                    } else if(DcFjConstants.FILE_EXT_PDF.equalsIgnoreCase(extname)) {
                        clueFile.setFileType(DcFjConstants.FILE_TYPE_PDF);
                    } else {
                        clueFile.setFileType(extname);
                    }
                    clueFile.setOperType(DcFjConstants.FILE_OPER_TYPE_OUT);
                    extname = resultFileName.substring(index);
                    String fileSrcname = StringUtils.replace(resultFileName, extname, "");
                    fileSrcname = fileSrcname + "(" + dict + ")" + extname;
                    clueFile.setFileSrcname(fileSrcname);
                    clueFile.setFileName(fileSrcname);
                    clueFile.setFilePath(filePath);
                    clueFile.setFileSize(file.length());
                    clueFile.setCreateTime(DateUtils.getDate());
                    clueFile.setCreateUser(user.getUsername());
                    clueFile.setCreateUsername(user.getRealname());
                    clueFileList.add(clueFile);
                }
            }
            if(clueFileList.size()>0) {
                clueFileService.saveBatch(clueFileList);
                //导出zip
                clueFileService.downloadZip(response, clueFileList);
            }
        } catch (Exception e) {
            FileUtil.del(outDirPath);
            throw e;
        } finally {

        }
    }

    @Override
    public StatOnsiteClueVo statisticsOnsiteClue(String projectOrgId) {
        return baseMapper.statisticsOnsiteClue(projectOrgId);
    }

    @Override
    public StatStepClueVo statisticsStepClue(QueryWrapper<YbFjProjectClueOnsite> wrapper) {
        return baseMapper.statisticsStepClue(wrapper);
    }
}
