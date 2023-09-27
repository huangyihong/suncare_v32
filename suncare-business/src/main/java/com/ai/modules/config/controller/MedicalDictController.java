package com.ai.modules.config.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.service.IMedicalDictClearService;
import com.ai.modules.config.vo.MedicalDictItemVO;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.service.IMedicalDictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date: 2020-01-16
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "医疗字典")
@RestController
@RequestMapping("/config/medicalDict")
public class MedicalDictController extends JeecgController<MedicalDict, IMedicalDictService> {
    @Autowired
    private IMedicalDictService medicalDictService;

    @Autowired
    IMedicalDictClearService medicalDictClearService;

    /**
     * 通过code查询
     *
     * @param code
     * @return
     */
    @AutoLog(value = "医疗字典-通过code查询")
    @ApiOperation(value = "医疗字典-通过code查询", notes = "医疗字典-通过code查询")
    @GetMapping(value = "/queryByCode")
    public Result<?> queryByCode(@RequestParam(name = "code") String code) {
        MedicalDict medicalDict = medicalDictService.getOne(new QueryWrapper<MedicalDict>().eq("GROUP_CODE", code));
        return Result.ok(medicalDict);
    }

    /**
     * 通过type清除缓存，不用登陆
     *
     * @param type
     * @return
     */
    @AutoLog(value = "医疗字典-通过code查询")
    @ApiOperation(value = "医疗字典-通过code查询", notes = "医疗字典-通过code查询")
    @GetMapping(value = "/common/clearByType")
    public Result<?> clearByType(@RequestParam(name = "type") String type) {
        medicalDictClearService.clearCache(type, MedicalConstant.DICT_KIND_COMMON);
        return Result.ok("操作成功");
    }

    /**
     * 通过type查询
     *
     * @param type
     * @return
     */
    @AutoLog(value = "医疗字典-通过type查询")
    @ApiOperation(value = "医疗字典-通过type查询", notes = "医疗字典-通过type查询")
    @GetMapping(value = "/common/queryByType")
    public Result<?> queryByType(@RequestParam(name = "type", required = true) String type) {
        List<MedicalDictItemVO> list = medicalDictService.queryByType(type, MedicalConstant.DICT_KIND_COMMON);
        return Result.ok(list);
    }

    /**
     * 通过types查询
     *
     * @param types
     * @return
     */
    @AutoLog(value = "医疗字典-通过types查询")
    @ApiOperation(value = "医疗字典-通过types查询", notes = "医疗字典-通过types查询")
    @GetMapping(value = "/common/queryByTypes")
    public Result<?> queryByTypes(@RequestParam(name = "types", required = true) String types) {
        return Result.ok(medicalDictService.queryByTypes(types.split(","), MedicalConstant.DICT_KIND_COMMON));
    }

    /**
     * 通过type查询
     *
     * @param type
     * @return
     */
    @AutoLog(value = "医疗字典-通过type和code查询值")
    @ApiOperation(value = "医疗字典-通过type和code查询值", notes = "医疗字典-通过type和code查询值")
    @GetMapping(value = "/common/queryValByTypeCode")
    public Result<?> queryValByTypeKey(@RequestParam(name = "type", required = true) String type, @RequestParam(name = "code", required = true) String code) {
        return Result.ok(medicalDictService.queryDictTextByKey(type, code, MedicalConstant.DICT_KIND_COMMON));
    }

    @AutoLog(value = "医疗字典-通过type查询字典编码为key的Map")
    @ApiOperation(value = "医疗字典-通过type查询字典编码为key的Map", notes = "医疗字典-通过type查询字典编码为key的Map")
    @GetMapping(value = "/common/queryMapByType")
    public Result<?> queryMapByType(@RequestParam(name = "type") String type) {
        return Result.ok(medicalDictService.queryMapByType(type));
    }

    @AutoLog(value = "医疗字典-通过type查询字典值为key的Map")
    @ApiOperation(value = "医疗字典-通过type查询字典值为key的Map", notes = "医疗字典-通过type查询字典值为key的Map")
    @GetMapping(value = "/common/queryNameMapByType")
    public Result<?> queryNameMapByType(@RequestParam(name = "type") String type) {
        return Result.ok(medicalDictService.queryNameMapByType(type));
    }

    @AutoLog(value = "其他字典-通过code查询")
    @ApiOperation(value="其他字典-通过code查询", notes="其他字典-通过code查询")
    @GetMapping(value = "/queryByCodes")
    public Result<?> queryByCodes(@RequestParam(name="codes") String codes) {
        List<String> codeList = Arrays.asList(codes.split(","));
        List<MedicalDict> list = medicalDictService.list(new QueryWrapper<MedicalDict>()
                .in("GROUP_CODE",codeList));
        return Result.ok(list);
    }

