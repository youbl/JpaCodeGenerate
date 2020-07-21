package cn.beinet.codegenerate.repository;

import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ColumnRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> findDatabases() {
        String sql = "SELECT DISTINCT table_schema FROM information_schema.tables " +
                "WHERE table_schema NOT IN ('performance_schema', 'mysql', 'information_schema', 'sys') " +
                "ORDER BY table_schema";
        return jdbcTemplate.query(sql, new MyRowMapper());
    }

    public List<String> findTables(String database) {
        String sql = "SELECT DISTINCT table_name FROM information_schema.tables WHERE table_schema=? ORDER BY table_name";
        return jdbcTemplate.query(sql, new Object[]{database}, new MyRowMapper());
    }

    /**
     * 返回指定表的字段定义
     * @param database 数据库
     * @param table 表名
     * @return 字段列表
     */
    public List<ColumnDto> findColumnByTable(String database, String table) {
        String sql = "SELECT c.table_schema, c.table_name, c.column_name, c.ordinal_position, c.column_default, c.column_type, " +
                "CASE WHEN c.column_key = 'PRI' THEN TRUE ELSE FALSE END AS is_primarykey, c.column_comment, c.extra " +
                " FROM information_schema.columns c " +
                " WHERE c.table_schema = ? AND c.table_name=? " +
                " ORDER BY c.ordinal_position";
        return jdbcTemplate.query(sql, new Object[]{database, table}, new RowMapper<ColumnDto>() {
            @Override
            public ColumnDto mapRow(ResultSet resultSet, int i) throws SQLException {
                ColumnDto ret = new ColumnDto();
                ret.setCatalog(resultSet.getString("table_schema"));
                ret.setTable(resultSet.getString("table_name"));
                ret.setColumn(resultSet.getString("column_name"));
                ret.setPosition(resultSet.getLong("ordinal_position"));
                ret.setDefaultVal(resultSet.getString("column_default"));
                ret.setType(resultSet.getString("column_type"));
                ret.setPrimaryKey(resultSet.getBoolean("is_primarykey"));
                ret.setComment(resultSet.getString("column_comment"));
                ret.setExtra(resultSet.getString("extra"));
                return ret;
            }
        });
    }

    static class MyRowMapper implements RowMapper<String> {
        /**
         * 把数据库的行，转换为字符串返回
         *
         * @param resultSet 数据集
         * @param i         当前第几行数据
         * @return 行数据得到的结果
         * @throws SQLException 数据库异常
         */
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(1);
        }
    }
}
