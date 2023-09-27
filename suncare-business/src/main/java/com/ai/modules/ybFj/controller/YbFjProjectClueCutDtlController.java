package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueCutDtl;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsiteDtl;
import com.ai.modules.ybFj.service.IYbFjProjectClueCutDtlService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
* @Description: 飞检项目终审确认线索明细
* @Author: jeecg-boot
* @Date:   2023-06-13
* @Version: V1.0
*/
@Slf4j
@Api(tags="飞检项目终审确认线索明细")
@RestController
@RequestMapping("/fj/clue/cut/dtl")
public class YbFjProjectClueCutDtlController extends JeecgController<YbFjProjectClueCutDtl, IYbFjProjectClueCutDtlService> {

    @Autowired
    private IYbFjProjectClueCutDtlService ybFjProjectClueCutDtlService;

    /**
     * 分页列表查询
     *
     * @param dto
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "飞检项目终审确认线索明细-分页列表查询")
    @ApiOperation(value="飞检项目终审确认线索明细-分页列表查询", notes="飞检项目终审确认线索明细-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<YbFjProjectClueCutDtl>> queryPageList(QryProjectClueDtlDto dto,
                                                                 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                                 HttpServletRequest req) throws Exception {
        //YbFjProjectClueDtl ybFjProjectClueDtl = BeanUtil.toBean(dto, YbFjProjectClueDtl.class);
        //QueryWrapper<YbFjProjectClueDtl> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProjectClueDtl, req.getParameterMap());
        Page<YbFjProjectClueCutDtl> page = new Page<YbFjProjectClueCutDtl>(pageNo, pageSize);
        IPage<YbFjProjectClueCutDtl> pageList = ybFjProjectClueCutDtlService.queryProjectClueDtl(page, dto);
        Result<IPage<YbFjProjectClueCutDtl>> result = new Result<>();
        result.setResult(pageList);
        return result;
    }
}
