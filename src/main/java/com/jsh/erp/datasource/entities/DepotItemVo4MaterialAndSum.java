package com.jsh.erp.datasource.entities;


/**
 * 视图对象 (VO)
 * 用于 DepotItemVo4MaterialAndSum 场景的数据传输
 *
 * @author jishenghua
 */
import java.math.BigDecimal;

public class DepotItemVo4MaterialAndSum {

    private Long materialExtendId;

    private BigDecimal operNumber;

    public Long getMaterialExtendId() {
        return materialExtendId;
    }

    public void setMaterialExtendId(Long materialExtendId) {
        this.materialExtendId = materialExtendId;
    }

    public BigDecimal getOperNumber() {
        return operNumber;
    }

    public void setOperNumber(BigDecimal operNumber) {
        this.operNumber = operNumber;
    }
}