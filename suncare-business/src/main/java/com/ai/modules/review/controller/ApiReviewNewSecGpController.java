package com.ai.modules.review.controller;

import com.ai.common.utils.ThreadUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.service.IMedicalUnreasonableActionService;
import com.ai.modules.review.service.impl.DynamicFieldConstant;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "项目复审")
@RestController
@RequestMapping("/gp/apiReviewSec")
public class ApiReviewNewSecGpController {
    @Autowired
    private IMedicalUnreasonableActionService medicalUnreasonableActionService;

    @AutoLog(value = "复审-判定结果")
    @ApiOperation(value = "复审-判定结果", notes = "复审-判定结果")
    @PutMapping(value = "/updateReviewStatus")
    public Result<?> updateReviewStatus(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableAction searchObj,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 判断结果信息
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);
        reviewObj.setSecReviewUserid(user.getId());
        reviewObj.setSecReviewUsername(user.getRealname());
        reviewObj.setSecReviewTime(TimeUtil.getNowTime());

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
            parameterMap.remove(QueryGenerator.ORDER_COLUMN);
            QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);
            queryWrapper.and(wrapper ->{
                wrapper.ne("SEC_PUSH_STATUS", "1");
                wrapper.or().isNull("SEC_PUSH_STATUS");
                return wrapper;
            });
            queryWrapper.apply("not (SEC_REVIEW_STATUS = '"+reviewObj.getFirReviewStatus()+"' and SEC_REVIEW_CLASSIFY='"+reviewObj.getFirReviewClassify()+"' )");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    queryWrapper.in(groupByList.get(0),ids.split(","));
                } else {
                    queryWrapper.and(wrapper -> {
                        Arrays.stream(ids.split(",")).forEach(id -> {
                            String[] groupVals = id.split("::");
                            wrapper.or().and(j ->{
                                for(int i=0;i<groupByList.size();i++){
                                    j.eq(groupByList.get(i),groupVals[i]);
                                }
                                return j;
                            });
                        });
                        return wrapper;
                    });
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
            String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
            String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
            //left join 字段
            Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
            resultFieldSet.add("ID");
            String fields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
            int count = medicalUnreasonableActionService.selectCount(queryWrapper,joinSql,whereSql,fields);
            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql,fields,reviewObj);
                return Result.ok("判定成功");
            } else {

                String finalFields = fields;
                MedicalUnreasonableActionVo searchObjVo = new MedicalUnreasonableActionVo();
                BeanUtils.copyProperties(searchObj,searchObjVo);
                ThreadUtils.ASYNC_POOL.addJudge(searchObjVo, new String[]{}, (int) count, (processFunc) -> {
                    try {
                        medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql, finalFields,reviewObj);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });

                return Result.ok("正在批量修改判定结果，请稍后查看");
            }
        } else {
            List<MedicalUnreasonableAction> list = new ArrayList<>();
            Arrays.asList(ids.split(",")).stream().forEach(id->{
                MedicalUnreasonableAction bean =  new MedicalUnreasonableAction();
                BeanUtils.copyProperties(reviewObj,bean);
                bean.setId(id);
                list.add(bean);
            });
            medicalUnreasonableActionService.updateBatchById(list);
            return Result.ok("判定成功");
        }
    }

    @AutoLog(value = "复审-批量导入审核数据")
    @ApiOperation(value = "复审-批量导入审核数据", notes = "复审-批量导入审核数据")
    @PostMapping(value = "/importReviewExcel")
    public Result<?> importCaseExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableActionVo searchObj
            , HttpServletResponse response) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                String msg = this.medicalUnreasonableActionService.importReviewStatusSec(file, searchObj);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }
    }

    @AutoLog(value = "复审-批量导入分组统计审核数据")
    @ApiOperation(value = "复审-批量导入分组统计审核数据", notes = "复审-批量导入分组统计审核数据")
    @PostMapping(value = "/importGroupReviewExcel")
    public Result<?> importGroupReviewExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableAction searchObj
            , String dynamicSearch, HttpServletRequest req) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {

                String msg = this.medicalUnreasonableActionService.importGroupReviewStatusSec(file, searchObj, dynamicSearch,req);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }




}
