package cn.beinet.codegenerate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

public class MySqlExecuteRepository {

    private JdbcTemplate jdbcTemplate;

    private final String url;
    private final String userName;
    private final String pwd;

    public MySqlExecuteRepository(String ip, int port, String userName, String pwd, String dbName) {
        this.url = "jdbc:mysql://" + ip + ":" + port +
                "/" + dbName + "?characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai&useSSL=false";
        this.userName = userName;
        this.pwd = pwd;
    }

    public List<Map<String, Object>> executeSql(String sql) {
        return getJdbcTemplate().queryForList(sql);//.query(sql, new MyRowMapper());
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

            //创建JdbcTemplate对象，设置数据源
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

}
