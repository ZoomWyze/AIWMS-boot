package com.jsh.erp.datasource.mappers;


/**
 * MaterialCurrentStockMapper 扩展 Mapper 接口
 * 定义 MaterialCurrentStockMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.MaterialCurrentStock;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MaterialCurrentStockMapperEx {

    int batchInsert(List<MaterialCurrentStock> list);

    List<MaterialCurrentStock> getCurrentStockMapByIdList(
            @Param("materialIdList") List<Long> materialIdList);

    void updateUnitPriceByMId(
            @Param("currentUnitPrice") BigDecimal currentUnitPrice,
            @Param("materialId") Long materialId);

    BigDecimal getCurrentUnitPriceByMId(@Param("materialId") Long materialId);

    void batchDeleteByDepots(@Param("ids") String ids[]);
}