package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - MaterialDepotStock
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialDepotStock {

    private Long id;

    private Long materialId;

    private Long depotId;

    private String depotName;

    private BigDecimal currentNumber;

    private BigDecimal currentUnitPrice;

    private BigDecimal purchaseDecimal;

    private BigDecimal unitPrice;

    private BigDecimal allPrice;

}