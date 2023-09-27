package com.ai.common.emport.example;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class TestUser implements Serializable {
    /** 用户ID */
    private String userId;

    /** 用户姓名 */
    private String userName;

    /** 登录账号 */
    private String userCode;

    /** 密码 */
    private String userPwd;

    /** 用户类型{1:后台管理员,2:前台注册用户} */
    private String userType;

    /** 密码加密盐值 */
    private String salt;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 是否是超级管理员 */
    private String isSupervisor;

    /** 状态 */
    private String userStatus;

    /** 所属组织 */
    private String orgId;

    /** 创建人 */
    private String createUser;

    /** 创建时间 */
    private Date createTime;

    /** 修改人 */
    private String updateUser;

    /** 修改时间 */
    private Date updateTime;

    /** 数据权限所属组织 */
    private String dataOrgId;

    /** 数据权限操作员 */
    private String dataOperatorId;
    
    private Integer age;
    
    private BigDecimal salary;

    private static final long serialVersionUID = 1L;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode == null ? null : userCode.trim();
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd == null ? null : userPwd.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt == null ? null : salt.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getIsSupervisor() {
        return isSupervisor;
    }

    public void setIsSupervisor(String isSupervisor) {
        this.isSupervisor = isSupervisor == null ? null : isSupervisor.trim();
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus == null ? null : userStatus.trim();
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId == null ? null : orgId.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDataOrgId() {
        return dataOrgId;
    }

    public void setDataOrgId(String dataOrgId) {
        this.dataOrgId = dataOrgId == null ? null : dataOrgId.trim();
    }

    public String getDataOperatorId() {
        return dataOperatorId;
    }

    public void setDataOperatorId(String dataOperatorId) {
        this.dataOperatorId = dataOperatorId == null ? null : dataOperatorId.trim();
    }

    public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", userName=").append(userName);
        sb.append(", userCode=").append(userCode);
        sb.append(", userPwd=").append(userPwd);
        sb.append(", userType=").append(userType);
        sb.append(", salt=").append(salt);
        sb.append(", phone=").append(phone);
        sb.append(", email=").append(email);
        sb.append(", isSupervisor=").append(isSupervisor);
        sb.append(", userStatus=").append(userStatus);
        sb.append(", orgId=").append(orgId);
        sb.append(", createUser=").append(createUser);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", dataOrgId=").append(dataOrgId);
        sb.append(", dataOperatorId=").append(dataOperatorId);
        sb.append(", age=").append(age);
        sb.append(", salary=").append(salary);
        sb.append("]");
        return sb.toString();
    }
}