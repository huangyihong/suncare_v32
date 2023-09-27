package com.ai.modules.config.service;

import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 各地不同医院级别报销比例
 * @Author: jeecg-boot
 * @Date:   2020-11-16
 * @Version: V1.0
 */
public interface IStdHoslevelFundpaypropService extends IService<StdHoslevelFundpayprop> {

    Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

    boolean exportExcel(List<StdHoslevelFundpayprop> list, OutputStream os, String suffix) throws Exception;

    boolean isExist(StdHoslevelFundpayprop bean);
}
