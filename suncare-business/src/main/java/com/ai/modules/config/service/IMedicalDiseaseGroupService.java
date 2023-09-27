package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDiseaseGroup;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 疾病组
 * @Author: jeecg-boot
 * @Date: 2020-03-03
 * @Version: V1.0
 */
public interface IMedicalDiseaseGroupService extends IService<MedicalDiseaseGroup> {

    void saveGroup(MedicalDiseaseGroup medicalDiseaseGroup, String codes, String names, String tableTypes);

    void updateGroup(MedicalDiseaseGroup medicalDiseaseGroup, String codes, String names, String tableTypes);

    void saveGroupItems(MedicalDiseaseGroup medicalDiseaseGroup, String[] codes, String[] names, String[] tableTypes);

    void removeGroupById(String id);

    void removeGroupByIds(List<String> idList);

    List<MedicalGroupVO> queryGroupItem(MedicalGroupVO bean);

    int queryGroupItemCount(MedicalGroupVO bean);

    /**
     * 导出excel
     *
     * @param list
     * @param os
     * @return
     */
    public boolean exportExcel(List<MedicalGroupVO> list, OutputStream os, String suffix) throws Exception;

    /**
     * 导入
     *
     * @param file
     * @return
     */
    public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

    /**
     * 编码是否重复
     *
     * @param groupCode
     * @param groupName
     * @param groupId
     * @return
     */
    boolean isExistName(String groupCode,String groupName, String groupId);

    List<MedicalGroupVO> queryGroupItem2(QueryWrapper<MedicalDiseaseGroup> queryWrapper);
    int queryGroupItemCount2(QueryWrapper<MedicalDiseaseGroup> queryWrapper);

}
