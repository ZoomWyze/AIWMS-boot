package com.jsh.erp.datasource.vo;



/**
 * 视图对象 (VO) - DepotHeadVo4StatementAccount
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import java.math.BigDecimal;

public class DepotHeadVo4StatementAccount {

    private Long id;

    private String supplier;

    private String contacts;

    private String telephone;

    private String phoneNum;

    private String email;

    /**
     * 璧峰鏈熷垵閲戦
     */
    private BigDecimal beginNeed;

    /**
     * 涓婃湡娆犳閲戦
     */
    private BigDecimal preDebtMoney;

    /**
     * 涓婃湡閫€璐х殑娆犳閲戦
     */
    private BigDecimal preReturnDebtMoney;


    /**
     * 涓婃湡鏀朵粯娆?     */
    private BigDecimal preBackMoney;

    /**
     * 鏈熷垵搴旀敹
     */
    private BigDecimal preNeed;

    /**
     * 鏈湡娆犳
     */
    private BigDecimal debtMoney;

    /**
     * 鏈湡閫€璐х殑娆犳閲戦
     */
    private BigDecimal returnDebtMoney;

    /**
     * 鏈湡鏀朵粯娆?     */
    private BigDecimal backMoney;

    /**
     * 鏈熸湯搴旀敹
     */
    private BigDecimal allNeed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBeginNeed() {
        return beginNeed;
    }

    public void setBeginNeed(BigDecimal beginNeed) {
        this.beginNeed = beginNeed;
    }

    public BigDecimal getPreDebtMoney() {
        return preDebtMoney;
    }

    public void setPreDebtMoney(BigDecimal preDebtMoney) {
        this.preDebtMoney = preDebtMoney;
    }

    public BigDecimal getPreReturnDebtMoney() {
        return preReturnDebtMoney;
    }

    public void setPreReturnDebtMoney(BigDecimal preReturnDebtMoney) {
        this.preReturnDebtMoney = preReturnDebtMoney;
    }

    public BigDecimal getPreBackMoney() {
        return preBackMoney;
    }

    public void setPreBackMoney(BigDecimal preBackMoney) {
        this.preBackMoney = preBackMoney;
    }

    public BigDecimal getPreNeed() {
        return preNeed;
    }

    public void setPreNeed(BigDecimal preNeed) {
        this.preNeed = preNeed;
    }

    public BigDecimal getDebtMoney() {
        return debtMoney;
    }

    public void setDebtMoney(BigDecimal debtMoney) {
        this.debtMoney = debtMoney;
    }

    public BigDecimal getReturnDebtMoney() {
        return returnDebtMoney;
    }

    public void setReturnDebtMoney(BigDecimal returnDebtMoney) {
        this.returnDebtMoney = returnDebtMoney;
    }

    public BigDecimal getBackMoney() {
        return backMoney;
    }

    public void setBackMoney(BigDecimal backMoney) {
        this.backMoney = backMoney;
    }

    public BigDecimal getAllNeed() {
        return allNeed;
    }

    public void setAllNeed(BigDecimal allNeed) {
        this.allNeed = allNeed;
    }
}