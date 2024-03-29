package cn.beinet.codegenerate.job.backup.impl;

import cn.beinet.codegenerate.controller.dto.NacosDto;
import cn.beinet.codegenerate.job.backup.Backup;
import cn.beinet.codegenerate.job.backup.BackupConfigs;
import cn.beinet.codegenerate.service.NacosService;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 遍历指定的Nacos所有配置，并进行备份
 *
 * @author : youbl
 * @create: 2022/4/8 10:57
 */
@Service
@Slf4j
public class NacosConfigBackup implements Backup {
    private final BackupConfigs.Nacos configs;
    private final NacosService nacosService;

    public NacosConfigBackup(BackupConfigs configs, NacosService nacosService) {
        this.configs = configs.getNacos();
        this.nacosService = nacosService;
    }

    @Override
    public boolean enabled() {
        if (configs == null)
            return false;
        if (configs.getEnable() != null && !configs.getEnable())
            return false;
        return true;
    }

    @Override
    public void operate() {
        if (configs.getSites() == null || configs.getSites().length <= 0)
            return;
        for (BackupConfigs.NacosSite item : configs.getSites()) {
            if (item.getEnable() != null && !item.getEnable())
                continue;

            log.info("准备备份url {}", item.getUrl());

            try {
                List<String> namespaceList = nacosService.getNamespaces(item.getUrl());
                for (String nameSpace : namespaceList) {
                    backupNameSpace(nameSpace, item);
                }
            } catch (Exception namespaceEx) {
                log.error("备份出错:{} {}", item.getUrl(), namespaceEx.getMessage());
            }
        }
    }

    private void backupNameSpace(String nameSpace, BackupConfigs.NacosSite item) {
        log.info("准备备份ns {} {}", item.getUrl(), nameSpace);
        int idx = 0;
        try {
            NacosDto dto = new NacosDto()
                    .setUrl(item.getUrl())
                    .setUser(item.getUsername())
                    .setPwd(item.getPassword())
                    .setNameSpace(nameSpace);
            List<String> fileList = nacosService.getFiles(dto);
            for (String file : fileList) {
                backupFile(nameSpace, file, item);
                idx++;
            }
            log.info("备份ns结束:{} {}个文件", nameSpace, idx);
        } catch (Exception exp) {
            log.error("备份ns出错:{}-{} {}", nameSpace, idx, exp.getMessage());
        }
    }

    private void backupFile(String nameSpace, String file, BackupConfigs.NacosSite item) {
        try {
            NacosDto dto = new NacosDto()
                    .setUrl(item.getUrl())
                    .setUser(item.getUsername())
                    .setPwd(item.getPassword())
                    .setNameSpace(nameSpace)
                    .setDataId(file)
                    .setGroup("DEFAULT_GROUP");
            String content = nacosService.getFile(dto);
            String filePath = getFileName(nameSpace, file, item);
            FileHelper.saveFile(filePath, content);
        } catch (Exception exp) {
            log.error("备份file出错:{} {}", file, exp.getMessage());
        }
    }

    private String getFileName(String nameSpace, String file, BackupConfigs.NacosSite item) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        String url = item.getUrl().replaceAll("(?i)https?://", "");
        String sufix = "/nacos/";
        if (url.endsWith(sufix))
            url = url.substring(0, url.length() - sufix.length());
        url = FileHelper.replaceInvalidCh(url);
        nameSpace = FileHelper.replaceInvalidCh(nameSpace);
        file = FileHelper.replaceInvalidCh(file);

        return dir + url + "/" + nameSpace + "/" + file;
    }

}
