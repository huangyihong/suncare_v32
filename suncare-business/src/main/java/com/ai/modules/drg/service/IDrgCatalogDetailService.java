package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgCatalogDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Description: DRG分组目录数据详细表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
public interface IDrgCatalogDetailService extends IService<DrgCatalogDetail> {
    Result<?> delete(String id);

    Result<?> deleteBatch(String ids);

    Result<?> deleteByCatalogId(String catalogId);

    Result<?> importExcel(MultipartFile file, LoginUser user, String catalogType, String catalogId, Map<String,Map<String,String>> dictMap) throws Exception;


    void saveBean(DrgCatalogDetail drgCatalogDetail);

    void updateBean(DrgCatalogDetail drgCatalogDetail);
}
