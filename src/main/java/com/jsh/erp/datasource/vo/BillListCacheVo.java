package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - BillListCacheVo
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
public class BillListCacheVo {

    private String number;
    
    private String organName;

    private String operTimeStr;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getOrganName() {
        return organName;
    }

    public void setOrganName(String organName) {
        this.organName = organName;
    }

    public String getOperTimeStr() {
        return operTimeStr;
    }

    public void setOperTimeStr(String operTimeStr) {
        this.operTimeStr = operTimeStr;
    }
}
