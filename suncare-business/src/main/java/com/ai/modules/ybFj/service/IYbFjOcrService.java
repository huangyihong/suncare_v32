package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.entity.YbFjOcr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: OCR识别工具
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
public interface IYbFjOcrService extends IService<YbFjOcr> {

    void add(YbFjOcr ybFjOcr);

    void edit(YbFjOcr ybFjOcr);

    String downloadZip(String ids) throws Exception;

    void delByIds(List<String> ids);
}
