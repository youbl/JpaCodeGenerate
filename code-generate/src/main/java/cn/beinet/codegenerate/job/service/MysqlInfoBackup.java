package cn.beinet.codegenerate.job.service;

import cn.beinet.codegenerate.job.config.BackupConfigs;
import cn.beinet.codegenerate.model.TableDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import cn.beinet.codegenerate.util.FileHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 遍历Mysql实例的所有DB，并备份数据增长情况到本地文件
 *
 * @author : youbl
 * @create: 2022/4/11 10:57
 */
@Service
@Slf4j
public class MysqlInfoBackup implements Backup {
    private final BackupConfigs.Mysql configs;
    private final ObjectMapper objectMapper;

    public MysqlInfoBackup(BackupConfigs configs, ObjectMapper objectMapper) {
        this.configs = configs.getMysql();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean enabled() {
        return true;
//        // 只有上午10点和下午10点允许备份
//        int hour = LocalDateTime.now().getHour();
//        return hour == 10 || hour == 22;
    }

    @Override
    public void operate() {
        for (BackupConfigs.MysqlInstance item : configs.getInstances()) {
            if (item.getPort() == null) {
                item.setPort(3306);
            }
            log.info("准备备份实例 {}:{}", item.getIp(), item.getPort());

            try {
                ColumnRepository repository = ColumnRepository.getRepository(
                        item.getIp(), 3306, item.getUsername(), item.getPassword());
                List<TableDto> tables = repository.getTableInfos();
                // 格式化输出json
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tables);

                String filePath = getFileName(repository);
                FileHelper.saveFile(filePath, json);
            } catch (Exception namespaceEx) {
                log.error("备份出错:{}:{} {}", item.getIp(), item.getPort(), namespaceEx.getMessage());
            }
        }
    }

    private String getFileName(ColumnRepository repository) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        String url = repository.getIpPort();
        url = FileHelper.replaceInvalidCh(url);

        return dir + url + ".txt";
    }

}
