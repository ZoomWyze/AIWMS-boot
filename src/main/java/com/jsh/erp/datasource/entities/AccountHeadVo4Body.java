package com.jsh.erp.datasource.entities;


/**
 * 视图对象 (VO)
 * 用于 AccountHeadVo4Body 场景的数据传输
 *
 * @author jishenghua
 */
public class AccountHeadVo4Body {

    private Long id;

    private String info;

    private String rows;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }
}