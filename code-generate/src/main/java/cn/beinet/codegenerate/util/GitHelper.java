package cn.beinet.codegenerate.util;

import cn.beinet.codegenerate.job.config.BackupConfigs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/4/8 15:00
 */
@Component
@RequiredArgsConstructor
@Slf4j
public final class GitHelper {
    private final BackupConfigs configs;

    @PostConstruct
    public void init() {
        File dir = new File(configs.getGitlab().getRootDir());
        if (dir.exists()) {
            log.info("Git目录已存在，跳过初始化");
            return;
        }
        UsernamePasswordCredentialsProvider provider = getCredentialsProvider();
        try {
            Git.cloneRepository()
                    .setURI(configs.getGitlab().getUrl())
                    .setCredentialsProvider(provider)
                    .setBranch("master")
                    .setDirectory(dir)
                    .call();
            log.info("Git初始化成功");
        } catch (GitAPIException e) {
            log.error("Git初始化失败", e);
        }
    }

    public boolean pull() {
        File dir = new File(configs.getGitlab().getRootDir());
        UsernamePasswordCredentialsProvider provider = getCredentialsProvider();
        try {
            Git git = Git.open(dir);
            PullResult result = git.pull()
                    .setCredentialsProvider(provider)
                    .call();
            log.info("pull结果: " + result.toString());
            return true;
        } catch (Exception e) {
            log.error("pull失败:", e);
            return false;
        }
    }

    public boolean commit(String msg) {
        File dir = new File(configs.getGitlab().getRootDir());
        UsernamePasswordCredentialsProvider provider = getCredentialsProvider();
        try {
            Git git = Git.open(dir);
            git.add().addFilepattern(".").call(); // 新增的文件
            git.add().setUpdate(true).addFilepattern(".").call(); // 更新的文件

            git.commit().setMessage(msg).setAllowEmpty(false).setAll(true).call();
            Iterable<PushResult> results = git.push()
                    .setCredentialsProvider(provider)
                    .call();
            StringBuilder str = new StringBuilder();
            for (PushResult item : results) {
                str.append(item).append("; ");
            }
            log.info("提交结果: " + str);
            return true;
        } catch (EmptyCommitException e) {
            log.warn("无变更");
            return false;
        } catch (Exception e) {
            log.error("提交失败:", e);
            return false;
        }

    }

    private UsernamePasswordCredentialsProvider getCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(
                configs.getGitlab().getUsername(),
                configs.getGitlab().getPassword()
        );
    }
}
