package com.jsh.erp.datasource.mappers;


/**
 * FunctionMapper 扩展 Mapper 接口
 * 定义 FunctionMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.FunctionEx;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface FunctionMapperEx {

    List<FunctionEx> selectByConditionFunction(
            @Param("name") String name,
            @Param("type") String type);

    int batchDeleteFunctionByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String ids[]);
}