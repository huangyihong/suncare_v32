package com.ai.modules.config.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.mapper.MedicalDictMapper;
import com.ai.modules.config.service.IMedicalDictClearService;
import com.ai.modules.config.service.IMedicalDictItemService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.mapper.MedicalFormalCaseMapper;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.service.ITaskActionFieldConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date: 2020-01-16
 * @Version: V1.0
 */
@Service
@Slf4j
public class MedicalDictServiceImpl extends ServiceImpl<MedicalDictMapper, MedicalDict> implements IMedicalDictService {

    @Autowired
    IMedicalDictItemService medicalDictItemService;

    @Autowired
    IMedicalDictClearService medicalDictClearService;

    @Autowired
    ITaskActionFieldConfigService taskActionFieldConfigService;

    @Autowired
    MedicalFormalCaseMapper medicalFormalCaseMapper;

    @Override
    @Cacheable(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE, key = "#code+':'+#key+':'+#kind")
    public String queryDictTextByKey(String code, String key, String kind) {
//        log.info("数据库查询医疗字典" +code +"-" +key);
        return this.baseMapper.queryDictTextByKey(code, key, kind);
    }

    @Override
    @Cacheable(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE, key = "#code+':'+#key+':99'")
    public String queryDictTextByKey(String code, String key) {
        if (key == null || "".equals(key)) {
            return "";
        }
//        log.info("获取医疗字典" + code + "-" + key);
        return this.baseMapper.queryDictTextByKey(code, key, MedicalConstant.DICT_KIND_COMMON);
    }

    @Override
    @Cacheable(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE, key = "#code+'::'+#text+':99'")
    public String queryDictKeyByText(String code, String text) {
        if (text == null || "".equals(text)) {
            return "";
        }
        return this.baseMapper.queryDictKeyByText(code, text, MedicalConstant.DICT_KIND_COMMON);
    }

    @Override
    @Cacheable(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE, key = "#code+'::'+#kind")
    public List<MedicalDictItemVO> queryByType(String code, String kind) {
        return this.baseMapper.queryDict(code, kind);
    }

    @Override
    @Cacheable(value = CacheConstant.MEDICAL_DICT_TABLE_CACHE, key = "#code+'::99'")
    public List<MedicalDictItemVO> queryByType(String code) {
        return this.baseMapper.queryDict(code, MedicalConstant.DICT_KIND_COMMON);
    }

    @Override
    public Map<String, String> queryNameMapByType(String type) {
        Map<String, String> map = new HashMap<>();
        List<MedicalDictItemVO> relaDictList = this.queryByType(type);
        for(MedicalDictItemVO bean: relaDictList){
            map.put(bean.getValue(), bean.getCode());
        }
        return map;
    }

    @Override
    public Map<String, String> queryMapByType(String type) {
        Map<String, String> map = new HashMap<>();
        List<MedicalDictItemVO> relaDictList = this.queryByType(type);
        for(MedicalDictItemVO bean: relaDictList){
            map.put(bean.getCode(), bean.getValue());
        }
        return map;
    }

    @Override
    public Map<String, List<MedicalDictItemVO>> queryByTypes(String[] codes, String kind) {
        Map<String, List<MedicalDictItemVO>> map = new HashMap<>();
        List<MedicalDictItemVO> dataList = this.baseMapper.queryDictByCodes(codes, kind);
        for (String code : codes) {
            map.put(code, new ArrayList<>());
        }
        for (MedicalDictItemVO dict : dataList) {
            map.get(dict.getGroupCode()).add(dict);
        }
        return map;
    }

    @Override
    public IPage list(Page<MedicalDictItemVO> page, MedicalDictItem medicalDictItem, MedicalDict medicalDict) {
        return this.baseMapper.queryItemsByGroup(page, medicalDictItem, medicalDict);
    }

