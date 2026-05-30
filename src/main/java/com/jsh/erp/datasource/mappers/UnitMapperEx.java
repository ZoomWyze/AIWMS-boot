package com.jsh.erp.datasource.mappers;


/**
 * UnitMapper 扩展 Mapper 接口
 * 定义 UnitMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.Unit;
import com.jsh.erp.datasource.entities.UnitExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface UnitMapperEx {

    List<Unit> selectByConditionUnit(
            @Param("name") String name);

    int batchDeleteUnitByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String ids[]);

    void updateRatioTwoById(@Param("id") Long id);

    void updateRatioThreeById(@Param("id") Long id);
}