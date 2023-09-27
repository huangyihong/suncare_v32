package com.ai.modules.config.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.*;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.mapper.MedicalActionDictMapper;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.medical.service.IMedicalRuleConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

/**
 * @Description: 不合规行为字典
 * @Author: jeecg-boot
 * @Date:   2021-03-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class MedicalActionDictServiceImpl extends ServiceImpl<MedicalActionDictMapper, MedicalActionDict> implements IMedicalActionDictService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IMedicalRuleConfigService medicalRuleConfigService;

    @Autowired
    @Lazy
    private IMedicalFormalCaseService medicalFormalCaseService;

    @Override
    public boolean isExistName(MedicalActionDict medicalActionDict) {
        //判断名称是否已存在
        QueryWrapper<MedicalActionDict> queryWrapper = new QueryWrapper<MedicalActionDict>();
        queryWrapper.eq(DbDataEncryptUtil.decryptFunc("ACTION_NAME"), medicalActionDict.getActionName());
        if(StringUtils.isNotBlank(medicalActionDict.getId())){
            queryWrapper.ne("ID",medicalActionDict.getId());
        }
        queryWrapper.ne("STATUS","-1");
        List<MedicalActionDict> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean isExistCode(MedicalActionDict medicalActionDict) {
        //判断编码是否已存在
        QueryWrapper<MedicalActionDict> queryWrapper = new QueryWrapper<MedicalActionDict>();
        queryWrapper.eq("ACTION_ID", medicalActionDict.getActionId());
        if(StringUtils.isNotBlank(medicalActionDict.getId())){
            queryWrapper.ne("ID",medicalActionDict.getId());
        }
        queryWrapper.ne("STATUS","-1");
        List<MedicalActionDict> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }

    @Override
    public Map<String, String> getMapByNames(List<String> names) {
        if(names.size() == 0){
            return new HashMap<>();
        }
        QueryWrapper<MedicalActionDict> queryWrapper = new QueryWrapper<MedicalActionDict>();
        queryWrapper.in(DbDataEncryptUtil.decryptFunc("ACTION_NAME"), names);
        queryWrapper.ne("STATUS","-1");
        List<MedicalActionDict> list = this.baseMapper.selectList(queryWrapper);
        Map<String, String> map = new HashMap<>();
        for(MedicalActionDict bean: list){
            map.put(bean.getActionName(), bean.getActionId());
        }
        return map;
    }

    @Override
    public Map<String, String> getMapByCodes(List<String> codes) {
        if(codes.size() == 0){
            return new HashMap<>();
        }
        QueryWrapper<MedicalActionDict> queryWrapper = new QueryWrapper<MedicalActionDict>();
        queryWrapper.in("ACTION_ID", codes);
        queryWrapper.ne("STATUS","-1");
        List<MedicalActionDict> list = this.baseMapper.selectList(queryWrapper);
        Map<String, String> map = new HashMap<>();
        for(MedicalActionDict bean: list){
            map.put(bean.getActionId(), bean.getActionName());
        }
        return map;
    }

    private MedicalActionDict getByName(String name) {
        QueryWrapper<MedicalActionDict> queryWrapper = new QueryWrapper<MedicalActionDict>();
        queryWrapper.eq(DbDataEncryptUtil.decryptFunc("ACTION_NAME"), name);
        queryWrapper.ne("STATUS","-1");
        List<MedicalActionDict> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    private MedicalActionDict getByCode(String code) {
        QueryWrapper<MedicalActionDict> queryWrapper = new QueryWrapper<MedicalActionDict>();
        queryWrapper.eq("ACTION_ID", code);
        queryWrapper.ne("STATUS","-1");
        List<MedicalActionDict> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
    }


    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "actionId,actionName,actionDesc,auditStandard,ruleLevel,calculate,policyBasisCode,policyBasis,information,method,difficultyLevel,remark";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importExcel(file, user,mappingFields);
    }

    @Override
    public boolean exportExcel(List<MedicalActionDict> list, OutputStream os, String suffix) throws Exception {
        String titleStr = "不合规行为编码,不合规行为名称（必填）,不合规行为释义,人工审核标准,规则级别（必填）,计算逻辑,规则/模型类别,政策依据编码,政策依据名称,现场查处需要资料,现场稽查查处方法,落地难易度,创建人,创建时间,修改人,修改时间,备注,状态";
        String[] titles = titleStr.split(",");
        String fieldStr = "actionId,actionName,actionDesc,auditStandard,ruleLevel,calculate,rules,policyBasisCode,policyBasis,information,method,difficultyLevel,createStaffName,createTime,updateStaffName,updateTime,remark,status";//导出的字段
        String[] fields = fieldStr.split(",");
        Map<String,String> statusDict = new HashMap<>();
        statusDict.put("1","已启用");
        statusDict.put("0","已禁用");
        statusDict.put("-1","删除");
        for(MedicalActionDict bean:list){
            if(StringUtils.isNotBlank(bean.getStatus())&&statusDict.get(bean.getStatus())!=null){
                bean.setStatus(statusDict.get(bean.getStatus()));
            }
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(list, MedicalActionDict.class, titles, fields, workbook, "不合规行为");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("不合规行为", 0);
            ExportUtils.exportExl(list, MedicalActionDict.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }

    @Override
    public int getMaxCode() throws Exception{
//        String sql = " select nvl(max(to_number(replace(t.action_id,'bhgxw-',''))),0) from  medical_action_dict t where t.action_id like 'bhgxw-%'";
        String sql = " select ifnull(max(cast((replace(t.action_id,'bhgxw-','')) as SIGNED)),0)  from  medical_action_dict t where t.action_id like 'bhgxw-%'";
        return jdbcTemplate.queryForObject(sql,Integer.class);
    }

    @Override
    public Map<String, String> queryNameMapByActionIds(Collection actionIds) {
        if(cacheTime == null || Duration.between(cacheTime, LocalTime.now()).toMinutes() > (MedicalConstant.EXPIRE_DICT_TIME / 60) ){
            List<MedicalActionDict> list = this.getBaseList();
            cacheMap = new HashMap<>();
            list.forEach(r -> cacheMap.put(r.getActionId(), r.getActionName()));
        }
        if(cacheMap != null) {
            return cacheMap;
        }
        // 还未初始化Map那就单独请求
        log.info("actionIds:" + Arrays.toString(actionIds.toArray(new Object[0])));
        Map<String, String> map = new HashMap<>();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("codes", StringUtils.join(actionIds, ","));
        List<MedicalActionDict> list = ApiTokenUtil.getArray("/config/medicalActionDict/queryByCodes", paramMap, MedicalActionDict.class);
        list.forEach(r -> map.put(r.getActionId(), r.getActionName()));
        return map;
    }

    @Override
    public String queryNameByActionId(String actionId) {
        if(cacheTime == null || Duration.between(cacheTime, LocalTime.now()).toMinutes() > (MedicalConstant.EXPIRE_DICT_TIME / 60) ){
            List<MedicalActionDict> list = this.getBaseList();
            cacheMap = new HashMap<>();
            list.forEach(r -> cacheMap.put(r.getActionId(), r.getActionName()));
        }
        if(cacheMap != null){
            return cacheMap.get(actionId);
        }
        // 还未初始化Map那就单独请求
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("codes", actionId);
        List<MedicalActionDict> list = ApiTokenUtil.getArray("/config/medicalActionDict/queryByCodes", paramMap, MedicalActionDict.class);
        return list.size() == 0? null: list.get(0).getActionName();
    }

    private static LocalTime cacheTime;
    private static Map<String, String> cacheMap;

    public List<MedicalActionDict> getBaseList() {
        cacheTime = LocalTime.now();
        List<MedicalActionDict> list = ApiTokenUtil.getArray("/config/medicalActionDict/listBase", new HashMap<>(), MedicalActionDict.class);
        return list;
    }

    private Result<?> importExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalActionDict> list = new ArrayList<>();
        List<MedicalActionDict> listUpdateName = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalActionDict.class, mappingFields, 0, 2, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalActionDict.class, mappingFields, 0, 2, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        System.out.println("校验开始：" + DateUtils.now());
        //字典值检验
        String[] ruleLevelArr = new String[]{"项目层级", "就诊层级", "明细层级", "统计层级"};
        List<String> ruleLevelList = Arrays.asList(ruleLevelArr);
        String[] difficultyLevelArr = new String[]{"容易", "中等", "困难"};
        List<String> difficultyLevelList = Arrays.asList(difficultyLevelArr);
        Set<String> nameSet = new HashSet<String>();
        int codeMax = this.getMaxCode();
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalActionDict bean = list.get(i);
            if (StringUtils.isBlank(bean.getActionName())) {
                message += "导入的数据中“不合规行为名称”不能为空，如：第" + (i + 3) + "行数据“不合规行为名称”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getRuleLevel())) {
                message += "导入的数据中“规则级别”不能为空，如：第" + (i + 3) + "行数据“规则级别”为空\n";
                flag = false;
            } else if (!ruleLevelList.contains(bean.getRuleLevel())) {
                message += "导入的数据中“规则级别”值不正确，如：第" + (i + 3) + "行数据“规则级别”值不在【项目层级】【就诊层级】【明细层级】【统计层级】中\n";
                flag = false;
            }
            if (StringUtils.isNotBlank(bean.getDifficultyLevel()) & !difficultyLevelList.contains(bean.getDifficultyLevel())) {
                message += "导入的数据中“落地难易度”值不正确，如：第" + (i + 3) + "行数据“落地难易度”值不在【容易】【中等】【困难】中\n";
                flag = false;
            }
            if (nameSet.contains(bean.getActionName())) {
                message += "导入的数据中“不合规行为名称”不能重复，如：第" + (i + 3) + "行数据在excel中重复\n";
                flag = false;
            }
            if (!flag) {
                continue;
            }
            //根据不合规行为编码判断记录是否存在
            MedicalActionDict oldBean = null;
            if(StringUtils.isNotBlank(bean.getActionId())){
                oldBean = this.getByCode(bean.getActionId());
                if (oldBean == null) {
                    //修改
                    message += "导入的数据中“不合规行为编码”不存在，如：第" + (i + 3) + "行数据在系统中不存在\n";
                    flag = false;
                }else{
                    //修改
                    bean.setId(oldBean.getId());
                    bean.setUpdateStaff(user.getId());
                    bean.setUpdateStaffName(user.getRealname());
                    bean.setUpdateTime(new Date());
                    //判断名称是否有更改
                    if(!oldBean.getActionName().equals(bean.getActionName())){
                        listUpdateName.add(bean);
                    }
                }
            }else{
                //新增
                bean.setActionId("bhgxw-"+String.format("%04d", ++codeMax));
                bean.setStatus("1");
                bean.setCreateStaff(user.getId());
                bean.setCreateStaffName(user.getRealname());
                bean.setCreateTime(new Date());
            }
            if (!flag) {
                continue;
            }
            if(this.isExistName(bean)){
                message += "导入的数据中“不合规行为名称”重复，如：第" + (i + 3) + "行数据在系统中已存在\n";
                flag = false;
            }
            if (!flag) {
                continue;
            }
            nameSet.add(bean.getActionName());
        }
        if (StringUtils.isNotBlank(message)) {
            message += "请核对数据后进行导入。";
            return Result.error(message);
        } else {
            System.out.println("开始插入时间：" + DateUtils.now());//删除表
            //批量新增修改
            if (list.size() > 0) {
                this.saveOrUpdateBatch(list, 1000);//直接插入
            }
            //同步修改名称
            if(listUpdateName.size()>0){
                listUpdateName.forEach(bean->{
                    this.medicalRuleConfigService.updateActionNameByActionId(bean.getActionId(),bean.getActionName());
                    this.medicalFormalCaseService.updateActionNameByActionId(bean.getActionId(),bean.getActionName());
                });
            }
            System.out.println("结束导入时间：" + DateUtils.now());
            message += "导入成功，共导入" + list.size() + "条数据。";
            return Result.ok(message,list.size());
        }
    }

    @Override
    @Transactional
    public void updateMedicalActionDict(MedicalActionDict medicalActionDict){
        this.baseMapper.updateById(medicalActionDict);
        this.medicalRuleConfigService.updateActionNameByActionId(medicalActionDict.getActionId(),medicalActionDict.getActionName());
        this.medicalFormalCaseService.updateActionNameByActionId(medicalActionDict.getActionId(),medicalActionDict.getActionName());
    }

}
