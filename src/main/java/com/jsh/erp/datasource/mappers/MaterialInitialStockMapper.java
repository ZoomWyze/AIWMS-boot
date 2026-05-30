package com.jsh.erp.datasource.mappers;


/**
 * MaterialInitialStock MyBatis Mapper 接口
 * 提供 MaterialInitialStock 实体的基本 CRUD 操作（由 MyBatis Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.MaterialInitialStock;
import com.jsh.erp.datasource.entities.MaterialInitialStockExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MaterialInitialStockMapper {
    long countByExample(MaterialInitialStockExample example);

    int deleteByExample(MaterialInitialStockExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MaterialInitialStock record);

    int insertSelective(MaterialInitialStock record);

    List<MaterialInitialStock> selectByExample(MaterialInitialStockExample example);

    MaterialInitialStock selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MaterialInitialStock record, @Param("example") MaterialInitialStockExample example);

    int updateByExample(@Param("record") MaterialInitialStock record, @Param("example") MaterialInitialStockExample example);

    int updateByPrimaryKeySelective(MaterialInitialStock record);

    int updateByPrimaryKey(MaterialInitialStock record);
}