    /**
     * 通过分组查询子项列表
     *
     * @param medicalDict
     * @param medicalDictItem
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "医疗字典-通过分组查询子项列表")
    @ApiOperation(value = "医疗字典-通过分组查询子项列表", notes = "医疗字典-通过分组查询子项列表")
    @GetMapping(value = "/queryItemsByGroup")
    public Result<?> queryItemsByGroup(MedicalDict medicalDict, MedicalDictItem medicalDictItem,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "9999999") Integer pageSize,
                                       HttpServletRequest req) {
        Page<MedicalDictItemVO> page = new Page<>(pageNo, pageSize);
        return Result.ok(medicalDictService.list(page, medicalDictItem, medicalDict));
    }

    /**
     * 通过id查询
     *
     * @param kinds
     * @return
     */
    @AutoLog(value = "药品合规规则分组-通过kinds查询分组字典")
    @ApiOperation(value = "药品合规规则分组-通过kinds查询分组字典", notes = "药品合规规则分组-通过kinds查询分组字典")
    @GetMapping(value = "/queryDistinctDictByKinds")
    public Result<?> queryDistinctDictByKinds(@RequestParam(name = "kinds", required = true) String kinds) {
        Map<String, List<MedicalDict>> map = medicalDictService.queryDistinctDictByKinds(kinds.trim().split(","));
        return Result.ok(map);
    }


    /**
     * 分页列表查询
     *
     * @param medicalDict
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "医疗字典-分页列表查询")
    @ApiOperation(value = "医疗字典-分页列表查询", notes = "医疗字典-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalDict medicalDict, MedicalDictItem medicalDictItem,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalDict, req.getParameterMap());
        String inStr = "";
        if (StringUtils.isNotEmpty(medicalDictItem.getCode())) {
            inStr += "  and CODE like '" + medicalDictItem.getCode().replaceAll("\\*", "%") + "'";
        }
        if (StringUtils.isNotEmpty(medicalDictItem.getValue())) {
            inStr += "  and VALUE like '" + medicalDictItem.getValue().replaceAll("\\*", "%") + "'";
        }
        if (inStr.length() > 0) {
            queryWrapper.inSql("GROUP_ID",
                    "SELECT GROUP_ID FROM MEDICAL_DICT_ITEM where 1=1" + inStr);

        }
        Page<MedicalDict> page = new Page<MedicalDict>(pageNo, pageSize);
        IPage<MedicalDict> pageList = medicalDictService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param medicalDict
     * @return
     */
    @AutoLog(value = "医疗字典-添加")
    @ApiOperation(value = "医疗字典-添加", notes = "医疗字典-添加")
    @PostMapping(value = "/add")
    public Result<?> add(MedicalDict medicalDict, String codes, String names) {
        medicalDictService.saveGroup(medicalDict, codes, names);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalDict
     * @return
     */
    @AutoLog(value = "医疗字典-编辑")
    @ApiOperation(value = "医疗字典-编辑", notes = "医疗字典-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(MedicalDict medicalDict, String codes, String names) {
        medicalDictService.updateGroup(medicalDict, codes, names);
        return Result.ok("编辑成功!");
    }

    /**
     * 更新子项列表
     *
     * @param groupCode
     * @return
     */
    @AutoLog(value = "医疗字典-编辑")
    @ApiOperation(value = "医疗字典-编辑", notes = "医疗字典-编辑")
    @PutMapping(value = "/updateItems")
    public Result<?> updateItems(String groupCode, String codes, String names, String dels) {
        medicalDictService.updateItems(groupCode, codes, names, dels);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "医疗字典-通过id删除")
    @ApiOperation(value = "医疗字典-通过id删除", notes = "医疗字典-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        MedicalDict bean = medicalDictService.getById(id);
        String groupCode = bean.getGroupCode();
        String groupKind = bean.getKind();
        medicalDictClearService.clearCache(groupCode, groupKind);
        List<MedicalDictItemVO> itemList = medicalDictService.queryByType(groupCode, groupKind);
        for (MedicalDictItemVO item : itemList) {
            medicalDictClearService.clearCache(groupCode, item.getCode(), groupKind);
            medicalDictClearService.clearTextCache(groupCode, item.getValue(), groupKind);
        }
        medicalDictService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "医疗字典-批量删除")
    @ApiOperation(value = "医疗字典-批量删除", notes = "医疗字典-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        Collection<MedicalDict> list = medicalDictService.listByIds(idList);
        for (MedicalDict bean : list) {
            String groupCode = bean.getGroupCode();
            String groupKind = bean.getKind();
            medicalDictClearService.clearCache(groupCode, groupKind);
            List<MedicalDictItemVO> itemList = medicalDictService.queryByType(groupCode, groupKind);
            for (MedicalDictItemVO item : itemList) {
                medicalDictClearService.clearCache(groupCode, item.getCode(), groupKind);
                medicalDictClearService.clearTextCache(groupCode, item.getValue(), groupKind);
            }
        }
        this.medicalDictService.removeByIds(idList);
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "医疗字典-通过id查询")
    @ApiOperation(value = "医疗字典-通过id查询", notes = "医疗字典-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalDict medicalDict = medicalDictService.getById(id);
        return Result.ok(medicalDict);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalDict
     */
    /*@RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalDict medicalDict) {
        return super.exportXls(request, medicalDict, MedicalDict.class, "医疗字典");
    }*/

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
   /* @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MedicalDict.class);
    }*/

}
