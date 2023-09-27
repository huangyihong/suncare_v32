package com.ai.modules.ybChargeSearch.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YearWeek;
import com.ai.modules.ybChargeSearch.service.IYearWeekService;
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
 * @Description: 年度周表
 * @Author: jeecg-boot
 * @Date: 2022-12-05
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "年度周表")
@RestController
@RequestMapping("/ybChargeSearch/yearWeek")
public class YearWeekController extends JeecgController<YearWeek, IYearWeekService> {
    @Autowired
    private IYearWeekService yearWeekService;

    /**
     * 分页列表查询
     *
     * @param yearWeek
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "年度周表-分页列表查询")
    @ApiOperation(value = "年度周表-分页列表查询", notes = "年度周表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YearWeek yearWeek,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<YearWeek> queryWrapper = QueryGenerator.initQueryWrapper(yearWeek, req.getParameterMap());
        Page<YearWeek> page = new Page<YearWeek>(pageNo, pageSize);
        IPage<YearWeek> pageList = yearWeekService.page(page, queryWrapper);
        return Result.ok(pageList);
    }



    /**
     * 添加
     *
     * @param yearWeek
     * @return
     */
    @AutoLog(value = "年度周表-添加")
    @ApiOperation(value = "年度周表-添加", notes = "年度周表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YearWeek yearWeek) {
        yearWeekService.save(yearWeek);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param yearWeek
     * @return
     */
    @AutoLog(value = "年度周表-编辑")
    @ApiOperation(value = "年度周表-编辑", notes = "年度周表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YearWeek yearWeek) {
        yearWeekService.updateById(yearWeek);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度周表-通过id删除")
    @ApiOperation(value = "年度周表-通过id删除", notes = "年度周表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        yearWeekService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "年度周表-批量删除")
    @ApiOperation(value = "年度周表-批量删除", notes = "年度周表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.yearWeekService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度周表-通过id查询")
    @ApiOperation(value = "年度周表-通过id查询", notes = "年度周表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YearWeek yearWeek = yearWeekService.getById(id);
        return Result.ok(yearWeek);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param yearWeek
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YearWeek yearWeek) {
        return super.exportXls(request, yearWeek, YearWeek.class, "年度周表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YearWeek.class);
    }

	/**
	 * 生成年度周
	 */
	@AutoLog(value = "生成年度周")
	@ApiOperation(value = "生成年度周", notes = "生成年度周")
	@GetMapping(value = "/yearWeek")
	public Result<?> createYearWeek(int beginYear,int endYear) {
        ArrayList<YearWeek> yearWeeks = new ArrayList<>();
		for(int i=beginYear;i<endYear;i++){
			//获取总周数
			int weekNumByYear = getWeekNumByYear(i);
			for(int j=1; j<=weekNumByYear;j++){
                YearWeek yearWeek = new YearWeek();
                //当年第一周第一天日期
				String weekFirstDay = getYearWeekDay(i, j, Calendar.MONDAY);
				//当年第一周最后一天日期
				String weekEndDay = getYearWeekDay(i, j, Calendar.SUNDAY);
                yearWeek.setYear(i);
                yearWeek.setWeekStart(weekFirstDay);
                yearWeek.setWeekEnd(weekEndDay);
                yearWeek.setWeekNum(j);
                yearWeeks.add(yearWeek);

			}

		}
		yearWeekService.saveBatch(yearWeeks);

		return Result.ok();
	}

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		//设置年份
		cal.set(Calendar.YEAR, 2019);
		//当年年份
		int year = cal.get(Calendar.YEAR);
		System.out.println(year);

	}


    public static int getWeekNumByYear(final int year) {
        if (year < 1900 || year > 9999) {
            throw new NullPointerException("年度必须大于等于1900年小于等于9999年");

        }

        //每年至少有52个周 ，最多有53个周。
        int result = 52;

        String date = getYearWeekDay(year, 53, Calendar.MONDAY);

        //判断年度是否相符，如果相符说明有53个周。
        if (date.substring(0, 4).equals(year + "")) {

            result = 53;

        }

        return result;

    }


    //获取某年某周的某一天日期
    public static String getYearWeekDay(int year, int weekNum, int calendarNum) {
        Calendar cal = Calendar.getInstance();
        //设置每周的第一天为星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        //设置查找周几
        cal.set(Calendar.DAY_OF_WEEK, calendarNum);
        //设置每周最少为7天
        cal.setMinimalDaysInFirstWeek(7);
        //设置年份
        cal.set(Calendar.YEAR, year);

        //获取指定日期
        cal.set(Calendar.WEEK_OF_YEAR, weekNum);
        Date time = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(time);
        return date;

    }

}
