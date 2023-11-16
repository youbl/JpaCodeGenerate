package cn.beinet.codegenerate.configs.logins;

import cn.beinet.codegenerate.configs.Consts;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通过固定的ak/sk进行登录验证的类，一般用于api调用
 *
 * @author youbl
 * @since 2023/1/4 18:18
 */
@Component
public class SDKValidator implements Validator {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean validated(HttpServletRequest request, HttpServletResponse response) {
        String appKey = Consts.getSdkAppKey();
        String securityKey = Consts.getSdkSecurityKey();
        if (!StringUtils.hasLength(appKey))
            return false;
        if (!StringUtils.hasLength(securityKey))
            return false;
        String authKey = appKey + ":" + securityKey;

        String header = request.getHeader(Consts.SDK_HEADER_NAME);
        if (!StringUtils.hasLength(header))
            return false;

        return header.equals(authKey);
    }

}