    @Override
    @Transactional
    public void saveGroup(MedicalDict medicalDict, String codes, String names) {
        this.save(medicalDict);
        // 插入子项
        if (StringUtils.isNotBlank(codes)) {
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            this.saveItems(medicalDict.getGroupId(), codeArray, nameArray);
        }

    }

    @Override
    public void updateItems(String groupCode, String codes, String names, String dels) {
        String groupKind = MedicalConstant.DICT_KIND_COMMON;
        MedicalDict medicalDict = this.baseMapper.selectOne(new QueryWrapper<MedicalDict>()
                .eq("GROUP_CODE", groupCode)
                .eq("KIND", groupKind)
        );
        String groupId = medicalDict.getGroupId();
        // 获取旧的字典子项
        List<MedicalDictItemVO> list = this.baseMapper.queryDict(groupCode, groupKind);

        Map<String, Double> map = new HashMap<>();
        // key -> bean
        Map<String, MedicalDictItemVO> itemKeyMap = new HashMap<>();
        // 被更新的  oldVal -> newVal
        Map<String, String> updatedValMap = new HashMap<>();
        for (MedicalDictItemVO bean : list) {
            map.put(bean.getCode() + "::" + bean.getValue(), Double.parseDouble(String.valueOf(bean.getIsOrder())));
            itemKeyMap.put(bean.getCode(), bean);
        }
        // 删除
        if (StringUtils.isNotBlank(dels)) {
            String[] delArray = dels.split(",");
            for (String key : delArray) {
                map.remove(key);
            }
        }
        if (StringUtils.isNotBlank(codes)) {
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            // 设置排序，新增靠后，旧的靠前 （可能在这之前被其他人修改过）
            for (int i = 0, len = codeArray.length; i < len; i++) {
                String key = codeArray[i] + "::" + nameArray[i];
                Double orderNo = map.get(key);
                if (orderNo == null) {
                    map.put(key, i + 0.1);
                } else {
                    map.put(key, i - 0.1);
                }
                MedicalDictItemVO bean = itemKeyMap.get(codeArray[i]);
                if(bean != null && !bean.getValue().equals(nameArray[i])){
                    updatedValMap.put(bean.getValue(), nameArray[i]);
                }
            }
        }
        AtomicInteger isOrder = new AtomicInteger(0);
        List<MedicalDictItem> dictItems = map.entrySet().stream().sorted((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .map(entry -> {
                    String[] array = entry.getKey().split("::");
                    MedicalDictItem dictItem = new MedicalDictItem();
                    dictItem.setGroupId(groupId);
                    dictItem.setItemId(IdUtils.uuid());
                    dictItem.setIsOrder((long) isOrder.getAndIncrement());
                    dictItem.setCode(array[0]);
                    dictItem.setValue(array[1]);
                    return dictItem;
                }).collect(Collectors.toList());
        // 删除旧的字典子项
        this.medicalDictItemService.remove(new QueryWrapper<MedicalDictItem>()
                .eq("GROUP_ID", groupId));
        // 插入子项
        if (dictItems.size() > 0) {
            this.medicalDictItemService.saveBatch(dictItems);
        }
        // 清除缓存
        for (MedicalDictItemVO bean : list) {
            map.put(bean.getCode() + "::" + bean.getValue(), Double.parseDouble(String.valueOf(bean.getIsOrder())));
            this.medicalDictClearService.clearCache(groupCode, bean.getCode(), groupKind);
            this.medicalDictClearService.clearTextCache(groupCode, bean.getValue(), groupKind);
        }
        this.medicalDictClearService.clearCache(groupCode, groupKind);

        // 更新不合规行为配置名称
        if("ACTION_LIST".equals(groupCode) && updatedValMap.size() > 0){
            List<TaskActionFieldConfig> fieldConfigList = taskActionFieldConfigService.list(new QueryWrapper<TaskActionFieldConfig>()
                    .in("ACTION_NAME", updatedValMap.keySet())
                    .select("ID", "ACTION_NAME")
            );
            if(fieldConfigList.size() > 0){
                for(TaskActionFieldConfig config: fieldConfigList){
                    config.setActionName(updatedValMap.get(config.getActionName()));
                }
                taskActionFieldConfigService.updateBatchById(fieldConfigList);
            }

            List<MedicalFormalCase> formalCaseList = medicalFormalCaseMapper.selectList(new QueryWrapper<MedicalFormalCase>()
                    .in("ACTION_NAME", updatedValMap.keySet())
                    .select("CASE_ID", "ACTION_NAME")
            );
            if(formalCaseList.size() > 0){
                for(MedicalFormalCase config: formalCaseList){
                    config.setActionName(updatedValMap.get(config.getActionName()));
                    medicalFormalCaseMapper.updateById(config);
                }
            }

        }
    }

    @Override
    public void addItems(List<MedicalDictItem> itemList, String groupCode){
        this.medicalDictItemService.saveBatch(itemList);
        this.medicalDictClearService.clearCache(groupCode, MedicalConstant.DICT_KIND_COMMON);

    }

    @Override
    public void saveItems(String groupId, String[] codeArray, String[] nameArray) {
        // 插入子项

        List<MedicalDictItem> itemList = new ArrayList<>();
        for (int i = 0, len = codeArray.length; i < len; i++) {
            MedicalDictItem dictItem = new MedicalDictItem();
            dictItem.setGroupId(groupId);
            dictItem.setItemId(IdUtils.uuid());
            dictItem.setIsOrder((long) i);
            dictItem.setCode(codeArray[i]);
            dictItem.setValue(nameArray[i]);
            itemList.add(dictItem);
        }
        this.medicalDictItemService.saveBatch(itemList);

    }

    @Override
    @Transactional
    public void updateGroup(MedicalDict medicalDict, String codes, String names) {
        String groupId = medicalDict.getGroupId();
        // 获取旧的字典
        MedicalDict oldDict = this.getById(groupId);
        QueryWrapper<MedicalDictItem> queryWrapper = new QueryWrapper<MedicalDictItem>()
                .eq("GROUP_ID", groupId);
        // 获取旧的字典子项
        List<MedicalDictItem> list = this.medicalDictItemService.list(queryWrapper);
        // 更新字典
        this.updateById(medicalDict);
        // 删除子项
        this.medicalDictItemService.remove(queryWrapper);
        // 插入子项
        if (StringUtils.isNotBlank(codes)) {
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            this.saveItems(medicalDict.getGroupId(), codeArray, nameArray);

        }
        String groupCode = oldDict.getGroupCode();
        String groupKind = oldDict.getKind();
        // 清除旧的子项缓存
        for(MedicalDictItem bean :list){
            this.medicalDictClearService.clearCache(groupCode, bean.getCode(), groupKind);
            this.medicalDictClearService.clearTextCache(groupCode, bean.getValue(), groupKind);
        }
        this.medicalDictClearService.clearCache(groupCode, groupKind);

    }

    @Override
    public Map<String, List<MedicalDict>> queryDistinctDictByKinds(String[] kinds) {
        Map<String, List<MedicalDict>> map = new HashMap<>();
        List<MedicalDict> list = this.baseMapper.queryDistinctDictByKinds(kinds);
        String kind = "";
        List<MedicalDict> tempList = new ArrayList<>();
        for (MedicalDict dict : list) {
            if (kind.equals(dict.getKind())) {
                tempList.add(dict);
            } else {
                kind = dict.getKind();
                tempList = new ArrayList<>();
                tempList.add(dict);
                map.put(kind, tempList);
            }

        }
        return map;
    }

    @Override
    public List<MedicalDictItemVO> queryMedicalDictByGroupId(String groupId) {
        return baseMapper.queryMedicalDictByGroupId(groupId);
    }

    @Override
    public List<MedicalDictItemVO> queryMedicalDictByKind(String kind) {
        return baseMapper.queryMedicalDictByKind(kind);
    }

}
