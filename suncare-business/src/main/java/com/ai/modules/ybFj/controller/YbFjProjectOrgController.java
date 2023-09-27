package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.entity.YbFjProjectOrg;
import com.ai.modules.ybFj.service.IYbFjProjectOrgService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

 /**
 * @Description: 飞检项目关联机构信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/fj/projectOrg")
public class YbFjProjectOrgController extends JeecgController<YbFjProjectOrg, IYbFjProjectOrgService> {

  @Autowired
  private IYbFjProjectOrgService ybFjProjectOrgService;
  /**
   * 通过id查询
   *
   * @param id
   * @return
   */
  @AutoLog(value = "飞检项目关联机构信息-通过id查询")
  @ApiOperation(value="飞检项目关联机构信息-通过id查询", notes="飞检项目关联机构信息-通过id查询")
  @GetMapping(value = "/queryById")
  public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
   YbFjProjectOrg bean = ybFjProjectOrgService.getById(id);
   return Result.ok(bean);
  }
}
