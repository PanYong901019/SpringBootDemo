package win.panyong.mapper.mysql;

import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import win.panyong.mapper.InsertParameter;

import java.util.List;

@Repository
public interface MysqlCommonMapper {

    @Options(useGeneratedKeys = true)
    @Insert("INSERT INTO ${table} ( ${columns} ) VALUES ( ${values} )")
    void executeInsertSqlQuery(InsertParameter parameter);

    @Delete("${sql}")
    void executeDeleteSqlQuery(@Param("sql") String sql);

    @Update("${sql}")
    void executeUpdateSqlQuery(@Param("sql") String sql);

    @Select("${sql}")
    List<JSONObject> executeSelectSqlQuery(@Param("sql") String sql);

    @Select("${sql}")
    List<JSONObject> executeSelectSqlBuilder(JSONObject parameterMap);

    @Select("${sql}")
    JSONObject executeSelectSingleSqlQuery(@Param("sql") String sql);

    @Select("${sql}")
    JSONObject executeSelectSingleSqlBuilder(JSONObject parameterMap);
}
