package cn.beinet.codegenerate.job.backup;

import cn.beinet.codegenerate.job.config.BackupConfigs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Description:
 * 备份指定的目录或文件
 *
 * @author : youbl
 * @create: 2022/11/17 10:57
 */
@Service
@Slf4j
public class FilesBackup implements Backup {
    private final BackupConfigs.BackFiles configs;

    public FilesBackup(BackupConfigs configs) {
        this.configs = configs.getFiles();
    }

    @Override
    public boolean enabled() {
        if (configs == null)
            return false;
        return configs.getEnable() == null || configs.getEnable();
    }

    @Override
    public void operate() {
        if (configs.getPaths() == null || configs.getPaths().length <= 0)
            return;
        for (String item : configs.getPaths()) {
            if (!StringUtils.hasLength(item)) {
                continue;
            }

            try {
                copy(item, configs.getBackDir());
            } catch (Exception namespaceEx) {
                log.error("备份出错:{} {}", item, namespaceEx.getMessage());
            }
        }
    }

    private static void copy(String source, String targetDir) throws IOException {
        File itemObj = new File(source);
        if (!itemObj.exists()) {
            log.error("要备份的文件或目录不存在: {}", source);
            return;
        }
        File itemTarget = new File(targetDir + "/" + itemObj.getName());
        if (itemObj.isDirectory()) {
            FileUtils.copyDirectory(itemObj, itemTarget);
            log.info("目录复制成功 {}", source);
        } else {
            FileUtils.copyFile(itemObj, itemTarget);
            log.info("文件复制成功 {}", source);
        }

    }
}
