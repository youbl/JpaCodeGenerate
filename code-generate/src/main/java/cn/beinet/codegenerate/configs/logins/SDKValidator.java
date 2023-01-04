package cn.beinet.codegenerate.configs.logins;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 新类
 *
 * @author youbl
 * @date 2023/1/4 18:18
 */
@Component
public class SDKValidator implements Validator {
    private static final String HEADER_NAME = "simple-auth";

    @Value("${sdk.app-key:}")
    private String appKey;
    @Value("${sdk.secure-key:}")
    private String securityKey;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean validated(HttpServletRequest request, HttpServletResponse response) {
        if (!StringUtils.hasLength(appKey))
            return false;
        if (!StringUtils.hasLength(securityKey))
            return false;

        String header = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasLength(header))
            return false;

        return header.equals(getAuthKey());
    }

    private String getAuthKey() {
        return appKey + ":" + securityKey;
    }
}
