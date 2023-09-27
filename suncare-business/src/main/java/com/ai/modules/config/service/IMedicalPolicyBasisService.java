package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalPolicyBasis;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 政策法规
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
public interface IMedicalPolicyBasisService extends IService<MedicalPolicyBasis> {
    /**
     * 名称是否重复
     * @param name
     * @param id
     * @return
     */
    boolean isExistName(String name,String id);

    void deleteById(String id);

    void deleteByIds(List<String> idList);

    Result<?> importExcel(MultipartFile file,  LoginUser user) throws Exception;

    boolean exportExcel(List<MedicalPolicyBasis> list, OutputStream os, String suffix) throws Exception;

    /**
     * 根据名称获取bean对象
     * @param name
     * @return
     */
    public MedicalPolicyBasis getBeanByName(String name);
}
