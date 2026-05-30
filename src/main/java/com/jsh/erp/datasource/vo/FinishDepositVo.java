package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - FinishDepositVo
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import java.math.BigDecimal;

public class FinishDepositVo {

    private String number;

    private BigDecimal finishDeposit;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getFinishDeposit() {
        return finishDeposit;
    }

    public void setFinishDeposit(BigDecimal finishDeposit) {
        this.finishDeposit = finishDeposit;
    }
}
