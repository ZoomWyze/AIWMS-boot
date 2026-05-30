package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - MaterialCountVo
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import java.math.BigDecimal;

public class MaterialCountVo {

    private Long headerId;

    private BigDecimal materialCount;

    public Long getHeaderId() {
        return headerId;
    }

    public void setHeaderId(Long headerId) {
        this.headerId = headerId;
    }

    public BigDecimal getMaterialCount() {
        return materialCount;
    }

    public void setMaterialCount(BigDecimal materialCount) {
        this.materialCount = materialCount;
    }
}
