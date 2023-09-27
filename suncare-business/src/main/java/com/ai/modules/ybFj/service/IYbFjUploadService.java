package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.entity.YbFjTemplateExport;
import com.ai.modules.ybFj.entity.YbFjUpload;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 飞检项目上传文件
 * @Author: jeecg-boot
 * @Date:   2023-02-06
 * @Version: V1.0
 */
public interface IYbFjUploadService extends IService<YbFjUpload> {
    void add(YbFjUpload ybFjUpload);

    void edit(YbFjUpload ybFjUpload);

    void delete(String id);

    void deleteBatch(String ids);

    String downloadZip(String ids,String templateCode,String resultIds) throws Exception;
}
