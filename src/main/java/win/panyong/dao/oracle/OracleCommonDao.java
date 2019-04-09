package win.panyong.dao.oracle;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import win.panyong.dao.InsertParameter;

import java.util.List;
import java.util.Map;

@Repository("oracleCommonDaoImpl")
public interface OracleCommonDao {
    @Options(useGeneratedKeys = true)
    @Insert("INSERT INTO ${table} ( ${columns} ) VALUES ( ${values} )")
    void executeInsertSqlQuery(InsertParameter parameter);

    @Delete("${sql}")
    void executeDeleteSqlQuery(@Param("sql") String sql);

    @Update("${sql}")
    void executeUpdateSqlQuery(@Param("sql") String sql);

    @Select("${sql}")
    List<Map> executeSelectSqlQuery(@Param("sql") String sql);
}
