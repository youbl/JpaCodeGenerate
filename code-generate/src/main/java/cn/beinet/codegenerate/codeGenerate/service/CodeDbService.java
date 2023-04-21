package cn.beinet.codegenerate.codeGenerate.service;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成所需的数据库服务
 *
 * @author youbl
 * @date 2023/4/19 10:47
 */
@Service
public class CodeDbService {
    private final ColumnRepository columnRepository;

    public CodeDbService(Environment env) {
        this.columnRepository = new ColumnRepository(env, ColumnRepository.DbEnv.DEFAULT);
    }


    /**
     * 返回所有数据库名
     *
     * @return 数据库名
     */
    public List<String> getDatabases(String ip, int port, String user, String pwd) {
        return getRepository(ip, port, user, pwd).findDatabases();
    }

    /**
     * 返回指定数据库的所有表名
     *
     * @param database 数据库
     * @return 表名
     */
    public List<String> getTables(String ip, int port, String user, String pwd, String database) {
        if (!StringUtils.hasLength(database))
            return new ArrayList<>();
        return getRepository(ip, port, user, pwd).findTables(database);
    }

    /**
     * 返回指定数据库下，若干表的字段信息
     *
     * @param database 数据库
     * @param tables   表名
     * @return 字段信息
     */
    public List<ColumnDto> getFields(String ip, int port, String user, String pwd,
                                     String database, String[] tables) {
        if (!StringUtils.hasLength(database) || tables == null || tables.length <= 0)
            return new ArrayList<>();

        ColumnRepository repository = getRepository(ip, port, user, pwd);
        List<ColumnDto> ret = new ArrayList<>();
        for (String table : tables) {
            ret.addAll(repository.findColumnByTable(database, table));
        }
        return ret;
    }

    /**
     * 返回指定数据库下，表的字段信息
     *
     * @param database 数据库
     * @param table    表名
     * @return 字段信息
     */
    public List<ColumnDto> getFields(String constr, String database, String table) {
        return columnRepository.findColumnByTable(database, table);
    }

    private ColumnRepository getRepository(String ip, int port, String user, String pwd) {
        if (StringUtils.hasLength(ip)) {
            return ColumnRepository.getRepository(ip, port, user, pwd);
        }
        return this.columnRepository;
    }
}
