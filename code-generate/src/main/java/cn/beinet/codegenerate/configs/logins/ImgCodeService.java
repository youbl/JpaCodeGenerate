package cn.beinet.codegenerate.configs.logins;

import cn.beinet.codegenerate.util.ImgCodeUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务类
 *
 * @author youbl
 * @since 2023/5/16 17:45
 */
@Service
@Slf4j
public class ImgCodeService {

    private Map<String, String> codeSnMap = new ConcurrentHashMap<>();

    /**
     * 获取图形验证码并返回
     *
     * @return
     */
    public ImgCodeDto getImgCode() {
        ImgCodeUtil.ImgCode code = ImgCodeUtil.getCode();

        // todo: 如果未使用过的code，需要在10分钟内过期，这里没做
        // 把生成的sn和code的关联关系保存
        saveCode(code.getSn(), code.getCode());

        // 注意不能把code返回，否则就搞笑了
        return new ImgCodeDto()
                .setCodeSn(code.getSn())
                .setCodeBase64(code.getCodeImg());
    }

    private void saveCode(String sn, String code) {
        codeSnMap.putIfAbsent(sn, code);
    }

    public boolean validImgCode(String sn, String code) {
        Assert.isTrue(StringUtils.hasLength(code) && StringUtils.hasLength(sn),
                "输入的sn或code为空");
        code = code.trim();
        sn = sn.trim();

        // 删除sn与code的映射关系，不让重复使用
        String value = codeSnMap.remove(sn);
        return (code.equalsIgnoreCase(value));
    }

    @Data
    @Accessors(chain = true)
    public static class ImgCodeDto {
        private String codeBase64;
        private String codeSn;
    }
}
