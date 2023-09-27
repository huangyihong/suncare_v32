package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
public interface IMedicalDictService extends IService<MedicalDict> {

    String queryDictTextByKey(String code, String key, String kind);
    String queryDictTextByKey(String code, String key);
    String queryDictKeyByText(String code, String text);

    List<MedicalDictItemVO> queryByType(String type, String kind );
    List<MedicalDictItemVO> queryByType(String type);
    Map<String, String> queryNameMapByType(String type);
    Map<String, String> queryMapByType(String type);

    Map<String, List<MedicalDictItemVO>> queryByTypes(String[] types, String kind);

    IPage list(Page<MedicalDictItemVO> page, MedicalDictItem medicalDictItem, MedicalDict medicalDict);

    void saveGroup(MedicalDict medicalDict, String codes, String names);

    void updateItems(String groupCode, String codes, String names, String dels);

    void addItems(List<MedicalDictItem> itemList, String groupCode);

    void saveItems(String groupId, String[] codeArray, String[] nameArray);

    void updateGroup(MedicalDict medicalDict, String codes, String names);

    Map<String, List<MedicalDict>> queryDistinctDictByKinds(String[] kinds);

    List<MedicalDictItemVO> queryMedicalDictByGroupId(String groupId);
    List<MedicalDictItemVO> queryMedicalDictByKind(String kind);
}
