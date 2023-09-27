package com.ai.modules.review.dto;

import com.ai.common.utils.TimeUtil;
import com.ai.modules.engine.util.SolrUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Auther: zhangpeng
 * @Date: 2020/9/8 16
 * @Description:
 */
@Data
public class ReviewInfoDTO {
//    @ApiModelProperty(value = "违规名称")
//    private String reviewName;
    @ApiModelProperty(value = "初审判定结果")
    private String firReviewStatus;

    @ApiModelProperty(value = "初审判定结果归类")
    private String firReviewClassify;

    @ApiModelProperty(value = "初审是否推送")
    private String pushStatus;

    @ApiModelProperty(value = "初审判定理由")
    private String firReviewRemark;


    @ApiModelProperty(value = "复审判定结果")
    private String secReviewStatus;

    @ApiModelProperty(value = "复审判定结果归类")
    private String secReviewClassify;

    @ApiModelProperty(value = "复审是否推送")
    private String secPushStatus;

    @ApiModelProperty(value = "复审判定理由")
    private String secReviewRemark;

    @ApiModelProperty(value = "AI识别的黑灰白结果")
    private String predictLabel;

    @ApiModelProperty(value = "初审人ID")
    private String firReviewUserid;

    @ApiModelProperty(value = "初审人姓名")
    private String firReviewUsername;

    @ApiModelProperty(value = "初审时间")
    private String firReviewTime;

    @ApiModelProperty(value = "复审人ID")
    private String secReviewUserid;

    @ApiModelProperty(value = "复审人姓名")
    private String secReviewUsername;

    @ApiModelProperty(value = "复审时间")
    private String secReviewTime;

    @ApiModelProperty(value = "推送时间")
    private java.lang.String pushTime;

    @ApiModelProperty(value = "推送人ID")
    private java.lang.String pushUserid;

    @ApiModelProperty(value = "推送人")
    private java.lang.String pushUsername;

    @ApiModelProperty(value = "处理状态0.待处理,1.已处理")
    private java.lang.String handleStatus;
}
