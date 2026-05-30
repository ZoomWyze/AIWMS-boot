package com.jsh.erp.datasource.entities;


/**
 * 视图对象 (VO)
 * 用于 DepotItemVo4Material 场景的数据传输
 *
 * @author jishenghua
 */
import java.math.BigDecimal;
import java.util.Date;

public class DepotItemVo4Material extends DepotItem{

    private String mname;

    private String mmodel;

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getMmodel() {
        return mmodel;
    }

    public void setMmodel(String mmodel) {
        this.mmodel = mmodel;
    }
}