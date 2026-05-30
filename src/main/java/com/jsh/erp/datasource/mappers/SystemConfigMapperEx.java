package com.jsh.erp.datasource.mappers;


/**
 * SystemConfigMapper 扩展 Mapper 接口
 * 定义 SystemConfigMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.SystemConfig;
import com.jsh.erp.datasource.entities.SystemConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SystemConfigMapperEx {

    List<SystemConfig> selectByConditionSystemConfig(
            @Param("companyName") String companyName);

    int batchDeleteSystemConfigByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String ids[]);
}