package com.ai.modules.ybChargeSearch.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.service.ISysDatabaseService;
import com.ai.modules.ybChargeSearch.entity.YbChargeitemChecklist;
import com.ai.modules.ybChargeSearch.mapper.YbChargeitemChecklistMapper;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybChargeSearch.service.IYbChargeitemChecklistService;
import com.ai.modules.ybChargeSearch.vo.YbChargeitemChecklistVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 收费明细风控检查内容
 * @Author: jeecg-boot
 * @Date:   2022-11-25
 * @Version: V1.0
 */
@Service
public class YbChargeitemChecklistServiceImpl extends ServiceImpl<YbChargeitemChecklistMapper, YbChargeitemChecklist> implements IYbChargeitemChecklistService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private YbChargeitemChecklistMapper ybChargeitemChecklistMapper;
    @Autowired
    private IYbChargeSearchTaskService ybChargeSearchTaskService;
    @Autowired
    private ISysDatabaseService sysDatabaseService;


    @Override
    public List<String> getRoleByUserName(String username) {
        List<String> roleByUserName = ybChargeitemChecklistMapper.getRoleByUserName(username);
        return roleByUserName;
    }

    @Override
    public List<String> getUserIdByRealName(String realName) {
        List<String> roleByUserName = ybChargeitemChecklistMapper.getUserIdByRealName(realName);
        return roleByUserName;

    }

    @Override
    public List<String> getUserRealName() {
        List<String> list = ybChargeitemChecklistMapper.getUserRealName();
        return list;
    }

    @Override
    public List<String> getUserBtnPermission(String username) {
        List<String> list = ybChargeitemChecklistMapper.getUserBtnPermission(username);
        return list;
    }

    @Override
    public IPage<?> getKeyWordsImportList(YbChargeitemChecklistVo ybChargeitemChecklistVo,Page<YbChargeitemChecklistVo> page) throws Exception{
        ybChargeitemChecklistVo.setItemClass("charge");
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = loginUser.getDataSource();
//        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase("medical_gbdp");


        SysDatabase sysDatabase = new SysDatabase();
        List<SysDatabase> dataList = sysDatabaseService.list((QueryWrapper) Wrappers.query().eq("dbname", "medical_gbdp"));
        if(dataList.size()>0){
            sysDatabase=dataList.get(0);
        }else {
            sysDatabase.setUrl("jdbc:postgresql://10.63.82.191:5432/test?currentSchema=medical_gbdp");
            sysDatabase.setDbver("org.postgresql.Driver");
            sysDatabase.setDbUser("dw_rw");
            sysDatabase.setDbPassword("Yxsj@123");
            sysDatabase.setDbtype("greenplum");

        }
        Connection conn = getDbConnection(sysDatabase);

//        Connection conn = getDbConnection(dbVO.getSysDatabase());

        String sqlId="detail_with_itemname_fee";
        String xmlFileName="YbChargeitemCheckList.xml";
        //获取list SQL
        String querySql = getKeyWordsListSql(ybChargeitemChecklistVo, xmlFileName, sqlId, dataSource, page,sysDatabase.getDbtype());
//        System.out.println(querySql);
        PreparedStatement pstmt = conn.prepareStatement(querySql);
        ResultSet rSet = pstmt.executeQuery();
        ArrayList<YbChargeitemChecklistVo> list = new ArrayList<>();
        while(rSet.next()){
            YbChargeitemChecklistVo resultVo = new YbChargeitemChecklistVo();
            resultVo.setItemname(rSet.getString("itemname"));
            resultVo.setItemname1(rSet.getString("itemname1"));
            resultVo.setItemType(rSet.getString("item_type"));
            resultVo.setIsSameDay(rSet.getString("is_same_day"));
            resultVo.setItem1Type(rSet.getString("item1_type"));
            resultVo.setItem1Wgtype(rSet.getString("item1_wgtype"));
            resultVo.setQtyType(rSet.getString("qty_type"));
            resultVo.setQtyNum(rSet.getInt("qty_num"));
            resultVo.setItemCode1(rSet.getString("item_code1"));
            resultVo.setPackageItem1(rSet.getString("package_item1"));
            resultVo.setItemCode2(rSet.getString("item_code2"));
            resultVo.setPackageItem2(rSet.getString("package_item2"));
            resultVo.setWgCaseExample(rSet.getString("wg_case_example"));
            resultVo.setSorter(rSet.getString("sorter"));

            list.add(resultVo);
        }
        if(list.size()>0){
            page.setRecords(list);
            //获取count SQL
            sqlId="detail_with_itemname_fee_count";
            String countSql = getKeyWordsListSql(ybChargeitemChecklistVo, xmlFileName, sqlId, dataSource, page,sysDatabase.getDbtype());
            Connection conn2 = getDbConnection(sysDatabase);
            PreparedStatement countStatement = conn2.prepareStatement(countSql);
            ResultSet countSet = countStatement.executeQuery();
            int total=0;
            while(countSet.next()){
                total = countSet.getInt("total");
            }

            page.setTotal(total);
            return page;
         //查询mysql
        }else {
            LambdaQueryWrapper<YbChargeitemChecklist> queryWrapper = new LambdaQueryWrapper<YbChargeitemChecklist>();
            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemname())){
                queryWrapper.like(YbChargeitemChecklist::getItemname,ybChargeitemChecklistVo.getItemname());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemname1())){
                queryWrapper.like(YbChargeitemChecklist::getItemname1,ybChargeitemChecklistVo.getItemname1());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getSorter())){
                queryWrapper.like(YbChargeitemChecklist::getSorter,ybChargeitemChecklistVo.getSorter());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemType())){
                queryWrapper.like(YbChargeitemChecklist::getItemType,ybChargeitemChecklistVo.getItemType());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getIsSameDay())){
                queryWrapper.eq(YbChargeitemChecklist::getIsSameDay,ybChargeitemChecklistVo.getIsSameDay());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItem1Type())){
                queryWrapper.eq(YbChargeitemChecklist::getItem1Type,ybChargeitemChecklistVo.getItem1Type());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItem1Wgtype())){
                queryWrapper.eq(YbChargeitemChecklist::getItem1Wgtype,ybChargeitemChecklistVo.getItem1Wgtype());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getQtyType())){
                queryWrapper.eq(YbChargeitemChecklist::getQtyType,ybChargeitemChecklistVo.getQtyType());
            }

            if(ObjectUtil.isNotEmpty(ybChargeitemChecklistVo.getQtyNum())){
                queryWrapper.eq(YbChargeitemChecklist::getQtyNum,ybChargeitemChecklistVo.getQtyNum());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemCode1())){
                queryWrapper.eq(YbChargeitemChecklist::getItemCode1,ybChargeitemChecklistVo.getItemCode1());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getPackageItem1())){
                queryWrapper.like(YbChargeitemChecklist::getPackageItem1,ybChargeitemChecklistVo.getPackageItem1());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemCode2())){
                queryWrapper.eq(YbChargeitemChecklist::getItemCode2,ybChargeitemChecklistVo.getItemCode2());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getPackageItem2())){
                queryWrapper.like(YbChargeitemChecklist::getPackageItem2,ybChargeitemChecklistVo.getPackageItem2());
            }

            if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getWgCaseExample())){
                queryWrapper.like(YbChargeitemChecklist::getWgCaseExample,ybChargeitemChecklistVo.getWgCaseExample());
            }
            queryWrapper.eq(YbChargeitemChecklist::getExamineStatus,"1");


            Page<YbChargeitemChecklist> page2 = new Page<YbChargeitemChecklist>(page.getCurrent(), page.getSize());
            IPage<YbChargeitemChecklist> pageList = page(page2, queryWrapper);
            return pageList;

        }





    }

    @Override
    public IPage<YbChargeitemChecklist> getPage(Page<YbChargeitemChecklist> page, QueryWrapper<YbChargeitemChecklist> queryWrapper) {
        return this.baseMapper.selectPageVO(page,queryWrapper);
    }


    public String getKeyWordsListSql(YbChargeitemChecklistVo ybChargeitemChecklistVo,String xmlFileName, String sqlId,String dataSource,Page page,String dbtype) throws Exception {


        GenHiveQueryCommon genHiveQueryCommon = new GenHiveQueryCommon();
        String querySql = genHiveQueryCommon.getSqlFromXml(xmlFileName, sqlId,dbtype);

        //替换变量
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        if(ObjectUtil.isNotEmpty(ybChargeitemChecklistVo.getChargedateStartdate())){
            String chargedateStartdate = dateFormat.format(ybChargeitemChecklistVo.getChargedateStartdate());
            querySql = StringUtils.replace(querySql,"${chargedateStartdate}" ,chargedateStartdate);
        }
        if(ObjectUtil.isNotEmpty(ybChargeitemChecklistVo.getChargedateEnddate())){
            String chargedateEnddate = dateFormat.format(ybChargeitemChecklistVo.getChargedateEnddate());
            querySql = StringUtils.replace(querySql,"${chargedateEnddate}" ,chargedateEnddate);
        }

        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getEtlSource())){
            querySql = StringUtils.replace(querySql,"${etlSource}" ,ybChargeitemChecklistVo.getEtlSource());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemClass())){
            querySql = StringUtils.replace(querySql,"${itemClass}" ,ybChargeitemChecklistVo.getItemClass());
        }

        if(StrUtil.isNotEmpty(dataSource)){
            querySql = StringUtils.replace(querySql,"${dataSource}" ,dataSource);
        }

        querySql = StringUtils.replace(querySql,"${size}" ,String.valueOf(page.getSize()));
        querySql = StringUtils.replace(querySql,"${current}" ,String.valueOf(page.getSize()*(page.getCurrent()-1)));

        String orgs=" (";
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getOrgs())){
            List<String> orgList = Arrays.asList(ybChargeitemChecklistVo.getOrgs().split(","));
            for(String org:orgList){
                orgs = orgs + "'"+org+"'"+",";
            }
            orgs=orgs.substring(0,orgs.length()-1);
            orgs +=")";
            querySql = StringUtils.replace(querySql,"${orgname}" ,orgs);
        }

        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemname())){
            querySql = StringUtils.replace(querySql,"${itemname}" ,ybChargeitemChecklistVo.getItemname());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemType())){
            querySql = StringUtils.replace(querySql,"${item_type}" ,ybChargeitemChecklistVo.getItemType());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getIsSameDay())){
            querySql = StringUtils.replace(querySql,"${is_same_day}" ,ybChargeitemChecklistVo.getIsSameDay());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItem1Type())){
            querySql = StringUtils.replace(querySql,"${item1_type}" ,ybChargeitemChecklistVo.getItem1Type());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItem1Wgtype())){
            querySql = StringUtils.replace(querySql,"${item1_wgtype}" ,ybChargeitemChecklistVo.getItem1Wgtype());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getQtyType())){
            querySql = StringUtils.replace(querySql,"${qty_type}" ,ybChargeitemChecklistVo.getQtyType());
        }
        if(ObjectUtil.isNotEmpty(ybChargeitemChecklistVo.getQtyNum())){
            querySql = StringUtils.replace(querySql,"${qty_num}" ,String.valueOf(ybChargeitemChecklistVo.getQtyNum()));
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemCode1())){
            querySql = StringUtils.replace(querySql,"${item_code1}" ,ybChargeitemChecklistVo.getItemCode1());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getPackageItem1())){
            querySql = StringUtils.replace(querySql,"${package_item1}" ,ybChargeitemChecklistVo.getPackageItem1());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getItemCode2())){
            querySql = StringUtils.replace(querySql,"${item_code2}" ,ybChargeitemChecklistVo.getItemCode2());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getPackageItem2())){
            querySql = StringUtils.replace(querySql,"${package_item2}" ,ybChargeitemChecklistVo.getPackageItem2());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getWgCaseExample())){
            querySql = StringUtils.replace(querySql,"${wg_case_example}" ,ybChargeitemChecklistVo.getWgCaseExample());
        }
        if(StrUtil.isNotEmpty(ybChargeitemChecklistVo.getSorter())){
            querySql = StringUtils.replace(querySql,"${sorter}" ,ybChargeitemChecklistVo.getSorter());
        }



        //删除SQL语句中没有被替换的变量
        ArrayList<String> sqlList = new ArrayList<String>();
        String tmpSqls[] = StringUtils.split(querySql ,"\n");
        for(int i=0 ;i<tmpSqls.length ; i++){
            String tmpSql = tmpSqls[i];

            //如果有变量没替换，说明需要注释改行
            if(StringUtils.indexOf(tmpSql,"${") >=0 && tmpSql.indexOf("}") >0){

                //TODO 调试完毕注释
                //tmpSql = "  --" + tmpSql;
                //sqlList.add(tmpSql);
            } else{
                sqlList.add(tmpSql);
            }
        }
        querySql = StringUtils.join(sqlList ,"\n");
        return querySql;
    }


    //获取数据源
    private Connection getDbConnection(SysDatabase sysDatabase)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

        String className = sysDatabase.getDbver();
        String url =  sysDatabase.getUrl();
        String username = sysDatabase.getDbUser();
        String password = sysDatabase.getDbPassword();
        if(!StringUtils.isBlank(password)){
            password = DbDataEncryptUtil.dbDataDecryptString(password);
        }
        Class.forName(className);
        Connection conn = DriverManager.getConnection(url,username,password);
        return conn;
    }

}
