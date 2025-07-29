package cn.beinet.codegenerate.configs.thirdLogin.dingtalk;

import com.fzzixun.etools.oauth.client.AuthService;
import com.fzzixun.etools.oauth.client.response.GetAuthTokenUserBodyResponse;
import com.fzzixun.etools.oauth.client.response.GetAuthUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/3 15:35
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EToolsLoginService {
    private final AuthService authService;

    public String redirectUrl(String redirectUrl, String state) {
        return authService.generateOauthLink(redirectUrl, state);
    }

    @SneakyThrows
    public GetAuthUserResponse getAuthTokenUser(String oauthCode) {
        var getAuthTokenUserResponse
                = authService.getUserInfoByOauthCode(oauthCode);
        if (getAuthTokenUserResponse.success()) {
            GetAuthTokenUserBodyResponse data = getAuthTokenUserResponse.getData();
            return data.getAuth_user();
        }
        throw new BadRequestException(getAuthTokenUserResponse.getMessage());
    }
}
