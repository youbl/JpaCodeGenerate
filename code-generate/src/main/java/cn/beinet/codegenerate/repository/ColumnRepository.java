package cn.beinet.codegenerate.repository;

import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ColumnRepository {
    private final Environment env;

    private JdbcTemplate jdbcTemplate;
    private final String configPrefix;

    public ColumnRepository(Environment env, DbEnv dbEnv) {
        this.env = env;
        this.configPrefix = dbEnv.getConfig();
    }

    public List<String> findDatabases() {
        String sql = "SELECT DISTINCT table_schema FROM information_schema.tables " +
                "WHERE table_schema NOT IN ('performance_schema', 'mysql', 'information_schema', 'sys') " +
                "ORDER BY table_schema";
        return getJdbcTemplate().query(sql, new MyRowMapper());
    }

    public List<String> findTables(String database) {
        String sql = "SELECT DISTINCT table_name FROM information_schema.tables WHERE table_schema=? ORDER BY table_name";
        return getJdbcTemplate().query(sql, new Object[]{database}, new MyRowMapper());
    }

    /**
     * 返回指定表的字段定义
     *
     * @param database 数据库
     * @param table    表名
     * @return 字段列表
     */
    public List<ColumnDto> findColumnByTable(String database, String table) {
        String sql = "SELECT c.table_schema, c.table_name, c.column_name, c.ordinal_position, " +
                "c.column_default, c.column_type, c.is_nullable, " +
                "CASE WHEN c.column_key = 'PRI' THEN TRUE ELSE FALSE END AS is_primarykey, c.column_comment, c.extra " +
                " FROM information_schema.columns c " +
                " WHERE c.table_schema = ? ";

        List<Object> arrPara = new ArrayList<>();
        arrPara.add(database);
        if (StringUtils.hasText(table)) {
            sql += "AND c.table_name=? ";
            arrPara.add(table);
        }
        sql += "ORDER BY c.table_schema, c.ordinal_position";
        return getJdbcTemplate().query(sql, arrPara.toArray(new Object[0]), (resultSet, i) -> {
            ColumnDto ret = new ColumnDto();
            ret.setCatalog(resultSet.getString("table_schema"));
            ret.setTable(resultSet.getString("table_name"));
            ret.setColumn(resultSet.getString("column_name"));
            ret.setPosition(resultSet.getLong("ordinal_position"));
            ret.setDefaultVal(resultSet.getString("column_default"));
            ret.setType(resultSet.getString("column_type"));
            ret.setPrimaryKey(resultSet.getBoolean("is_primarykey"));
            ret.setNullable("YES".equalsIgnoreCase(resultSet.getString("is_nullable")));
            ret.setComment(resultSet.getString("column_comment"));
            ret.setExtra(resultSet.getString("extra"));
            return ret;
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

    private JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            //dataSource.setDriverClassName(env.getProperty("xxx.driver-class-name"));
            dataSource.setUrl(env.getProperty(configPrefix + ".url"));
            dataSource.setUsername(env.getProperty(configPrefix + ".username"));
            dataSource.setPassword(env.getProperty(configPrefix + ".password"));

            //创建JdbcTemplate对象，设置数据源
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    public enum DbEnv {
        /**
         * 默认的数据库连接串
         */
        DEFAULT("spring.datasource"),
        /**
         * 测试环境数据库连接串
         */
        TEST("spring.datasourceTest"),
        /**
         * 生产环境数据库连接串
         */
        PROD("spring.datasourceProd");

        final String config;

        DbEnv(String config) {
            this.config = config;
        }

        public String getConfig() {
            return config;
        }
    }
}
