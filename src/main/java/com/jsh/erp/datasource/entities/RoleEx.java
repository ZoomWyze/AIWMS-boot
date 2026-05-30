package com.jsh.erp.datasource.entities;


/**
 * Role 实体的扩展类
 * 在 Role 基础上增加关联查询字段
 *
 * @author jishenghua
 */
public class RoleEx extends Role{

    private String priceLimitStr;

    public String getPriceLimitStr() {
        return priceLimitStr;
    }

    public void setPriceLimitStr(String priceLimitStr) {
        this.priceLimitStr = priceLimitStr;
    }
}