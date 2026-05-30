package com.jsh.erp.datasource.mappers;


/**
 * MaterialAttributeMapper 扩展 Mapper 接口
 * 定义 MaterialAttributeMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.MaterialAttribute;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MaterialAttributeMapperEx {

    List<MaterialAttribute> selectByConditionMaterialAttribute(
            @Param("attributeName") String attributeName,
            @Param("attributeValue") String attributeValue);

    int batchDeleteMaterialAttributeByIds(
            @Param("ids") String ids[]);
}