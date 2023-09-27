package com.ai.modules.task.service.impl;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.task.entity.AiTask;
import com.ai.modules.task.mapper.AiTaskMapper;
import com.ai.modules.task.service.IAiTaskService;
import com.ai.modules.task.vo.AiModelResultVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI任务表
 * @Author: jeecg-boot
 * @Date:   2022-02-28
 * @Version: V1.0
 */
@Service
public class AiTaskServiceImpl extends ServiceImpl<AiTaskMapper, AiTask> implements IAiTaskService {

    @Override
    public boolean exportExcelSolr(List<AiModelResultVO> listVO, List<Map<String, Object>> listMap, OutputStream os, String suffix) throws Exception {
        String titleStr = "明细标识（id）,模型ID（batch_id）,orgname,visitid,项目编码（itemcode）,项目名称（itemname）,概率（probability）,预测结果（predict_result）,特征（feature）,原因（reason）,临床合理,临床原因";
        String[] titles = titleStr.split(",");
        String fieldStr =  "mxId,batchId,orgname,visitid,itemcode,itemname,probabilityStr,predictResult,feature,reason,handleLabel,handleReason";//导出的字段
        String[] fields = fieldStr.split(",");

        String title1Str = "模型ID（batch_id）,orgname,visitid,可疑标记,黑名单数量,黑名单占比,白名单数量,白名单占比,灰名单数量,灰名单占比";
        String[] title1s = title1Str.split(",");
        String field1Str =  "batchId,orgname,visitid,predict_result_count,blank_count,blank_count_ratio,white_count,white_count_ratio,grey_count,grey_count_ratio";//导出的字段
        String[] field1s = field1Str.split(",");

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
        for(AiModelResultVO vo:listVO){
            vo.setHandleStatusStr(1==vo.getHandleStatus()?"已审核":"未审核");
            vo.setProbabilityStr(vo.getProbability()+"");
            if (StringUtils.isNotBlank(vo.getHandleLabel())) {
                vo.setHandleLabel(reviewStatusMap.getString(vo.getHandleLabel()));
            }
        }

        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(listVO, AiModelResultVO.class, titles, fields, workbook, "明细层级");
            ExportXUtils.exportExl(listMap, title1s, field1s, workbook, "就诊层级");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("明细层级", 0);
            ExportUtils.exportExl(listVO, AiModelResultVO.class, titles, fields, sheet, "");
            WritableSheet sheet1 = wwb.createSheet("就诊层级", 1);
            ExportUtils.exportExl(listMap, title1s, field1s, sheet1, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }
}
