package cn.beinet.codegenerate.repository;

import cn.beinet.codegenerate.linkinfo.service.entity.LinkInfo;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySqlExecuteRepository {

    private JdbcTemplate jdbcTemplate;

    private String url;
    private String userName;
    private String pwd;
    private String ip;
    private String db;

    public MySqlExecuteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initField();
    }

    public MySqlExecuteRepository(LinkInfo info, String dbName) {
        this(info.getAddress(), info.getPort(), info.getAccount(), info.getPwd(), dbName, null);
    }

    public MySqlExecuteRepository(String ip, int port, String userName, String pwd, String dbName, Integer timeout) {
        if (timeout == null || timeout <= 0)
            timeout = 5;
        timeout *= 1000;

        if (port <= 0)
            port = 3306;
        this.url = "jdbc:mysql://" + ip + ":" + port +
                "/" + dbName + "?characterEncoding=utf8&allowMultiQueries=false&serverTimezone=Asia/Shanghai" +
                "&useSSL=false&socketTimeout=" + timeout + "&connectTimeout=" + timeout;
        this.userName = userName;
        this.pwd = pwd;

        this.ip = ip;
        this.db = dbName;
    }

    private void initField() {
        this.url = "";
        this.userName = "";
        this.pwd = "";
        this.db = "";
        this.ip = "";
        if (jdbcTemplate == null)
            return;
        if (jdbcTemplate.getDataSource() instanceof HikariDataSource) {
            HikariDataSource source = (HikariDataSource) jdbcTemplate.getDataSource();
            this.url = source.getJdbcUrl();
        } else if (jdbcTemplate.getDataSource() instanceof DriverManagerDataSource) {
            DriverManagerDataSource source = (DriverManagerDataSource) jdbcTemplate.getDataSource();
            this.url = source.getUrl();
        }
        if (StringUtils.hasLength(this.url)) {
            Matcher matcher = Pattern.compile("mysql://([^/?]+)/([^/?]+)(\\?|$)").matcher(this.url);
            if (matcher.find()) {
                this.ip = matcher.group(1);
                this.db = matcher.group(2);
            }
        }
    }

    public String getIp() {
        return ip;
    }

    public String getDb() {
        return db;
    }

    public List<Map<String, Object>> queryData(String sql) {
        return getJdbcTemplate().queryForList(sql);//.query(sql, new MyRowMapper());
    }

    public int executeDml(String sql, Object... args) {
        if (args == null || args.length <= 0)
            return getJdbcTemplate().update(sql);
        return getJdbcTemplate().update(sql, args);
    }

    /**
     * 返回表的自定义字段列表（不含计算列）
     *
     * @param db    数据库
     * @param table 表名
     * @return 字段列表
     */
    public List<String> findColumnByTable(String db, String table) {
        String sql = "SELECT a.COLUMN_NAME FROM information_schema.columns a " +
                "WHERE a.table_schema=? and a.TABLE_NAME=? AND a.GENERATION_EXPRESSION=''";
        return getJdbcTemplate().queryForList(sql, String.class, db, table);
    }

    /**
     * 返回首行首列
     *
     * @param sql sql
     * @return 结果
     */
    public Object queryOneField(String sql) {
        List<Object> objList = getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getObject(1));
        if (objList.isEmpty()) {
            return null;
        }
        return objList.get(0);
    }

    /**
     * 指定的表是否存在。
     *
     * @param db        数据库
     * @param tableName 表名
     * @return 是否存在
     */
    public boolean existTable(String db, String tableName) {
        String sql = "select count(1) from information_schema.tables where table_schema=? and table_name = ?";
        Integer ret = getJdbcTemplate().queryForObject(sql, Integer.class, db, tableName);
        return ret != null && ret > 0;
    }

//    static class MyRowMapper implements RowMapper<Map<String, String>> {
//        /**
//         * 把数据库的行，转换为字符串返回
//         *
//         * @param resultSet 数据集
//         * @param colIdx    当前第几行数据
//         * @return 行数据得到的结果
//         * @throws SQLException 数据库异常
//         */
//        public Map<String, String> mapRow(ResultSet resultSet, int colIdx) throws SQLException {
//            Map<String, String> row = new HashMap<>();
//            ResultSetMetaData rsmd = resultSet.getMetaData();
//            for (int i = 0, j = rsmd.getColumnCount(); i < j; i++) {
//                String colName = rsmd.getColumnName(i);
//                String colVal = resultSet.getString(colName);
//
//                row.put(colName, colVal);
//            }
//
//            return row;
//        }
//    }

    private JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            //dataSource.setDriverClassName(env.getProperty("xxx.driver-class-name"));
            dataSource.setUrl(url);
            dataSource.setUsername(userName);
            dataSource.setPassword(pwd);

            Properties properties = new Properties();
            // 转换MySQL的 0000-00-00 日期，避免错误 Zero date value prohibited
            properties.setProperty("zeroDateTimeBehavior", "convertToNull");
            properties.setProperty("characterEncoding", "utf8");
            properties.setProperty("useSSL", "false");
            properties.setProperty("serverTimezone", "Asia/Shanghai");
            dataSource.setConnectionProperties(properties);

            //创建JdbcTemplate对象，设置数据源
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
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
