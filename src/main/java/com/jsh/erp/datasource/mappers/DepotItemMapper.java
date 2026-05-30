package com.jsh.erp.datasource.mappers;


/**
 * DepotItem MyBatis Mapper 接口
 * 提供 DepotItem 实体的基本 CRUD 操作（由 MyBatis Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.DepotItem;
import com.jsh.erp.datasource.entities.DepotItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DepotItemMapper {
    long countByExample(DepotItemExample example);

    int deleteByExample(DepotItemExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DepotItem record);

    int insertSelective(DepotItem record);

    List<DepotItem> selectByExample(DepotItemExample example);

    DepotItem selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DepotItem record, @Param("example") DepotItemExample example);

    int updateByExample(@Param("record") DepotItem record, @Param("example") DepotItemExample example);

    int updateByPrimaryKeySelective(DepotItem record);

    int updateByPrimaryKey(DepotItem record);
}