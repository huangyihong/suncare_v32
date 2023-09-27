package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.entity.YbFjTemplateExport;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description: 文件模板信息
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
public interface IYbFjTemplateExportService extends IService<YbFjTemplateExport> {

    void add(YbFjTemplateExport ybFjTemplate);

    void edit(YbFjTemplateExport ybFjTemplate);

    void delete(String id);

    void deleteBatch(String ids);

    String upload(MultipartFile mf, String bizPath) throws IOException;
}
