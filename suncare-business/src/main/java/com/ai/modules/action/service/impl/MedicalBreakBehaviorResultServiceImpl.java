package com.ai.modules.action.service.impl;

import com.ai.common.utils.ExportUtils;
import com.ai.modules.action.dto.BreakBehaviorCaseExport;
import com.ai.modules.action.dto.BreakBehaviorClientExport;
import com.ai.modules.action.dto.BreakBehaviorDocExport;
import com.ai.modules.action.dto.BreakBehaviorHospExport;
import com.ai.modules.action.entity.MedicalBreakBehaviorResult;
import com.ai.modules.action.mapper.MedicalBreakBehaviorResultMapper;
import com.ai.modules.action.service.IMedicalBreakBehaviorResultService;
import com.ai.modules.engine.service.impl.EngineBehaviorServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.mapper.MedicalFormalBehaviorMapper;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Description: 不合规行为结果
 * @Author: jeecg-boot
 * @Date:   2020-02-14
 * @Version: V1.0
 */
@Service
public class MedicalBreakBehaviorResultServiceImpl extends ServiceImpl<MedicalBreakBehaviorResultMapper, MedicalBreakBehaviorResult> implements IMedicalBreakBehaviorResultService {

    @Autowired
    MedicalFormalBehaviorMapper medicalFormalBehaviorMapper;

}
