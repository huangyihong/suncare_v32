package com.ai.modules.config.mapper;

import java.util.List;

import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.config.entity.MedicalDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
public interface MedicalDictMapper extends BaseMapper<MedicalDict> {
    String queryDictTextByKey(@Param("code") String code,@Param("key") String key, @Param("kind") String kind);
    String queryDictKeyByText(@Param("code") String code,@Param("text") String text, @Param("kind") String kind);
    List<MedicalDictItemVO> queryDict(@Param("code") String code, @Param("kind") String kind);
    List<MedicalDictItemVO> queryDictByCodes(@Param("codes") String[] codes, @Param("kind") String kind);
    IPage<MedicalDictItemVO> queryItemsByGroup(Page<MedicalDictItemVO> page, @Param("del") MedicalDictItem dictItem, @Param("group") MedicalDict medicalDict);

    List<MedicalDict> queryDistinctDictByKinds(String[] kinds);

    List<MedicalDictItemVO> queryMedicalDictByGroupId(String groupId);
    List<MedicalDictItemVO> queryMedicalDictByKind(String kind);
}
