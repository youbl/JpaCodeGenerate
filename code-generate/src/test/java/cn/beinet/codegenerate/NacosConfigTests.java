package cn.beinet.codegenerate;

import cn.beinet.codegenerate.rpc.FeignNacos;
import cn.beinet.codegenerate.rpc.dto.NacosNameSpaces;
import cn.beinet.codegenerate.rpc.dto.NacosToken;
import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest
public class NacosConfigTests {

    @Autowired
    FeignNacos feignNacos;

    private String nacosUrl = "http://10.51.30.102/nacos/";
    private String nacosUser = "nacos";
    private String nacosPwd = "nacos";

    @Test
    void login_test_must_success() throws URISyntaxException {
        Assertions.assertNotNull(feignNacos);

        URI uri = new URI(nacosUrl);
        NacosToken token = feignNacos.login(uri, nacosUser, nacosPwd);
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getAccessToken());
        System.out.println(token.getAccessToken());
    }


    @Test
    void login_test_fail_user() throws URISyntaxException {
        Assertions.assertNotNull(feignNacos);

        URI uri = new URI(nacosUrl);
        try {
            feignNacos.login(uri, "xxxxx", nacosPwd);
        } catch (FeignException exp) {
            Assertions.assertEquals(exp.status(), 403);
            Assertions.assertTrue(exp.getMessage().contains("unknown user"));
            System.out.println(exp);
        }
    }


    @Test
    void login_test_fail_pwd() throws URISyntaxException {
        Assertions.assertNotNull(feignNacos);

        URI uri = new URI(nacosUrl);
        try {
            feignNacos.login(uri, nacosUser, "xxxxx");
        } catch (FeignException exp) {
            Assertions.assertEquals(exp.status(), 403);
            // 密码错，也报unknown user
            Assertions.assertTrue(exp.getMessage().contains("unknown user"));
            System.out.println(exp);
        }
    }

    @Test
    void get_namespace_test() throws URISyntaxException {
        URI uri = new URI(nacosUrl);
        NacosNameSpaces nameSpaces = feignNacos.getNamespaces(uri);

        Assertions.assertNotNull(nameSpaces);
        Assertions.assertEquals(nameSpaces.getCode(), 200);
        Assertions.assertTrue(nameSpaces.getData().size() > 0);
        System.out.println(nameSpaces.getData());
    }
}
