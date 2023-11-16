package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.util.SpringUtil;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/11/16 11:12
 */
public final class Consts {
    public static final String SDK_HEADER_NAME = "simple-auth";

    public static String getSdkAppKey() {
        return SpringUtil.getEnv().getProperty("sdk.app-key");
    }

    public static String getSdkSecurityKey() {
        return SpringUtil.getEnv().getProperty("sdk.secure-key");
    }
}
