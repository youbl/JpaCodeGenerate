package cn.beinet.codegenerate.util;

import cn.beinet.codegenerate.job.backup.BackupConfigs;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
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
        if (!enabled()) {
            return;
        }

        try {
            log.info("Git初始化开始");
            cloneGitIgnoreSsl();
            log.info("Git初始化成功");
        } catch (Exception e) {
            log.error("Git初始化失败", e);
        }
    }

    public boolean enabled() {
        return (configs.getEnable() != null && configs.getEnable());
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

    // 与cloneGit方法对比，这个方法，可以绕过https认证，直接克隆
    @SneakyThrows
    private void cloneGitIgnoreSsl() {
        File dir = new File(configs.getGitlab().getRootDir());
        if (dir.exists()) {
            log.info("Git目录已存在，跳过初始化");
            return;
        }
        Git repo = Git.init().setDirectory(dir).call();
        repo.getRepository()
                .getConfig()
                .setBoolean("http", null, "sslVerify", false);
        //.setBoolean("http", "https://git.beinet.cn", "sslVerify", false);
        RemoteAddCommand command = repo.remoteAdd();
        command.setName("origin");
        URIish uri = new URIish(configs.getGitlab().getUrl());
        command.setUri(uri);
        command.call();
        repo.pull()
                .setCredentialsProvider(getCredentialsProvider())
                .setRemote("origin")
                .setRemoteBranchName("master")
                .call();
    }

    // 无法跳过ssl认证，如果不需要，可以直接用这个方法
    @SneakyThrows
    private void cloneGit() {
        File dir = new File(configs.getGitlab().getRootDir());
        if (dir.exists()) {
            log.info("Git目录已存在，跳过初始化");
            return;
        }
        CloneCommand command = Git.cloneRepository()
                .setURI(configs.getGitlab().getUrl())
                .setCredentialsProvider(getCredentialsProvider())
                .setBranch("master")
                .setDirectory(dir);
        // cloneRepository 无法getConfig做操作
//            command.getRepository().getConfig()
//                    .setBoolean("http", "https://git.ziniao.com", "sslVerify", false);
        command.call();
    }
}
