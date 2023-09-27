package com.ai.modules.ybFj.service.impl;

import com.ai.common.export.ExcelExportUtil;
import com.ai.common.export.ExportColModel;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueCutDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.entity.YbFjProjectClueCut;
import com.ai.modules.ybFj.entity.YbFjProjectClueFile;
import com.ai.modules.ybFj.entity.YbFjProjectCluePush;
import com.ai.modules.ybFj.mapper.YbFjProjectClueCutMapper;
import com.ai.modules.ybFj.service.IYbFjProjectClueCutService;
import com.ai.modules.ybFj.service.IYbFjProjectClueFileService;
import com.ai.modules.ybFj.service.IYbFjProjectCluePushService;
import com.ai.modules.ybFj.service.IYbFjProjectClueService;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description: 飞检项目线索核减
 * @Author: jeecg-boot
 * @Date:   2023-03-14
 * @Version: V1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class YbFjProjectClueCutServiceImpl extends ServiceImpl<YbFjProjectClueCutMapper, YbFjProjectClueCut> implements IYbFjProjectClueCutService {

    @Autowired
    private IYbFjProjectCluePushService cluePushService;
    @Autowired
    private IYbFjProjectClueFileService clueFileService;
    @Autowired
    @Lazy
    private IYbFjProjectClueService clueService;

    @Override
    public void saveProjectClueCut(YbFjProjectClueCutDto dto) throws Exception {
        //YbFjProjectClue clue = clueService.getById(dto.getClueId());
        YbFjProjectClueCut cut = this.getById(dto.getClueId());
        cut.setCutAmount(dto.getCutAmount());
        cut.setCutFee(dto.getCutFee());
        cut.setCutPersonCnt(dto.getCutPersonCnt());
        cut.setCutFundFee(dto.getCutFundFee());
        //设置状态-已核减
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        cut.setAuditState(DcFjConstants.CLUE_STATE_FINISH);
        cut.setAuditTime(DateUtils.getDate());
        cut.setAuditUser(user.getUsername());
        cut.setAuditUserName(user.getRealname());
        this.updateById(cut);
    }

    @Override
    public int insertCutClue(SyncClueDto dto) {
        return baseMapper.insertCutClue(dto);
    }

    @Override
    public void pushProjectClueToOrg(String clueIds) throws Exception {
        String[] ids = clueIds.split(",");
        YbFjProjectClueCut clue = this.getById(ids[0]);
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
    public IPage<YbFjProjectClueCut> queryCutClues(IPage<YbFjProjectClueCut> page, YbFjProjectClueOnsiteDto dto) throws Exception {
        return page(page, buileQueryWrapper(dto));
    }

    @Override
    public StatStepClueVo statisticsStepClue(QueryWrapper<YbFjProjectClueCut> wrapper) {
        return baseMapper.statisticsStepClue(wrapper);
    }

    @Override
    public void downloadCutClueFilesZip(String clueId, HttpServletResponse response) throws Exception {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<>();
        wrapper.eq("clue_id", clueId);
        wrapper.eq("step_type", DcFjConstants.CLUE_STEP_CUT);
        wrapper.eq("step_group", DcFjConstants.CLUE_STEP_CUT);
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        wrapper.orderByAsc("create_time");
        List<YbFjProjectClueFile> fileList = clueFileService.list(wrapper);
        clueFileService.downloadZip(response, fileList);
    }

    @Override
    public void exportProjectClues(String projectOrgId, HttpServletResponse response) throws Exception {
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("project_org_id", projectOrgId);
        fileQueryWrapper.eq("step_type", DcFjConstants.CLUE_STEP_CUT);
        fileQueryWrapper.eq("step_group", DcFjConstants.CLUE_STEP_CUT);
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
            QueryWrapper<YbFjProjectClueCut> wrapper = new QueryWrapper<>();
            wrapper.eq("project_org_id", projectOrgId);
            wrapper.orderByDesc("create_time").orderByAsc("seq");
            List<YbFjProjectClueCut> dataList = this.list(wrapper);
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

    private QueryWrapper<YbFjProjectClueCut> buileQueryWrapper(YbFjProjectClueOnsiteDto onsite) throws Exception {
        if(StringUtils.isBlank(onsite.getProjectOrgId())) {
            throw new Exception("projectOrgId参数不能为空");
        }
        QueryWrapper<YbFjProjectClueCut> wrapper = new QueryWrapper<>();
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
}
