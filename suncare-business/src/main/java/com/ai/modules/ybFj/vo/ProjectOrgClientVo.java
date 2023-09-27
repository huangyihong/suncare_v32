package com.ai.modules.ybFj.vo;

import com.ai.modules.ybFj.entity.YbFjProject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @author : zhangly
 * @date : 2023/3/29 12:13
 */
@Data
public class ProjectOrgClientVo extends YbFjProject {

    /**关联yb_fj_project_org.project_org_id*/
    @Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
    private java.lang.String projectOrgId;
}
