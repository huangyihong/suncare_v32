package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalActionDict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 不合规行为字典
 * @Author: jeecg-boot
 * @Date:   2021-03-31
 * @Version: V1.0
 */
public interface IMedicalActionDictService extends IService<MedicalActionDict> {

    boolean isExistName(MedicalActionDict medicalActionDict);
    boolean isExistCode(MedicalActionDict medicalActionDict);

    Map<String, String> getMapByNames(List<String> names);

    Map<String, String> getMapByCodes(List<String> codes);

    Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

    boolean exportExcel(List<MedicalActionDict> list, OutputStream os, String suffix) throws Exception;

    int getMaxCode() throws Exception;

    Map<String, String> queryNameMapByActionIds(Collection actionIds);

    String queryNameByActionId(String actionId);

    void updateMedicalActionDict(MedicalActionDict medicalActionDict);
}
