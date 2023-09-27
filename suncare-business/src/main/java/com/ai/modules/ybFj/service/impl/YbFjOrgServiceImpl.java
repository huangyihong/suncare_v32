package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.IdUtils;
import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.service.ISysDatabaseService;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjOrgDto;
import com.ai.modules.ybFj.entity.YbFjOrg;
import com.ai.modules.ybFj.mapper.YbFjOrgMapper;
import com.ai.modules.ybFj.service.IYbFjOrgService;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Description: 医疗机构信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class YbFjOrgServiceImpl extends ServiceImpl<YbFjOrgMapper, YbFjOrg> implements IYbFjOrgService {
    @Autowired
    private ISysDatabaseService sysDatabaseService;
    @Autowired
    private YbFjOrgMapper ybFjOrgMapper;

    @Override
    public void saveOrg(YbFjOrgDto dto) throws Exception {
        if(existsOrg(dto.getOrgId())) {
            throw new Exception("医疗机构编码（"+dto.getOrgId()+"）已存在");
        }
        YbFjOrg org = new YbFjOrg();
        org = BeanUtil.toBean(dto, YbFjOrg.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String id = IdUtils.uuid();
        org.setId(id);
        org.setCreateTime(DateUtils.getDate());
        org.setCreateUser(user.getUsername());
        org.setCreateUsername(user.getRealname());
        org.setState("1");
        org.setAuditState(DcFjConstants.PROJECT_STATE_INIT);
        this.save(org);
    }

    @Override
    public void updateOrg(YbFjOrgDto dto) throws Exception {
        YbFjOrg old = this.getById(dto.getId());
        if(!old.getOrgId().equals(dto.getOrgId())) {
            if(existsOrg(dto.getOrgId())) {
                throw new Exception("医疗机构编码（"+dto.getOrgId()+"）已存在");
            }
        }
        BeanUtil.copyProperties(dto, old, CopyOptions.create().ignoreNullValue());
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        old.setUpdateTime(DateUtils.getDate());
        old.setUpdateUser(user.getUsername());
        old.setUpdateUsername(user.getRealname());
        this.updateById(old);
    }

    @Override
    public void audit(String ids, String auditState, String auditOpinion) {
        String[] array = ids.split(",");
        YbFjOrg org = new YbFjOrg();
        org.setAuditState(auditState);
        org.setAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        org.setAuditTime(DateUtils.getDate());
        org.setAuditUser(user.getUsername());
        org.setAuditUserName(user.getRealname());
        QueryWrapper<YbFjOrg> wrapper = new QueryWrapper<>();
        wrapper.in("id", array);
        this.update(org, wrapper);
    }

    @Override
    public void dataImportGp() throws Exception{
        //mysql数据
        List<YbFjOrg> list = list();
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            //创建GP连接
            SysDatabase sysDatabase = new SysDatabase();
            List<SysDatabase> dataList = sysDatabaseService.list((QueryWrapper) Wrappers.query().eq("dbname", "medical_gbdp"));
            if(dataList.size()>0){
                sysDatabase=dataList.get(0);
            }else {
                sysDatabase.setUrl("jdbc:postgresql://10.63.82.191:5432/test?currentSchema=medical_gbdp");
                sysDatabase.setDbver("org.postgresql.Driver");
                sysDatabase.setDbUser("dw_rw");
                sysDatabase.setDbPassword("Yxsj@123");

            }
            conn = getDbConnection(sysDatabase);
            String tableName="medical_gbdp.yb_fj_org_temp";
            //删除临时表sql
            String delSql = "DELETE FROM medical_gbdp.yb_fj_org_temp";
            preparedStatement = conn.prepareStatement(delSql);
            preparedStatement.execute();
            //插入临时表sql
            String insertSqlTmp = getInsertSql(list, tableName);
            preparedStatement = conn.prepareStatement(insertSqlTmp);
            preparedStatement.execute();
            //删除表
            preparedStatement = conn.prepareStatement("DELETE FROM medical_gbdp.yb_fj_org");
            preparedStatement.execute();
            //插入表
            String insertSql = getInsertSql(list, "medical_gbdp.yb_fj_org");
            preparedStatement = conn.prepareStatement(insertSql);
            preparedStatement.execute();



        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //释放资源
            try {
                if(conn!=null && preparedStatement!=null) {
                    conn.close();
                    preparedStatement.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }



    }

    private String getInsertSql(List<YbFjOrg>list,String tableName){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer insertSQL = new StringBuffer();
        insertSQL.append("insert into "+tableName+"(id, org_id, org_name, org_used_name, org_address, PROVINCE_CODE, " +
                " PROVINCE_NAME, CITY_CODE, CITY_NAME, COUNTY_CODE, COUNTY_NAME, TOWN_CODE, TOWN_NAME, VILLAGE_CODE," +
                " VILLAGE_NAME, social_code, legalperson, legalperson_phone, responsible, responsible_phone, hosplevel," +
                " hospgrade, orgtype, busstype, OWNERSHIP_CODE, STATE, AUDIT_STATE, AUDIT_USER, AUDIT_USER_NAME, AUDIT_TIME," +
                " AUDIT_OPINION, bed_amount, create_time, create_username, create_user, update_time, update_username, update_user) ");
        insertSQL.append("values");
        for(int i=0;i<list.size();i++){
            YbFjOrg ybFjOrg = list.get(i);
            insertSQL.append("(");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getId()==null?"":ybFjOrg.getId());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getOrgId()==null?"":ybFjOrg.getOrgId());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getOrgName()==null?"":ybFjOrg.getOrgName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getOrgUsedName()==null?"":ybFjOrg.getOrgUsedName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getOrgAddress()==null?"":ybFjOrg.getOrgAddress());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getProvinceCode()==null?"":ybFjOrg.getProvinceCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getProvinceName()==null?"":ybFjOrg.getProvinceName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getCityCode()==null?"":ybFjOrg.getCityCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getCityName()==null?"":ybFjOrg.getCityName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getCountyCode()==null?"":ybFjOrg.getCountyCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getCountyName()==null?"":ybFjOrg.getCountyName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getTownCode()==null?"":ybFjOrg.getTownCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getTownName()==null?"":ybFjOrg.getTownName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getVillageCode()==null?"":ybFjOrg.getVillageCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getVillageName()==null?"":ybFjOrg.getVillageName());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getSocialCode()==null?"":ybFjOrg.getSocialCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getLegalperson()==null?"":ybFjOrg.getLegalperson());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getLegalpersonPhone()==null?"":ybFjOrg.getLegalpersonPhone());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getResponsible()==null?"":ybFjOrg.getResponsible());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getResponsiblePhone()==null?"":ybFjOrg.getResponsiblePhone());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getHosplevel()==null?"":ybFjOrg.getHosplevel());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getHospgrade()==null?"":ybFjOrg.getHospgrade());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getOrgtype()==null?"":ybFjOrg.getOrgtype());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getBusstype()==null?"":ybFjOrg.getBusstype());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getOwnershipCode()==null?"":ybFjOrg.getOwnershipCode());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getState()==null?"":ybFjOrg.getState());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getAuditState()==null?"":ybFjOrg.getAuditState());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getAuditUser()==null?"":ybFjOrg.getAuditUser());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getAuditUserName()==null?"":ybFjOrg.getAuditUserName());
            insertSQL.append("'");
            insertSQL.append(",");
            Date auditTime = ybFjOrg.getAuditTime();
            if(ObjectUtil.isNotEmpty(auditTime)){
                String format = dateFormat.format(auditTime);
                insertSQL.append("'");
                insertSQL.append(format);
                insertSQL.append("'");
            }else{
                insertSQL.append("''");
            }
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getAuditOpinion()==null?"":ybFjOrg.getAuditOpinion());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append(ybFjOrg.getBedAmount());
            insertSQL.append(",");
            Date createTime = ybFjOrg.getCreateTime();
            if(ObjectUtil.isNotEmpty(createTime)){
                String format = dateFormat.format(createTime);
                insertSQL.append("'");
                insertSQL.append(format);
                insertSQL.append("'");
            }else{
                insertSQL.append("''");
            }
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getCreateUsername()==null?"":ybFjOrg.getCreateUsername());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getCreateUser()==null?"":ybFjOrg.getCreateUser());
            insertSQL.append("'");
            insertSQL.append(",");
            Date updateTime = ybFjOrg.getUpdateTime();
            if(ObjectUtil.isNotEmpty(updateTime)){
                String format = dateFormat.format(updateTime);
                insertSQL.append("'");
                insertSQL.append(format);
                insertSQL.append("'");
            }else{
                insertSQL.append("''");
            }
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getUpdateUsername()==null?"":ybFjOrg.getUpdateUsername());
            insertSQL.append("'");
            insertSQL.append(",");
            insertSQL.append("'");
            insertSQL.append(ybFjOrg.getUpdateUser()==null?"":ybFjOrg.getUpdateUser());
            insertSQL.append("'");
            insertSQL.append(")");
            if(i != list.size()-1){
                insertSQL.append(",");
            }
        }
        return insertSQL.toString();
    }

    private boolean existsOrg(String orgId) {
        QueryWrapper<YbFjOrg> wrapper = new QueryWrapper<YbFjOrg>();
        wrapper.eq("org_id", orgId);
        return count(wrapper)>0;
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

    @Override
    public YbFjOrg findOrg(String orgId) {
        QueryWrapper<YbFjOrg> wrapper = new QueryWrapper<>();
        wrapper.in("org_id", orgId);
        return this.getOne(wrapper);
    }

    @Override
    public YbFjOrg findOrgByUser(String userId) {
        QueryWrapper<YbFjOrg> wrapper = new QueryWrapper<>();
        wrapper.inSql("org_id", "select org_id from yb_fj_user_org where user_id='"+userId+"'");
        return this.getOne(wrapper);
    }

    @Override
    public IPage<OrgUserVo> getOrgUser(Page<OrgUserVo> page, OrgUserVo orgUserVo) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String systemCode = loginUser.getSystemCode();
        orgUserVo.setSystemCode(systemCode);
        IPage<OrgUserVo> pageList = ybFjOrgMapper.getOrgUser(page, orgUserVo);
        return pageList;
    }

    @Override
    public IPage<OrgUserVo> getUserOrgList(Page<OrgUserVo> page, OrgUserVo orgUserVo) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String systemCode = loginUser.getSystemCode();
        orgUserVo.setSystemCode(systemCode);
        IPage<OrgUserVo> pageList = ybFjOrgMapper.getUserOrgList(page, orgUserVo);
        return pageList;
    }
}
