package com.jsh.erp.datasource.entities;


/**
 * Tenant 实体的扩展类
 * 在 Tenant 基础上增加关联查询字段
 *
 * @author jishenghua
 */
public class TenantEx extends Tenant{

    private String createTimeStr;

    private String expireTimeStr;

    private Integer userCount;

    private Long roleId;

    private String roleName;

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getExpireTimeStr() {
        return expireTimeStr;
    }

    public void setExpireTimeStr(String expireTimeStr) {
        this.expireTimeStr = expireTimeStr;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}