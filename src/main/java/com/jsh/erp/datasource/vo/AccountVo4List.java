package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - AccountVo4List
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.Account;

public class AccountVo4List extends Account{

    private String thisMonthAmount;

    public String getThisMonthAmount() {
        return thisMonthAmount;
    }

    public void setThisMonthAmount(String thisMonthAmount) {
        this.thisMonthAmount = thisMonthAmount;
    }
}