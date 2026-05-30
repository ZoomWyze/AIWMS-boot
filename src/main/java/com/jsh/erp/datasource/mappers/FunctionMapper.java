package com.jsh.erp.datasource.mappers;


/**
 * Function MyBatis Mapper 接口
 * 提供 Function 实体的基本 CRUD 操作（由 MyBatis Generator 生成）
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.Function;
import com.jsh.erp.datasource.entities.FunctionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionMapper {
    long countByExample(FunctionExample example);

    int deleteByExample(FunctionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Function record);

    int insertSelective(Function record);

    List<Function> selectByExample(FunctionExample example);

    Function selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Function record, @Param("example") FunctionExample example);

    int updateByExample(@Param("record") Function record, @Param("example") FunctionExample example);

    int updateByPrimaryKeySelective(Function record);

    int updateByPrimaryKey(Function record);
}