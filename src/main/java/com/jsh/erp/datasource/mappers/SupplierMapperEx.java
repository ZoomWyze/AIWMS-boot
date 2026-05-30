package com.jsh.erp.datasource.mappers;


/**
 * SupplierMapper 扩展 Mapper 接口
 * 定义 SupplierMapper 自定义的复杂 SQL 查询方法（非 Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.Supplier;
import com.jsh.erp.datasource.entities.SupplierExample;
import com.jsh.erp.datasource.vo.SupplierSimple;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SupplierMapperEx {

    List<Supplier> selectByConditionSupplier(
            @Param("supplier") String supplier,
            @Param("type") String type,
            @Param("contacts") String contacts,
            @Param("phonenum") String phonenum,
            @Param("telephone") String telephone,
            @Param("creatorArray") String[] creatorArray);

    List<Supplier> findByAll(
            @Param("supplier") String supplier,
            @Param("type") String type,
            @Param("phonenum") String phonenum,
            @Param("telephone") String telephone);

    int batchDeleteSupplierByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String ids[]);

    Supplier getSupplierByNameAndType(
            @Param("supplier") String supplier,
            @Param("type") String type);

    List<SupplierSimple> getAllCustomer();

    List<Supplier> findByTypeAndKey(
            @Param("type") String type,
            @Param("key") String key,
            @Param("limit") Integer limit);

    Supplier getInfoById(
            @Param("id") Long id);

    Supplier getInfoByName(
            @Param("name") String name,
            @Param("type") String type);
}