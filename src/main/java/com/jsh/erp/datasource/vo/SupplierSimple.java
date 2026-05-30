package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - SupplierSimple
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import lombok.Data;

@Data
public class SupplierSimple {

    private Long id;

    private String supplier;

}
