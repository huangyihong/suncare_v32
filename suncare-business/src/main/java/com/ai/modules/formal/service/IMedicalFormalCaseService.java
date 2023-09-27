package com.ai.modules.formal.service;

import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.formal.entity.MedicalFormalFlowRuleGrade;
import com.ai.modules.formal.vo.MedicalFormalCaseBusiVO;
import com.ai.modules.formal.vo.MedicalFormalCaseVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 风控模型正式表
 * @Author: jeecg-boot
 * @Date:   2019-11-26
 * @Version: V1.0
 */
public interface IMedicalFormalCaseService extends IService<MedicalFormalCase> {

    void addFormalCase(MedicalFormalCaseVO medicalFormalCase);

    void updateFormalCase(MedicalFormalCaseVO medicalFormalCase) throws JeecgBootException;

    void submitFormalCase(List<String> ids) throws JeecgBootException;

    void removeFormalCaseByIds(List<String> ids);

    void removeFormalCaseById(String id);

    JSONObject getFormalCaseById(String id);

    List<MedicalFormalCaseBusiVO> selectCaseBusiVOPage(IPage page,MedicalFormalCaseBusiVO voParams);

    //根据批次获取该批次下的业务组模型
    List<String> selectCaseIdByBatchId(String batchId);

    //根据业务组Id获取业务组下的模型
	List<String> selectCaseIdByBusiId(String busiId);

	//根据不合规行为Id获取不合规行为下的模型
	List<String> selectCaseIdByBehaviorId(String behaviorId);

    int importExcel(MultipartFile file) throws Exception;

    void exportCaseInfo(QueryWrapper<MedicalFormalCase> queryWrapper, OutputStream os) throws Exception;

    int importCaseInfo(MultipartFile file) throws Exception;

    void copyAdd(String[] split);

    //根据actionId同步修改actionName
    void updateActionNameByActionId(String actionId, String actionName);
}
