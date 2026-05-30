package com.jsh.erp.datasource.entities;


/**
 * Function 实体的扩展类
 * 在 Function 基础上增加关联查询字段
 *
 * @author jishenghua
 */
public class FunctionEx extends Function {

    private String parentName;

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}