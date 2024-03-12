package cn.beinet.codegenerate;

import cn.beinet.codegenerate.repository.RedisRepository;
import cn.beinet.codegenerate.util.AESUtil;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableAsync
public class CodeGenerateApplication implements CommandLineRunner {

    public static void main(String[] args) {
        System.out.println(AESUtil.encrypt("abc"));
        SpringApplication.run(CodeGenerateApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Redis内存增长，先尝试用scan输出所有的key
        // 再读取每个key的ttl，看看哪些key没有设置过期时间，并对应输出方案修复
        new Thread(CodeGenerateApplication::runJob).start();
    }

    @SneakyThrows
    public static void runJob() {
        String fileName = "/data/app/keys-0.txt";
        File keysFile = new File(fileName);
        if (!keysFile.exists())
            return;
        System.out.println("开始处理:" + fileName);

        RedisRepository redis = getRepository();
        AtomicInteger idx = new AtomicInteger();
        String keyPrefix = "STORE:OPEN_TOKEN";
        FileHelper.readEachLine(fileName, row -> {
            int currentIdx = idx.getAndIncrement();
            if (currentIdx % 10000 == 0) {
                System.out.println(currentIdx + "--");
                Thread.sleep(5);
            }

            if (row == null)
                return;
            String key = row.toString();
            if (key.isEmpty())
                return;
            long ttl = redis.getTTL(key);
            if (ttl <= 0) {
                System.out.println(key + " " + ttl);
            }
        });

        if (keysFile.delete())
            System.out.println("删除文件:" + fileName);
    }

    private static RedisRepository getRepository() {
        return new RedisRepository("redis.ip", 6379, 0, "redis.pwd");
    }
}
