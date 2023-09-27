package com.ai.modules.ybChargeSearch.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.BeanUtil;
import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.service.ISysDatabaseService;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.mapper.YbChargeDrugRuleMapper;
import com.ai.modules.ybChargeSearch.service.IYbChargeDrugRuleService;
import com.ai.modules.ybChargeSearch.vo.YbChargeDrugRuleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 药品规则库
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
@Service
public class YbChargeDrugRuleServiceImpl extends ServiceImpl<YbChargeDrugRuleMapper, YbChargeDrugRule> implements IYbChargeDrugRuleService {
    @Autowired
    private ISysDatabaseService sysDatabaseService;

    @Override
    public IPage<?> drugRuleImportList(YbChargeDrugRuleVo ybChargeDrugRuleVo, Page<YbChargeDrugRuleVo> page, HttpServletRequest req) throws Exception {
        ybChargeDrugRuleVo.setItemClass("drug");
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String dataSource = loginUser.getDataSource();
//        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase("medical_gbdp");


        SysDatabase sysDatabase = new SysDatabase();
        List<SysDatabase> dataList = sysDatabaseService.list((QueryWrapper) Wrappers.query().eq("dbname", "medical_gbdp"));
        if (dataList.size() > 0) {
            sysDatabase = dataList.get(0);
        } else {
            sysDatabase.setUrl("jdbc:postgresql://10.63.82.191:5432/test?currentSchema=medical_gbdp");
            sysDatabase.setDbver("org.postgresql.Driver");
            sysDatabase.setDbUser("dw_rw");
            sysDatabase.setDbPassword("Yxsj@123");
            sysDatabase.setDbtype("greenplum");

        }
        Connection conn = getDbConnection(sysDatabase);

//        Connection conn = getDbConnection(dbVO.getSysDatabase());

        String sqlId = "detail_with_itemname_fee";
        String xmlFileName = "YbChargeDrugCase.xml";
        //获取list SQL
        String querySql = getDrugCaseListSql(ybChargeDrugRuleVo, xmlFileName, sqlId, dataSource, page,sysDatabase.getDbtype());
//        System.out.println(querySql);
        PreparedStatement pstmt = conn.prepareStatement(querySql);
        ResultSet rSet = pstmt.executeQuery();
        ArrayList<YbChargeDrugRuleVo> list = new ArrayList<>();
        while (rSet.next()) {
            YbChargeDrugRuleVo resultVo = new YbChargeDrugRuleVo();
            resultVo.setDrugType(rSet.getString("drug_type"));
            resultVo.setDrugTypeSmall(rSet.getString("drug_type_small"));
            resultVo.setFunType(rSet.getString("fun_type"));
            resultVo.setDrugName(rSet.getString("drug_name"));
            resultVo.setDosageType(rSet.getString("dosage_type"));
            resultVo.setRemark(rSet.getString("remark"));
            resultVo.setLimitType(rSet.getString("limit_type"));
            resultVo.setLimitContent(rSet.getString("limit_content"));
            resultVo.setSorter(rSet.getString("sorter"));

            list.add(resultVo);
        }
        if (list.size() > 0) {
            page.setRecords(list);
            //获取count SQL
            sqlId = "detail_with_itemname_fee_count";
            String countSql = getDrugCaseListSql(ybChargeDrugRuleVo, xmlFileName, sqlId, dataSource, page,sysDatabase.getDbtype());
            Connection conn2 = getDbConnection(sysDatabase);
            PreparedStatement countStatement = conn2.prepareStatement(countSql);
            ResultSet countSet = countStatement.executeQuery();
            int total = 0;
            while (countSet.next()) {
                total = countSet.getInt("total");
            }

            page.setTotal(total);
            return page;
            //查询mysql
        } else {
            LambdaQueryWrapper<YbChargeDrugRule> queryWrapper = new LambdaQueryWrapper<YbChargeDrugRule>();
            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDrugType())){
                queryWrapper.like(YbChargeDrugRule::getDrugType,ybChargeDrugRuleVo.getDrugType());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDrugName())){
                queryWrapper.like(YbChargeDrugRule::getDrugName,ybChargeDrugRuleVo.getDrugName());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getSorter())){
                queryWrapper.like(YbChargeDrugRule::getSorter,ybChargeDrugRuleVo.getSorter());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getExamineStatus())){
                queryWrapper.eq(YbChargeDrugRule::getExamineStatus,ybChargeDrugRuleVo.getExamineStatus());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDrugTypeSmall())){
                queryWrapper.like(YbChargeDrugRule::getDrugTypeSmall,ybChargeDrugRuleVo.getDrugTypeSmall());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getFunType())){
                queryWrapper.like(YbChargeDrugRule::getFunType,ybChargeDrugRuleVo.getFunType());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDosageType())){
                queryWrapper.like(YbChargeDrugRule::getDosageType,ybChargeDrugRuleVo.getDosageType());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getLimitType())){
                queryWrapper.eq(YbChargeDrugRule::getLimitType,ybChargeDrugRuleVo.getLimitType());
            }

            if(ObjectUtil.isNotEmpty(ybChargeDrugRuleVo.getLimitContent())){
                queryWrapper.like(YbChargeDrugRule::getLimitContent,ybChargeDrugRuleVo.getLimitContent());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getSorter())){
                queryWrapper.like(YbChargeDrugRule::getSorter,ybChargeDrugRuleVo.getSorter());
            }

            if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getRemark())){
                queryWrapper.like(YbChargeDrugRule::getRemark,ybChargeDrugRuleVo.getRemark());
            }



            Page<YbChargeDrugRule> page2 = new Page<YbChargeDrugRule>(page.getCurrent(), page.getSize());
            IPage<YbChargeDrugRule> pageList = page(page2, queryWrapper);
            return pageList;
        }
    }



    public String getDrugCaseListSql(YbChargeDrugRuleVo ybChargeDrugRuleVo, String xmlFileName, String sqlId, String dataSource, Page page,String dbtype) throws Exception {


        GenHiveQueryCommon genHiveQueryCommon = new GenHiveQueryCommon();
        String querySql = genHiveQueryCommon.getSqlFromXml(xmlFileName, sqlId,dbtype);

        //替换变量
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        if(ObjectUtil.isNotEmpty(ybChargeDrugRuleVo.getChargedateStartdate())){
            String chargedateStartdate = dateFormat.format(ybChargeDrugRuleVo.getChargedateStartdate());
            querySql = StringUtils.replace(querySql,"${chargedateStartdate}" ,chargedateStartdate);
        }
        if(ObjectUtil.isNotEmpty(ybChargeDrugRuleVo.getChargedateEnddate())){
            String chargedateEnddate = dateFormat.format(ybChargeDrugRuleVo.getChargedateEnddate());
            querySql = StringUtils.replace(querySql,"${chargedateEnddate}" ,chargedateEnddate);
        }

        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getEtlSource())){
            querySql = StringUtils.replace(querySql,"${etlSource}" ,ybChargeDrugRuleVo.getEtlSource());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getItemClass())){
            querySql = StringUtils.replace(querySql,"${itemClass}" ,ybChargeDrugRuleVo.getItemClass());
        }

        if(StrUtil.isNotEmpty(dataSource)){
            querySql = StringUtils.replace(querySql,"${dataSource}" ,dataSource);
        }

        querySql = StringUtils.replace(querySql,"${size}" ,String.valueOf(page.getSize()));
        querySql = StringUtils.replace(querySql,"${current}" ,String.valueOf(page.getSize()*(page.getCurrent()-1)));

        String orgs=" (";
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getOrgs())){
            List<String> orgList = Arrays.asList(ybChargeDrugRuleVo.getOrgs().split(","));
            for(String org:orgList){
                orgs = orgs + "'"+org+"'"+",";
            }
            orgs=orgs.substring(0,orgs.length()-1);
            orgs +=")";
            querySql = StringUtils.replace(querySql,"${orgname}" ,orgs);
        }

        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDrugType())){
            querySql = StringUtils.replace(querySql,"${drug_type}" ,ybChargeDrugRuleVo.getDrugType());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDrugName())){
            querySql = StringUtils.replace(querySql,"${drug_name}" ,ybChargeDrugRuleVo.getDrugName());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getExamineStatus())){
            querySql = StringUtils.replace(querySql,"${examine_status}" ,ybChargeDrugRuleVo.getExamineStatus());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDrugTypeSmall())){
            querySql = StringUtils.replace(querySql,"${drug_type_small}" ,ybChargeDrugRuleVo.getDrugTypeSmall());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getFunType())){
            querySql = StringUtils.replace(querySql,"${fun_type}" ,ybChargeDrugRuleVo.getFunType());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getDosageType())){
            querySql = StringUtils.replace(querySql,"${dosage_type}" ,ybChargeDrugRuleVo.getDosageType());
        }
        if(ObjectUtil.isNotEmpty(ybChargeDrugRuleVo.getLimitType())){
            querySql = StringUtils.replace(querySql,"${limit_type}" ,String.valueOf(ybChargeDrugRuleVo.getLimitType()));
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getLimitContent())){
            querySql = StringUtils.replace(querySql,"${limit_content}" ,ybChargeDrugRuleVo.getLimitContent());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getSorter())){
            querySql = StringUtils.replace(querySql,"${sorter}" ,ybChargeDrugRuleVo.getSorter());
        }
        if(StrUtil.isNotEmpty(ybChargeDrugRuleVo.getRemark())){
            querySql = StringUtils.replace(querySql,"${remark}" ,ybChargeDrugRuleVo.getRemark());
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
        String url = sysDatabase.getUrl();
        String username = sysDatabase.getDbUser();
        String password = sysDatabase.getDbPassword();
        if (!StringUtils.isBlank(password)) {
            password = DbDataEncryptUtil.dbDataDecryptString(password);
        }
        Class.forName(className);
        Connection conn = DriverManager.getConnection(url, username, password);
        return conn;
    }

}
