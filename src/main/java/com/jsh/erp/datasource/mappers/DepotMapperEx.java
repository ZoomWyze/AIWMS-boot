package com.jsh.erp.datasource.mappers;


/**
 * DepotMapper 扩展 Mapper 接口
 * 定义 DepotMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.Depot;
import com.jsh.erp.datasource.entities.DepotEx;
import com.jsh.erp.datasource.entities.DepotExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DepotMapperEx {

    List<DepotEx> selectByConditionDepot(
            @Param("name") String name,
            @Param("type") Integer type,
            @Param("remark") String remark);

    int batchDeleteDepotByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String ids[]);
}