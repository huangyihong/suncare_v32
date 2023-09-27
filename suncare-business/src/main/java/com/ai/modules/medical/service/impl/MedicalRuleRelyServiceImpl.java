package com.ai.modules.medical.service.impl;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.medical.entity.MedicalRuleRely;
import com.ai.modules.medical.mapper.MedicalRuleRelyMapper;
import com.ai.modules.medical.service.IMedicalRuleRelyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 规则依赖字段表
 * @Author: jeecg-boot
 * @Date:   2022-01-20
 * @Version: V1.0
 */
@Service
public class MedicalRuleRelyServiceImpl extends ServiceImpl<MedicalRuleRelyMapper, MedicalRuleRely> implements IMedicalRuleRelyService {

    @Override
    public boolean exportExcel(QueryWrapper<MedicalRuleRely> queryWrapper, OutputStream os, String suffix) throws Exception {
        List<MedicalRuleRely> list = this.list(queryWrapper);
        String titleStr = "规则ID,不合规行为类型,不合规行为,规则名称,准入字段名称,判断字段名称,最后更新时间";//导出的字段
        String[] titles = titleStr.split(",");
        String fieldStr = "ruleId,ruleType,actionName,ruleName,accessColumn,judgeColumn,createTime";//导出的字段
        String[] fields = fieldStr.split(",");
        Map<String,String> ruletypeMap = new HashMap<String, String>();
        ruletypeMap.put("CASE", "模型");
        ruletypeMap.put("DRUG", "药品合规");
        ruletypeMap.put("CHARGE", "收费合规");
        ruletypeMap.put("TREAT", "合理诊疗");
        ruletypeMap.put("DRUGUSE", "合理用药");
        for(MedicalRuleRely exportBean:list){
            if(StringUtils.isNotBlank(exportBean.getRuleType())){
                exportBean.setRuleType(ruletypeMap.get(exportBean.getRuleType().toUpperCase()));
            }
        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(list, MedicalRuleRely.class, titles, fields, workbook, "规则依赖字段信息");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("规则依赖字段信息", 0);
            ExportUtils.exportExl(list, MedicalRuleRely.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }
}
