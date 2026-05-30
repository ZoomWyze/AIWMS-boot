package com.jsh.erp.datasource.mappers;


/**
 * PlatformConfigMapper 扩展 Mapper 接口
 * 定义 PlatformConfigMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.PlatformConfig;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface PlatformConfigMapperEx {

    List<PlatformConfig> selectByConditionPlatformConfig(
            @Param("platformKey") String platformKey);

}