package com.ai.modules.review.mapper;


import com.ai.modules.review.entity.NewV3Tmp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Description: 医保风控数据推送表
 * @Author: jeecg-boot
 * @Date:   2020-09-07
 * @Version: V1.0
 */
public interface NewV3TmpMapper extends BaseMapper<NewV3Tmp> {

    @Select("<script>" +
            "{call news_ctl_to_tmp_pro2(#{fileName}, #{createBy}, #{taskBatchId})}" +
            "</script>")
    public void newsCtlToTmpPro(@Param("fileName") String fileName, @Param("createBy") String createBy,@Param("taskBatchId") String taskBatchId);
}
