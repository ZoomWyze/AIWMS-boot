package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - AccountItemVo4List
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.AccountItem;

public class AccountItemVo4List extends AccountItem {

    private String accountName;

    private String inOutItemName;

    private String billNumber;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getInOutItemName() {
        return inOutItemName;
    }

    public void setInOutItemName(String inOutItemName) {
        this.inOutItemName = inOutItemName;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
}