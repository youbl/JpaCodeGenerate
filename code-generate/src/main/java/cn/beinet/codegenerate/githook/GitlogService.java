package cn.beinet.codegenerate.githook;

import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author youbl.blog.csdn.net
 * @since 2023-06-05 14:51:04
 */
@Service
public class GitlogService {
    private final MySqlExecuteRepository mySqlExecuteRepository;

    public GitlogService(JdbcTemplate jdbcTemplate) {
        this.mySqlExecuteRepository = new MySqlExecuteRepository(jdbcTemplate);
    }

    public int add(String data) {
        String sql = "INSERT INTO gitlog (postdata) VALUES(?)";
        return mySqlExecuteRepository.executeDml(sql, data);
    }
}