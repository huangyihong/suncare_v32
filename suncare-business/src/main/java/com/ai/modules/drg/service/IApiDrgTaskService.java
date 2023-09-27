package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgTask;

/**
 * @author : zhangly
 * @date : 2023/4/6 11:13
 */
public interface IApiDrgTaskService {
    /**
     *
     * 功能描述：drg目录
     * @author zhangly
     * @date 2023-04-06 11:00:19
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findDrgCatalog(String version);
    /**
     *
     * 功能描述：mdc目录
     * @author zhangly
     * @date 2023-04-06 11:00:29
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findMdcCatalog(String version);

    /**
     *
     * 功能描述：adrg目录
     * @author zhangly
     * @date 2023-04-06 11:00:39
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findAdrgCatalog(String version);

    /**
     *
     * 功能描述：adrg诊断手术目录
     * @author zhangly
     * @date 2023-04-06 11:00:50
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findAdrgListCatalog(String version);

    /**
     *
     * 功能描述：mcc目录
     * @author zhangly
     * @date 2023-04-06 11:01:06
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findMccCatalog(String version);

    /**
     *
     * 功能描述：cc目录
     * @author zhangly
     * @date 2023-04-06 11:01:15
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findCcCatalog(String version);

    /**
     *
     * 功能描述：mdc诊断目录
     * @author zhangly
     * @date 2023-04-06 11:01:23
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findMdcDiagCatalog(String version);

    /**
     *
     * 功能描述：手术目录
     * @author zhangly
     * @date 2023-04-06 11:01:43
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findSurgeryCatalog(String version);

    /**
     *
     * 功能描述：排除表目录
     * @author zhangly
     * @date 2023-04-06 11:02:03
     *
     * @param version
     *
     * @return com.ai.modules.drg.entity.DrgCatalog
     *
     */
    DrgCatalog findExcludeCatalog(String version);

    DrgTask findDrgTask(String id);

    DrgTask findDrgTaskByBatch(String batchId);

    void updateDrgTask(String batchId, DrgTask up);
}
