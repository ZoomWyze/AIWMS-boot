package com.jsh.erp.datasource.entities;


/**
 * Msg 实体的扩展类
 * 在 Msg 基础上增加关联查询字段
 *
 * @author jishenghua
 */
public class MsgEx extends Msg{

    private String createTimeStr;

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }
}