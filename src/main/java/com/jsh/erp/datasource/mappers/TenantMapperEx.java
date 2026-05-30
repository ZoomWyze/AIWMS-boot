package com.jsh.erp.datasource.mappers;


/**
 * TenantMapper 扩展 Mapper 接口
 * 定义 TenantMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jsh.erp.datasource.entities.TenantEx;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TenantMapperEx {

    List<TenantEx> selectByConditionTenant(
            @Param("loginName") String loginName,
            @Param("type") String type,
            @Param("enabled") String enabled,
            @Param("remark") String remark);
}