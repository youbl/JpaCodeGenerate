package cn.beinet.codegenerate.job.service;

import cn.beinet.codegenerate.job.config.NacosConfigs;
import cn.beinet.codegenerate.service.NacosService;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/4/8 10:57
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NacosConfigBackup implements Backup {
    private final NacosConfigs configs;
    private final NacosService nacosService;

    @Override
    public void operate() {
        for (NacosConfigs.Nacos item : configs.getNacos()) {
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

    private void backupNameSpace(String nameSpace, NacosConfigs.Nacos item) {
        log.info("准备备份ns {} {}", item.getUrl(), nameSpace);
        int idx = 0;
        try {
            List<String> fileList = nacosService.getFiles(
                    item.getUrl(),
                    item.getUsername(),
                    item.getPassword(),
                    nameSpace);
            for (String file : fileList) {
                backupFile(nameSpace, file, item);
                idx++;
            }
            log.info("备份ns结束:{} {}个文件", nameSpace, idx);
        } catch (Exception exp) {
            log.error("备份ns出错:{}-{} {}", nameSpace, idx, exp.getMessage());
        }
    }

    private void backupFile(String nameSpace, String file, NacosConfigs.Nacos item) {
        try {
            String content = nacosService.getFile(
                    item.getUrl(),
                    item.getUsername(),
                    item.getPassword(),
                    nameSpace,
                    file,
                    "DEFAULT_GROUP");
            String filePath = getFileName(nameSpace, file, item);
            FileHelper.saveFile(filePath, content);
        } catch (Exception exp) {
            log.error("备份file出错:{} {}", file, exp.getMessage());
        }
    }

    private String getFileName(String nameSpace, String file, NacosConfigs.Nacos item) {
        String dir = configs.getLocalDir();
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
