package cn.beinet.codegenerate;

import cn.beinet.codegenerate.rpc.FeignNacos;
import cn.beinet.codegenerate.rpc.dto.NacosNameSpaces;
import cn.beinet.codegenerate.rpc.dto.NacosToken;
import cn.beinet.codegenerate.util.StringHelper;
import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;

//@SpringBootTest
public class UtilsTests {

    @Test
    void stringbuilder_replaceAll_test() {
        String oldStr = "aaabbbcccdddeeefffgggbbbcccdddbbb";
        StringBuilder sb = new StringBuilder(oldStr);

        int replaceTime = StringHelper.replaceAll(sb, "bb", "11bbb");
        String newStr = "aaa11bbbbcccdddeeefffggg11bbbbcccddd11bbbb";
        System.out.println(sb.toString());
        Assertions.assertEquals(3, replaceTime);
        Assertions.assertEquals(newStr, sb.toString());

        sb = new StringBuilder(oldStr);
        replaceTime = StringHelper.replaceAll(sb, "bb", "11bbb", 2);
        newStr = "aaa11bbbbcccdddeeefffggg11bbbbcccdddbbb";
        System.out.println(sb.toString());
        Assertions.assertEquals(2, replaceTime);
        Assertions.assertEquals(newStr, sb.toString());

        sb = new StringBuilder(oldStr);
        replaceTime = StringHelper.replaceAll(sb, "bb", "11bbb");
        newStr = "aaa11bbbbcccdddeeefffggg11bbbbcccddd11bbbb";
        System.out.println(sb.toString());
        Assertions.assertEquals(3, replaceTime);
        Assertions.assertEquals(newStr, sb.toString());
    }

}
