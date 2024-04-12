package cn.beinet.codegenerate.repository;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.model.IndexDto;
import cn.beinet.codegenerate.model.TableDto;
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

    private JdbcTemplate jdbcTemplate;

    private final String url;
    private final String userName;
    private final String pwd;

    public static ColumnRepository getRepository(String ip,
                                                 int port,
                                                 String user,
                                                 String pwd) {
        int idx = ip.indexOf(':');
        if (idx > 0) {
            String tmp = ip.substring(idx + 1);
            port = Integer.parseInt(tmp);
            ip = ip.substring(0, idx);
        }
        return new ColumnRepository(ip, port, user, pwd, null);
    }

    public ColumnRepository(Environment env, DbEnv dbEnv) {
        String configPrefix = dbEnv.getConfig();
        this.url = env.getProperty(configPrefix + ".url");
        this.userName = env.getProperty(configPrefix + ".username");
        this.pwd = env.getProperty(configPrefix + ".password");
    }


    public ColumnRepository(String ip, int port, String userName, String pwd, String db) {
        if (!StringUtils.hasLength(db))
            db = "information_schema";
        this.url = "jdbc:mysql://" + ip + ":" + port + "/" + db +
                "?characterEncoding=utf8&allowMultiQueries=false&serverTimezone=Asia/Shanghai&useSSL=false&socketTimeout=1000&connectTimeout=1000";
        // ?allowMultiQueries=true&useUnicode=true&characterEncoding=utf8&socketTimeout=2000&connectTimeout=2000&rewriteBatchedStatements=true&useSSL=false&serverTimezone=Asia/Shanghai&useSSL=false
        this.userName = userName;
        this.pwd = pwd;
    }

    public List<String> findDatabases() {
        String sql = "SELECT DISTINCT schema_name FROM information_schema.SCHEMATA " +
                "WHERE schema_name NOT IN ('performance_schema', 'mysql', 'information_schema', 'sys') " +
                "ORDER BY schema_name";
        return getJdbcTemplate().query(sql, new MyRowMapper());
    }

    public List<String> findTables(String database) {
        String sql = "SELECT DISTINCT table_name FROM information_schema.tables WHERE table_schema=? ORDER BY table_name";
        return getJdbcTemplate().query(sql, new Object[]{database}, new MyRowMapper());
    }

    public String getTableDDL(String database, String tableName) {
        String sql = "SHOW CREATE TABLE `" + database + "`.`" + tableName + "`";
        List<String> ret = getJdbcTemplate().query(sql, new MyRowMapper(2));
        return ret.get(0);
    }

    /**
     * 返回指定表的字段定义
     *
     * @param database 数据库
     * @param table    表名，为空表示返回所有表
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

    /**
     * 返回指定表的索引定义
     *
     * @param database 数据库
     * @param table    表名，为空表示返回所有表
     * @return 索引列表
     */
    public List<IndexDto> findIndexesByTable(String database, String table) {
        String sql = "SELECT b.TABLE_NAME,b.INDEX_NAME, b.SEQ_IN_INDEX, b.COLUMN_NAME, b.SUB_PART, b.NON_UNIQUE, b.INDEX_TYPE, b.INDEX_COMMENT " +
                "FROM information_schema.STATISTICS b " +
                " WHERE b.TABLE_SCHEMA=? ";

        List<Object> arrPara = new ArrayList<>();
        arrPara.add(database);
        if (StringUtils.hasText(table)) {
            sql += "AND b.TABLE_NAME=? ";
            arrPara.add(table);
        }
        sql += "ORDER BY b.TABLE_NAME, b.INDEX_NAME, b.SEQ_IN_INDEX";
        return getJdbcTemplate().query(sql, arrPara.toArray(new Object[0]), (resultSet, i) -> {
            IndexDto ret = new IndexDto();
            ret.setCatalog(database);
            ret.setTable(resultSet.getString("table_name"));
            ret.setIndexName(resultSet.getString("INDEX_NAME"));
            ret.setIndex(resultSet.getInt("SEQ_IN_INDEX"));
            ret.setColumn(resultSet.getString("COLUMN_NAME"));

            String subPart = resultSet.getString("SUB_PART");
            if (StringUtils.isEmpty(subPart))
                ret.setSubPart(0);
            else
                ret.setSubPart(Integer.parseInt(subPart));

            int unique = resultSet.getInt("NON_UNIQUE");
            ret.setUnique(unique == 0);
            ret.setIndexType(resultSet.getString("INDEX_TYPE"));
            ret.setComment(resultSet.getString("INDEX_COMMENT"));
            return ret;
        });
    }

    /**
     * 获取表行数、空间占用数据
     *
     * @return
     */
    public List<TableDto> getTableInfos() {
        String sql = "SELECT t.table_schema, t.table_name, t.table_rows, t.avg_row_length, t.data_length, t.index_length, " +
                "t.auto_increment, t.create_time, t.table_comment " +
                "FROM information_schema.tables t " +
                "ORDER BY t.table_schema, t.table_name";
        return getJdbcTemplate().query(sql, (resultSet, i) -> {
            TableDto ret = new TableDto();
            ret.setTable_schema(resultSet.getString("table_schema"));
            ret.setTable_name(resultSet.getString("table_name"));
            ret.setTable_rows(resultSet.getLong("table_rows"));
            ret.setAvg_row_length(resultSet.getLong("avg_row_length"));
            ret.setData_length(resultSet.getLong("data_length"));
            ret.setIndex_length(resultSet.getLong("index_length"));
            ret.setAuto_increment(resultSet.getLong("auto_increment"));
            ret.setCreate_time(resultSet.getString("create_time"));
            ret.setTable_comment(resultSet.getString("table_comment"));

            return ret;
        });
    }

    static class MyRowMapper implements RowMapper<String> {
        private int colIndex;

        public MyRowMapper() {
            this(1);
        }

        public MyRowMapper(int colIndex) {
            this.colIndex = colIndex;
        }

        /**
         * 把数据库的行，转换为字符串返回
         *
         * @param resultSet 数据集
         * @param i         当前第几行数据
         * @return 行数据得到的结果
         * @throws SQLException 数据库异常
         */
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(colIndex);
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            //dataSource.setDriverClassName(env.getProperty("xxx.driver-class-name"));
            dataSource.setUrl(url);
            dataSource.setUsername(userName);
            dataSource.setPassword(pwd);

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
         * 左库连接串
         */
        TEST("spring.datasourceTest"),
        /**
         * 右库连接串
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

    public String getIpPort() {
        String ret = url;

        String findStr1 = "://";
        int idx = ret.indexOf(findStr1);
        if (idx >= 0) {
            ret = ret.substring(idx + findStr1.length());
        }
        idx = ret.indexOf("/");
        if (idx > 0) {
            ret = ret.substring(0, idx);
        } else {
            idx = ret.indexOf("?");
            if (idx > 0) {
                ret = ret.substring(0, idx);
            }
        }

        return ret;
    }
}
