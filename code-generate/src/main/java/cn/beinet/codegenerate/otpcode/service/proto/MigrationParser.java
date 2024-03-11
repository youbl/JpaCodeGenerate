package cn.beinet.codegenerate.otpcode.service.proto;

import cn.beinet.codegenerate.otpcode.service.OtpCodeGeneratorTool;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Base64;

/**
 * 解析"Google Authenticator"导出的二维码数据，
 * 该二维码解析得到的数据格式如下：
 * otpauth-migration://offline?data=xxx
 * <p>
 * 该数据是proto格式数据，需要通过proto协议转换：
 * 1、定义proto格式文件，这个是固定的，参考：MigrationPayload.proto
 * 2、下载 protoc.exe 程序，去这里下载：https://github.com/protocolbuffers/protobuf/releases
 * 我下载的是： protoc-25.0-win64
 * 3、解压，并执行命令，生成java类文件：
 * protoc-25.0-win64\bin\protoc.exe --java_out=. MigrationPayload.proto
 * 4、把生成的文件复制到项目里
 * 5、项目pom引用：
 * <dependency>
 * <groupId>com.google.protobuf</groupId>
 * <artifactId>protobuf-java</artifactId>
 * <version>3.25.3</version>
 * </dependency>
 * 6、OK，再使用本封装类
 *
 * @author youbl
 * @since 2024/3/11 19:53
 */
public class MigrationParser {
    /**
     * 转换为标准的 otpauth 协议串.
     * 注：只解析第一个结果
     *
     * @return 标准otpauth协议串
     */
    @SneakyThrows
    public static String convert(String otpauthMigration) {
        if (!StringUtils.hasLength(otpauthMigration))
            throw new RuntimeException("参数不能为空");
        URI uri = new URI(otpauthMigration);
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.equals("otpauth-migration"))
            throw new RuntimeException(uri + " Unsupported protocol");

        String host = uri.getHost();
        if (host == null || !host.equals("offline"))
            throw new RuntimeException(uri + " Unsupported host");

        String data = uri.getQuery().substring(5);
        byte[] decodeArr = Base64.getDecoder().decode(data);
        MigrationPayloadOuterClass.MigrationPayload payload =
                MigrationPayloadOuterClass.MigrationPayload.parseFrom(decodeArr);
        if (payload.getOtpParametersCount() <= 0)
            return "";

        MigrationPayloadOuterClass.MigrationPayload.OtpParameters parameter = payload.getOtpParameters(0);
        String title = parameter.getName();
        byte[] secretArr = parameter.getSecret().toByteArray();
        String secret = OtpCodeGeneratorTool.restoreSecret(secretArr);
        return OtpCodeGeneratorTool.getQRBarcode("", title, secret);
    }
}
