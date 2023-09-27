package com.ai.modules.config.service;

import com.ai.modules.config.entity.StdYbDrugcatalog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 药品医保目录
 * @Author: jeecg-boot
 * @Date:   2021-04-13
 * @Version: V1.0
 */
public interface IStdYbDrugcatalogService extends IService<StdYbDrugcatalog> {
    /**
     * 编码是否重复
     * @param code
     * @param id
     * @return
     */
    boolean isExistName(String code,String fileName,String id);

    Result<?> importExcel(MultipartFile file,  LoginUser user) throws Exception;

    boolean exportExcel(List<StdYbDrugcatalog> list, OutputStream os, String suffix) throws Exception;
}
