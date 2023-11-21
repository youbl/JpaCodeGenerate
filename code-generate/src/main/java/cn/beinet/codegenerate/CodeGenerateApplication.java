package cn.beinet.codegenerate;

import cn.beinet.codegenerate.util.AESUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableAsync
public class CodeGenerateApplication {

    public static void main(String[] args) {
        System.out.println(AESUtil.encrypt("abc"));
        SpringApplication.run(CodeGenerateApplication.class, args);
    }

}
